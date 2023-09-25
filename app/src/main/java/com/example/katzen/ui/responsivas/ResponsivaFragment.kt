package com.example.katzen.ui.responsivas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.PDF.ConvertPDF
import com.example.katzen.PacienteModel
import com.example.katzen.databinding.FragmentResponsivasBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ResponsivaFragment : Fragment() {
    val TAG = "ResponsivaFragment"
    private lateinit var database: DatabaseReference
    private var _binding: FragmentResponsivasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResponsivasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = Firebase.database.reference
        ConvertPDF(requireActivity()).askPermissions()

        _binding!!.btnPrintPdf.setOnClickListener {
            writeNewUser("1","pancho","Felino")
            ConvertPDF(requireActivity()).convertXmlToPdf()
        }


        return root
    }

    fun writeNewUser(id: String, nombre: String, especie: String) {
        val pacienteModel = PacienteModel(nombre, especie)

        database.child("Katzen")
                .child("Campaña")
                .child("campaña_24_09_2023")
                .child("pacientes")
                .child(id).setValue(pacienteModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}