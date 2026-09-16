package com.example.managementsafetyvisit.retrofit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.managementsafetyvisit.config.AppConfig
import com.example.managementsafetyvisit.utils.showDialog
import com.example.managementsafetyvisit.utils.showToast
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

private const val TAG = "RetrofitFunctions"

class RetrofitFunctions() {

    fun retrofitGet(file: File, path: String, number: String) {
        val response = SendApi().getTest().execute()
        val res: String = response.body()!!.message.trim()
        if (res == "OK") {
            uploadPhoto(file, path, number)
        }
    }

    fun getImageCount(folder: String): String {
        val response = SendApi().getImageNumber(folder).execute()
        return response.body()!!.message.trim()
    }

    fun fetchDataProperties(context: Context) {
        if (!AppConfig.FETCH_FROM_SERVER) {
            return
        }
        if (AppConfig.PROD_PHOTO_SHARE_PATH.isEmpty()) {
            AppConfig.PROD_PHOTO_SHARE_PATH = """\\fs\MSV\foto"""
        }
        try {
            val response = SendApi().getDataProperties().execute()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                data.getCleanSqlIp()?.let { if (it.isNotEmpty()) AppConfig.PROD_SQL_IP = it }
                data.getCleanString(data.prodDbName)?.let { if (it.isNotEmpty()) AppConfig.PROD_DB_NAME = it }
                data.getCleanString(data.prodReadUser)?.let { if (it.isNotEmpty()) AppConfig.PROD_READ_USER = it }
                data.getCleanString(data.prodReadPw)?.let { if (it.isNotEmpty()) AppConfig.PROD_READ_PW = it }
                data.getCleanString(data.prodWriteUser)?.let { if (it.isNotEmpty()) AppConfig.PROD_WRITE_USER = it }
                data.getCleanString(data.prodWritePw)?.let { if (it.isNotEmpty()) AppConfig.PROD_WRITE_PW = it }
                data.getCleanString(data.prodApiUrl)?.let { if (it.isNotEmpty()) AppConfig.PROD_API_BASE_URL = it }
                // NOTE: Photo share path is NOT mapped from API response per user requirement.

                AppConfig.save(context)
                Log.d(TAG, "fetchDataProperties: Successfully updated and saved PROD config from server")
                Handler(Looper.getMainLooper()).post {
                    showToast("Konfiguráció sikeresen letöltve!", context)
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    showDialog("Add a tabletet az adminnak és állítsa be az api elérési útvonalat", context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchDataProperties error: $e")
            Handler(Looper.getMainLooper()).post {
                showDialog("Add a tabletet az adminnak és állítsa be az api elérési útvonalat", context)
            }
        }
    }

    private fun uploadPhoto(file: File, path: String, number: String) {
        val body = UploadRequestBody(file, "file")
        val photoResponse = SendApi().sendPhoto(
            RequestBody.create(MediaType.parse("multipart/form-data"), path),
            MultipartBody.Part.createFormData("file", file.name, body),
            number
        ).execute()
        val xmlRes = photoResponse.body()!!.message.trim()
        if (xmlRes == "success") {
            if (file.exists()) {
                file.delete()
                Log.d("MainActivity", "onResponse: delete successful")
            }
        }
    }
}
