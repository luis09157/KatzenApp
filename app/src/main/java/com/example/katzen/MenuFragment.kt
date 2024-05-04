package com.example.katzen

import com.example.katzen.Fragment.Producto.MenuProductosFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Fragment.Cliente.ClienteFragment
import com.example.katzen.Fragment.Inventario.InventarioFragment
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Producto.AddProductoFragment
import com.example.katzen.Fragment.Venta.VentasFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.MenuModel
import com.example.katzen.Model.ProductoModel
import com.example.katzen.databinding.MenuFragmentBinding
import com.example.katzen.ui.card.PaymetCardFragment
import com.example.katzen.ui.gasolina.FuellFragment

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

        menuList = listOf(
            MenuModel(requireActivity().getString(R.string.menu_gasolina), R.drawable.img_cliente),
            MenuModel(requireActivity().getString(R.string.menu_pago_tarjeta), R.drawable.img_cliente),
            MenuModel(requireActivity().getString(R.string.menu_paciente), R.drawable.img_paciente),
            MenuModel(requireActivity().getString(R.string.menu_cliente), R.drawable.img_cliente)
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
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}