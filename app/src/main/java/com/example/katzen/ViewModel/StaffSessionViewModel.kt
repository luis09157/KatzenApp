package com.example.katzen.ViewModel

import androidx.lifecycle.ViewModel
import com.example.katzen.Helper.StaffRoleHelper

class StaffSessionViewModel : ViewModel() {
    var staffRole: String = ""

    fun hasPermission(permission: StaffRoleHelper.Permission): Boolean =
        StaffRoleHelper.hasPermission(staffRole, permission)

    fun canAccessNavItem(itemId: Int): Boolean =
        StaffRoleHelper.canAccessNavItem(staffRole, itemId)
}
