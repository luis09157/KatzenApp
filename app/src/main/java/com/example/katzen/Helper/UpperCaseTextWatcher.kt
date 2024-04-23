package com.example.katzen.Helper

import android.text.Editable

import android.text.TextWatcher
import android.widget.EditText
import java.util.Locale


class UpperCaseTextWatcher {

    companion object{
        fun UpperText(editText: EditText) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // No es necesario implementar este método
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // No es necesario implementar este método
                }

                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        editText.removeTextChangedListener(this) // Evitar un bucle infinito
                        val upperCaseText = s.toString().uppercase(Locale.getDefault())
                        editText.setText(upperCaseText)
                        editText.setSelection(upperCaseText.length) // Mantener el cursor al final del texto
                        editText.addTextChangedListener(this)
                    }
                }
            })
        }
    }

}