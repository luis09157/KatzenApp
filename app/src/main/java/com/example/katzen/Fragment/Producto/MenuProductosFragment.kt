package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.MenuModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.MenuFragmentBinding

class MenuProductosFragment : Fragment() {
    val TAG : String  = "MenuFragment"

    private var _binding: MenuFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var menuList: List<MenuModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFragmentBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.menu_productos)
        setupMenu()
        setupListeners()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), MenuFragment(), TAG)
                }
            })
    }

    private fun setupMenu() {
        menuList = listOf(
            MenuModel(requireActivity().getString(R.string.submenu_productos_prod_varios), R.drawable.img_prod_varios),
            MenuModel(requireActivity().getString(R.string.submenu_productos_alimentos), R.drawable.img_alimento),
            MenuModel(requireActivity().getString(R.string.submenu_productos_servicios), R.drawable.img_servicios),
            MenuModel(requireActivity().getString(R.string.submenu_productos_medicamentos), R.drawable.img_medicamentos),
            MenuModel(requireActivity().getString(R.string.submenu_productos_procedimientos), R.drawable.img_procedimientos),
            MenuModel(requireActivity().getString(R.string.submenu_productos_m_complementarios), R.drawable.img_prod_complementario),
            MenuModel(requireActivity().getString(R.string.submenu_productos_estetica), R.drawable.img_prod_estetica)
        )
        val adapter = MenuAdapter(requireContext(), menuList)
        binding.lisMenu.adapter = adapter
    }

    private fun setupListeners() {
        binding.lisMenu.setOnItemClickListener { adapterView, view, position, l ->
            val menuItem = menuList[position]
            val titulo = menuItem.titulo

            when (titulo) {
                requireActivity().getString(R.string.submenu_productos_medicamentos) -> {
                    UtilFragment.changeFragment(requireActivity(), ListaMedicamentosFragment(), TAG)
                }
                requireActivity().getString(R.string.submenu_productos_estetica) -> {
                    UtilFragment.changeFragment(requireActivity(), ListaProductosEsteticaFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_campania) -> {
                    UtilFragment.changeFragment(requireActivity(), CampañaFragment(),TAG)
                }
                requireActivity().getString(R.string.submenu_productos_alimentos) -> {
                    UtilFragment.changeFragment(requireActivity(), ListaAlimentosFragment(),TAG)
                }
                requireActivity().getString(R.string.submenu_productos_prod_varios) -> {
                    UtilFragment.changeFragment(requireActivity(), ListaProductosVariosFragment(),TAG)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}