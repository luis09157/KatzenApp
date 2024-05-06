package com.example.katzen.Fragment.Paciente

import PacienteModel
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Paciente.PacienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.R
import com.example.katzen.databinding.PacienteFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PacienteFragment : Fragment() {
    val TAG : String  = "PacienteFragment"

    private var _binding: PacienteFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var mascotasList: MutableList<PacienteModel>
    private lateinit var mascotasAdapter: PacienteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PacienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_paciente)

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        mascotasList = mutableListOf()
        mascotasAdapter = PacienteAdapter(requireActivity(), mascotasList)
        binding.lisMenuMascota.adapter = mascotasAdapter
        binding.lisMenuMascota.divider = null

        obtenerMascotas()
    }
    fun listeners(){
        binding.btnAddPaciente.setOnClickListener {
            AddPacienteFragment.ADD_PACIENTE = PacienteModel()
            FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY
            UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(),TAG)
        }
        binding.buscarMascota.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No se necesita implementación aquí, ya que filtramos a medida que el usuario escribe
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Aplicar el filtro del adaptador al escribir en el SearchView
                filterMascotas(newText.toString())
                return true
            }
        })
        binding.lisMenuMascota.setOnItemClickListener { adapterView, view, i, l ->
            EditarPacienteFragment.PACIENTE_EDIT = mascotasList[i]
            UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(),TAG)
        }
    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun obtenerMascotas(){
        FirebasePacienteUtil.obtenerListaMascotas(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                mascotasList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de productos
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(PacienteModel::class.java)
                    producto?.let { mascotasList.add(it) }
                }

                // Notificar al adaptador que los datos han cambiado
                mascotasAdapter.notifyDataSetChanged()
                if (mascotasList.size > 0){
                    ConfigLoading.hideLoadingAnimation()
                }else{
                    ConfigLoading.showNodata()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                ConfigLoading.showNodata()
                // Manejar errores de la consulta a la base de datos
                // Por ejemplo, mostrar un mensaje de error
            }
        })
    }
    fun filterMascotas(text: String) {
        val filteredList = mascotasList.filter { mascota ->
            mascota.nombre.contains(text, ignoreCase = true)
        }
        mascotasAdapter.updateList(filteredList)
    }
    override fun onResume() {
        super.onResume()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext() , MenuFragment() ,TAG)
            }
        })
    }
}