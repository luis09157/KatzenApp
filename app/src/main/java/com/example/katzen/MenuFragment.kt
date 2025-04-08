package com.example.katzen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Fragment.Campaña.YearListFragment
import com.example.katzen.Fragment.Cliente.ClienteFragment
import com.example.katzen.Fragment.Inventario.InventarioFragment
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Producto.MenuProductosFragment
import com.example.katzen.Fragment.Venta.VentasFragment
import com.example.katzen.Fragment.Viajes.YearViajeListFragment
import com.example.katzen.Fragment.Card.PaymetCardFragment
import com.example.katzen.Fragment.Gasolina.FuellFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.MenuModel
import com.example.katzen.Service.Notificador
import com.ninodev.katzen.databinding.MenuFragmentBinding
import com.ninodev.katzen.R

class MenuFragment : Fragment() {

    val TAG: String = "MenuFragment"

    private var _binding: MenuFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var menuList: List<MenuModel>
    private lateinit var notificador: Notificador

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            notificador.mostrarNotificacion("¡Gracias!", "Permiso concedido, ahora puedes recibir notificaciones.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.nav_home)
        notificador = Notificador(requireActivity())

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

        binding.lisMenu.setOnItemClickListener { _, _, position, _ ->
            val menuItem = menuList[position]
            val titulo = menuItem.titulo

            when (titulo) {
                "Venta" -> {
                    UtilFragment.changeFragment(requireActivity(), VentasFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_productos) -> {
                    UtilFragment.changeFragment(requireActivity(), MenuProductosFragment(), TAG)
                }
                "Balance General" -> {
                    // Función aún no implementada
                }
                "Lista Productos" -> {
                    // UtilFragment.changeFragment(requireActivity(), MenuProductosFragment(), TAG)
                }
                "Inventario" -> {
                    UtilFragment.changeFragment(requireActivity(), InventarioFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_paciente) -> {
                    UtilFragment.changeFragment(requireActivity(), PacienteFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_cliente) -> {
                    UtilFragment.changeFragment(requireActivity(), ClienteFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_gasolina) -> {
                    UtilFragment.changeFragment(requireActivity(), FuellFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_pago_tarjeta) -> {
                    UtilFragment.changeFragment(requireActivity(), PaymetCardFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_viajes) -> {
                    UtilFragment.changeFragment(requireActivity(), YearViajeListFragment(), TAG)
                }
                requireActivity().getString(R.string.menu_campania) -> {
                    UtilFragment.changeFragment(requireActivity(), YearListFragment(), TAG)
                }
            }
        }

        // Notificación de prueba
        if (verificarPermisoDeNotificaciones()) {
            notificador.mostrarNotificacion("Hola!", "Esta es una notificación de prueba.")
        } else {
            pedirPermisoDeNotificaciones()
        }

        return root
    }

    private fun verificarPermisoDeNotificaciones(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun pedirPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()

        var doubleBackToExitPressedOnce = false

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        requireActivity().finish()
                        return
                    }

                    doubleBackToExitPressedOnce = true
                    Toast.makeText(requireContext(), "Presione de nuevo para salir", Toast.LENGTH_SHORT).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
