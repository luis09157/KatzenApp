package com.example.katzen.Fragment.Paciente

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Cliente.ClienteListAdapter
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Fragment.Cliente.ClienteFragment
import com.example.katzen.Fragment.Cliente.EditClienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.example.katzen.databinding.PacienteDetalleFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PacienteDetalleFragment : Fragment() {
    val TAG : String  = "ClienteDetalleFragment"

    private var _binding: PacienteDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var clienteListAdapter: ClienteListAdapter
    private lateinit var clientesList: MutableList<ClienteModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PacienteDetalleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_cliente_detalle)

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        setValues()
    }
    fun setValues(){
        if(EditarPacienteFragment.PACIENTE_EDIT.nombre == ""){
            ConfigLoading.showNodata()
        }else{
            binding.textNombreCliente.text = EditarPacienteFragment.PACIENTE_EDIT.nombre
            binding.textEspecie.text = EditarPacienteFragment.PACIENTE_EDIT.especie
            binding.textRaza.text = EditarPacienteFragment.PACIENTE_EDIT.raza
            binding.textSexo.text = EditarPacienteFragment.PACIENTE_EDIT.sexo
            binding.textEdad.text =  "${EditarPacienteFragment.PACIENTE_EDIT.edad} años"
            binding.textPeso.text =  "${EditarPacienteFragment.PACIENTE_EDIT.peso} Kilos"

            ConfigLoading.hideLoadingAnimation()
            obtenerClientePorId(EditarPacienteFragment.PACIENTE_EDIT.idCliente)
        }
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun listeners(){
        binding.btnEdit.setOnClickListener {
            UtilFragment.changeFragment(requireActivity() , EditarPacienteFragment() ,TAG)
        }

        binding.btnAddCliente.setOnClickListener {
            AddPacienteFragment.ADD_PACIENTE.idCliente = EditClienteFragment.CLIENTE_EDIT.id
            AddPacienteFragment.ADD_PACIENTE.nombreCliente = EditClienteFragment.CLIENTE_EDIT.nombre
            UtilFragment.changeFragment(requireActivity() , AddPacienteFragment() ,TAG)
        }
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
                UtilFragment.changeFragment(requireContext() , PacienteFragment() ,TAG)
            }
        })
    }

}