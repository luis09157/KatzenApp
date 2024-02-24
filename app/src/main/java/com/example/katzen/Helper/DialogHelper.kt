package com.example.katzen.Helper

import KatzenDataBase
import android.app.Activity
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Helper.ValidateFormulario.Companion.validarFormulario
import com.example.katzen.MainActivity
import com.example.katzen.Model.MascotaModel
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
                vMDM.isEdit = false
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
                android.R.layout.simple_list_item_1,Config.CATEGORIAS)


            txt_categoria.setAdapter(adapter)

            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        }
        fun dialogEditDomicilio(activity: Activity, vMDM : VentaMesDetalleModel,
                               myTopPostsQuery: DatabaseReference, loadingHelper: LoadingHelper){
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()
            val builder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .create()
            val view = activity.layoutInflater.inflate(R.layout.vista_agregar_viajes_detalle_edit,null)


            val  txt_domicilio = view.findViewById<TextInputEditText>(R.id.text_domicilio)
            val  txt_categoria = view.findViewById<AutoCompleteTextView>(R.id.autoTextView)
            val  txt_fecha_detalle = view.findViewById<TextInputEditText>(R.id.text_fecha_detalle)
            val  txt_kilometros = view.findViewById<TextInputEditText>(R.id.text_kilometros)
            val  text_precio_venta = view.findViewById<TextInputEditText>(R.id.text_precio_venta)
            val  txt_link_maps = view.findViewById<TextInputEditText>(R.id.text_link_maps)
            val  btn_cancelar = view.findViewById<Button>(R.id.btn_cancelar)
            val  btn_guardar = view.findViewById<Button>(R.id.btn_guardar)

            txt_fecha_detalle.setText(vMDM.fecha)
            txt_categoria.setText(vMDM.categoria)
            txt_kilometros.setText(vMDM.kilometros)
            txt_link_maps.setText(vMDM.linkMaps)
            txt_domicilio.setText(vMDM.domicilio)
            text_precio_venta.setText(vMDM.venta)

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
                vMDM.venta = text_precio_venta.text.toString()
                vMDM.isEdit = true
                dialogConfirmEdit(builder,activity,vMDM,myTopPostsQuery,loadingHelper)
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
                android.R.layout.simple_list_item_1,Config.CATEGORIAS)


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

                        if (vMDM.isEdit){
                            FirebaseHelper.editAdress(alertDialog,vMDM,activity,myTopPostsQuery,loadingHelper)
                        }else{
                            FirebaseHelper.newAdress(alertDialog,vMDM,activity,myTopPostsQuery,loadingHelper)
                        }
                    }else{
                        Toast.makeText(activity,message, Toast.LENGTH_SHORT).show()
                    }

                }
                .show()
        }
        fun dialogConfirm(alertDialog: AlertDialog, activity: Activity,
                          mM: MascotaModel, loadingHelper: LoadingHelper){
            MaterialAlertDialogBuilder(activity,
                com.google.android.material.R.style.MaterialAlertDialog_Material3 )
                .setTitle(activity.resources.getString(R.string.dialog_msg_title_mascota))
                .setMessage(activity.resources.getString(R.string.dialog_msg_save_mascota))
                .setNegativeButton(activity.resources.getString(R.string.btn_cancelar)) { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton(activity.resources.getString(R.string.btn_save)) { dialog, which ->
                    val (message, flag) = validarFormulario(mM)
                    if(flag){
                        try {
                            val katzenDataBase = KatzenDataBase()
                            katzenDataBase.agregarMascota(mM)

                            Toast.makeText(activity,activity.getString(R.string.dialog_msg_add_mascota_exito), Toast.LENGTH_LONG).show()
                            alertDialog.hide()
                        }
                        catch (e: Exception) {
                            loadingHelper.not_loading_result()
                            Toast.makeText(activity,"Hubo una problema al guardar el domicilio, intenta nuevamente.",
                                Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(activity,message, Toast.LENGTH_SHORT).show()
                    }

                }
                .show()
        }
        fun dialogConfirmEdit(alertDialog: AlertDialog, activity: Activity,
                          vMDM: VentaMesDetalleModel, myTopPostsQuery: DatabaseReference, loadingHelper: LoadingHelper){
            MaterialAlertDialogBuilder(activity,
                com.google.android.material.R.style.MaterialAlertDialog_Material3)
                .setTitle(activity.resources.getString(R.string.dialog_msg_title))
                .setMessage(activity.resources.getString(R.string.dialog_msg_edit))
                .setNegativeButton(activity.resources.getString(R.string.btn_cancelar)) { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton(activity.resources.getString(R.string.btn_edit)) { dialog, which ->
                    val (message, flag) = validarFormulario(vMDM)
                    if(flag){
                        if (vMDM.isEdit){
                            FirebaseHelper.editAdress(alertDialog,vMDM,activity,myTopPostsQuery,loadingHelper)
                        }
                    }else{
                        Toast.makeText(activity,message, Toast.LENGTH_SHORT).show()
                    }

                }
                .show()
        }
    }
}