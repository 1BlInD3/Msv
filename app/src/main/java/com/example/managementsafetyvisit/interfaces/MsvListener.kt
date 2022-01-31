package com.example.managementsafetyvisit.interfaces

import android.graphics.Bitmap

interface MsvListener {
    fun sendImageBitmap(image: Bitmap)
    fun setProgressOn()
    fun setProgressOff()
}