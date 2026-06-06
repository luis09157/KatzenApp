package com.example.katzen.Adapter.Viaje

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.DataBaseFirebase.FirebaseViajesUtil
import com.example.katzen.Fragment.Viajes.AddViajeFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.ninodev.katzen.R

class ViajeMesDetalleAdapter(
    private val activity: Activity,
    private val onEditViaje: (VentaMesDetalleModel) -> Unit = { viaje ->
        AddViajeFragment.EDIT_VIAJE = viaje
        UtilFragment.changeFragment(activity, AddViajeFragment(), "ViajeMesDetalleAdapter")
    }
) : ListAdapter<VentaMesDetalleModel, ViajeMesDetalleAdapter.ViewHolder>(DIFF) {

    var tag: String = "ViajeMesDetalleAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.vista_viajes_detalle, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<VentaMesDetalleModel>) = submitList(newList.toList())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCont: CardView = itemView.findViewById(R.id.card_cont)
        private val txtPaciente: TextView = itemView.findViewById(R.id.txt_paciente)
        private val txtCategoria: TextView = itemView.findViewById(R.id.txt_categoria)
        private val txtDomicilio: TextView = itemView.findViewById(R.id.txt_domicilio)
        private val txtKilometros: TextView = itemView.findViewById(R.id.txt_kilometros)
        private val txtFecha: TextView = itemView.findViewById(R.id.txt_fecha)
        private val txtGanancia: TextView = itemView.findViewById(R.id.txt_ganancia)
        private val txtCosto: TextView = itemView.findViewById(R.id.txt_costo)
        private val txtVenta: TextView = itemView.findViewById(R.id.txt_venta)
        private val btnEditar: CardView = itemView.findViewById(R.id.btn_edit)
        private val btnEliminar: CardView = itemView.findViewById(R.id.btn_eliminar)

        fun bind(viaje: VentaMesDetalleModel) {
            txtPaciente.text = viaje.nombreDomicilio
            txtCategoria.text = viaje.categoria
            txtDomicilio.text = viaje.domicilio
            txtKilometros.text = viaje.kilometros
            txtFecha.text = viaje.fecha
            txtGanancia.text = "$ ${viaje.ganancia}"
            txtCosto.text = "$ ${viaje.costo}"
            txtVenta.text = "$ ${viaje.venta}"

            cardCont.setOnClickListener {
                UtilHelper.abrirGoogleMaps(activity, viaje.linkMaps)
            }
            btnEditar.setOnClickListener { onEditViaje(viaje) }
            btnEliminar.setOnClickListener {
                DialogMaterialHelper.mostrarConfirmDeleteDialog(activity, "¿Estás seguro de que deseas eliminar este elemento?") { confirmed ->
                    if (confirmed) FirebaseViajesUtil.eliminarViaje(viaje)
                }
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VentaMesDetalleModel>() {
            override fun areItemsTheSame(a: VentaMesDetalleModel, b: VentaMesDetalleModel) = a.id == b.id
            override fun areContentsTheSame(a: VentaMesDetalleModel, b: VentaMesDetalleModel) = a == b
        }
    }
}
