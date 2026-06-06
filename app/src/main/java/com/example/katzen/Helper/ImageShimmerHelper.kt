package com.example.katzen.Helper

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.facebook.shimmer.ShimmerFrameLayout
import com.ninodev.katzen.R

/**
 * Animación de carga sobre avatares en listas.
 * Usa Facebook Shimmer (solo UI). La carga eficiente la hace Coil en [ImageLoaderHelper].
 */
object ImageShimmerHelper {

    private const val PULSE_DURATION_MS = 650L

    data class LoadingViews(
        val shimmerContainer: ShimmerFrameLayout?,
        val progressBar: ProgressBar?
    )

    fun findLoadingViews(imageView: ImageView): LoadingViews {
        var group: ViewGroup? = imageView.parent as? ViewGroup
        while (group != null) {
            if (group.findViewById<ImageView>(R.id.imgPerfil) === imageView) {
                return LoadingViews(
                    group.findViewById(R.id.shimmerContainer),
                    group.findViewById(R.id.progressImageLoading)
                )
            }
            group = group.parent as? ViewGroup
        }
        return LoadingViews(null, null)
    }

    fun start(imageView: ImageView) {
        val views = findLoadingViews(imageView)
        if (views.shimmerContainer != null) {
            views.shimmerContainer.visibility = View.VISIBLE
            views.shimmerContainer.startShimmer()
        } else {
            startImagePulse(imageView)
        }
        views.progressBar?.visibility = View.VISIBLE
    }

    fun soften(imageView: ImageView) {
        views(imageView).progressBar?.visibility = View.VISIBLE
    }

    fun stop(imageView: ImageView) {
        stopImagePulse(imageView)
        val views = findLoadingViews(imageView)
        views.shimmerContainer?.apply {
            stopShimmer()
            visibility = View.GONE
        }
        views.progressBar?.visibility = View.GONE
        imageView.alpha = 1f
    }

    private fun views(imageView: ImageView) = findLoadingViews(imageView)

    private fun startImagePulse(imageView: ImageView) {
        stopImagePulse(imageView)
        imageView.setBackgroundResource(R.drawable.bg_image_loading_placeholder)
        val anim = ObjectAnimator.ofFloat(imageView, View.ALPHA, 0.6f, 1f).apply {
            duration = PULSE_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        anim.start()
        imageView.setTag(R.id.shimmer_slide_animator, anim)
    }

    private fun stopImagePulse(imageView: ImageView) {
        (imageView.getTag(R.id.shimmer_slide_animator) as? ObjectAnimator)?.cancel()
        imageView.setTag(R.id.shimmer_slide_animator, null)
        imageView.animate().cancel()
    }
}
