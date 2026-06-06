package com.example.katzen.Helper

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

object SearchUiHelper {

    fun bindSearch(editText: TextInputEditText?, onQuery: (String) -> Unit) {
        editText ?: return
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onQuery(s?.toString().orEmpty())
            }
        })
    }
}
