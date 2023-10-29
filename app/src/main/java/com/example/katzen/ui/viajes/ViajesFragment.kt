package com.example.katzen.ui.viajes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.VentaMesAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.VentaMesModel
import com.example.katzen.databinding.FragmentViajesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class ViajesFragment : Fragment() {
    val TAG = "ViajesFragment"
    private var _binding: FragmentViajesBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    var listVentaMes = arrayListOf<VentaMesModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentViajesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initUI()
        initFirebase()
        getGasolinaApi()

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initUI(){
        binding.listViajes.divider = null
        binding.listViajes.setOnItemClickListener { adapterView, view, i, l ->

            Config.MES_DETALLE = listVentaMes.get(i).fecha
            UtilFragment.changeFragment(requireContext(),ViajesDetalleFragment(),TAG)
        }
    }
    fun initFirebase(){
        database = Firebase.database.reference
    }
    private fun getGasolinaApi(){
        val myTopPostsQuery = database.child("Katzen").child("Gasolina")
            .orderByChild("starCount")

        myTopPostsQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listVentaMes.clear()
                if (dataSnapshot.children.count() > 0){
                    for (postSnapshot in dataSnapshot.children) {
                        var ventaMesModel = VentaMesModel()

                        ventaMesModel.mes = postSnapshot.child("mes").value.toString()
                        ventaMesModel.costo = postSnapshot.child("costo").value.toString()
                        ventaMesModel.ganancia = postSnapshot.child("ganancia").value.toString()
                        ventaMesModel.venta = postSnapshot.child("venta").value.toString()
                        ventaMesModel.anio = postSnapshot.child("anio").value.toString()
                        ventaMesModel.fecha = postSnapshot.key.toString()

                        listVentaMes.add(ventaMesModel)
                    }
                    val adapter = VentaMesAdapter(requireActivity(), listVentaMes)
                    binding.listViajes.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }


}