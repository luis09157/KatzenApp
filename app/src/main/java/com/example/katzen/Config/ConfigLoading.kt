package com.example.katzen.Config

import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.ninodev.katzen.R

object ConfigLoading {
    var LOTTIE_ANIMATION_VIEW: LottieAnimationView? = null
    var CONT_ADD_PRODUCTO: View? = null
    var FRAGMENT_NO_DATA: View? = null

    fun showLoadingAnimation() {
        LOTTIE_ANIMATION_VIEW?.let {
            it.visibility = View.VISIBLE
            it.setAnimation(R.raw.gato_loading)
            it.playAnimation()
        }
        CONT_ADD_PRODUCTO?.visibility = View.GONE
        FRAGMENT_NO_DATA?.visibility = View.GONE
    }

    fun hideLoadingAnimation() {
        LOTTIE_ANIMATION_VIEW?.let {
            it.visibility = View.GONE
            it.cancelAnimation()
        }
        CONT_ADD_PRODUCTO?.visibility = View.VISIBLE
        FRAGMENT_NO_DATA?.visibility = View.GONE
    }

    fun showNodata() {
        LOTTIE_ANIMATION_VIEW?.let {
            it.visibility = View.GONE
            it.cancelAnimation()
        }
        CONT_ADD_PRODUCTO?.visibility = View.GONE
        FRAGMENT_NO_DATA?.visibility = View.VISIBLE
    }
}
