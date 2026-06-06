package com.example.katzen.Helper

import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.PacienteModel
import com.example.katzen.ViewModel.PortalSessionViewModel
import com.example.katzen.ViewModel.StaffEditSessionViewModel

/**
 * Puente entre companion objects legacy y ViewModels con alcance de Activity.
 */
object StaffEditSessionBridge {
    private var session: StaffEditSessionViewModel? = null
    private var clienteFallback = ClienteModel()
    private var pacienteFallback = PacienteModel()

    fun bind(viewModel: StaffEditSessionViewModel) {
        session = viewModel
        if (viewModel.clienteEdit.id.isBlank() && clienteFallback.id.isNotBlank()) {
            viewModel.clienteEdit = clienteFallback
        }
        if (viewModel.pacienteEdit.id.isBlank() && pacienteFallback.id.isNotBlank()) {
            viewModel.pacienteEdit = pacienteFallback
        }
    }

    fun unbind() {
        session = null
    }

    var clienteEdit: ClienteModel
        get() = session?.clienteEdit ?: clienteFallback
        set(value) {
            session?.clienteEdit = value
            clienteFallback = value
        }

    var pacienteEdit: PacienteModel
        get() = session?.pacienteEdit ?: pacienteFallback
        set(value) {
            session?.pacienteEdit = value
            pacienteFallback = value
        }
}

object PortalSessionBridge {
    private var session: PortalSessionViewModel? = null
    private var clienteIdFallback = ""
    private var clienteNombreFallback = ""

    fun bind(viewModel: PortalSessionViewModel) {
        session = viewModel
        if (viewModel.clienteId.isBlank() && clienteIdFallback.isNotBlank()) {
            viewModel.clienteId = clienteIdFallback
            viewModel.clienteNombre = clienteNombreFallback
        }
    }

    fun unbind() {
        session = null
    }

    var clienteId: String
        get() = session?.clienteId?.takeIf { it.isNotBlank() } ?: clienteIdFallback
        set(value) {
            session?.clienteId = value
            clienteIdFallback = value
        }

    var clienteNombre: String
        get() = session?.clienteNombre ?: clienteNombreFallback
        set(value) {
            session?.clienteNombre = value
            clienteNombreFallback = value
        }
}
