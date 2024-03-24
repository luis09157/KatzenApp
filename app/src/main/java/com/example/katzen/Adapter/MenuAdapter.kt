package com.example.katzen.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.katzen.Model.MenuModel
import com.example.katzen.R

class MenuAdapter(private val context: Context, private val menuList: List<MenuModel>) : BaseAdapter() {

    override fun getCount(): Int {
        return menuList.size
    }

    override fun getItem(position: Int): Any {
        return menuList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_view_gridview, parent, false)
            holder = ViewHolder()
            holder.imagen = view.findViewById(R.id.imagen)
            holder.titulo = view.findViewById(R.id.titulo)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val menu = menuList[position]

        holder.titulo?.text = menu.titulo
        holder.imagen?.setImageResource(menu.imagen)

        return view!!
    }

    private class ViewHolder {
        var titulo: TextView? = null
        var imagen: ImageView? = null
    }
}
