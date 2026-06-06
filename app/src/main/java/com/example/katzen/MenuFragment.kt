package com.example.katzen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.katzen.DataBaseFirebase.FirebaseAuthPerfilUtil
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.StaffRoleHelper
import com.example.katzen.Helper.staffSession
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MainActivity
import com.example.katzen.Model.MenuModel
import com.example.katzen.Service.Notificador
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.ninodev.katzen.databinding.MenuFragmentBinding
import com.ninodev.katzen.R
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuFragment : Fragment() {

    val TAG: String = "MenuFragment"

    private var _binding: MenuFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificador: Notificador

    private lateinit var menuAdapter: MenuAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            verificarRecordatoriosHoy()
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

        menuAdapter = MenuAdapter { menu -> navigateToMenu(menu.titulo) }
        ListUiHelper.setupGridList(binding.lisMenu, 2)
        binding.lisMenu.adapter = menuAdapter
        refreshMenuGrid()

        bindWelcomeCard()

        if (verificarPermisoDeNotificaciones()) {
            verificarRecordatoriosHoy()
        } else {
            pedirPermisoDeNotificaciones()
        }

        return root
    }

    private fun bindWelcomeCard() {
        val user = MainActivity._INFO_USER
        val authUser = FirebaseAuth.getInstance().currentUser
        val displayName = user.nombreUsuario.ifBlank {
            authUser?.displayName.orEmpty().ifBlank {
                authUser?.email?.substringBefore("@").orEmpty()
            }
        }

        binding.tvWelcomeTitle.text = if (displayName.isNotBlank()) {
            getString(R.string.menu_welcome_user, displayName)
        } else {
            getString(R.string.menu_welcome_title)
        }

        val fecha = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
            .format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.chipFecha.text = fecha

        val uid = authUser?.uid.orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            val perfil = withContext(Dispatchers.IO) {
                runCatching { FirebaseAuthPerfilUtil.obtenerPerfil(uid) }.getOrNull()
            }
            perfil?.staffRole?.takeIf { it.isNotBlank() }?.let { staffSession().staffRole = it }
            val rol = getString(StaffRoleHelper.roleLabelRes(perfil?.staffRole ?: staffSession().staffRole))
            binding.tvWelcomeSubtitle.text = getString(R.string.menu_welcome_role, rol)
            refreshMenuGrid()
        }
    }

    private fun refreshMenuGrid() {
        if (_binding == null || !::menuAdapter.isInitialized) return
        val items = buildMenuList()
        menuAdapter.updateList(items)
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.MENU_PRINCIPAL,
            binding.lisMenu,
            items.map { it.titulo }
        )
    }

    private fun buildMenuList(): List<MenuModel> = listOf(
        MenuModel(getString(R.string.menu_gasolina), R.drawable.img_gasolina),
        MenuModel(getString(R.string.menu_pago_tarjeta), R.drawable.img_pago_tarjeta),
        MenuModel(getString(R.string.menu_paciente), R.drawable.img_paciente),
        MenuModel(getString(R.string.menu_cliente), R.drawable.img_cliente),
        MenuModel(getString(R.string.menu_productos), R.drawable.img_productos),
        MenuModel(getString(R.string.menu_venta), R.drawable.img_productos),
        MenuModel(getString(R.string.menu_inventario), R.drawable.img_productos),
        MenuModel(getString(R.string.menu_viajes), R.drawable.img_viajes),
        MenuModel(getString(R.string.menu_campania), R.drawable.img_campania)
    ).filter { menu ->
        StaffRoleHelper.canAccessMenuTitle(
            staffSession().staffRole,
            menu.titulo,
            menuTitles()
        )
    }

    private fun menuTitles() = StaffRoleHelper.MenuTitles(
        gasolina = getString(R.string.menu_gasolina),
        pagoTarjeta = getString(R.string.menu_pago_tarjeta),
        paciente = getString(R.string.menu_paciente),
        cliente = getString(R.string.menu_cliente),
        productos = getString(R.string.menu_productos),
        venta = getString(R.string.menu_venta),
        inventario = getString(R.string.menu_inventario),
        viajes = getString(R.string.menu_viajes),
        campania = getString(R.string.menu_campania)
    )

    private fun navigateToMenu(titulo: String) {
        val fragment = when (titulo) {
            getString(R.string.menu_productos) -> MenuProductosFragment()
            getString(R.string.menu_paciente) -> PacienteFragment()
            getString(R.string.menu_cliente) -> ClienteFragment()
            getString(R.string.menu_gasolina) -> FuellFragment()
            getString(R.string.menu_pago_tarjeta) -> PaymetCardFragment()
            getString(R.string.menu_viajes) -> YearViajeListFragment()
            getString(R.string.menu_campania) -> YearListFragment()
            getString(R.string.menu_venta) -> VentasFragment()
            getString(R.string.menu_inventario) -> InventarioFragment()
            else -> return
        }
        UtilFragment.changeFragment(
            requireActivity(),
            fragment,
            TAG,
            listKey = ListScrollKeys.MENU_PRINCIPAL,
            listRecyclerView = binding.lisMenu,
            selectedItemId = titulo
        )
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
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun verificarRecordatoriosHoy() {
        if (!isAdded) return

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        if (prefs.getString(KEY_VACUNAS_NOTIF_FECHA, "") == hoy) {
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        database.child("Katzen/Vacunas")
            .orderByChild("fechaRecordatorio")
            .startAt(hoy)
            .endAt("$hoy\uf8ff")
            .get()
            .addOnSuccessListener { vacunasSnap ->
                if (!isAdded) return@addOnSuccessListener

                val mascotaCache = mutableMapOf<String, DataSnapshot>()
                val clienteCache = mutableMapOf<String, DataSnapshot>()
                var notificacionesEnviadas = 0
                var pendientes = vacunasSnap.children.count()

                if (pendientes == 0) return@addOnSuccessListener

                fun finalizarSiListo() {
                    pendientes--
                    if (pendientes <= 0 && notificacionesEnviadas > 0) {
                        prefs.edit().putString(KEY_VACUNAS_NOTIF_FECHA, hoy).apply()
                    }
                }

                for (vacunaSnapshot in vacunasSnap.children) {
                    val fechaRecordatorio = vacunaSnapshot
                        .child("fechaRecordatorio")
                        .getValue(String::class.java)
                        ?.split(" ")
                        ?.firstOrNull()

                    if (fechaRecordatorio != hoy) {
                        finalizarSiListo()
                        continue
                    }

                    val idMascota = vacunaSnapshot
                        .child("idPaciente")
                        .getValue(String::class.java)
                        ?: run {
                            finalizarSiListo()
                            continue
                        }
                    val nombreVacuna = vacunaSnapshot
                        .child("vacuna")
                        .getValue(String::class.java)
                        ?: "Vacuna"

                    fun procesarConMascota(mascotaSnapshot: DataSnapshot) {
                        val nombreMascota = mascotaSnapshot.child("nombre").getValue(String::class.java)
                        val idCliente = mascotaSnapshot.child("idCliente").getValue(String::class.java)
                        if (nombreMascota.isNullOrBlank() || idCliente.isNullOrBlank()) {
                            finalizarSiListo()
                            return
                        }

                        fun procesarConCliente(clienteSnapshot: DataSnapshot) {
                            val nombreCliente = clienteSnapshot.child("nombre").getValue(String::class.java)
                            if (nombreCliente.isNullOrBlank()) {
                                finalizarSiListo()
                                return
                            }
                            val apellidoPaterno = clienteSnapshot
                                .child("apellidoPaterno")
                                .getValue(String::class.java)
                                .orEmpty()
                            val apellidoMaterno = clienteSnapshot
                                .child("apellidoMaterno")
                                .getValue(String::class.java)
                                .orEmpty()
                            val nombreCompletoCliente =
                                "$nombreCliente $apellidoPaterno $apellidoMaterno".trim()

                            if (verificarPermisoDeNotificaciones()) {
                                notificador.mostrarNotificacion(
                                    getString(R.string.title_notification),
                                    getString(
                                        R.string.notification_vacuna_hoy,
                                        nombreMascota,
                                        nombreCompletoCliente,
                                        nombreVacuna
                                    )
                                )
                                notificacionesEnviadas++
                            }
                            finalizarSiListo()
                        }

                        val cachedCliente = clienteCache[idCliente]
                        if (cachedCliente != null) {
                            procesarConCliente(cachedCliente)
                        } else {
                            database.child("Katzen/Cliente").child(idCliente).get()
                                .addOnSuccessListener { clienteSnap ->
                                    if (!isAdded) {
                                        finalizarSiListo()
                                        return@addOnSuccessListener
                                    }
                                    clienteCache[idCliente] = clienteSnap
                                    procesarConCliente(clienteSnap)
                                }
                                .addOnFailureListener { finalizarSiListo() }
                        }
                    }

                    val cachedMascota = mascotaCache[idMascota]
                    if (cachedMascota != null) {
                        procesarConMascota(cachedMascota)
                    } else {
                        database.child("Katzen/Mascota").child(idMascota).get()
                            .addOnSuccessListener { mascotaSnap ->
                                if (!isAdded) {
                                    finalizarSiListo()
                                    return@addOnSuccessListener
                                }
                                mascotaCache[idMascota] = mascotaSnap
                                procesarConMascota(mascotaSnap)
                            }
                            .addOnFailureListener { finalizarSiListo() }
                    }
                }
            }
            .addOnFailureListener {
                showVacunasError()
            }
    }

    private fun showVacunasError() {
        if (isAdded) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_loading_vacunas),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val PREFS_NAME = "katzen_prefs"
        private const val KEY_VACUNAS_NOTIF_FECHA = "vacunas_notif_fecha"
    }
}
