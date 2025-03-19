package com.example.katzen.Fragment.Paciente

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseVacunaUtil
import com.example.katzen.Dialog.SeleccionarMedicamentoDialog
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ProductoAplicadoModel
import com.example.katzen.Model.ProductoMedicamentoModel
import com.example.katzen.Model.VacunaModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentAgregarVacunaBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AgregarVacunaFragment : Fragment() {
    private var _binding: FragmentAgregarVacunaBinding? = null
    private val binding get() = _binding!!

    private val TAG = "AgregarVacunaFragment"
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private lateinit var productosAdapter: ProductosAplicadosAdapter
    private val productosAplicados = mutableListOf<ProductoAplicadoModel>()

    private var isRecordatorioConfigured = false
    private var fechaRecordatorio = ""

    companion object {
        fun newInstance(idPaciente: String, idCliente: String): AgregarVacunaFragment {
            val fragment = AgregarVacunaFragment()
            val args = Bundle()
            args.putString("idPaciente", idPaciente)
            args.putString("idCliente", idCliente)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(idPaciente: String, idCliente: String, vacuna: VacunaModel): AgregarVacunaFragment {
            val fragment = AgregarVacunaFragment()
            val args = Bundle()
            args.putString("idPaciente", idPaciente)
            args.putString("idCliente", idCliente)
            args.putParcelable("vacuna", vacuna)
            fragment.arguments = args
            return fragment
        }
    }

    private var idPaciente: String = ""
    private var idCliente: String = ""
    private var vacunaToEdit: VacunaModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        idPaciente = arguments?.getString("idPaciente") ?: ""
        idCliente = arguments?.getString("idCliente") ?: ""
        vacunaToEdit = arguments?.getParcelable("vacuna")
        isEditMode = vacunaToEdit != null


        if (idPaciente.isEmpty()) {
            Log.e(TAG, "ID del paciente no proporcionado")
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: No se pudo identificar al paciente")
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgregarVacunaBinding.inflate(inflater, container, false)

        // Ocultar ActionBar de la actividad para usar nuestro Toolbar personalizado


        // Configurar el Toolbar personalizado
        val titulo = if (isEditMode) "Editar Vacunación" else "Nueva Vacunación"
        binding.toolbarVacuna.title = titulo
        
        // Configurar el comportamiento de retroceso usando el Toolbar personalizado
        binding.toolbarVacuna.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        initLoading()
        setupUI()
        setupListeners()

        if (isEditMode) {
            cargarDatosVacuna()
        } else {
            // Establecer fecha actual por defecto
            binding.etFecha.setText(dateFormat.format(calendar.time))
        }

        return binding.root
    }

    private fun initLoading() {
        val emptyView = View(requireContext())
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contFormulario,
            emptyView
        )
    }

    private fun setupUI() {
        // Ya no usamos el ArrayAdapter para el spinner de vacunas
        // En su lugar, configuramos el campo para abrir el diálogo de selección
        
        binding.actvVacuna.isFocusable = false
        binding.actvVacuna.isClickable = true
        binding.actvVacuna.setOnClickListener {
            mostrarDialogoSeleccionMedicamento()
        }

        // Configurar RecyclerView de productos aplicados
        productosAdapter = ProductosAplicadosAdapter(productosAplicados) { position ->
            productosAplicados.removeAt(position)
            actualizarVistaProductos()
        }

        binding.rvProductosAplicados.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productosAdapter
        }
    }

    private fun mostrarDialogoSeleccionMedicamento() {
        val dialog = SeleccionarMedicamentoDialog(requireContext()) { medicamento ->
            if (medicamento.id.isNotEmpty()) {
                binding.actvVacuna.setText(medicamento.nombre)
                // Si deseas almacenar el medicamento seleccionado, puedes hacerlo aquí
                // Verificamos primero si el campo dosis está vacío 
                if (binding.etCantidadAplicada.text.toString().isEmpty()) {
                    // Intentamos usar la dosis recomendada si está disponible como propiedad
                    try {
                        // Si hay una descripción, podríamos extraer la dosis de ahí
                        if (medicamento.descripcion.isNotEmpty() && medicamento.descripcion.contains("dosis")) {
                            binding.etCantidadAplicada.setText(medicamento.descripcion.split("dosis:")[1].trim().split(" ")[0])
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "No se pudo extraer la dosis de la descripción")
                    }
                }
            } else {
                // El usuario seleccionó "BORRAR SELECCIÓN"
                binding.actvVacuna.setText("")
            }
        }
        dialog.show()
    }

    private fun setupListeners() {
        // Selector de fecha
        binding.etFecha.setOnClickListener {
            showDatePicker()
        }

        // Botón para configurar recordatorio
        binding.btnConfigurarRecordatorio.setOnClickListener {
            if (binding.etFecha.text.toString().isEmpty() || binding.etValidezDias.text.toString().isEmpty()) {
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Para configurar un recordatorio, primero ingresa la fecha y los días de validez")
                return@setOnClickListener
            }

            showDateTimePicker()
        }

        // Botón para agregar producto
        binding.btnAgregarProducto.setOnClickListener {
            agregarProductoDemo()
        }

        // Botón guardar
        binding.fabGuardar.setOnClickListener {
            guardarVacuna()
        }
    }

    private fun cargarDatosVacuna() {
        vacunaToEdit?.let { vacuna ->
            binding.etFecha.setText(vacuna.fecha)
            // Ya no usamos setAdapter para el spinner, sólo establecemos el texto
            binding.actvVacuna.setText(vacuna.vacuna)
            binding.etCantidadAplicada.setText(vacuna.cantidadAplicada)
            binding.etLote.setText(vacuna.lote)
            binding.etValidezDias.setText(vacuna.validezDias)
            binding.etObservaciones.setText(vacuna.observaciones)

            // Configurar recordatorio si existe
            isRecordatorioConfigured = vacuna.recordatorio
            fechaRecordatorio = vacuna.fechaRecordatorio

            if (isRecordatorioConfigured) {
                binding.btnConfigurarRecordatorio.text = "Recordatorio: ${vacuna.fechaRecordatorio}"
                binding.btnConfigurarRecordatorio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_recordatorio_activo, 0, 0, 0)
            }

            // Cargar productos aplicados
            productosAplicados.clear()
            productosAplicados.addAll(vacuna.productosAplicados)
            actualizarVistaProductos()
        }
    }

    private fun actualizarVistaProductos() {
        if (productosAplicados.isEmpty()) {
            binding.tvNoProductos.visibility = View.VISIBLE
            binding.rvProductosAplicados.visibility = View.GONE
        } else {
            binding.tvNoProductos.visibility = View.GONE
            binding.rvProductosAplicados.visibility = View.VISIBLE
            productosAdapter.notifyDataSetChanged()
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.etFecha.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showDateTimePicker() {
        // Primero seleccionar fecha
        val futureCalendar = Calendar.getInstance()

        try {
            // Calcular fecha futura basada en la validez
            val fechaAplicacion = dateFormat.parse(binding.etFecha.text.toString())
            val validezDias = binding.etValidezDias.text.toString().toInt()

            if (fechaAplicacion != null) {
                futureCalendar.time = fechaAplicacion
                futureCalendar.add(Calendar.DAY_OF_YEAR, validezDias)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular fecha futura: ${e.message}")
        }

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                futureCalendar.set(Calendar.YEAR, year)
                futureCalendar.set(Calendar.MONTH, month)
                futureCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Luego seleccionar hora
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        futureCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        futureCalendar.set(Calendar.MINUTE, minute)

                        // Guardar fecha y hora del recordatorio
                        fechaRecordatorio = "${dateFormat.format(futureCalendar.time)} ${timeFormat.format(futureCalendar.time)}"
                        isRecordatorioConfigured = true

                        // Actualizar UI
                        binding.btnConfigurarRecordatorio.text = "Recordatorio: $fechaRecordatorio"
                        binding.btnConfigurarRecordatorio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_recordatorio_activo, 0, 0, 0)
                    },
                    futureCalendar.get(Calendar.HOUR_OF_DAY),
                    futureCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            futureCalendar.get(Calendar.YEAR),
            futureCalendar.get(Calendar.MONTH),
            futureCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun agregarProductoDemo() {
        // En una implementación real, aquí se mostraría un diálogo para seleccionar productos
        // Por ahora, agregamos uno de demostración
        val nuevoProducto = ProductoAplicadoModel(
            id = UUID.randomUUID().toString(),
            nombre = "Producto ${productosAplicados.size + 1}",
            cantidad = "${(1..10).random()} ml",
            tipo = if ((0..1).random() == 0) "Medicamento" else "Alimento"
        )

        productosAplicados.add(nuevoProducto)
        actualizarVistaProductos()
    }

    private fun guardarVacuna() {
        // Validar campos obligatorios
        val fecha = binding.etFecha.text.toString().trim()
        val vacunaNombre = binding.actvVacuna.text.toString().trim()
        val cantidadAplicada = binding.etCantidadAplicada.text.toString().trim()

        if (fecha.isEmpty() || vacunaNombre.isEmpty() || cantidadAplicada.isEmpty()) {
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Los campos marcados con * son obligatorios")
            return
        }

        ConfigLoading.showLoadingAnimation()

        try {
            val lote = binding.etLote.text.toString().trim()
            val validezDias = binding.etValidezDias.text.toString().trim()
            val observaciones = binding.etObservaciones.text.toString().trim()

            val vacuna = if (isEditMode && vacunaToEdit != null) {
                vacunaToEdit!!.apply {
                    this.fecha = fecha
                    this.vacuna = vacunaNombre
                    this.cantidadAplicada = cantidadAplicada
                    this.lote = lote
                    this.validezDias = validezDias
                    this.recordatorio = isRecordatorioConfigured
                    this.fechaRecordatorio = fechaRecordatorio
                    this.observaciones = observaciones
                    this.productosAplicados = productosAplicados
                }
            } else {
                VacunaModel(
                    id = UUID.randomUUID().toString(),
                    idPaciente = idPaciente,
                    idCliente = idCliente,
                    fecha = fecha,
                    vacuna = vacunaNombre,
                    cantidadAplicada = cantidadAplicada,
                    lote = lote,
                    validezDias = validezDias,
                    recordatorio = isRecordatorioConfigured,
                    fechaRecordatorio = fechaRecordatorio,
                    observaciones = observaciones,
                    productosAplicados = productosAplicados,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            // Llamar directamente a los métodos con sus callbacks
            if (isEditMode) {
                FirebaseVacunaUtil.actualizarVacuna(vacuna) { success, message ->
                    ConfigLoading.hideLoadingAnimation()

                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(requireActivity(), "Vacuna actualizada correctamente") {
                            requireActivity().onBackPressed()
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            } else {
                FirebaseVacunaUtil.agregarVacuna(vacuna) { success, message ->
                    ConfigLoading.hideLoadingAnimation()

                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(requireActivity(), "Vacuna registrada correctamente") {
                            requireActivity().onBackPressed()
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            }

        } catch (e: Exception) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
            Log.e(TAG, "Error al guardar vacuna: ${e.message}", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            // Usar el comportamiento estándar de retroceso para volver al fragmento anterior
            requireActivity().onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restaurar la ActionBar al salir
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).supportActionBar?.show()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).supportActionBar?.hide()
        // No configuramos callback personalizado para permitir el retroceso normal
    }

    inner class ProductosAplicadosAdapter(
        private val productos: List<ProductoAplicadoModel>,
        private val onRemoveClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ProductosAplicadosAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_aplicado, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val producto = productos[position]
            holder.bind(producto, position)
        }

        override fun getItemCount(): Int = productos.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvNombreProducto: TextView = itemView.findViewById(R.id.tvNombreProducto)
            private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
            private val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)
            private val btnRemover: View = itemView.findViewById(R.id.btnRemover)

            fun bind(producto: ProductoAplicadoModel, position: Int) {
                tvNombreProducto.text = producto.nombre
                tvCantidad.text = producto.cantidad
                tvTipo.text = producto.tipo

                btnRemover.setOnClickListener {
                    onRemoveClick(position)
                }
            }
        }
    }
}