package com.example.katzen.ViewModel

import androidx.lifecycle.ViewModel
import com.example.katzen.Helper.PortalDeepLinkHelper

class PortalSessionViewModel : ViewModel() {
    var clienteId: String = ""
    var clienteNombre: String = ""
    var pendingDeepLink: PortalDeepLinkHelper.Target? = null
}
