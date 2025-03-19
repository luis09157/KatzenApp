package com.example.katzen.Fragment.Paciente

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.katzen.Adapter.Cliente.ClienteListAdapter
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Fragment.Campaña.CampañaPacienteFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.MenuModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PacienteDetalleFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PacienteDetalleFragment : Fragment() {
    val TAG : String  = "ClienteDetalleFragment"

    private var _binding: PacienteDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var clienteListAdapter: ClienteListAdapter
    private lateinit var clientesList: MutableList<ClienteModel>
    private lateinit var menuList: List<MenuModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PacienteDetalleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_paciente_detalle)

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        //PacienteListAdapter.FLAG_IN_PACIENTE = true
        ConfigLoading.showLoadingAnimation()
        setValues()
        menuList = listOf(
            MenuModel(requireActivity().getString(R.string.submenu_recordatorio), R.drawable.img_recordatorios),
            MenuModel(requireActivity().getString(R.string.submenu_consultas), R.drawable.img_consulta),
            MenuModel(requireActivity().getString(R.string.submenu_vacunas), R.drawable.img_vacunas),
            MenuModel(requireActivity().getString(R.string.submenu_estetica), R.drawable.img_estetica)
        )
        val adapter = MenuAdapter(requireContext(), menuList)
        binding.menuOpciones.adapter = adapter
        
        // Configurar listener para los elementos del menú
        binding.menuOpciones.setOnItemClickListener { _, _, position, _ ->
            handleMenuClick(position)
        }
    }

    private fun handleMenuClick(position: Int) {
        when (position) {
            0 -> { // Recordatorios
                // Implementar navegación a Recordatorios
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Funcionalidad de Recordatorios en desarrollo")
            }
            1 -> { // Consultas
                // Implementar navegación a Consultas
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Funcionalidad de Consultas en desarrollo")
            }
            2 -> { // Vacunas
                // Navegación a Vacunas
                val idPaciente = EditarPacienteFragment.PACIENTE_EDIT.id
                val idCliente = EditarPacienteFragment.PACIENTE_EDIT.idCliente
                val fragment = VacunasListaFragment.newInstance(idPaciente, idCliente)
                UtilFragment.changeFragment(requireContext(), fragment, TAG)
            }
            3 -> { // Estética
                // Implementar navegación a Estética
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Funcionalidad de Estética en desarrollo")
            }
        }
    }

    fun setValues(){

        if(EditarPacienteFragment.PACIENTE_EDIT.nombre == ""){
            ConfigLoading.showNodata()
        }else{

            if (EditarPacienteFragment.PACIENTE_EDIT.imageUrl.isNotEmpty()) {
                Glide.with(binding.imgPerfil.context)
                    .load(EditarPacienteFragment.PACIENTE_EDIT.imageUrl)
                    .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                    .error(R.drawable.no_disponible_rosa) // Opcional: establecer una imagen en caso de error al cargar
                    .into(binding.imgPerfil)
            } else {
                binding.imgPerfil.setImageResource(R.drawable.no_disponible_rosa)
            }
            binding.textNombreCliente.text = EditarPacienteFragment.PACIENTE_EDIT.nombre
            binding.textEspecie.text = EditarPacienteFragment.PACIENTE_EDIT.especie
            binding.textRaza.text = EditarPacienteFragment.PACIENTE_EDIT.raza
            binding.textSexo.text = EditarPacienteFragment.PACIENTE_EDIT.sexo
            try {
                val (anios, meses) = CalendarioUtil.calcularEdadMascota(EditarPacienteFragment.PACIENTE_EDIT.edad)
                binding.textEdad.text =  "${anios} años y ${meses} meses"
            }catch (e : Exception){
                print(e.message)
            }

            binding.textPeso.text =  "${EditarPacienteFragment.PACIENTE_EDIT.peso} Kilos"

            ConfigLoading.hideLoadingAnimation()
            obtenerClientePorId(EditarPacienteFragment.PACIENTE_EDIT.idCliente)
        }
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    fun listeners(){
        binding.btnEdit.setOnClickListener {
            UtilFragment.changeFragment(requireActivity() , EditarPacienteFragment() ,TAG)
        }

        /*binding.btnAddCliente.setOnClickListener {
            AddPacienteFragment.ADD_PACIENTE.idCliente = EditClienteFragment.CLIENTE_EDIT.id
            AddPacienteFragment.ADD_PACIENTE.nombreCliente = EditClienteFragment.CLIENTE_EDIT.nombre
            UtilFragment.changeFragment(requireActivity() , AddPacienteFragment() ,TAG)
        }*/
    }

    private fun obtenerClientePorId(idCliente: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val cliente = FirebaseClienteUtil.obtenerClientePorId(idCliente)
                if (cliente != null) {
                    clientesList = mutableListOf()
                    clientesList.add(cliente)
                    clienteListAdapter = ClienteListAdapter(requireActivity(), clientesList)
                    binding.listaPacientes.adapter = clienteListAdapter
                    binding.listaPacientes.divider = null
                }
            } catch (e: Exception) {
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al obtener el cliente: ${e.message}")
                Log.e("FirebaseClienteUtil", "Error al obtener el cliente", e)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(PacienteListAdapter.FLAG_IN_PACIENTE){
                    UtilFragment.changeFragment(requireContext() , PacienteFragment() ,TAG)
                }else{
                    UtilFragment.changeFragment(requireContext() , CampañaPacienteFragment() ,TAG)
                }

            }
        })
    }

}