package com.example.katzen.Config

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ninodev.katzen.R
import java.lang.ref.WeakReference

object ConfigLoading {
    private var lottieAnimationView: WeakReference<LottieAnimationView>? = null
    private var contAddProducto: WeakReference<View>? = null
    private var fragmentNoData: WeakReference<View>? = null
    private var noDataMessageView: WeakReference<TextView>? = null
    private var retryButton: WeakReference<Button>? = null

    fun init(lottieView: LottieAnimationView, addProductoView: View, noDataView: View) {
        lottieAnimationView = WeakReference(lottieView)
        contAddProducto = WeakReference(addProductoView)
        fragmentNoData = WeakReference(noDataView)
        noDataMessageView = WeakReference(noDataView.findViewById(R.id.tvNoDataMessage))
        retryButton = WeakReference(noDataView.findViewById(R.id.btnAdd))
        showLoadingAnimation()
    }

    fun showLoadingAnimation() {
        lottieAnimationView?.get()?.apply {
            setAnimation(R.raw.gato_loading)
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
            visibility = View.VISIBLE
        }
        contAddProducto?.get()?.visibility = View.GONE
        fragmentNoData?.get()?.visibility = View.GONE
    }

    fun hideLoadingAnimation() {
        lottieAnimationView?.get()?.apply {
            cancelAnimation()
            visibility = View.GONE
        }
        contAddProducto?.get()?.visibility = View.VISIBLE
        fragmentNoData?.get()?.visibility = View.GONE
    }

    fun showNodata(message: String? = null) {
        lottieAnimationView?.get()?.visibility = View.GONE
        contAddProducto?.get()?.visibility = View.GONE
        fragmentNoData?.get()?.visibility = View.VISIBLE
        noDataMessageView?.get()?.text = message ?: noDataMessageView?.get()?.context
            ?.getString(R.string.no_data)
        retryButton?.get()?.visibility = View.GONE
    }

    fun showError(message: String, retryAction: (() -> Unit)? = null) {
        lottieAnimationView?.get()?.visibility = View.GONE
        contAddProducto?.get()?.visibility = View.GONE
        fragmentNoData?.get()?.visibility = View.VISIBLE
        noDataMessageView?.get()?.text = message
        retryButton?.get()?.apply {
            if (retryAction != null) {
                visibility = View.VISIBLE
                text = context.getString(R.string.btn_retry)
                setOnClickListener { retryAction.invoke() }
            } else {
                visibility = View.GONE
                setOnClickListener(null)
            }
        }
    }
}
