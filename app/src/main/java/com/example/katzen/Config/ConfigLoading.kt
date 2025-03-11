package com.example.katzen.Config

import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ninodev.katzen.R
import java.lang.ref.WeakReference

object ConfigLoading {
    private var lottieAnimationView: WeakReference<LottieAnimationView>? = null
    private var contAddProducto: WeakReference<View>? = null
    private var fragmentNoData: WeakReference<View>? = null

    fun init(lottieView: LottieAnimationView, addProductoView: View, noDataView: View) {
        lottieAnimationView = WeakReference(lottieView)
        contAddProducto = WeakReference(addProductoView)
        fragmentNoData = WeakReference(noDataView)
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
        lottieAnimationView?.get()?.visibility = View.GONE
        contAddProducto?.get()?.visibility = View.VISIBLE
        fragmentNoData?.get()?.visibility = View.GONE
    }

    fun showNodata() {
        lottieAnimationView?.get()?.visibility = View.GONE
        contAddProducto?.get()?.visibility = View.GONE
        fragmentNoData?.get()?.visibility = View.VISIBLE
    }
}
