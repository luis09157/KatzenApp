package com.example.katzen.Helper

import android.view.View
import androidx.constraintlayout.widget.Group

/**
 * Estados de pantalla al cargar datos: loading → contenido o vacío.
 * Evita mostrar formularios/listas vacías mientras llega la información.
 */
object DataLoadUiHelper {

    fun showLoading(loadingView: View, contentView: View, emptyView: View? = null) {
        loadingView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        emptyView?.visibility = View.GONE
    }

    fun showContent(loadingView: View, contentView: View, emptyView: View? = null) {
        loadingView.visibility = View.GONE
        contentView.visibility = View.VISIBLE
        emptyView?.visibility = View.GONE
    }

    fun showEmpty(loadingView: View, contentView: View, emptyView: View) {
        loadingView.visibility = View.GONE
        contentView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    fun setGroupVisible(group: Group, visible: Boolean) {
        group.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun showSectionLoading(loadingView: View, contentGroup: Group) {
        loadingView.visibility = View.VISIBLE
        setGroupVisible(contentGroup, false)
    }

    fun hideSectionLoading(loadingView: View, contentGroup: Group) {
        loadingView.visibility = View.GONE
        setGroupVisible(contentGroup, true)
    }

    fun showOverlayLoading(loadingView: View, contentView: View) {
        loadingView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
    }

    fun hideOverlayLoading(loadingView: View, contentView: View) {
        loadingView.visibility = View.GONE
        contentView.visibility = View.VISIBLE
    }
}
