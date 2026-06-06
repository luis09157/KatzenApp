package com.example.katzen.Helper

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import coil.imageLoader
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ninodev.katzen.R
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object ImageLoaderHelper {

    private const val LEGACY_BUCKET = "rutasmagicas-2514a.appspot.com"
    private const val CURRENT_BUCKET = "katzen-a0e3e.appspot.com"
    private const val LIST_IMAGE_SIZE_PX = 160
    private const val BLUR_PREVIEW_SIZE_PX = 32
    private const val BLUR_RENDER_RADIUS = 18f
    private const val CROSSFADE_MS = 300

    private val IMAGE_URL_KEYS = listOf(
        "imageUrl",
        "imageURL",
        "foto",
        "urlImagen",
        "rutaImagen",
        "photoUrl",
        "imagenUrl",
        "imagen"
    )

    private val IMAGE_FILE_NAME_KEYS = listOf(
        "imageFileName",
        "fileName",
        "nombreArchivo",
        "nombreImagen",
        "fotoFileName"
    )

    private val PLACEHOLDER_URL_MARKERS = listOf(
        "no_disponible",
        "no_disponible_rosa",
        "ic_perfil",
        "ic_person",
        "img_cliente",
        "placeholder",
        "default_avatar",
        "sin_imagen",
        "no_image",
        "no_foto",
        "avatar_default"
    )

    private val failedUrls: MutableSet<String> =
        Collections.newSetFromMap(ConcurrentHashMap())

    private val failedStoragePaths: MutableSet<String> =
        Collections.newSetFromMap(ConcurrentHashMap())

    fun readImageUrl(snapshot: DataSnapshot): String {
        for (key in IMAGE_URL_KEYS) {
            sanitizeImageUrl(snapshot.child(key).getValue(String::class.java))?.let { return it }
        }
        return ""
    }

    fun readImageUrl(map: Map<String, Any?>): String {
        for (key in IMAGE_URL_KEYS) {
            sanitizeImageUrl(map[key]?.toString())?.let { return it }
        }
        return ""
    }

    fun readImageFileName(snapshot: DataSnapshot): String {
        for (key in IMAGE_FILE_NAME_KEYS) {
            sanitizeImageFileName(snapshot.child(key).getValue(String::class.java))?.let { return it }
        }
        return ""
    }

    fun readImageFileName(map: Map<String, Any?>): String {
        for (key in IMAGE_FILE_NAME_KEYS) {
            sanitizeImageFileName(map[key]?.toString())?.let { return it }
        }
        return ""
    }

    /**
     * Normaliza campos de imagen leídos de Firebase (claves legacy, "null", placeholders).
     */
    fun resolveProfileImage(
        imageUrl: String?,
        imageFileName: String?,
        snapshot: DataSnapshot? = null,
        map: Map<String, Any?>? = null
    ): ResolvedProfileImage {
        var url = sanitizeImageUrl(imageUrl)
        var fileName = sanitizeImageFileName(imageFileName)

        if (url.isNullOrBlank()) {
            url = when {
                snapshot != null -> sanitizeImageUrl(readImageUrl(snapshot))
                map != null -> sanitizeImageUrl(readImageUrl(map))
                else -> null
            }
        }

        if (fileName.isNullOrBlank()) {
            fileName = when {
                snapshot != null -> sanitizeImageFileName(readImageFileName(snapshot))
                map != null -> sanitizeImageFileName(readImageFileName(map))
                else -> null
            }
        }

        if (!url.isNullOrBlank() && isPlaceholderImageUrl(url)) {
            url = null
        }

        return ResolvedProfileImage(
            imageUrl = url.orEmpty(),
            imageFileName = if (url.isNullOrBlank()) fileName.orEmpty() else ""
        )
    }

    fun sanitizeImageUrl(value: String?): String? {
        val trimmed = value?.trim() ?: return null
        if (trimmed.isBlank() ||
            trimmed.equals("null", ignoreCase = true) ||
            trimmed.equals("undefined", ignoreCase = true)
        ) {
            return null
        }
        if (!isValidUrl(trimmed)) return null
        val normalized = normalizeStorageUrl(trimmed)
        return if (isPlaceholderImageUrl(normalized)) null else normalized
    }

    fun sanitizeImageFileName(value: String?): String? {
        val trimmed = value?.trim() ?: return null
        if (trimmed.isBlank() ||
            trimmed.equals("null", ignoreCase = true) ||
            trimmed.equals("undefined", ignoreCase = true) ||
            trimmed == "0"
        ) {
            return null
        }
        return trimmed
    }

    fun hasRemoteImage(imageUrl: String, imageFileName: String, storageFolder: String?): Boolean {
        if (sanitizeImageUrl(imageUrl) != null) return true
        if (!storageFolder.isNullOrBlank() && sanitizeImageFileName(imageFileName) != null) return true
        return false
    }

    data class ResolvedProfileImage(
        val imageUrl: String,
        val imageFileName: String
    )

    fun load(
        imageView: ImageView,
        imageUrl: String?,
        placeholderRes: Int,
        errorRes: Int,
        storageFolder: String? = null,
        imageFileName: String? = null,
        progressBar: ProgressBar? = null,
        forList: Boolean = false
    ) {
        loadInternal(
            imageView = imageView,
            progressBar = progressBar,
            imageUrl = imageUrl,
            placeholderRes = placeholderRes,
            errorRes = errorRes,
            storageFolder = storageFolder,
            imageFileName = imageFileName,
            forList = forList
        )
    }

    fun loadListImage(
        imageView: ImageView,
        progressBar: ProgressBar?,
        imageUrl: String?,
        placeholderRes: Int,
        errorRes: Int,
        storageFolder: String? = null,
        imageFileName: String? = null
    ) {
        loadInternal(
            imageView = imageView,
            progressBar = progressBar,
            imageUrl = imageUrl,
            placeholderRes = placeholderRes,
            errorRes = errorRes,
            storageFolder = storageFolder,
            imageFileName = imageFileName,
            forList = true
        )
    }

    fun clearListImage(imageView: ImageView, progressBar: ProgressBar?, placeholderRes: Int) {
        imageView.setTag(R.id.image_load_key, null)
        imageView.context.imageLoader.enqueue(
            ImageRequest.Builder(imageView.context)
                .data(null as String?)
                .target(imageView)
                .build()
        )
        showError(imageView, progressBar, placeholderRes)
    }

    private fun prepareLoadingSurface(imageView: ImageView) {
        imageView.setImageDrawable(null)
        imageView.setBackgroundResource(R.drawable.bg_image_loading_placeholder)
    }

    private fun clearLoadingSurface(imageView: ImageView) {
        imageView.background = null
    }

    private fun loadInternal(
        imageView: ImageView,
        progressBar: ProgressBar?,
        imageUrl: String?,
        placeholderRes: Int,
        errorRes: Int,
        storageFolder: String?,
        imageFileName: String?,
        forList: Boolean
    ) {
        val resolved = resolveProfileImage(imageUrl, imageFileName)
        val normalizedUrl = sanitizeImageUrl(resolved.imageUrl)
        val normalizedFileName = sanitizeImageFileName(resolved.imageFileName)
        val requestKey = normalizedUrl ?: normalizedFileName?.let { "$storageFolder/$it" }.orEmpty()

        if (!hasRemoteImage(resolved.imageUrl, resolved.imageFileName, storageFolder)) {
            showError(imageView, progressBar, errorRes)
            return
        }

        if (forList && normalizedUrl != null && failedUrls.contains(normalizedUrl)) {
            showError(imageView, progressBar, errorRes)
            return
        }

        val currentKey = imageView.getTag(R.id.image_load_key) as? String
        if (currentKey == requestKey) {
            return
        }

        clearBlurEffect(imageView)
        imageView.setTag(R.id.image_load_key, requestKey)
        prepareLoadingSurface(imageView)
        startLoadingUi(imageView, progressBar)

        when {
            !normalizedUrl.isNullOrEmpty() -> {
                loadWithBlurUp(
                    imageView = imageView,
                    progressBar = progressBar,
                    data = normalizedUrl,
                    requestKey = requestKey,
                    errorRes = errorRes,
                    forList = forList,
                    storagePath = extractStoragePath(normalizedUrl),
                    placeholderRes = placeholderRes
                )
            }

            normalizedFileName != null && !storageFolder.isNullOrBlank() -> {
                loadFromStoragePath(
                    imageView = imageView,
                    progressBar = progressBar,
                    storagePath = "$storageFolder/$normalizedFileName",
                    requestKey = requestKey,
                    placeholderRes = placeholderRes,
                    errorRes = errorRes,
                    forList = forList
                )
            }

            else -> showError(imageView, progressBar, errorRes)
        }
    }

    /**
     * Blur-up: miniatura pequeña (difuminada en Android 12+) y luego imagen nítida con crossfade.
     * Mismo efecto visual que BlurHash / Medium / Pinterest sin guardar hashes en Firebase.
     */
    private fun loadWithBlurUp(
        imageView: ImageView,
        progressBar: ProgressBar?,
        data: Any,
        requestKey: String,
        errorRes: Int,
        forList: Boolean,
        storagePath: String?,
        placeholderRes: Int
    ) {
        imageView.load(data, imageView.context.imageLoader) {
            size(BLUR_PREVIEW_SIZE_PX, BLUR_PREVIEW_SIZE_PX)
            scale(Scale.FILL)
            allowRgb565(true)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            allowHardware(false)
            listener(
                onSuccess = { _, _ ->
                    if (!isCurrentRequest(imageView, requestKey)) return@listener
                    applyBlurEffect(imageView)
                    softenLoadingUi(imageView, progressBar)
                    loadFullResolution(
                        imageView = imageView,
                        progressBar = progressBar,
                        data = data,
                        requestKey = requestKey,
                        errorRes = errorRes,
                        forList = forList,
                        storagePath = storagePath,
                        placeholderRes = placeholderRes
                    )
                },
                onError = { _, _ ->
                    if (!isCurrentRequest(imageView, requestKey)) return@listener
                    loadFullResolution(
                        imageView = imageView,
                        progressBar = progressBar,
                        data = data,
                        requestKey = requestKey,
                        errorRes = errorRes,
                        forList = forList,
                        storagePath = storagePath,
                        placeholderRes = placeholderRes
                    )
                }
            )
        }
    }

    private fun loadFullResolution(
        imageView: ImageView,
        progressBar: ProgressBar?,
        data: Any,
        requestKey: String,
        errorRes: Int,
        forList: Boolean,
        storagePath: String?,
        placeholderRes: Int
    ) {
        imageView.load(data, imageView.context.imageLoader) {
            crossfade(CROSSFADE_MS)
            if (forList) {
                size(LIST_IMAGE_SIZE_PX, LIST_IMAGE_SIZE_PX)
            }
            scale(Scale.FILL)
            error(errorRes)
            placeholder(R.drawable.bg_image_loading_placeholder)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            allowHardware(true)
            listener(
                onSuccess = { _, _ ->
                    if (isCurrentRequest(imageView, requestKey)) {
                        clearBlurEffect(imageView)
                        clearLoadingSurface(imageView)
                        stopLoadingUi(imageView, progressBar)
                    }
                },
                onCancel = {
                    if (isCurrentRequest(imageView, requestKey)) {
                        showError(imageView, progressBar, errorRes)
                    }
                },
                onError = { _, _ ->
                    if (!isCurrentRequest(imageView, requestKey)) return@listener
                    clearBlurEffect(imageView)
                    val url = data as? String
                    if (!url.isNullOrBlank()) {
                        failedUrls.add(url)
                    }
                    if (forList || storagePath.isNullOrBlank() || failedStoragePaths.contains(storagePath)) {
                        showError(imageView, progressBar, errorRes)
                    } else {
                        loadFromStoragePath(
                            imageView = imageView,
                            progressBar = progressBar,
                            storagePath = storagePath,
                            requestKey = requestKey,
                            placeholderRes = placeholderRes,
                            errorRes = errorRes,
                            forList = forList
                        )
                    }
                }
            )
        }
    }

    private fun applyBlurEffect(imageView: ImageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            imageView.setRenderEffect(
                RenderEffect.createBlurEffect(
                    BLUR_RENDER_RADIUS,
                    BLUR_RENDER_RADIUS,
                    Shader.TileMode.CLAMP
                )
            )
        }
    }

    private fun clearBlurEffect(imageView: ImageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            imageView.setRenderEffect(null)
        }
    }

    private fun loadFromStoragePath(
        imageView: ImageView,
        progressBar: ProgressBar?,
        storagePath: String,
        requestKey: String,
        placeholderRes: Int,
        errorRes: Int,
        forList: Boolean
    ) {
        if (failedStoragePaths.contains(storagePath)) {
            showError(imageView, progressBar, errorRes)
            return
        }

        startLoadingUi(imageView, progressBar)
        Firebase.storage.reference.child(storagePath).downloadUrl
            .addOnSuccessListener { uri ->
                if (!isCurrentRequest(imageView, requestKey)) return@addOnSuccessListener
                loadWithBlurUp(
                    imageView = imageView,
                    progressBar = progressBar,
                    data = uri,
                    requestKey = requestKey,
                    errorRes = errorRes,
                    forList = forList,
                    storagePath = null,
                    placeholderRes = placeholderRes
                )
            }
            .addOnFailureListener {
                failedStoragePaths.add(storagePath)
                if (isCurrentRequest(imageView, requestKey)) {
                    showError(imageView, progressBar, errorRes)
                }
            }
    }

    private fun showError(imageView: ImageView, progressBar: ProgressBar?, errorRes: Int) {
        clearBlurEffect(imageView)
        clearLoadingSurface(imageView)
        stopLoadingUi(imageView, progressBar)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(errorRes)
        imageView.contentDescription = imageView.context.getString(R.string.sin_imagen)
    }

    private fun startLoadingUi(imageView: ImageView, progressBar: ProgressBar?) {
        ImageShimmerHelper.start(imageView)
        progressBar?.visibility = View.VISIBLE
    }

    private fun softenLoadingUi(imageView: ImageView, progressBar: ProgressBar?) {
        ImageShimmerHelper.soften(imageView)
        progressBar?.visibility = View.VISIBLE
    }

    private fun stopLoadingUi(imageView: ImageView, progressBar: ProgressBar?) {
        ImageShimmerHelper.stop(imageView)
        progressBar?.visibility = View.GONE
    }

    private fun isCurrentRequest(imageView: ImageView, requestKey: String): Boolean {
        return imageView.getTag(R.id.image_load_key) == requestKey
    }

    private fun normalizeStorageUrl(url: String): String {
        return url.trim().replace(LEGACY_BUCKET, CURRENT_BUCKET)
    }

    private fun isPlaceholderImageUrl(url: String): Boolean {
        val lower = url.lowercase()
        return PLACEHOLDER_URL_MARKERS.any { lower.contains(it) }
    }

    private fun isValidUrl(value: String?): Boolean {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return false
        return value.startsWith("http://") ||
            value.startsWith("https://") ||
            value.startsWith("content://") ||
            value.startsWith("file://")
    }

    private fun extractStoragePath(url: String): String? {
        val pathSegment = url.substringAfter("/o/", "").substringBefore("?")
        if (pathSegment.isBlank()) return null
        return URLDecoder.decode(pathSegment, StandardCharsets.UTF_8.name())
    }
}
