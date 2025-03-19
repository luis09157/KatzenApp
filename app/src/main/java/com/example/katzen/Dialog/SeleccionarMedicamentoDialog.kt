package com.example.katzen.Dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
        
        // Estados iniciales correctos
        tvLoading.visibility = View.VISIBLE
        llResultados.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvNoResults.visibility = View.VISIBLE
        
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
        
        FirebaseMedicamentoUtil.obtenerMedicamentosTipoVacuna { success, result ->
            Log.d("SeleccionarMedicamentoDialog", "Callback recibido, success: $success, resultados: ${result.size}")
            
            try {
                // Asegurarnos de ejecutar en el hilo principal para actualizar la UI
                (context as? android.app.Activity)?.runOnUiThread {
                    if (success && result.isNotEmpty()) {
                        Log.d("SeleccionarMedicamentoDialog", "Mostrando ${result.size} medicamentos")
                        
                        // Actualizar las listas
                        medicamentos.clear()
                        medicamentos.addAll(result)
                        filteredMedicamentos.clear()
                        filteredMedicamentos.addAll(medicamentos)
                        
                        // Mostrar la lista
                        llResultados.visibility = View.VISIBLE
                        tvLoading.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        tvNoResults.visibility = View.GONE
                        
                        // Notificar al adaptador
                        adapter.notifyDataSetChanged()
                        
                        Log.d("SeleccionarMedicamentoDialog", "Lista actualizada con ${filteredMedicamentos.size} medicamentos visibles")
                    } else {
                        Log.d("SeleccionarMedicamentoDialog", "No se encontraron medicamentos o hubo un error")
                        llResultados.visibility = View.VISIBLE
                        tvLoading.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        tvNoResults.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("SeleccionarMedicamentoDialog", "Error al actualizar UI: ${e.message}")
                e.printStackTrace()
            }
        }
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
        
        Log.d("SeleccionarMedicamentoDialog", "Filtrado: ${filteredMedicamentos.size} medicamentos coinciden con '$query'")
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
        Log.d("SeleccionarMedicamentoDialog", "showLoading: $show")
    }
    
    private fun showNoResults(show: Boolean) {
        tvNoResults.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        Log.d("SeleccionarMedicamentoDialog", "showNoResults: $show")
    }
} 