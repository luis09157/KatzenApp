package com.example.katzen.Config

import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.ninodev.katzen.R

object ConfigLoading {
    lateinit var LOTTIE_ANIMATION_VIEW: LottieAnimationView
    lateinit var CONT_ADD_PRODUCTO: View
    lateinit var FRAGMENT_NO_DATA: View

    fun showLoadingAnimation() {
        LOTTIE_ANIMATION_VIEW.setAnimation(R.raw.gato_loading) // Cambia aquí
        LOTTIE_ANIMATION_VIEW.visibility = View.VISIBLE
        CONT_ADD_PRODUCTO.visibility = View.GONE
        FRAGMENT_NO_DATA.visibility = View.GONE
    }

    fun hideLoadingAnimation() {
        LOTTIE_ANIMATION_VIEW.visibility = View.GONE
        CONT_ADD_PRODUCTO.visibility = View.VISIBLE
        FRAGMENT_NO_DATA.visibility = View.GONE
    }

    fun showNodata() {
        LOTTIE_ANIMATION_VIEW.visibility = View.GONE
        CONT_ADD_PRODUCTO.visibility = View.GONE
        FRAGMENT_NO_DATA.visibility = View.VISIBLE
    }
}
