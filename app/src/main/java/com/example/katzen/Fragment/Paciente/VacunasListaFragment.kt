package com.example.katzen.Fragment.Paciente

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseVacunaUtil
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.VacunaModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaVacunasBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VacunasListaFragment : Fragment() {
    private var _binding: FragmentListaVacunasBinding? = null
    private val binding get() = _binding!!
    
    private val vacunasList = mutableListOf<VacunaModel>()
    private lateinit var vacunasAdapter: VacunasAdapter
    private var valueEventListener: ValueEventListener? = null
    
    private val TAG = "VacunasListaFragment"
    
    companion object {
        fun newInstance(idPaciente: String, idCliente: String): VacunasListaFragment {
            val fragment = VacunasListaFragment()
            val args = Bundle()
            args.putString("idPaciente", idPaciente)
            args.putString("idCliente", idCliente)
            fragment.arguments = args
            return fragment
        }
    }
    
    private var idPaciente: String = ""
    private var idCliente: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idPaciente = arguments?.getString("idPaciente") ?: ""
        idCliente = arguments?.getString("idCliente") ?: ""
        
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
        _binding = FragmentListaVacunasBinding.inflate(inflater, container, false)
        
        // Ocultar ActionBar de la actividad para usar nuestro Toolbar personalizado

        
        // Configurar el Toolbar personalizado
        binding.toolbarVacunas.title = getString(R.string.submenu_vacunas)
        
        // Configurar el comportamiento de retroceso usando el Toolbar personalizado
        binding.toolbarVacunas.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        
        initLoading()
        setupAdapter()
        setupDatosPaciente()
        setupListeners()
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarVacunas()
    }
    
    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contListaVacunas,
            binding.fragmentNoData.contNoData
        )
    }
    
    private fun setupAdapter() {
        vacunasAdapter = VacunasAdapter(vacunasList, 
            onItemClick = { vacuna ->
                editarVacuna(vacuna)
            },
            onDeleteClick = { vacuna ->
                eliminarVacuna(vacuna)
            }
        )
        binding.listaVacunas.adapter = vacunasAdapter
    }
    
    private fun setupDatosPaciente() {
        // Cargar datos del paciente desde EditarPacienteFragment.PACIENTE_EDIT
        val paciente = EditarPacienteFragment.PACIENTE_EDIT
        
        binding.tvNombrePaciente.text = paciente.nombre
        binding.tvEspecieRaza.text = "${paciente.especie} - ${paciente.raza}"
        
        try {
            val (anios, meses) = CalendarioUtil.calcularEdadMascota(paciente.edad)
            binding.tvEdad.text = "${anios} años y ${meses} meses"
        } catch (e: Exception) {
            binding.tvEdad.text = "Edad no disponible"
            Log.e(TAG, "Error al calcular edad: ${e.message}")
        }
        
        // Cargar imagen del paciente
        if (paciente.imageUrl.isNotEmpty()) {
            Glide.with(binding.imgPaciente.context)
                .load(paciente.imageUrl)
                .placeholder(R.drawable.ic_perfil)
                .error(R.drawable.no_disponible_rosa)
                .into(binding.imgPaciente)
        } else {
            binding.imgPaciente.setImageResource(R.drawable.no_disponible_rosa)
        }
    }

    private fun setupListeners() {
        binding.btnAgregarVacuna.setOnClickListener {
            val fragment = AgregarVacunaFragment.newInstance(idPaciente, idCliente)
            UtilFragment.changeFragment(requireActivity(), fragment, TAG)
        }
    }
    
    private fun cargarVacunas() {
        ConfigLoading.showLoadingAnimation()
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                vacunasList.clear()
                
                for (vacunaSnapshot in snapshot.children) {
                    try {
                        val vacuna = vacunaSnapshot.getValue(VacunaModel::class.java)
                        vacuna?.let {
                            vacunasList.add(it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al convertir vacuna: ${e.message}")
                    }
                }
                
                // Ordenar vacunas por fecha (más reciente primero)
                vacunasList.sortByDescending { it.fecha }
                
                vacunasAdapter.notifyDataSetChanged()
                
                if (vacunasList.isNotEmpty()) {
                    binding.tvHistorialVacunas.text = "Historial de Vacunas (${vacunasList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay vacunas registradas para este paciente"
                    binding.fragmentNoData.btnAdd?.apply {
                        visibility = View.GONE
                        text = "Agregar Vacuna"
                        setOnClickListener {
                            val fragment = AgregarVacunaFragment.newInstance(idPaciente, idCliente)
                            UtilFragment.changeFragment(requireActivity(), fragment, TAG)
                        }
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                
                Log.e(TAG, "Error al cargar vacunas: ${error.message}")
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar vacunas: ${error.message}"
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar las vacunas: ${error.message}"
                )
            }
        }
        
        valueEventListener?.let {
            FirebaseVacunaUtil.obtenerVacunasPorPaciente(idPaciente, it)
        }
    }
    
    private fun editarVacuna(vacuna: VacunaModel) {
        val fragment = AgregarVacunaFragment.newInstance(idPaciente, idCliente, vacuna)
        UtilFragment.changeFragment(requireActivity(), fragment, TAG)
    }
    
    private fun eliminarVacuna(vacuna: VacunaModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar esta vacuna?") { confirmed ->
            if (confirmed) {
                ConfigLoading.showLoadingAnimation()
                
                FirebaseVacunaUtil.eliminarVacuna(vacuna.id)
                    .addOnSuccessListener {
                        ConfigLoading.hideLoadingAnimation()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Vacuna eliminada correctamente")
                        cargarVacunas()
                    }
                    .addOnFailureListener { e ->
                        ConfigLoading.hideLoadingAnimation()
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al eliminar: ${e.message}")
                    }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).supportActionBar?.hide()
        cargarVacunas()
        
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), PacienteDetalleFragment(), TAG)
                }
            })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Restaurar la ActionBar al salir
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).supportActionBar?.show()
        valueEventListener?.let {
            FirebaseVacunaUtil.removerListener(it)
        }
        _binding = null
    }
    
    inner class VacunasAdapter(
        private val vacunas: List<VacunaModel>,
        private val onItemClick: (VacunaModel) -> Unit,
        private val onDeleteClick: (VacunaModel) -> Unit
    ) : BaseAdapter() {
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        override fun getCount(): Int = vacunas.size
        
        override fun getItem(position: Int): Any = vacunas[position]
        
        override fun getItemId(position: Int): Long = position.toLong()
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacuna, parent, false)
            
            val vacuna = vacunas[position]
            
            val tvNombreVacuna: TextView = view.findViewById(R.id.tvNombreVacuna)
            val tvFechaAplicacion: TextView = view.findViewById(R.id.tvFechaAplicacion)
            val imgRecordatorio: ImageView = view.findViewById(R.id.imgRecordatorio)
            val tvProximaAplicacion: TextView = view.findViewById(R.id.tvProximaAplicacion)
            val tvObservaciones: TextView = view.findViewById(R.id.tvObservaciones)
            val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
            val tvDosisAplicada: TextView = view.findViewById(R.id.tvDosisAplicada)
            
            // Configurar datos básicos
            tvNombreVacuna.text = vacuna.vacuna
            tvFechaAplicacion.text = "Fecha: ${vacuna.fecha}"
            
            // Mostrar dosis con "ml"
            if (vacuna.dosis.isNotEmpty()) {
                tvDosisAplicada.visibility = View.VISIBLE
                tvDosisAplicada.text = "Dosis: ${vacuna.dosis} ml"
            } else {
                tvDosisAplicada.visibility = View.GONE
            }
            
            // Manejo de próxima aplicación y recordatorio
            if (vacuna.recordatorio && vacuna.fechaRecordatorio.isNotEmpty()) {
                tvProximaAplicacion.text = "Próx: ${vacuna.fechaRecordatorio}"
                tvProximaAplicacion.visibility = View.VISIBLE
                imgRecordatorio.visibility = View.VISIBLE
            } else {
                tvProximaAplicacion.visibility = View.GONE
                imgRecordatorio.visibility = View.GONE
            }
            
            // Mostrar observaciones si existen
            if (vacuna.observaciones.isNotEmpty()) {
                tvObservaciones.text = "Observaciones: ${vacuna.observaciones}"
                tvObservaciones.visibility = View.VISIBLE
            } else {
                tvObservaciones.visibility = View.GONE
            }
            
            // Configurar listeners
            btnEliminar.setOnClickListener {
                onDeleteClick(vacuna)
            }
            
            view.setOnClickListener {
                onItemClick(vacuna)
            }
            
            return view
        }
    }
} 