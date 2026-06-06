package com.example.katzen.ViewModel

import androidx.lifecycle.ViewModel
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.PacienteModel

/**
 * Estado de edición staff (cliente / paciente en flujo de detalle).
 * Reemplaza progresivamente los companion objects globales.
 */
class StaffEditSessionViewModel : ViewModel() {
    var clienteEdit: ClienteModel = ClienteModel()
    var pacienteEdit: PacienteModel = PacienteModel()
}
