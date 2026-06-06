package com.example.katzen.Helper

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.katzen.ViewModel.PortalSessionViewModel
import com.example.katzen.ViewModel.StaffEditSessionViewModel
import com.example.katzen.ViewModel.StaffSessionViewModel

fun FragmentActivity.staffSession(): StaffSessionViewModel {
    return ViewModelProvider(this)[StaffSessionViewModel::class.java]
}

fun Fragment.staffSession(): StaffSessionViewModel {
    return requireActivity().staffSession()
}

fun FragmentActivity.staffEditSession(): StaffEditSessionViewModel {
    return ViewModelProvider(this)[StaffEditSessionViewModel::class.java]
}

fun Fragment.staffEditSession(): StaffEditSessionViewModel {
    return requireActivity().staffEditSession()
}

fun FragmentActivity.portalSession(): PortalSessionViewModel {
    return ViewModelProvider(this)[PortalSessionViewModel::class.java]
}

fun Fragment.portalSession(): PortalSessionViewModel {
    return requireActivity().portalSession()
}
