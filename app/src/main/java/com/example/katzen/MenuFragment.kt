package com.example.katzen

import com.example.katzen.Fragment.Producto.MenuProductosFragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Fragment.Cliente.ClienteFragment
import com.example.katzen.Fragment.Inventario.InventarioFragment
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Producto.AddProductoFragment
import com.example.katzen.Fragment.Venta.VentasFragment
import com.example.katzen.Fragment.Viajes.ViajesFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.MenuModel
import com.example.katzen.Model.ProductoModel
import com.ninodev.katzen.databinding.MenuFragmentBinding
import com.example.katzen.Fragment.Card.PaymetCardFragment
import com.example.katzen.Fragment.Gasolina.FuellFragment
import com.ninodev.katzen.R

class MenuFragment : Fragment() {
    val TAG : String  = "MenuFragment"

    private var _binding: MenuFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var menuList: List<MenuModel>


    //MenuModel("Venta", R.drawable.img_venta),
    //MenuModel("Producto", R.drawable.img_producto),
    //MenuModel("Lista Productos", R.drawable.img_lista_productos),
    //MenuModel("Balance General", R.drawable.img_balance_general),
    //MenuModel("Inventario", R.drawable.img_inventario),

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.nav_home)

        menuList = listOf(
            MenuModel(requireActivity().getString(R.string.menu_gasolina), R.drawable.img_gasolina),
            MenuModel(requireActivity().getString(R.string.menu_pago_tarjeta), R.drawable.img_pago_tarjeta),
            MenuModel(requireActivity().getString(R.string.menu_paciente), R.drawable.img_paciente),
            MenuModel(requireActivity().getString(R.string.menu_cliente), R.drawable.img_cliente),
            MenuModel(requireActivity().getString(R.string.menu_productos), R.drawable.img_productos),
            MenuModel(requireActivity().getString(R.string.menu_viajes), R.drawable.img_viajes),
            MenuModel(requireActivity().getString(R.string.menu_campania), R.drawable.img_campania)
        )
        val adapter = MenuAdapter(requireContext(), menuList)
        binding.lisMenu.adapter = adapter

        binding.lisMenu.setOnItemClickListener { adapterView, view, position, l ->
            val menuItem = menuList[position]
            val titulo = menuItem.titulo

            when (titulo) {
                "Venta" -> {
                    UtilFragment.changeFragment(requireActivity(), VentasFragment(),TAG)
                }
                "Producto" -> {
                    Config.PRODUCTO_EDIT = ProductoModel()
                    UtilFragment.changeFragment(requireActivity(), AddProductoFragment(),TAG)
                }
                "Balance General" -> {
                }
                "Lista Productos" -> {
                    UtilFragment.changeFragment(requireActivity(), MenuProductosFragment(),TAG)
                }
                "Inventario" -> {
                    UtilFragment.changeFragment(requireActivity(), InventarioFragment(),TAG)
                }
                requireActivity().getString(R.string.menu_paciente) -> {
                    UtilFragment.changeFragment(requireActivity(), PacienteFragment(),TAG)
                }
                requireActivity().getString(R.string.menu_cliente) -> {
                    UtilFragment.changeFragment(requireActivity(), ClienteFragment(),TAG)
                }
                requireActivity().getString(R.string.menu_gasolina) -> {
                    UtilFragment.changeFragment(requireActivity(), FuellFragment(),TAG)
                }
                requireActivity().getString(R.string.menu_pago_tarjeta) -> {
                    UtilFragment.changeFragment(requireActivity(), PaymetCardFragment(),TAG)
                }
                requireActivity().getString(R.string.menu_viajes) -> {
                    //UtilFragment.changeFragment(requireActivity(), ViajesFragment(),TAG)
                    UtilFragment.changeFragment(requireActivity(), ViajesFragment(),TAG)
                }
                requireActivity().getString(R.string.menu_campania) -> {
                    UtilFragment.changeFragment(requireActivity(), CampañaFragment(),TAG)
                }
            }
        }


        return root
    }

    override fun onResume() {
        super.onResume()

        var doubleBackToExitPressedOnce = false

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        // Si se presionó dos veces, se sale de la aplicación
                        requireActivity().finish()
                        return
                    }

                    doubleBackToExitPressedOnce = true
                    Toast.makeText(requireContext(), "Presione de nuevo para salir", Toast.LENGTH_SHORT).show()

                    // Se establece el tiempo de espera para el segundo botón de retroceso
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000) // 2 segundos
                }
            })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}