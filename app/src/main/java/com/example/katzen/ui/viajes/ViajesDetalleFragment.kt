package com.example.katzen.ui.viajes

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.VentaMesDetalleAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.MainActivity
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.R
import com.example.katzen.databinding.FragmentViajesDetalleBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


class ViajesDetalleFragment : Fragment() {
    val TAG = "ViajesDetalleFragment"
    private var _binding: FragmentViajesDetalleBinding? = null
    var listVentaMesDetalle = arrayListOf<VentaMesDetalleModel>()
    val categorias = listOf("Semana X4","Semana X2", "Campaña", "Ruta", "Moto")
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    var myTopPostsQuery: DatabaseReference? = null
    var adapter: VentaMesDetalleAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentViajesDetalleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initFirebase()
        initUI()
        getGasolinaApi()

        binding.btnAddTravel.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()

            val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                .create()
            val view = layoutInflater.inflate(R.layout.vista_agregar_viajes_detalle,null)


            val  txt_categoria = view.findViewById<AutoCompleteTextView>(R.id.autoTextView)
            val  txt_fecha_detalle = view.findViewById<TextInputEditText>(R.id.text_fecha_detalle)
            val  btn_cancelar = view.findViewById<Button>(R.id.btn_cancelar)
            val  btn_guardar = view.findViewById<Button>(R.id.btn_guardar)

            txt_fecha_detalle.setText(getDateNow())


            txt_categoria.setOnClickListener {
                view.hideKeyboard()
            }

            btn_cancelar.setOnClickListener {
                builder.hide()
            }
            btn_guardar.setOnClickListener {
                var ventaMesDetalleModel = VentaMesDetalleModel()
                ventaMesDetalleModel.fecha = txt_fecha_detalle.text.toString()
                ventaMesDetalleModel.kilometros = "5"
                ventaMesDetalleModel.ganancia = "100.00"
                ventaMesDetalleModel.costo = "200.00"
                ventaMesDetalleModel.venta = "300.00"
                ventaMesDetalleModel.domicilio = "Zinc 318"

                dialogConfirm(ventaMesDetalleModel)

            }
            txt_fecha_detalle.setOnClickListener {
                view.hideKeyboard()
                datePicker.show((requireActivity() as MainActivity).supportFragmentManager, TAG);
            }
            txt_fecha_detalle.setOnFocusChangeListener { view, b ->
                if(b){
                    view.hideKeyboard()
                    datePicker.show((requireActivity() as MainActivity).supportFragmentManager, TAG);
                }
            }

            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                txt_fecha_detalle.setText("${calendar.get(Calendar.DAY_OF_MONTH)}-" +
                        "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}")
            }

            val adapter = ArrayAdapter(requireActivity(),
                android.R.layout.simple_list_item_1,categorias)


            txt_categoria.setAdapter(adapter)

            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        }

        return root
    }
    fun initUI(){
        adapter = VentaMesDetalleAdapter(requireActivity(), listVentaMesDetalle)
        binding.listViajesDetalle.adapter = adapter
        binding.listViajesDetalle.divider = null
    }

    fun newAdress(ventaMesDetalleModel: VentaMesDetalleModel){
        myTopPostsQuery!!.child(getDateNow()).child(getIdFirebase()).setValue(ventaMesDetalleModel)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                UtilFragment.changeFragment(requireContext(),ViajesFragment(),TAG)
                true
            } else false
        }
    }

    fun initFirebase(){
        database = Firebase.database.reference
        myTopPostsQuery = database.child("Katzen").child("Gasolina").child(Config.MES_DETALLE).child("cargos")
    }
    private fun getGasolinaApi(){
        Log.e(TAG,Config.MES_DETALLE)
        val query = myTopPostsQuery!!.orderByChild("starCount")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listVentaMesDetalle.clear()

                for (postSnapshot in dataSnapshot.children) {

                    for (data in postSnapshot.children) {
                        var ventaMesDetalleModel = VentaMesDetalleModel()
                        ventaMesDetalleModel.venta = data.child("venta").value.toString()
                        ventaMesDetalleModel.categoria = data.child("categoria").value.toString()
                        ventaMesDetalleModel.costo = data.child("costo").value.toString()
                        ventaMesDetalleModel.domicilio = data.child("domicilio").value.toString()
                        ventaMesDetalleModel.fecha = data.child("fecha").value.toString()
                        ventaMesDetalleModel.ganancia = data.child("ganancia").value.toString()
                        ventaMesDetalleModel.kilometros = data.child("kilometros").value.toString()

                        listVentaMesDetalle.add(ventaMesDetalleModel)
                    }
                    adapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    fun getDateNow(): String{
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
        return dateFormatter.format(Date())
    }
    fun getIdFirebase(): String{
        val dateFormatter = SimpleDateFormat("ddMMyyyyHHMMSS")
        return dateFormatter.format(Date())
    }

    fun dialogConfirm(ventaMesDetalleModel: VentaMesDetalleModel){
        MaterialAlertDialogBuilder(requireContext(),
            com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle(resources.getString(R.string.dialog_msg_title))
            .setMessage(resources.getString(R.string.dialog_msg_save))
            .setNegativeButton(resources.getString(R.string.btn_cancelar)) { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton(resources.getString(R.string.btn_save)) { dialog, which ->
                newAdress(ventaMesDetalleModel)
            }
            .show()
    }

}