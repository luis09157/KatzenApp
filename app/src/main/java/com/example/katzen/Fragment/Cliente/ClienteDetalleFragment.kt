package com.example.katzen.Fragment.Cliente

import PacienteModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.katzen.Adapter.Paciente.PacienteAdapter
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Paciente.AddPacienteFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.MenuFragment
import com.example.katzen.R
import com.example.katzen.databinding.ClienteDetalleFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClienteDetalleFragment : Fragment() {
    val TAG : String  = "ClienteDetalleFragment"

    private var _binding: ClienteDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var pacienteListAdapter: PacienteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ClienteDetalleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_cliente_detalle)

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        PacienteListAdapter.FLAG_IN_PACIENTE = true
        ConfigLoading.showLoadingAnimation()
        setValues()
    }
    fun setValues(){
        if(EditClienteFragment.CLIENTE_EDIT.nombre == ""){
            ConfigLoading.showNodata()
        }else{

            binding.textNombreCliente.text = "${EditClienteFragment.CLIENTE_EDIT.nombre} ${EditClienteFragment.CLIENTE_EDIT.apellidoPaterno} ${EditClienteFragment.CLIENTE_EDIT.apellidoMaterno}"
            binding.textExpediente.text = EditClienteFragment.CLIENTE_EDIT.expediente
            binding.textCorreo.text = EditClienteFragment.CLIENTE_EDIT.correo
            binding.textTelefono.text = EditClienteFragment.CLIENTE_EDIT.telefono
            binding.textCorreo.text = EditClienteFragment.CLIENTE_EDIT.correo
            binding.txtWhatssap.text = EditClienteFragment.CLIENTE_EDIT.telefono
            binding.txtDireccion.text = "${EditClienteFragment.CLIENTE_EDIT.calle} #${EditClienteFragment.CLIENTE_EDIT.numero}, ${EditClienteFragment.CLIENTE_EDIT.colonia} ${EditClienteFragment.CLIENTE_EDIT.municipio}"


            obtenerPacientes()
        }
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun listeners(){
        binding.btnEdit.setOnClickListener {
            UtilFragment.changeFragment(requireActivity() , EditClienteFragment() ,TAG)
        }
        binding.btnTelefono.setOnClickListener {
            UtilHelper.llamarCliente(requireActivity(), EditClienteFragment.CLIENTE_EDIT.telefono)
        }
        binding.btnGoogleMaps.setOnClickListener {
            UtilHelper.abrirGoogleMaps(requireActivity(), EditClienteFragment.CLIENTE_EDIT.urlGoogleMaps)
        }
        binding.btnWhatssap.setOnClickListener {
            UtilHelper.enviarMensajeWhatsApp(requireActivity(), EditClienteFragment.CLIENTE_EDIT.telefono)
        }
        binding.btnCorreo.setOnClickListener {
            UtilHelper.enviarCorreoElectronicoGmail(requireActivity(), EditClienteFragment.CLIENTE_EDIT.correo)
        }
        binding.btnAddCliente.setOnClickListener {
            AddPacienteFragment.ADD_PACIENTE.idCliente = EditClienteFragment.CLIENTE_EDIT.id
            AddPacienteFragment.ADD_PACIENTE.nombreCliente = EditClienteFragment.CLIENTE_EDIT.nombre
            UtilFragment.changeFragment(requireActivity() , AddPacienteFragment() ,TAG)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun obtenerPacientes() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val pacientes = FirebasePacienteUtil.obtenerPacientesDeCliente(EditClienteFragment.CLIENTE_EDIT.id)
                mostrarPacientes(pacientes)
            } catch (e: Exception) {

            }
        }
    }

    private fun mostrarPacientes(pacientes: List<PacienteModel>) {
        if (pacientes.isEmpty()) {
           // ConfigLoading.showNodata()
        } else {
            pacienteListAdapter = PacienteListAdapter(requireActivity(),pacientes)
            binding.listaPacientes.adapter = pacienteListAdapter
            binding.listaPacientes.divider = null
        }
        ConfigLoading.hideLoadingAnimation()
    }

    override fun onResume() {
        super.onResume()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext() , ClienteFragment() ,TAG)
            }
        })
    }

}