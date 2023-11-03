package com.example.katzen.Helper

import android.app.Activity
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.example.katzen.Helper.FirebaseHelper.Companion.newAdress
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Helper.ValidateFormulario.Companion.validarFormulario
import com.example.katzen.MainActivity
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class DialogHelper {

    companion object{
        val TAG = "DialogHelper"
        val categorias = listOf("Semana X4","Semana X2", "Campaña", "Ruta", "Moto")

        fun getDateNow(): String{
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
            return dateFormatter.format(Date())
        }
        fun dialogAddDomicilio(activity: Activity, vMDM : VentaMesDetalleModel,
                               myTopPostsQuery: DatabaseReference, loadingHelper: LoadingHelper){
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()
            val builder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .create()
            val view = activity.layoutInflater.inflate(R.layout.vista_agregar_viajes_detalle,null)


            val  txt_domicilio = view.findViewById<TextInputEditText>(R.id.text_domicilio)
            val  txt_categoria = view.findViewById<AutoCompleteTextView>(R.id.autoTextView)
            val  txt_fecha_detalle = view.findViewById<TextInputEditText>(R.id.text_fecha_detalle)
            val  txt_kilometros = view.findViewById<TextInputEditText>(R.id.text_kilometros)
            val  txt_link_maps = view.findViewById<TextInputEditText>(R.id.text_link_maps)
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
                vMDM.linkMaps = txt_link_maps.text.toString()
                dialogConfirm(builder,activity,vMDM,myTopPostsQuery,loadingHelper)
            }
            txt_fecha_detalle.setOnClickListener {
                view.hideKeyboard()
                datePicker.show((activity as MainActivity).supportFragmentManager, TAG);
            }
            txt_fecha_detalle.setOnFocusChangeListener { view, b ->
                if(b){
                    view.hideKeyboard()
                    datePicker.show((activity as MainActivity).supportFragmentManager, TAG);
                }
            }

            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                txt_fecha_detalle.setText("${calendar.get(Calendar.DAY_OF_MONTH)}-" +
                        "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}")
            }

            val adapter = ArrayAdapter(activity,
                android.R.layout.simple_list_item_1,categorias)


            txt_categoria.setAdapter(adapter)

            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        }

        fun dialogConfirm(alertDialog: AlertDialog, activity: Activity,
                          vMDM: VentaMesDetalleModel, myTopPostsQuery: DatabaseReference, loadingHelper: LoadingHelper){
            MaterialAlertDialogBuilder(activity,
                com.google.android.material.R.style.MaterialAlertDialog_Material3)
                .setTitle(activity.resources.getString(R.string.dialog_msg_title))
                .setMessage(activity.resources.getString(R.string.dialog_msg_save))
                .setNegativeButton(activity.resources.getString(R.string.btn_cancelar)) { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton(activity.resources.getString(R.string.btn_save)) { dialog, which ->
                    val (message, flag) = validarFormulario(vMDM)
                    if(flag){
                        newAdress(alertDialog,vMDM,activity,myTopPostsQuery,loadingHelper)
                    }else{
                        Toast.makeText(activity,message, Toast.LENGTH_SHORT).show()
                    }

                }
                .show()
        }
    }
}