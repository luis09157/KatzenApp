package com.example.katzen.ui.mascota

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MascotaAdapter
import com.example.katzen.Fragment.AddMascotaFragment
import com.example.katzen.Helper.FirebaseHelper
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.MascotaModel
import com.example.katzen.databinding.FragmentMascotaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class MascotaFragment : Fragment() {
    val TAG : String = "MascotaFragment"
    private var _binding: FragmentMascotaBinding? = null
    lateinit var loadingHelper : LoadingHelper
    var adapter: MascotaAdapter? = null
    var listMascotas = arrayListOf<MascotaModel>()
    var postListener : ValueEventListener? = null
    var queryMascota: DatabaseReference? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMascotaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadingHelper = LoadingHelper(binding.loading,binding.contentList,binding.listMascotas,
            binding.btnAddMascota,binding.contentNotResult)

        loadingHelper.loading()

        queryMascota = FirebaseHelper.getRefFirebaseMascotas()
        initUI()
        getMascotasAPI()

        binding.btnAddMascota.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), AddMascotaFragment(),TAG)
        }


        return root
    }
    fun initUI(){
        adapter = MascotaAdapter(requireContext(),listMascotas)
        binding.listMascotas.adapter = adapter
        binding.listMascotas.divider = null
        binding.listMascotas.setOnItemClickListener { _, _, i, _ ->
            UtilFragment.changeFragment(requireContext(),MascotaDetalleFragment(listMascotas[i]),"MascotaDetalleFragment")
        }
    }
    private fun getMascotasAPI(){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                loadingHelper.loading()
                getData(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                loadingHelper.not_loading_result()
            }
        }
        queryMascota!!.orderByChild("fecha")!!.addValueEventListener(postListener!!)
    }
    fun getData(dataSnapshot: DataSnapshot){
        listMascotas.clear()
        if(dataSnapshot.children.count() > 0){

            for (postSnapshot in dataSnapshot.children) {
                for (data in postSnapshot.children) {
                    var mascotaModel = MascotaModel()

                    mascotaModel.nombre = data.child("nombre").value.toString()
                    mascotaModel.peso = data.child("peso").value.toString()
                    mascotaModel.fecha = data.child("fecha").value.toString()
                    mascotaModel.sexo = data.child("sexo").value.toString()
                    mascotaModel.especie = data.child("especie").value.toString()
                    mascotaModel.edad = data.child("edad").value.toString()
                    mascotaModel.raza = data.child("raza").value.toString()

                    Log.e("checateesto",mascotaModel.nombre)

                    listMascotas.add(mascotaModel)
                }
            }
            adapter!!.notifyDataSetChanged()
            loadingHelper.not_loading()
        }else{
            loadingHelper.not_loading_result()
            Toast.makeText(requireContext(),"No hay domicilios agregados.", Toast.LENGTH_LONG).show()
        }
    }
    override fun onResume() { 
        super.onResume()
        if (view == null) {
            return
        }
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}