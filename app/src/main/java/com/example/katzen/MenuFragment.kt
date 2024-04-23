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
import com.example.katzen.Fragment.Mascota.PacienteFragment
import com.example.katzen.Fragment.Producto.AddProductoFragment
import com.example.katzen.Fragment.Venta.AddVentaFragment
import com.example.katzen.Fragment.Venta.VentasFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.MenuModel
import com.example.katzen.Model.ProductoModel
import com.example.katzen.R
import com.example.katzen.databinding.MenuFragmentBinding

class MenuFragment : Fragment() {
    val TAG : String  = "MenuFragment"

    private var _binding: MenuFragmentBinding? = null
    private val binding get() = _binding!!
    val menuList = listOf(
        MenuModel("Venta", R.drawable.img_venta),
        MenuModel("Producto", R.drawable.img_producto),
        MenuModel("Lista Productos", R.drawable.img_lista_productos),
        MenuModel("Balance General", R.drawable.img_balance_general),
        MenuModel("Inventario", R.drawable.img_inventario),
        MenuModel("Paciente", R.drawable.img_paciente),
        MenuModel("Cliente", R.drawable.img_cliente)
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
                "Paciente" -> {
                    UtilFragment.changeFragment(requireActivity(), PacienteFragment(),TAG)
                }
                "Cliente" -> {
                    UtilFragment.changeFragment(requireActivity(), ClienteFragment(),TAG)
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