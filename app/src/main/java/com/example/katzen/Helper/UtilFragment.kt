package com.example.katzen.Helper

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.katzen.MainActivity
import com.ninodev.katzen.R

class UtilFragment {
    companion object{
        fun changeFragment(context: Context,fragment: Fragment,name:String) {
            val transition = (context as MainActivity).supportFragmentManager.beginTransaction()
            transition.replace(R.id.nav_host_fragment_content_main, fragment)
                .addToBackStack(null).commit()
        }
    }

}