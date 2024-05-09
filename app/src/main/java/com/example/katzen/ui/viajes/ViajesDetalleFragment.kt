package com.example.katzen.ui.viajes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Venta.VentaMesDetalleAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Fragment.Viajes.ViajesFragment
import com.example.katzen.Helper.DialogHelper.Companion.dialogAddDomicilio
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.databinding.FragmentViajesDetalleBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat


class ViajesDetalleFragment : Fragment() {
    val TAG = "ViajesDetalleFragment"
    private var _binding: FragmentViajesDetalleBinding? = null
    var listVentaMesDetalle = arrayListOf<VentaMesDetalleModel>()


    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    var myTopPostsQuery: DatabaseReference? = null
    var queryRefreshCost: DatabaseReference? = null
    var adapter: VentaMesDetalleAdapter? = null
    var vMDM = VentaMesDetalleModel()
    var postListener : ValueEventListener? = null
    lateinit var loadingHelper : LoadingHelper




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentViajesDetalleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadingHelper = LoadingHelper(binding.loading,binding.contentList,binding.listViajesDetalle,
            binding.btnAddTravel,binding.contentNotResult)

        loadingHelper.loading()
        initFirebase()
        initUI()
        getGasolinaApi()

        binding.btnAddTravel.setOnClickListener {
          dialogAddDomicilio(requireActivity(),vMDM,myTopPostsQuery!!,loadingHelper)
        }

        return root
    }

    fun initUI(){
        adapter = VentaMesDetalleAdapter(requireActivity(), listVentaMesDetalle, myTopPostsQuery!!,loadingHelper)
        binding.listViajesDetalle.adapter = adapter
        binding.listViajesDetalle.divider = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        database.onDisconnect()
        myTopPostsQuery!!.removeEventListener(postListener!!)
        myTopPostsQuery!!.onDisconnect()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        database.onDisconnect()
        myTopPostsQuery!!.removeEventListener(postListener!!)
        myTopPostsQuery!!.onDisconnect()
    }
    fun initFirebase(){
        database = Firebase.database.reference
        queryRefreshCost =  database.child("Katzen").child("Gasolina").child(UtilHelper.getDateYear())
                            .child(Config.MES_DETALLE)
        myTopPostsQuery = database.child("Katzen").child("Gasolina")
                            .child(UtilHelper.getDateYear()).child(Config.MES_DETALLE).child("cargos")

    }
    fun getData(dataSnapshot: DataSnapshot){
        var key_date = ""
        listVentaMesDetalle.clear()
        Config.COSTO = 0.00
        Config.GANANCIA = 0.00
        Config.VENTA = 0.00

        if(dataSnapshot.children.count() > 0){

            for (postSnapshot in dataSnapshot.children) {
                key_date = ""
                key_date = postSnapshot.key.toString()
                Log.e("vemostodoporaqui", postSnapshot.key.toString())

                for (data in postSnapshot.children) {
                    var ventaMesDetalleModel = VentaMesDetalleModel()
                    ventaMesDetalleModel.venta = data.child("venta").value.toString()
                    ventaMesDetalleModel.categoria = data.child("categoria").value.toString()
                    ventaMesDetalleModel.costo = data.child("costo").value.toString()
                    ventaMesDetalleModel.domicilio = data.child("domicilio").value.toString()
                    ventaMesDetalleModel.fecha = data.child("fecha").value.toString()
                    ventaMesDetalleModel.ganancia = data.child("ganancia").value.toString()
                    ventaMesDetalleModel.kilometros = data.child("kilometros").value.toString()
                    ventaMesDetalleModel.linkMaps = data.child("linkMaps").value.toString()
                    ventaMesDetalleModel.key = data.child("key").value.toString()
                    ventaMesDetalleModel.key_fecha_hora = data.child("key_fecha_hora").value.toString()
                    ventaMesDetalleModel.key_date = data.child("key_date").value.toString()
                    if(ventaMesDetalleModel.key_date.equals("")){
                        ventaMesDetalleModel.key_date = key_date
                    }



                    Config.COSTO += ventaMesDetalleModel.costo.toDouble()
                    Config.VENTA += ventaMesDetalleModel.venta.toDouble()
                    Config.GANANCIA += ventaMesDetalleModel.ganancia.toDouble()


                    listVentaMesDetalle.add(ventaMesDetalleModel)
                }
            }
            adapter!!.notifyDataSetChanged()
            refreshCosts()
            loadingHelper.not_loading()
        }else{
            loadingHelper.not_loading_result()
            Toast.makeText(requireContext(),"No hay domicilios agregados.",Toast.LENGTH_LONG).show()
        }
    }
    private fun getGasolinaApi(){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                loadingHelper.loading()
                Log.e("vemostodoporaqui", "entramo de nuevo despues de agregar")
                getData(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                loadingHelper.not_loading_result()
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        myTopPostsQuery!!.orderByChild("key_fecha_hora")!!.addValueEventListener(postListener!!)
    }
    fun refreshCosts(){
        val df = DecimalFormat("#.##")

        queryRefreshCost!!.child("costo").setValue(df.format(Config.COSTO))
        queryRefreshCost!!.child("ganancia").setValue(df.format(Config.GANANCIA))
        queryRefreshCost!!.child("venta").setValue(df.format(Config.VENTA))
    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext(),ViajesFragment(),TAG)
            }
        })
    }

}