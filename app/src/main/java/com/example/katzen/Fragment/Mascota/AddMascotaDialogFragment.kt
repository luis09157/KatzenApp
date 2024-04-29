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
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseDatabaseManager
import com.example.katzen.DataBaseFirebase.FirebaseMascotaUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Helper.UpperCaseTextWatcher
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.MascotaModel
import com.example.katzen.R
import com.example.katzen.databinding.VistaAgregarMascotaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddMascotaFragment : Fragment() {
    val TAG = "AddMascotaFragment"
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"


    private var _binding: VistaAgregarMascotaBinding? = null
    private val binding get() = _binding!!
    companion object{
        val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = VistaAgregarMascotaBinding.inflate(inflater, container, false)
        val view = binding.root

        initLoading()
        init()
        listeners()
        view.setOnClickListener { it.hideKeyboard() }

        return view
    }

    fun listeners(){
        binding.spSexo.setOnClickListener { it.hideKeyboard() }
        binding.spEspecie.setOnClickListener { it.hideKeyboard() }

        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext() ,PacienteFragment() ,TAG)
        }
        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()
            guardarMascota()
        }
        binding.btnSubirImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }
    fun init(){
        FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY
        val adapterSEXO = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.SEXO)
        binding.spSexo.setAdapter(adapterSEXO)
        val adapterESPECIE = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.ESPECIE)
        binding.spEspecie.setAdapter(adapterESPECIE)

        UpperCaseTextWatcher.UpperText(binding.textColor)
        UpperCaseTextWatcher.UpperText(binding.textPeso)
        UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textEdad)
        UpperCaseTextWatcher.UpperText(binding.spEspecie)
        UpperCaseTextWatcher.UpperText(binding.spRaza)
        UpperCaseTextWatcher.UpperText(binding.spSexo)
    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun guardarMascota(){
        val mascota = MascotaModel()
        mascota.nombre = binding.textNombre.text.toString()
        mascota.peso = binding.textPeso.text.toString()
        mascota.raza = binding.spRaza.text.toString()
        mascota.especie = binding.spEspecie.text.toString()
        mascota.edad = binding.textEdad.text.toString()
        mascota.color = binding.textColor.text.toString()
        mascota.sexo = binding.spSexo.text.toString()
        mascota.fecha = UtilHelper.getDate()

        // Validar la mascota
        val validationResult = MascotaModel.validarMascota(requireContext(), mascota)

        if (validationResult.isValid) {

            GlobalScope.launch(Dispatchers.IO) {
                if (FirebaseStorageManager.hasSelectedImage()){
                    requireActivity().runOnUiThread {  ConfigLoading.showLoadingAnimation() }
                    val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                    println("URL de descarga de la imagen: $imageUrl")
                    mascota.imageUrl = imageUrl

                    GlobalScope.launch(Dispatchers.IO) {
                        val ( flag,message ) =  FirebaseDatabaseManager.insertModel(mascota, mascota.id,PATH_FIREBASE)

                        if (flag) {
                            // La operación de guardado fue exitosa
                            println("La mascota se guardó exitosamente.")
                            limpiarCampos()
                            requireActivity().runOnUiThread {  ConfigLoading.hideLoadingAnimation() }

                            DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "La mascota se guardó exitosamente.")
                        } else {
                            // Hubo un error en la operación de guardado
                            requireActivity().runOnUiThread {  ConfigLoading.hideLoadingAnimation() }
                            println("Hubo un error al guardar la mascota.")
                            //DialogMaterialHelper.mostrarErrorDialog(requireContext(), "Hubo un error al guardar la mascota.")
                        }
                    }
                }else{
                    requireActivity().runOnUiThread {  ConfigLoading.hideLoadingAnimation() }
                    DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Selecciona una iamgen.")
                }
            }
        }else {
            // La mascota no es válida, mostrar mensaje de error
            println(validationResult.message)
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
        }
    }
    fun limpiarCampos() {
        requireActivity().runOnUiThread {
            binding.apply {
                textNombre.text?.clear()
                spSexo.text?.clear()
                spEspecie.text?.clear()
                spRaza.text?.clear()
                textPeso.text?.clear()
                textEdad.text?.clear()
                textColor.text?.clear()
                fotoMascota.setImageResource(R.drawable.ic_imagen)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FirebaseStorageManager.URI_IMG_SELECTED = data.data!!
            // You can now upload this image to Firebase Storage and display it in the ImageView

            binding.fotoMascota.setImageURI(FirebaseStorageManager.URI_IMG_SELECTED)
        }
    }
}
