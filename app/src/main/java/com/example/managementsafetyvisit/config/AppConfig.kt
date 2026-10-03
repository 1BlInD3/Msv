package com.example.managementsafetyvisit.config

import android.content.Context

object AppConfig {
    private const val PREF_NAME = "msv_app_config"

    /**
     * Set to true to use mock data for local testing without an active database connection.
     */
    var USE_MOCK_DATA: Boolean = false

    /**
     * Set to true for local testing environment (e.g. local Docker MS SQL Server), false for production.
     */
    var IS_LOCAL_TESTING: Boolean = false

    /**
     * Set to true to fetch configuration properties from server endpoint on startup and save.
     */
    var FETCH_FROM_SERVER: Boolean = true

    // --- Driver ---
    const val DRIVER_CLASS: String = "net.sourceforge.jtds.jdbc.Driver"

    // --- Local Environment Settings (Docker MS SQL Server) ---
    var LOCAL_SQL_IP: String = "192.168.1.9"
    var LOCAL_SQL_PORT: Int = 1433
    var LOCAL_DB_NAME: String = "fusetech"
    var LOCAL_USER: String = "fuse"
    var LOCAL_PASSWORD: String = ""
    var LOCAL_API_BASE_URL: String = "http://192.168.1.9:8020/"
    var LOCAL_PHOTO_SHARE_PATH: String = """\\local-server\MSV\foto"""

    // --- Production Environment Settings ---
    var PROD_SQL_IP: String = ""
    var PROD_DB_NAME: String = ""
    var PROD_READ_USER: String = ""
    var PROD_READ_PW: String = ""
    var PROD_WRITE_USER: String = ""
    var PROD_WRITE_PW: String = ""
    var PROD_API_BASE_URL: String = "http://10.0.1.69:8020/"
    var PROD_PHOTO_SHARE_PATH: String = """\\fs\MSV\foto"""

    // --- Dynamic Properties ---
    val API_BASE_URL: String
        get() = if (IS_LOCAL_TESTING) LOCAL_API_BASE_URL else PROD_API_BASE_URL

    val PHOTO_SHARE_PATH: String
        get() = if (IS_LOCAL_TESTING) LOCAL_PHOTO_SHARE_PATH else PROD_PHOTO_SHARE_PATH

    val READ_CONNECT: String
        get() = if (IS_LOCAL_TESTING) {
            "jdbc:jtds:sqlserver://$LOCAL_SQL_IP:$LOCAL_SQL_PORT;databaseName=$LOCAL_DB_NAME;user=$LOCAL_USER;password=$LOCAL_PASSWORD;loginTimeout=10"
        } else {
            "jdbc:jtds:sqlserver://$PROD_SQL_IP;databaseName=$PROD_DB_NAME;user=$PROD_READ_USER;password=$PROD_READ_PW;loginTimeout=10"
        }

    val WRITE_CONNECT: String
        get() = if (IS_LOCAL_TESTING) {
            "jdbc:jtds:sqlserver://$LOCAL_SQL_IP:$LOCAL_SQL_PORT;databaseName=$LOCAL_DB_NAME;user=$LOCAL_USER;password=$LOCAL_PASSWORD;loginTimeout=10"
        } else {
            "jdbc:jtds:sqlserver://$PROD_SQL_IP;databaseName=$PROD_DB_NAME;user=$PROD_WRITE_USER;password=$PROD_WRITE_PW;loginTimeout=10"
        }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        USE_MOCK_DATA = prefs.getBoolean("USE_MOCK_DATA", USE_MOCK_DATA)
        IS_LOCAL_TESTING = prefs.getBoolean("IS_LOCAL_TESTING", IS_LOCAL_TESTING)
        FETCH_FROM_SERVER = prefs.getBoolean("FETCH_FROM_SERVER", FETCH_FROM_SERVER)

