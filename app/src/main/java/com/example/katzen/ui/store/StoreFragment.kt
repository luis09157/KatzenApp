package com.example.katzen.ui.store

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.StoreModel
import com.example.katzen.Model.VentaModel
import com.example.katzen.R
import com.example.katzen.StoreAdapter
import com.example.katzen.databinding.FragmentStoreBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class StoreFragment : Fragment() {
    val TAG = "StoreFragment"
    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    val database = Firebase.database
    val myRef = database.getReference("Tienda")
    val categorias = listOf("Venta", "Gasolina", "Proveedores", "Consulta", "Baño", "Campaña","Otro")
    private var listStore : MutableList<StoreModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root
        llenarStore()
        val storeAdapter = StoreAdapter(requireContext(), listStore)
        binding.listTindaMes.adapter = storeAdapter
        binding.listTindaMes.divider = null
        binding.listTindaMes.setOnItemClickListener { adapterView, view, i, l ->
            Log.d(TAG, listStore.get(i).nombre.toString())
        }


        _binding!!.btnVenta.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                .create()
            val view = layoutInflater.inflate(R.layout.add_cost_store,null)

            val  txt_categoria = view.findViewById<AutoCompleteTextView>(R.id.autoTextView)
            val  btn_cancelar = view.findViewById<Button>(R.id.btn_cancelar)
            val  btn_guardar = view.findViewById<Button>(R.id.btn_guardar)

            txt_categoria.setOnClickListener {
                view.hideKeyboard()
            }

            btn_cancelar.setOnClickListener {
                builder.hide()
            }
            btn_guardar.setOnClickListener {
               getFormData(view)
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

    fun llenarStore(){
        listStore.add(StoreModel( "1","ALBAÑILERIA"))
        listStore.add(StoreModel( "2","ALBAÑILERIA"))
    }

    fun getFormData(view: View){
        val text_producto = view.findViewById<TextInputEditText>(R.id.text_producto)
        val text_categoria = view.findViewById<AutoCompleteTextView>(R.id.autoTextView)
        val text_precio_compra = view.findViewById<TextInputEditText>(R.id.text_precio_compra)
        val text_precio_venta = view.findViewById<TextInputEditText>(R.id.text_precio_venta)

        guardarVenta(VentaModel(UtilHelper.getID(),text_producto.text.toString(),text_categoria.text.toString()
                    ,text_precio_compra.text.toString().toDouble(),text_precio_venta.text.toString().toDouble()
                    ,UtilHelper.getDate()))

    }
    fun guardarVenta(ventaModel: VentaModel){
        myRef.child(UtilHelper.getDateIdMonth()).child(ventaModel.id).setValue(ventaModel)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

