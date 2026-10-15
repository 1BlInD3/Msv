package com.example.managementsafetyvisit.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.config.AppConfig
import com.example.managementsafetyvisit.retrofit.RetrofitFunctions
import com.example.managementsafetyvisit.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val cbIsLocalTesting: CheckBox = view.findViewById(R.id.cbIsLocalTesting)
        val cbUseMockData: CheckBox = view.findViewById(R.id.cbUseMockData)
        val cbFetchFromServer: CheckBox = view.findViewById(R.id.cbFetchFromServer)

        val editProdSqlIp: EditText = view.findViewById(R.id.editProdSqlIp)
        val editProdDbName: EditText = view.findViewById(R.id.editProdDbName)
        val editProdReadUser: EditText = view.findViewById(R.id.editProdReadUser)
        val editProdReadPw: EditText = view.findViewById(R.id.editProdReadPw)
        val editProdWriteUser: EditText = view.findViewById(R.id.editProdWriteUser)
        val editProdWritePw: EditText = view.findViewById(R.id.editProdWritePw)
        val editProdApiBaseUrl: EditText = view.findViewById(R.id.editProdApiBaseUrl)
        val editProdPhotoSharePath: EditText = view.findViewById(R.id.editProdPhotoSharePath)

        val editLocalSqlIp: EditText = view.findViewById(R.id.editLocalSqlIp)
        val editLocalSqlPort: EditText = view.findViewById(R.id.editLocalSqlPort)
        val editLocalDbName: EditText = view.findViewById(R.id.editLocalDbName)
        val editLocalUser: EditText = view.findViewById(R.id.editLocalUser)
        val editLocalPassword: EditText = view.findViewById(R.id.editLocalPassword)
        val editLocalApiBaseUrl: EditText = view.findViewById(R.id.editLocalApiBaseUrl)
        val editLocalPhotoSharePath: EditText = view.findViewById(R.id.editLocalPhotoSharePath)

        val btnTestProd: Button = view.findViewById(R.id.btnTestProd)
        val btnTestLocal: Button = view.findViewById(R.id.btnTestLocal)
        val btnSave: Button = view.findViewById(R.id.btnSave)

        // Pre-populate fields from AppConfig
        cbIsLocalTesting.isChecked = AppConfig.IS_LOCAL_TESTING
        cbUseMockData.isChecked = AppConfig.USE_MOCK_DATA
        cbFetchFromServer.isChecked = AppConfig.FETCH_FROM_SERVER

        editProdSqlIp.setText(AppConfig.PROD_SQL_IP)
        editProdDbName.setText(AppConfig.PROD_DB_NAME)
        editProdReadUser.setText(AppConfig.PROD_READ_USER)
        editProdReadPw.setText(AppConfig.PROD_READ_PW)
        editProdWriteUser.setText(AppConfig.PROD_WRITE_USER)
        editProdWritePw.setText(AppConfig.PROD_WRITE_PW)
        editProdApiBaseUrl.setText(AppConfig.PROD_API_BASE_URL)
        editProdPhotoSharePath.setText(AppConfig.PROD_PHOTO_SHARE_PATH)

        editLocalSqlIp.setText(AppConfig.LOCAL_SQL_IP)
        editLocalSqlPort.setText(AppConfig.LOCAL_SQL_PORT.toString())
        editLocalDbName.setText(AppConfig.LOCAL_DB_NAME)
        editLocalUser.setText(AppConfig.LOCAL_USER)
        editLocalPassword.setText(AppConfig.LOCAL_PASSWORD)
        editLocalApiBaseUrl.setText(AppConfig.LOCAL_API_BASE_URL)
        editLocalPhotoSharePath.setText(AppConfig.LOCAL_PHOTO_SHARE_PATH)

        val allEditTexts = listOf(
            editProdSqlIp, editProdDbName, editProdReadUser, editProdReadPw,
            editProdWriteUser, editProdWritePw, editProdApiBaseUrl, editProdPhotoSharePath,
            editLocalSqlIp, editLocalSqlPort, editLocalDbName, editLocalUser,
            editLocalPassword, editLocalApiBaseUrl, editLocalPhotoSharePath
        )
        for (i in allEditTexts.indices) {
            val editText = allEditTexts[i]
            editText.maxLines = 1
            editText.isSingleLine = true
            editText.imeOptions = if (i < allEditTexts.size - 1) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_DONE

            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editText.post {
                        if (editText.text.isNotEmpty()) {
                            editText.setSelection(editText.text.length)
                        }
                    }
                }
            }
            editText.setOnClickListener {
                if (editText.text.isNotEmpty()) {
                    editText.setSelection(editText.text.length)
                }
            }
            editText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    if (i < allEditTexts.size - 1) {
                        allEditTexts[i + 1].requestFocus()
                        true
                    } else {
                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(editText.windowToken, 0)
                        true
                    }
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(editText.windowToken, 0)
                    true
                } else {
                    false
                }
            }
        }

        btnTestProd.setOnClickListener {
            val ip = editProdSqlIp.text.toString().trim()
            val db = editProdDbName.text.toString().trim()
            val readUser = editProdReadUser.text.toString().trim()
            val readPw = editProdReadPw.text.toString().trim()
            val writeUser = editProdWriteUser.text.toString().trim()
            val writePw = editProdWritePw.text.toString().trim()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Class.forName(AppConfig.DRIVER_CLASS)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast("Driver betöltési hiba: ${e.message}", requireContext())
                    }
                    return@launch
                }

                // 1. Test READ Connection
                try {
                    val readUrl = "jdbc:jtds:sqlserver://$ip;databaseName=$db;user=$readUser;password=$readPw;loginTimeout=5"
                    val readConn = DriverManager.getConnection(readUrl)
                    readConn.close()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast("PROD Olvasási (READ) kapcsolat sikertelen: ${e.message}", requireContext())
                    }
                    return@launch
                }

                // 2. Test WRITE Connection
                try {
                    val writeUrl = "jdbc:jtds:sqlserver://$ip;databaseName=$db;user=$writeUser;password=$writePw;loginTimeout=5"
                    val writeConn = DriverManager.getConnection(writeUrl)
                    writeConn.close()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast("PROD Írási (WRITE) kapcsolat sikertelen: ${e.message}", requireContext())
                    }
                    return@launch
                }

                // Both READ and WRITE connections succeeded
                withContext(Dispatchers.Main) {
                    showToast("Termelési (READ és WRITE) kapcsolat sikeres!", requireContext())
                }
            }
        }

        btnTestLocal.setOnClickListener {
            val ip = editLocalSqlIp.text.toString().trim()
            val port = editLocalSqlPort.text.toString().trim().toIntOrNull() ?: 1433
            val db = editLocalDbName.text.toString().trim()
            val user = editLocalUser.text.toString().trim()
            val pw = editLocalPassword.text.toString().trim()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Class.forName(AppConfig.DRIVER_CLASS)
                    val url = "jdbc:jtds:sqlserver://$ip:$port;databaseName=$db;user=$user;password=$pw;loginTimeout=5"
                    val conn = DriverManager.getConnection(url)
                    conn.close()
                    withContext(Dispatchers.Main) {
                        showToast("Helyi kapcsolat sikeres!", requireContext())
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast("Helyi kapcsolat sikertelen: ${e.message}", requireContext())
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val context = requireActivity()
            AppConfig.IS_LOCAL_TESTING = cbIsLocalTesting.isChecked
            AppConfig.USE_MOCK_DATA = cbUseMockData.isChecked
            AppConfig.FETCH_FROM_SERVER = cbFetchFromServer.isChecked

            AppConfig.PROD_SQL_IP = editProdSqlIp.text.toString().trim()
            AppConfig.PROD_DB_NAME = editProdDbName.text.toString().trim()
            AppConfig.PROD_READ_USER = editProdReadUser.text.toString().trim()
            AppConfig.PROD_READ_PW = editProdReadPw.text.toString().trim()
            AppConfig.PROD_WRITE_USER = editProdWriteUser.text.toString().trim()
            AppConfig.PROD_WRITE_PW = editProdWritePw.text.toString().trim()
            AppConfig.PROD_API_BASE_URL = editProdApiBaseUrl.text.toString().trim()
            AppConfig.PROD_PHOTO_SHARE_PATH = editProdPhotoSharePath.text.toString().trim()

            AppConfig.LOCAL_SQL_IP = editLocalSqlIp.text.toString().trim()
            AppConfig.LOCAL_SQL_PORT = editLocalSqlPort.text.toString().trim().toIntOrNull() ?: 1433
            AppConfig.LOCAL_DB_NAME = editLocalDbName.text.toString().trim()
            AppConfig.LOCAL_USER = editLocalUser.text.toString().trim()
            AppConfig.LOCAL_PASSWORD = editLocalPassword.text.toString().trim()
            AppConfig.LOCAL_API_BASE_URL = editLocalApiBaseUrl.text.toString().trim()
            AppConfig.LOCAL_PHOTO_SHARE_PATH = editLocalPhotoSharePath.text.toString().trim()

            AppConfig.save(context)
            showToast("Beállítások elmentve!", requireContext())

            if (AppConfig.FETCH_FROM_SERVER) {
                CoroutineScope(Dispatchers.IO).launch {
                    RetrofitFunctions().fetchDataProperties(context)
                }
            }

            parentFragmentManager.popBackStack()
        }

        return view
    }
}