        LOCAL_SQL_IP = prefs.getString("LOCAL_SQL_IP", LOCAL_SQL_IP)?.takeIf { it.isNotEmpty() } ?: LOCAL_SQL_IP
        LOCAL_SQL_PORT = prefs.getInt("LOCAL_SQL_PORT", LOCAL_SQL_PORT)
        LOCAL_DB_NAME = prefs.getString("LOCAL_DB_NAME", LOCAL_DB_NAME)?.takeIf { it.isNotEmpty() } ?: LOCAL_DB_NAME
        LOCAL_USER = prefs.getString("LOCAL_USER", LOCAL_USER)?.takeIf { it.isNotEmpty() } ?: LOCAL_USER
        LOCAL_PASSWORD = prefs.getString("LOCAL_PASSWORD", LOCAL_PASSWORD)?.takeIf { it.isNotEmpty() } ?: LOCAL_PASSWORD
        LOCAL_API_BASE_URL = prefs.getString("LOCAL_API_BASE_URL", LOCAL_API_BASE_URL)?.takeIf { it.isNotEmpty() } ?: LOCAL_API_BASE_URL
        LOCAL_PHOTO_SHARE_PATH = prefs.getString("LOCAL_PHOTO_SHARE_PATH", LOCAL_PHOTO_SHARE_PATH)?.takeIf { it.isNotEmpty() } ?: LOCAL_PHOTO_SHARE_PATH

        PROD_SQL_IP = prefs.getString("PROD_SQL_IP", PROD_SQL_IP)?.takeIf { it.isNotEmpty() } ?: PROD_SQL_IP
        PROD_DB_NAME = prefs.getString("PROD_DB_NAME", PROD_DB_NAME)?.takeIf { it.isNotEmpty() } ?: PROD_DB_NAME
        PROD_READ_USER = prefs.getString("PROD_READ_USER", PROD_READ_USER)?.takeIf { it.isNotEmpty() } ?: PROD_READ_USER
        PROD_READ_PW = prefs.getString("PROD_READ_PW", PROD_READ_PW)?.takeIf { it.isNotEmpty() } ?: PROD_READ_PW
        PROD_WRITE_USER = prefs.getString("PROD_WRITE_USER", PROD_WRITE_USER)?.takeIf { it.isNotEmpty() } ?: PROD_WRITE_USER
        PROD_WRITE_PW = prefs.getString("PROD_WRITE_PW", PROD_WRITE_PW)?.takeIf { it.isNotEmpty() } ?: PROD_WRITE_PW
        PROD_API_BASE_URL = prefs.getString("PROD_API_BASE_URL", PROD_API_BASE_URL)?.takeIf { it.isNotEmpty() } ?: PROD_API_BASE_URL
        PROD_PHOTO_SHARE_PATH = prefs.getString("PROD_PHOTO_SHARE_PATH", PROD_PHOTO_SHARE_PATH)?.takeIf { it.isNotEmpty() } ?: PROD_PHOTO_SHARE_PATH
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("USE_MOCK_DATA", USE_MOCK_DATA)
            .putBoolean("IS_LOCAL_TESTING", IS_LOCAL_TESTING)
            .putBoolean("FETCH_FROM_SERVER", FETCH_FROM_SERVER)

            .putString("LOCAL_SQL_IP", LOCAL_SQL_IP)
            .putInt("LOCAL_SQL_PORT", LOCAL_SQL_PORT)
            .putString("LOCAL_DB_NAME", LOCAL_DB_NAME)
            .putString("LOCAL_USER", LOCAL_USER)
            .putString("LOCAL_PASSWORD", LOCAL_PASSWORD)
            .putString("LOCAL_API_BASE_URL", LOCAL_API_BASE_URL)
            .putString("LOCAL_PHOTO_SHARE_PATH", LOCAL_PHOTO_SHARE_PATH)

            .putString("PROD_SQL_IP", PROD_SQL_IP)
            .putString("PROD_DB_NAME", PROD_DB_NAME)
            .putString("PROD_READ_USER", PROD_READ_USER)
            .putString("PROD_READ_PW", PROD_READ_PW)
            .putString("PROD_WRITE_USER", PROD_WRITE_USER)
            .putString("PROD_WRITE_PW", PROD_WRITE_PW)
            .putString("PROD_API_BASE_URL", PROD_API_BASE_URL)
            .putString("PROD_PHOTO_SHARE_PATH", PROD_PHOTO_SHARE_PATH)
            .apply()
    }
}
