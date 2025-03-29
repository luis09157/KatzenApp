package com.example.katzen.Dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Adapter.MedicamentoAdapter
import com.example.katzen.DataBaseFirebase.FirebaseMedicamentoUtil
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Diálogo para seleccionar un medicamento de tipo vacuna
 */
class SeleccionarMedicamentoDialog(
    context: Context,
    private val onMedicamentoSelected: (ProductoMedicamentoModel) -> Unit
) : Dialog(context) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicamentoAdapter
    private lateinit var etSearch: EditText
    private lateinit var tvNoResults: TextView
    private lateinit var tvLoading: TextView
    private lateinit var btnBorrarSeleccion: Button
    private lateinit var llResultados: LinearLayout
    
    private val medicamentos = mutableListOf<ProductoMedicamentoModel>()
    private val filteredMedicamentos = mutableListOf<ProductoMedicamentoModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_seleccionar_medicamento)
        
        // Configurar diálogo de pantalla completa
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        
        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewMedicamentos)
        etSearch = findViewById(R.id.etSearch)
        tvNoResults = findViewById(R.id.tvNoResults)
        tvLoading = findViewById(R.id.tvLoading)
        btnBorrarSeleccion = findViewById(R.id.btnBorrarSeleccion)
        llResultados = findViewById(R.id.llResultados)
        
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        
        // Configurar adapter
        adapter = MedicamentoAdapter(filteredMedicamentos) { medicamento ->
            onMedicamentoSelected(medicamento)
            dismiss()
        }
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        // Configurar búsqueda
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMedicamentos(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Configurar botón cerrar
        btnClose.setOnClickListener {
            dismiss()
        }
        
        // Configurar botón borrar selección
        btnBorrarSeleccion.setOnClickListener {
            // Crear un objeto con ID vacío para indicar que se debe borrar la selección
            val emptyMedicamento = ProductoMedicamentoModel()
            onMedicamentoSelected(emptyMedicamento)
            dismiss()
        }
        
        // Cargar medicamentos
        cargarMedicamentos()
    }
    
    private fun cargarMedicamentos() {
        showLoading(true)
        
        FirebaseMedicamentoUtil.obtenerListaMedicamentos(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicamentos.clear()
                for (medicamentoSnapshot in snapshot.children) {
                    val medicamento = medicamentoSnapshot.getValue(ProductoMedicamentoModel::class.java)
                    medicamento?.let {
                        medicamentos.add(it)
                    }
                }
                
                filterMedicamentos(etSearch.text.toString())
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeleccionarMedicamentoDialog", "Error al cargar medicamentos: ${error.message}")
                showLoading(false)
                showNoResults(true)
            }
        })
    }
    
    private fun filterMedicamentos(query: String) {
        filteredMedicamentos.clear()
        
        if (query.isEmpty()) {
            filteredMedicamentos.addAll(medicamentos)
        } else {
            val lowerCaseQuery = query.toLowerCase()
            filteredMedicamentos.addAll(medicamentos.filter {
                it.nombre.toLowerCase().contains(lowerCaseQuery) ||
                it.descripcion.toLowerCase().contains(lowerCaseQuery)
            })
        }
        
        adapter.notifyDataSetChanged()
        
        if (filteredMedicamentos.isEmpty()) {
            showNoResults(true)
        } else {
            showNoResults(false)
        }
    }
    
    private fun showLoading(show: Boolean) {
        tvLoading.visibility = if (show) View.VISIBLE else View.GONE
        llResultados.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showNoResults(show: Boolean) {
        tvNoResults.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
} 