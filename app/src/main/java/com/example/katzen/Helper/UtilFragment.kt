package com.example.katzen.Helper

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.MainActivity
import com.example.katzen.MenuFragment
import com.ninodev.katzen.R

class UtilFragment {
    companion object {
        fun changeFragment(
            context: Context,
            fragment: Fragment,
            name: String,
            clearBackStack: Boolean = false,
            listKey: String? = null,
            listRecyclerView: RecyclerView? = null,
            selectedItemId: String? = null
        ) {
            if (!listKey.isNullOrBlank() && listRecyclerView != null && !selectedItemId.isNullOrBlank()) {
                ListScrollStateHelper.saveSelection(listKey, listRecyclerView, selectedItemId)
            }

            val activity = context as? MainActivity ?: return
            val fragmentManager = activity.supportFragmentManager

            if (clearBackStack) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

            if (fragmentManager.isStateSaved) {
                fragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .addToBackStack(name)
                    .commitAllowingStateLoss()
            } else {
                fragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .addToBackStack(name)
                    .commit()
            }
        }

        fun goHome(context: Context) {
            changeFragment(context, MenuFragment(), "MenuFragment", clearBackStack = true)
        }

        fun canGoBack(context: Context): Boolean {
            val activity = context as? MainActivity ?: return false
            return activity.supportFragmentManager.backStackEntryCount > 1
        }

        /**
         * Retrocede en el back stack nativo. Evita duplicar fragments al volver.
         * @return true si se hizo pop, false si no hay entrada previa.
         */
        fun goBack(context: Context): Boolean {
            val activity = context as? MainActivity ?: return false
            val fragmentManager = activity.supportFragmentManager
            if (fragmentManager.backStackEntryCount <= 1) return false

            if (fragmentManager.isStateSaved) {
                fragmentManager.popBackStack()
            } else {
                fragmentManager.popBackStack()
            }
            return true
        }

        fun goBackOrHome(context: Context) {
            if (!goBack(context)) {
                goHome(context)
            }
        }
    }
}
