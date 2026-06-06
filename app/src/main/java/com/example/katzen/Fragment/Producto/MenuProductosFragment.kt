package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.MenuModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.MenuFragmentBinding

class MenuProductosFragment : Fragment() {
    val TAG: String = "MenuFragment"

    private var _binding: MenuFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFragmentBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.menu_productos)
        setupMenu()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.goBackOrHome(requireContext())
                }
            })
    }

    private fun setupMenu() {
        val menuList = listOf(
            MenuModel(requireActivity().getString(R.string.submenu_productos_prod_varios), R.drawable.img_prod_varios),
            MenuModel(requireActivity().getString(R.string.submenu_productos_alimentos), R.drawable.img_alimento),
            MenuModel(requireActivity().getString(R.string.submenu_productos_servicios), R.drawable.img_servicios),
            MenuModel(requireActivity().getString(R.string.submenu_productos_medicamentos), R.drawable.img_medicamentos),
            MenuModel(requireActivity().getString(R.string.submenu_productos_procedimientos), R.drawable.img_procedimientos),
            MenuModel(requireActivity().getString(R.string.submenu_productos_m_complementarios), R.drawable.img_prod_complementario),
            MenuModel(requireActivity().getString(R.string.submenu_productos_estetica), R.drawable.img_prod_estetica)
        )

        val adapter = MenuAdapter { menuItem ->
            val fragment = when (menuItem.titulo) {
                requireActivity().getString(R.string.submenu_productos_medicamentos) ->
                    ListaMedicamentosFragment()
                requireActivity().getString(R.string.submenu_productos_estetica) ->
                    ListaProductosEsteticaFragment()
                requireActivity().getString(R.string.menu_campania) ->
                    CampañaFragment()
                requireActivity().getString(R.string.submenu_productos_alimentos) ->
                    ListaAlimentosFragment()
                requireActivity().getString(R.string.submenu_productos_prod_varios) ->
                    ListaProductosVariosFragment()
                requireActivity().getString(R.string.submenu_productos_servicios) ->
                    ListaServiciosFragment()
                requireActivity().getString(R.string.submenu_productos_procedimientos) ->
                    ListaProcedimientosFragment()
                requireActivity().getString(R.string.submenu_productos_m_complementarios) ->
                    ListaAuxiliaresFragment()
                else -> return@MenuAdapter
            }
            UtilFragment.changeFragment(
                requireActivity(),
                fragment,
                TAG,
                listKey = ListScrollKeys.MENU_PRODUCTOS,
                listRecyclerView = binding.lisMenu,
                selectedItemId = menuItem.titulo
            )
        }
        adapter.updateList(menuList)
        ListUiHelper.setupGridList(binding.lisMenu, 2)
        binding.lisMenu.adapter = adapter
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.MENU_PRODUCTOS,
            binding.lisMenu,
            menuList.map { it.titulo }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
