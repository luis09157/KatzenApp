package com.example.katzen.Fragment.Mascota

import KatzenDataBase
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Model.MascotaModel
import com.example.katzen.databinding.VistaAgregarMascotaBinding

class AddMascotaFragment : Fragment() {
    val TAG = "AddMascotaFragment"

    private lateinit var loadingHelper: LoadingHelper
    private var _binding: VistaAgregarMascotaBinding? = null
    private val binding get() = _binding!!
    companion object{
        val PICK_IMAGE_REQUEST = 1
        var URI_IMG_SELECTED : Uri = Uri.EMPTY
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = VistaAgregarMascotaBinding.inflate(inflater, container, false)
        val view = binding.root

        val txt_nombre = binding.textNombre
        val sp_especie = binding.spEspecie
        val sp_raza = binding.spRaza
        val sp_sexo = binding.spSexo
        val txt_peso = binding.textPeso
        val txt_edad = binding.textEdad
        val btn_cancelar = binding.btnCancelar
        val btn_guardar = binding.btnGuardar

        sp_raza.setOnClickListener {
            //view.hideKeyboard()
        }
        sp_especie.setOnClickListener {
           // view.hideKeyboard()
        }
        sp_sexo.setOnClickListener {
           // view.hideKeyboard()
        }

        btn_cancelar.setOnClickListener {
            //view.hideKeyboard()
            // Depending on your logic, you might want to navigate back to the previous fragment here
        }

        btn_guardar.setOnClickListener {
            //view.hideKeyboard()

            val mM = MascotaModel(
                nombre = txt_nombre.text.toString(),
                especie = sp_especie.text.toString(),
                raza = sp_raza.text.toString(),
                sexo = sp_sexo.text.toString(),
                peso = txt_peso.text.toString(),
                edad = txt_edad.text.toString()
            )

            // Depending on your logic, you might want to pass the mM object to another function for further processing
            dialogConfirm(mM)
        }

        binding.btnSubirImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val adapterSEXO = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.SEXO)
        sp_sexo.setAdapter(adapterSEXO)
        val adapterESPECIE = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.ESPECIE)
        sp_especie.setAdapter(adapterESPECIE)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dialogConfirm(mascota: MascotaModel) {
        try {
            KatzenDataBase(requireActivity()).agregarMascota(mascota, URI_IMG_SELECTED)
        } catch (e: Exception) {
            // Handle the exception here, you can log it or show a Toast message
            Log.e(TAG, "Error adding Mascota to database: ${e.message}")
            Toast.makeText(requireContext(), "Error adding Mascota to database", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            URI_IMG_SELECTED = data.data!!
            // You can now upload this image to Firebase Storage and display it in the ImageView

            binding.fotoMascota.setImageURI(URI_IMG_SELECTED)
        }
    }
}
