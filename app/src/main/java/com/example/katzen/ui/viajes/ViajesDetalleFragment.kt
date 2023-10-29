package com.example.katzen.ui.viajes

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.VentaMesDetalleAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
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
import java.lang.Exception
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.cos


class ViajesDetalleFragment : Fragment() {
    val TAG = "ViajesDetalleFragment"
    private var _binding: FragmentViajesDetalleBinding? = null
    var listVentaMesDetalle = arrayListOf<VentaMesDetalleModel>()
    val categorias = listOf("Semana X4","Semana X2", "Campaña", "Ruta", "Moto")
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    var myTopPostsQuery: DatabaseReference? = null
    var queryRefreshCost: DatabaseReference? = null
    var adapter: VentaMesDetalleAdapter? = null
    var vMDM = VentaMesDetalleModel()


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


            val  txt_domicilio = view.findViewById<TextInputEditText>(R.id.text_domicilio)
            val  txt_categoria = view.findViewById<AutoCompleteTextView>(R.id.autoTextView)
            val  txt_fecha_detalle = view.findViewById<TextInputEditText>(R.id.text_fecha_detalle)
            val  txt_kilometros = view.findViewById<TextInputEditText>(R.id.text_kilometros)
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
                vMDM.fecha = txt_fecha_detalle.text.toString()
                vMDM.kilometros = txt_kilometros.text.toString()
                vMDM.categoria = txt_categoria.text.toString()
                vMDM.domicilio = txt_domicilio.text.toString()

                dialogConfirm(builder)
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

    fun newAdress(alertDialog: AlertDialog) {
        try {
            myTopPostsQuery!!.child(getDateNow()).child(getIdFirebase()).setValue(vMDM)
            Toast.makeText(requireContext(),"Direccion Guardada.",Toast.LENGTH_LONG).show()
            alertDialog.hide()
        }
        catch (e: Exception) {
            Toast.makeText(requireContext(),"Hubo una problema al guardar el domicilio, intenta nuevamente.",Toast.LENGTH_LONG).show()
        }

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
                database.onDisconnect()
                UtilFragment.changeFragment(requireContext(),ViajesFragment(),TAG)
                true
            } else false
        }
    }

    fun initFirebase(){
        database = Firebase.database.reference
        queryRefreshCost =  database.child("Katzen").child("Gasolina").child(Config.MES_DETALLE)
        myTopPostsQuery = database.child("Katzen").child("Gasolina").child(Config.MES_DETALLE).child("cargos")
    }
    private fun getGasolinaApi(){
        val query = myTopPostsQuery!!.orderByChild("starCount")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listVentaMesDetalle.clear()
                Config.COSTO = 0.00
                Config.GANANCIA = 0.00
                Config.VENTA = 0.00

                if(dataSnapshot.children.count() > 0){
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


                            Config.COSTO += ventaMesDetalleModel.costo.toDouble()
                            Config.VENTA += ventaMesDetalleModel.venta.toDouble()
                            Config.GANANCIA += ventaMesDetalleModel.ganancia.toDouble()


                            listVentaMesDetalle.add(ventaMesDetalleModel)
                        }
                    }
                    adapter!!.notifyDataSetChanged()
                    refreshCosts()
                }else{
                    Toast.makeText(requireContext(),"No hay domicilios agregados.",Toast.LENGTH_LONG).show()
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

    fun dialogConfirm(alertDialog: AlertDialog){
        MaterialAlertDialogBuilder(requireContext(),
            com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle(resources.getString(R.string.dialog_msg_title))
            .setMessage(resources.getString(R.string.dialog_msg_save))
            .setNegativeButton(resources.getString(R.string.btn_cancelar)) { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton(resources.getString(R.string.btn_save)) { dialog, which ->
                val (message, flag) = validarFormulario()
                if(flag){
                    newAdress(alertDialog)
                }else{
                    Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
                }

            }
            .show()
    }

    fun validarFormulario() :  Pair<String, Boolean>  {
        var flag = true
        var message = ""

        if(!vMDM.kilometros.equals("")){
            val km =  vMDM.kilometros.toFloat()
            if(km < 1){
                flag = false
                message = "La cantidad de kilometros debe ser mayor a 0."
            }
        }else{
            flag = false
            message = "Debes escribir la cantidad de kilometros."
        }

        if(vMDM.categoria.equals("")){
            flag = false
            message = "Debes seleccionar una categoria."
        }else{
            val (costo,ganancia,venta) = UtilHelper.calcular(vMDM.kilometros.toDouble(),vMDM.categoria)
            vMDM.ganancia = ganancia
            vMDM.costo = costo
            vMDM.venta = venta
        }



        if (vMDM.fecha.equals("")){
            flag = false
            message = "Debes seleccionar una fecha."
        }else if(vMDM.venta.equals("")
            || vMDM.costo.equals("")
            || vMDM.ganancia.equals("")){
            flag = false
           // message = "Hubo un problema en el proceso intenta grabando el domicilio de nuevo."
        }

        return Pair(message, flag)
    }

    fun refreshCosts(){
        val df = DecimalFormat("#.##")

        queryRefreshCost!!.child("costo").setValue(df.format(Config.COSTO))
        queryRefreshCost!!.child("ganancia").setValue(df.format(Config.GANANCIA))
        queryRefreshCost!!.child("venta").setValue(df.format(Config.VENTA))
    }

}