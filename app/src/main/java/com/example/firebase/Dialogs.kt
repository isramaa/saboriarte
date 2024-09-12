package com.example.firebase

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Dialogs {
    fun alertError(message: String?, context: Context){
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("ERROR")
        builder.setMessage(message ?: "Algo ha salido mal")
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}