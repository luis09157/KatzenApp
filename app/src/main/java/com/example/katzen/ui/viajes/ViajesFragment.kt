package com.example.katzen.ui.viajes

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Venta.VentaMesAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesModel
import com.example.katzen.databinding.FragmentViajesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ViajesFragment : Fragment() {
    val TAG = "ViajesFragmentLog"
    private var _binding: FragmentViajesBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    var listVentaMes = arrayListOf<VentaMesModel>()
    var myTopPostsQuery: DatabaseReference? = null
    var QueryGas: DatabaseReference? = null
    var postListener : ValueEventListener? = null
    var adapter: VentaMesAdapter? = null
    lateinit var loadingHelper : LoadingHelper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentViajesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadingHelper = LoadingHelper(binding.loading,binding.contentList,binding.listViajes,null,binding.contentNotResult)

        loadingHelper.loading()
        initUI()
        initFirebase()
        getGasolinaApi()
        UtilHelper.hideKeyBoardWorld(requireActivity(),root)

        return root
    }

    fun desconectedQuery(){
        database.onDisconnect()
        myTopPostsQuery!!.removeEventListener(postListener!!)
        QueryGas!!.removeEventListener(postListener!!)
        myTopPostsQuery!!.onDisconnect()
        QueryGas!!.onDisconnect()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        desconectedQuery()
        _binding = null
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

    override fun onStop() {
        super.onStop()
        desconectedQuery()
    }

    fun initUI(){
        adapter = VentaMesAdapter(requireActivity(), listVentaMes)
        binding.listViajes.adapter = adapter
        binding.listViajes.divider = null

        binding.listViajes.setOnItemClickListener { adapterView, view, i, l ->
            Config.MES_DETALLE = listVentaMes.get(i).fecha
            database.onDisconnect()
            myTopPostsQuery!!.removeEventListener(postListener!!)
            myTopPostsQuery!!.onDisconnect()
            UtilFragment.changeFragment(requireContext(),ViajesDetalleFragment(),TAG)
        }
    }
    fun initFirebase(){
        database = Firebase.database.reference
        myTopPostsQuery = database.child("Katzen").child("Gasolina").child(UtilHelper.getDateYear())
        QueryGas = database.child("Katzen")
    }
    fun getData(dataSnapshot: DataSnapshot){
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
            Log.e("vemostodoporaqui","estamos fuera getdata")
            Log.e("vemostodoporaqui",listVentaMes.size.toString())
            adapter!!.notifyDataSetChanged()
            loadingHelper.not_loading()
        }else{
            loadingHelper.not_loading_result()
        }

    }
    fun getGasolinaApi(){
         postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("vemostodoporaqui","estamos en la venta de los meses")
                initYearFirebase(dataSnapshot)
                getData(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                loadingHelper.not_loading_result()
            }
        }
        myTopPostsQuery!!.addValueEventListener(postListener!!)
    }
    fun initYearFirebase(dataSnapshot: DataSnapshot){
        var listMonths = UtilHelper.getMontsThisYears()
        var ventaMesModel = VentaMesModel()

        if (dataSnapshot.children.count() != 12){
            for (postSnapshot in dataSnapshot.children) {
                listMonths.remove(postSnapshot.key.toString())
            }
            for(i in 0..listMonths.size - 1){
                ventaMesModel.venta = "0.00"
                ventaMesModel.costo = "0.00"
                ventaMesModel.ganancia = "0.00"
                ventaMesModel.anio = UtilHelper.getDateYear()
                ventaMesModel.mes = UtilHelper.getMonthYear(listMonths.get(i).split("-")[0].toInt())
                ventaMesModel.fecha = UtilHelper.getDate()

                myTopPostsQuery!!.child(listMonths.get(i)).setValue(ventaMesModel)
            }
        }
    }
}