package com.example.katzen.ui.responsivas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.PDF.ConvertPDF
import com.example.katzen.databinding.FragmentResponsivasBinding

class ResponsivaFragment : Fragment() {
    val TAG = "ResponsivaFragment"

    private var _binding: FragmentResponsivasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResponsivasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        ConvertPDF(requireActivity()).askPermissions()

        _binding!!.btnPrintPdf.setOnClickListener {
            ConvertPDF(requireActivity()).convertXmlToPdf()
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}