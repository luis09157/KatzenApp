package com.example.katzen.ui.example

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.katzen.Helper.NotificacionesHelper
import com.example.katzen.Helper.NotificationView
import com.example.katzen.R
import com.example.katzen.databinding.ExampleFragmentBinding


class ExampleFragment : Fragment() {
    private var _binding: ExampleFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ExampleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnNoti.setOnClickListener {
            NotificacionesHelper(requireActivity()).newNotification("hola")
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}