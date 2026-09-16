package com.example.managementsafetyvisit.data

import com.google.gson.annotations.SerializedName

data class AppDbData(
    @SerializedName("prodSqlIp") val prodSqlIp: String? = null,
    @SerializedName("prodDbName") val prodDbName: String? = null,
    @SerializedName("prodReadUser") val prodReadUser: String? = null,
    @SerializedName("prodReadPw") val prodReadPw: String? = null,
    @SerializedName("prodWriteUser") val prodWriteUser: String? = null,
    @SerializedName("prodWritePw") val prodWritePw: String? = null,
    @SerializedName("prodApiUrl") val prodApiUrl: String? = null,
    @SerializedName("prodPhotoPath") val prodPhotoPath: String? = null,
    @SerializedName("prodPhotoShare") val prodPhotoShare: String? = null
) {
    fun getCleanPhotoPath(): String? {
        val raw = prodPhotoShare ?: prodPhotoPath ?: return null
        return raw.replace("\"", "").trim()
    }

    fun getCleanSqlIp(): String? {
        val raw = prodSqlIp ?: return null
        val cleaned = raw.replace("\"", "").trim()
        return if (cleaned.startsWith("http://")) {
            cleaned.removePrefix("http://").substringBefore(":").substringBefore("/")
        } else if (cleaned.startsWith("https://")) {
            cleaned.removePrefix("https://").substringBefore(":").substringBefore("/")
        } else {
            cleaned
        }
    }

    fun getCleanString(value: String?): String? {
        return value?.replace("\"", "")?.trim()
    }
}
