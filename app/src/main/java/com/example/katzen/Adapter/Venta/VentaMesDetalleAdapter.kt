package com.example.katzen.Adapter.Venta

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.katzen.Helper.DialogHelper
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference


class VentaMesDetalleAdapter(activity: Activity, val listVentaMes: ArrayList<VentaMesDetalleModel>,
                             myTopPostsQuery: DatabaseReference, loadingHelper: LoadingHelper) : BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(activity)
    private val activity = activity
    private val myTopPostsQuery = myTopPostsQuery
    private val loadingHelper = loadingHelper
    private val TAG = "VentaMesDetalleAdapter"

    override fun getCount(): Int {
        return listVentaMes.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        val viewHolder: ViewHolder
        val rowView: View?

        if (view == null) {
            rowView = layoutInflater.inflate(R.layout.vista_viajes_detalle, viewGroup, false)

            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder

        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }

        viewHolder.txt_categoria.text = listVentaMes.get(position).categoria
        viewHolder.txt_domicilio.text = listVentaMes.get(position).domicilio
        viewHolder.txt_kilometros.text = listVentaMes.get(position).kilometros
        viewHolder.txt_fecha.text = listVentaMes.get(position).fecha

        viewHolder.txt_ganancia.text = "$ " +listVentaMes.get(position).ganancia
        viewHolder.txt_costo.text = "$ " +listVentaMes.get(position).costo
        viewHolder.txt_venta.text = "$ " +listVentaMes.get(position).venta

        viewHolder.btn_edit.setOnClickListener {
          DialogHelper.dialogEditDomicilio(activity,listVentaMes.get(position),myTopPostsQuery,loadingHelper )
        }

        viewHolder.card_cont.setOnClickListener {
            if(!listVentaMes.get(position).linkMaps.equals("")
                && listVentaMes.get(position).linkMaps != null){
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(listVentaMes.get(position).linkMaps)
                )
                activity.startActivity(intent)
            }else{
                Toast.makeText(activity,"No se registro la direccion en google maps.",Toast.LENGTH_SHORT).show()
            }
        }


        return rowView
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private class ViewHolder(view: View?) {
        val card_cont = view?.findViewById(R.id.card_cont) as CardView

        val txt_fecha = view?.findViewById(R.id.txt_fecha) as TextView
        val txt_categoria = view?.findViewById(R.id.txt_categoria) as TextView
        val txt_domicilio = view?.findViewById(R.id.txt_domicilio) as TextView
        val txt_kilometros = view?.findViewById(R.id.txt_kilometros) as TextView

        val txt_ganancia = view?.findViewById(R.id.txt_ganancia) as TextView
        val txt_costo = view?.findViewById(R.id.txt_costo) as TextView
        val txt_venta = view?.findViewById(R.id.txt_venta) as TextView

        val btn_edit = view?.findViewById(R.id.btn_edit) as FloatingActionButton
    }
}