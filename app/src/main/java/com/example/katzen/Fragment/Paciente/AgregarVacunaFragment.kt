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

    private var isRecordatorioConfigured = false
    private var fechaRecordatorio = ""

    private var selectedVacunaId: String = ""

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
        binding.actvVacuna.isFocusable = false
        binding.actvVacuna.isClickable = true
        binding.actvVacuna.setOnClickListener {
            binding.tilVacuna.error = null
            mostrarDialogoSeleccionMedicamento()
        }

        binding.etFecha.setOnClickListener {
            binding.tilFecha.error = null
            showDatePicker()
        }
    }

    private fun mostrarDialogoSeleccionMedicamento() {
        val dialog = SeleccionarMedicamentoDialog(requireContext()) { medicamento ->
            if (medicamento.id.isNotEmpty()) {
                binding.actvVacuna.setText(medicamento.nombre)
                selectedVacunaId = medicamento.id
            } else {
                // El usuario seleccionó "BORRAR SELECCIÓN"
                binding.actvVacuna.setText("")
                selectedVacunaId = ""
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
            mostrarDatePicker()
        }

        // Botón guardar
        binding.fabGuardar.setOnClickListener {
            guardarVacuna()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                // Establecer hora fija a las 10:00 AM
                selectedDate.set(Calendar.HOUR_OF_DAY, 10)
                selectedDate.set(Calendar.MINUTE, 0)
                
                // Formatear la fecha para mostrarla
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                
                // Guardar fecha y hora del recordatorio
                fechaRecordatorio = "$formattedDate 10:00"
                isRecordatorioConfigured = true
                
                // Actualizar UI del botón
                binding.btnConfigurarRecordatorio.text = "Recordatorio: $fechaRecordatorio"
                binding.btnConfigurarRecordatorio.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_recordatorio_activo, 
                    0, 
                    0, 
                    0
                )
                
                // Calcular y mostrar días restantes
                actualizarDiasRestantes(selectedDate.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Establecer fecha mínima (mañana, ya que hoy no tiene sentido un recordatorio)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        datePickerDialog.datePicker.minDate = tomorrow.timeInMillis
        
        datePickerDialog.show()
    }

    private fun actualizarDiasRestantes(fechaRecordatorioMillis: Long) {
        val today = Calendar.getInstance().timeInMillis
        val diffInMillis = fechaRecordatorioMillis - today
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
        
        binding.tvDiasRestantes.apply {
            visibility = View.VISIBLE
            text = when {
                diffInDays == 1L -> "Falta 1 día para el recordatorio"
                diffInDays > 1L -> "Faltan $diffInDays días para el recordatorio"
                else -> "El recordatorio se activará hoy"
            }
        }
    }

    private fun cargarDatosVacuna() {
        vacunaToEdit?.let { vacuna ->
            binding.etFecha.setText(vacuna.fecha)
            binding.actvVacuna.setText(vacuna.vacuna)
            binding.etDosis.setText(vacuna.dosis)
            binding.etObservaciones.setText(vacuna.observaciones)
            selectedVacunaId = vacuna.idVacuna

            // Configurar recordatorio si existe
            isRecordatorioConfigured = vacuna.recordatorio
            fechaRecordatorio = vacuna.fechaRecordatorio

            if (isRecordatorioConfigured) {
                binding.btnConfigurarRecordatorio.text = "Recordatorio: ${vacuna.fechaRecordatorio}"
                binding.btnConfigurarRecordatorio.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_recordatorio_activo, 
                    0, 
                    0, 
                    0
                )
                
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val fechaRecordatorio = dateFormat.parse(vacuna.fechaRecordatorio)
                    fechaRecordatorio?.let {
                        actualizarDiasRestantes(it.time)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al parsear fecha de recordatorio: ${e.message}")
                }
            }
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

            if (fechaAplicacion != null) {
                futureCalendar.time = fechaAplicacion
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

    private fun guardarVacuna() {
        // Validar campos obligatorios
        val fecha = binding.etFecha.text.toString().trim()
        val vacunaNombre = binding.actvVacuna.text.toString().trim()
        val dosis = binding.etDosis.text.toString().trim()
        var hayErrores = false

        // Validar cada campo individualmente
        if (fecha.isEmpty()) {
            binding.tilFecha.error = "Selecciona una fecha de aplicación"
            hayErrores = true
        } else {
            binding.tilFecha.error = null
        }

        if (vacunaNombre.isEmpty() || selectedVacunaId.isEmpty()) {
            binding.tilVacuna.error = "Selecciona una vacuna"
            hayErrores = true
        } else {
            binding.tilVacuna.error = null
        }

        if (dosis.isEmpty()) {
            binding.tilDosis.error = "Ingresa la dosis aplicada"
            hayErrores = true
        } else {
            binding.tilDosis.error = null
        }

        if (hayErrores) {
            return
        }

        ConfigLoading.showLoadingAnimation()

        try {
            val observaciones = binding.etObservaciones.text.toString().trim()

            val vacuna = if (isEditMode && vacunaToEdit != null) {
                vacunaToEdit!!.apply {
                    this.fecha = fecha
                    this.vacuna = vacunaNombre
                    this.idVacuna = selectedVacunaId
                    this.dosis = dosis
                    this.recordatorio = isRecordatorioConfigured
                    this.fechaRecordatorio = fechaRecordatorio
                    this.observaciones = observaciones
                }
            } else {
                VacunaModel(
                    id = UUID.randomUUID().toString(),
                    idPaciente = idPaciente,
                    fecha = fecha,
                    vacuna = vacunaNombre,
                    idVacuna = selectedVacunaId,
                    dosis = dosis,
                    recordatorio = isRecordatorioConfigured,
                    fechaRecordatorio = fechaRecordatorio,
                    observaciones = observaciones,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            // Guardar la vacuna
            if (isEditMode) {
                FirebaseVacunaUtil.actualizarVacuna(vacuna) { success, message ->
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(), 
                            "Vacuna actualizada correctamente"
                        ) {
                            requireActivity().onBackPressed()
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(
                            requireActivity(), 
                            "Error al actualizar: $message"
                        )
                    }
                }
            } else {
                FirebaseVacunaUtil.agregarVacuna(vacuna) { success, message ->
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(), 
                            "Vacuna registrada correctamente"
                        ) {
                            requireActivity().onBackPressed()
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(
                            requireActivity(), 
                            "Error al guardar: $message"
                        )
                    }
                }
            }

        } catch (e: Exception) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(), 
                "Error inesperado: ${e.message}"
            )
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
}