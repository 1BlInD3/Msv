package com.example.managementsafetyvisit.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast

fun showDialog(message: String, context: Context) {
    val dialog = AlertDialog.Builder(context)
    dialog.setTitle("Figyelem")
    dialog.setMessage(message)
    dialog.setPositiveButton("OK") { _, _ -> }
    dialog.create()
    dialog.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
}
fun showToast(message: String, context: Context){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}