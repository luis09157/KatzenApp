package com.example.katzen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.katzen.Model.StoreModel
class StoreAdapter(context: Context, val listCategorias: List<StoreModel>) : BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(context)
    private val context = context

    override fun getCount(): Int {
        return listCategorias.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        val viewHolder: ViewHolder
        val rowView: View?

        if (view == null) {
            rowView = layoutInflater.inflate(R.layout.vista_store, viewGroup, false)

            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder

        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }

       // viewHolder.txtTitulo.text = listCategorias.get(position).nombre
       // viewHolder.txtDescripcion.text = listCategorias.get(position).descripcion
       // viewHolder.imgPortada.setImageDrawable(ContextCompat.getDrawable(context, listCategorias.get(position).imgPortada))


        return rowView
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private class ViewHolder(view: View?) {
        //val txtTitulo = view?.findViewById(R.id.txtTitulo) as TextView
        //val txtDescripcion = view?.findViewById(R.id.txtDescription) as TextView
        //val imgPortada = view?.findViewById(R.id.imgPortada) as ImageView
    }
}