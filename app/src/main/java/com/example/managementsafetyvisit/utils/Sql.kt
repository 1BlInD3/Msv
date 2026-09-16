package com.example.managementsafetyvisit.utils

import android.os.Bundle
import android.util.Log
import com.example.managementsafetyvisit.MainActivity
import com.example.managementsafetyvisit.MainActivity.Companion.closingTime
import com.example.managementsafetyvisit.MainActivity.Companion.dataArray
import com.example.managementsafetyvisit.MainActivity.Companion.felelos
import com.example.managementsafetyvisit.MainActivity.Companion.managerArray
import com.example.managementsafetyvisit.MainActivity.Companion.msvFragment
import com.example.managementsafetyvisit.MainActivity.Companion.newPerceptionArray
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.MainActivity.Companion.perceptionFragment
import com.example.managementsafetyvisit.MainActivity.Companion.rtsz
import com.example.managementsafetyvisit.MainActivity.Companion.signed
import com.example.managementsafetyvisit.MainActivity.Companion.signing
import com.example.managementsafetyvisit.config.AppConfig
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.data.ObservationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.text.SimpleDateFormat

class Sql(private val sqlMessage: SqlMessage) {

    interface SqlMessage {
        fun sendMessage(message: String)
        fun noEntry()
    }

    private var updateId = 0
    private var update = false
    private val TAG = "Sql"


    fun getDataByName(code: String): Boolean {
        observationArray.clear()
        dataArray.clear()
        if (AppConfig.USE_MOCK_DATA) {
            managerArray.clear()
            managerArray.add("")
            managerArray.add("Kovács Péter")
            managerArray.add("Szabó Gábor")
            managerArray.add("Tóth István")

            dataArray.add(
                Data(
                    101,
                    "Nagy Anna",
                    if (code.isNotEmpty()) code else "E200",
                    "Kovács Péter",
                    "M100",
                    "Szabó Gábor",
                    "E201",
                    "Szerelde_1",
                    "2026-09-05",
                    2,
                    "2026-09-05 08:00"
                )
            )
            dataArray.add(
                Data(
                    102,
                    "Kovács Béla",
                    "E202",
                    "Kovács Péter",
                    "M100",
                    "Molnár István",
                    "E203",
                    "Forgácsoló_2",
                    "2026-09-05",
                    1,
                    "2026-09-05 08:15"
                )
            )
            dataArray.add(
                Data(
                    103,
                    "Tóth Mária",
                    "E204",
                    "Kovács Péter",
                    "M100",
                    "Varga Zoltán",
                    "E205",
                    "Raktár",
                    "2026-09-05",
                    1,
                    "2026-09-05 08:30"
                )
            )

            return true
        }

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(AppConfig.READ_CONNECT)
            //val connectionWrite = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
            val statementManager =
                connection.prepareStatement("""SELECT TextDescription FROM [Fusetech].[dbo].[DolgKodok] WHERE MSVStatusz = ? ORDER BY TextDescription""")
            statementManager.setString(1, "2")
            val resultManager = statementManager.executeQuery()
            if (!resultManager.next()) {
                sqlMessage.sendMessage("Nem sikerült a managereket letölteni")
                return false
            } else {
                managerArray.clear()
                managerArray.add("")
                do {
                    val manager = resultManager.getString("TextDescription")
                    managerArray.add(manager)
                } while (resultManager.next())
            }
            val statement =
                connection.prepareStatement("""SELECT TextDescription, TSz FROM [Fusetech].[dbo].[DolgKodok] where Key1 = ?""")
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                felelos = ""
                sqlMessage.sendMessage("Biztos jó kódot vittél fel?")
                return false
            } else {
                felelos = resultSet.getString("TSz").trim()
                val felelosNev = resultSet.getString("TextDescription").trim()
                val statement1 =
                    connection.prepareStatement("""SELECT [ID],[Nev],[Tsz],[FelelosSzemely],[FelelosTsz],[Resztvevo],[ResztvevoTsz],[Helyszin],[Datum],[Statusz],[BelepesDatum] FROM [Fusetech].[dbo].[MsvData] where FelelosTsz =? AND Statusz = 1""")
                statement1.setString(1, felelos)
                val resultSet1 = statement1.executeQuery()
                if (!resultSet1.next()) {
                    managerArray.clear()
                    sqlMessage.sendMessage("$felelosNev nevén nincs aktív MSV!")
                    return false
                } else {
                    do {
                        val id = resultSet1.getInt("ID")
                        val name = resultSet1.getString("Nev")
                        val tsz = resultSet1.getString("Tsz")
                        val felelosSzemely = resultSet1.getString("FelelosSzemely")
                        val ftsz = resultSet1.getString("FelelosTsz")
                        val resztvevo = resultSet1.getString("Resztvevo")
                        val rtsz = resultSet1.getString("ResztvevoTsz")
                        val location = resultSet1.getString("Helyszin")
                        val date = resultSet1.getString("Datum")
                        val status = resultSet1.getInt("Statusz")
                        val entryDate = resultSet1.getString("BelepesDatum")
                        dataArray.add(
                            Data(
                                id,
                                name,
                                tsz,
                                felelosSzemely,
                                ftsz,
                                resztvevo,
                                rtsz,
                                location,
                                date,
                                status,
                                entryDate
                            )
                        )
                    } while (resultSet1.next())
                    return true
                }
            }
        } catch (e: Exception) {
            sqlMessage.sendMessage("Nincs hálózat $e")
        }
        return false
    }

    fun loadVisitForSelectedPerson(selectedData: Data): Boolean {
        signed = false
        signing = false
        closingTime = false
        observationArray.clear()
        val selectedArray = ArrayList<Data>()
        selectedArray.add(selectedData)
        MainActivity.msvNumber = selectedData.id.toString()

        if (AppConfig.USE_MOCK_DATA) {
            MainActivity.rtsz = selectedData.rtsz.trim()
            when (selectedData.id) {
                101 -> {
                    // Nagy Anna
                    observationArray.add(
                        ObservationData(
                            "Védőszemüveg használata rendben",
                            "PP",
                            "Példás munkavégzés",
                            "Nincs szükség intézkedésre",
                            false,
                            selectedData.fsz,
                            "2026-09-05",
                            "1"
                        )
                    )
                    observationArray.add(
                        ObservationData(
                            "Olajfolyás a gép alatt",
                            "UC",
                            "Azonnal felitatva homokkal",
                            "Karbantartás értesítve",
                            true,
                            "Tóth István",
                            "2026-09-05",
                            "2"
                        )
                    )
                }
                102 -> {
                    // Kovács Béla
                    observationArray.add(
                        ObservationData(
                            "Hallásvédő dugó használata megfelelő",
                            "PP",
                            "Megfelelő munkavédelem",
                            "Dicséretben részesült",
                            false,
                            selectedData.fsz,
                            "2026-09-05",
                            "3"
                        )
                    )
                    observationArray.add(
                        ObservationData(
                            "Rendezetlen szerszámasztal",
                            "UA",
                            "Elpakolás a műszak végén",
                            "Szerszámtartó állvány kihelyezése",
                            false,
                            "Molnár István",
                            "2026-09-06",
                            "4"
                        )
                    )
                    observationArray.add(
                        ObservationData(
                            "Hiányzó védőburkolat a csiszológépen",
                            "UC",
                            "Gép leállítva",
                            "Védőburkolat pótlása és ellenőrzése",
                            true,
                            "Szabó Gábor",
                            "2026-09-05",
                            "5"
                        )
                    )
                }
                103 -> {
                    // Tóth Mária
                    observationArray.add(
                        ObservationData(
                            "Tiszta, rendezett raktári folyosó",
                            "PP",
                            "Kiváló folyosórend",
                            "Folyamatos szinten tartás",
                            false,
                            selectedData.fsz,
                            "2026-09-05",
                            "6"
                        )
                    )
                    observationArray.add(
                        ObservationData(
                            "Forgalmi út eltorlaszolva raklappal",
                            "UA",
                            "Kocsi azonnal áthelyezve",
                            "Sárga vonalazás megújítása a raktárban",
                            true,
                            "Varga Zoltán",
                            "2026-09-05",
                            "7"
                        )
                    )
                }
                else -> {
                    observationArray.add(
                        ObservationData(
                            "Munkavédelmi előírások betartása rendben",
                            "PP",
                            "Rendben",
                            "Nincs szükség intézkedésre",
                            false,
                            selectedData.fsz,
                            "2026-09-05",
                            "8"
                        )
                    )
                }
            }
            val bundle = Bundle()
            bundle.putSerializable("EMBER", selectedArray)
            msvFragment.arguments = bundle
            return true
        }

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(AppConfig.READ_CONNECT)
            MainActivity.rtsz = selectedData.rtsz.trim()
            val statement3 =
                connection.prepareStatement("""SELECT ID, Eszrevetel, Tipus, Valasz, Intezkedes, Azonnali, Javito, Datum, Statusz FROM MsvNotes WHERE IdData = ? AND Statusz > 0 order by ID""")
            statement3.setInt(1, selectedData.id)
            val resultSet3 = statement3.executeQuery()
            if (resultSet3.next()) {
                do {
                    val idM = resultSet3.getInt("ID")
                    val eszrevetel = resultSet3.getString("Eszrevetel")
                    val tipus = resultSet3.getString("Tipus")
                    val valasz = resultSet3.getString("Valasz")
                    val intezkedes = resultSet3.getString("Intezkedes")
                    val azonnali = resultSet3.getInt("Azonnali")
                    val urgent: Boolean = azonnali != 0
                    val javito = resultSet3.getString("Javito")
                    val datum = resultSet3.getString("Datum")
                    observationArray.add(
                        ObservationData(
                            eszrevetel,
                            tipus,
                            valasz,
                            intezkedes,
                            urgent,
                            javito,
                            datum,
                            idM.toString().trim()
                        )
                    )
                } while (resultSet3.next())
            }
            val bundle = Bundle()
            bundle.putSerializable("EMBER", selectedArray)
            msvFragment.arguments = bundle
            return true
        } catch (e: Exception) {
            sqlMessage.sendMessage("Nincs hálózat $e")
        }
        return false
    }

    fun loadPerceptionPanel(msvCode: String, name: String) {
        newPerceptionArray.clear()
        if (AppConfig.USE_MOCK_DATA) {
            val mockId = (System.currentTimeMillis() % 10000).toString()
            newPerceptionArray.add(
                ObservationData(
                    "",
                    "PP",
                    "",
                    "",
                    false,
                    "",
                    "",
                    mockId
                )
            )
            val bundle = Bundle()
            bundle.putString("MYSTRING", name)
            bundle.putSerializable("EMPTYARRAY", newPerceptionArray)
            perceptionFragment.arguments = bundle
            return
        }

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection1 = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
            val statement1 =
                connection1.prepareStatement("""SELECT ID, IdData FROM MsvNotes WHERE Statusz = 0 AND IdData = ?""")
            statement1.setInt(1, msvCode.toInt())
            val resultSet1 = statement1.executeQuery()
            if (!resultSet1.next()) {
                val statement =
                    connection1.prepareStatement("""INSERT INTO MsvNotes (IdData,Statusz) Values(?,?)""")
                statement.setInt(1, msvCode.toInt())
                statement.setInt(2, 0)
                statement.executeUpdate()
                val statement2 =
                    connection1.prepareStatement("""SELECT ID, IdData FROM MsvNotes WHERE Statusz = 0 AND IdData = ?""")
                statement2.setInt(1, msvCode.toInt())
                val resultSet2 = statement2.executeQuery()
                if (!resultSet2.next()) {
                    Log.d(TAG, "loadPerceptionPanel: Kurva nagy baj van")
                } else {
                    val id = resultSet2.getInt("ID")
                    newPerceptionArray.add(
                        ObservationData(
                            "",
                            "PP",
                            "",
                            "",
                            false,
                            "",
                            "",
                            id.toString().trim()
                        )
                    )
                    val bundle = Bundle()
                    bundle.putString("MYSTRING", name)
                    bundle.putSerializable("EMPTYARRAY", newPerceptionArray)
                    perceptionFragment.arguments = bundle
                }
            } else {
                val id = resultSet1.getInt("ID")
                newPerceptionArray.add(
                    ObservationData(
                        "",
                        "PP",
                        "",
                        "",
                        false,
                        "",
                        "",
                        id.toString().trim()
                    )
                )
                val bundle = Bundle()
                bundle.putString("MYSTRING", name)
                bundle.putSerializable("EMPTYARRAY", newPerceptionArray)
                perceptionFragment.arguments = bundle
            }
        } catch (e: Exception) {
            Log.d(TAG, "loadPerceptionPanel: $e")
        }
    }

    fun saveNewPerception(
        perception: String?,
        answer: String?,
        measure: String?,
        type: String?,
        urgent: Boolean,
        corrector: String?,
        date: String?,
        id: Int,
        statusz: Int
    ) {
        if (AppConfig.USE_MOCK_DATA) {
            observationArray.add(
                ObservationData(
                    perception,
                    type,
                    answer,
                    measure,
                    urgent,
                    corrector,
                    date,
                    id.toString()
                )
            )
            return
        }
        var now = 0
        now = if (urgent) {
            1
        } else {
            0
        }
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
            val statement =
                connection.prepareStatement("""UPDATE MsvNotes SET Eszrevetel = ?, Tipus = ?, Valasz = ?, Intezkedes = ?, Azonnali = ?, Javito = ?, Datum = ?, Statusz = ? WHERE ID = ? AND Statusz = 0""")
            statement.setString(1, perception)
            statement.setString(2, type)
            statement.setString(3, answer)
            statement.setString(4, measure)
            statement.setInt(5, now)
            statement.setString(6, corrector)
            statement.setString(7, date)
            statement.setInt(8, statusz)
            statement.setInt(9, id)
            statement.executeUpdate()
            observationArray.add(
                ObservationData(
                    perception,
                    type,
                    answer,
                    measure,
                    urgent,
                    corrector,
                    date,
                    id.toString()
                )
            )
        } catch (e: Exception) {
            Log.d(TAG, "saveNewPerception: $e")
        }
    }

    fun updateExisting(
        perception: String?,
        answer: String?,
        measure: String?,
        type: String?,
        urgent: Boolean,
        corrector: String?,
        date: String?,
        id: Int,
        statusz: Int
    ) {
        if (AppConfig.USE_MOCK_DATA) {
            getPositionByValue(id)
            if (update) {
                observationArray[updateId].perception = perception
                observationArray[updateId].type = type
                observationArray[updateId].response = answer
                observationArray[updateId].measure = measure
                observationArray[updateId].now = urgent
                observationArray[updateId].corrector = corrector
                observationArray[updateId].date = date
            }
            return
        }
        var now = 0
        now = if (urgent) {
            1
        } else {
            0
        }
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
            val statement =
                connection.prepareStatement("""UPDATE MsvNotes SET Eszrevetel = ?, Tipus = ?, Valasz = ?, Intezkedes = ?, Azonnali = ?, Javito = ?, Datum = ?, Statusz = ? WHERE ID = ?""")
            statement.setString(1, perception)
            statement.setString(2, type)
            statement.setString(3, answer)
            statement.setString(4, measure)
            statement.setInt(5, now)
            statement.setString(6, corrector)
            statement.setString(7, date)
            statement.setInt(8, statusz)
            statement.setInt(9, id)
            statement.executeUpdate()
            getPositionByValue(id)
            if (update) {
                observationArray[updateId].perception = perception
                observationArray[updateId].type = type
                observationArray[updateId].response = answer
                observationArray[updateId].measure = measure
                observationArray[updateId].now = urgent
                observationArray[updateId].corrector = corrector
                observationArray[updateId].date = date
            }
        } catch (e: Exception) {
            Log.d(TAG, "saveNewPerception: $e")
        }
    }

    fun deleteExisting(id: Int) {
        if (AppConfig.USE_MOCK_DATA) {
            for (i in observationArray.indices.reversed()) {
                if (observationArray[i].id == id.toString()) {
                    observationArray.removeAt(i)
                }
            }
            return
        }
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
            val statement =
                connection.prepareStatement("""DELETE FROM MsvNotes WHERE ID = ?""")
            statement.setInt(1, id)
            statement.executeUpdate()
        } catch (e: Exception) {
            Log.d(TAG, "deleteExisting: $e")
        }
    }

    private fun getPositionByValue(id: Int) {
        for (i in 0 until observationArray.size) {
            if (observationArray[i].id == id.toString()) {
                update = true
                updateId = i
            }
        }
    }

    fun closeCommissarMsv(status: Int, id: Int, code: String) {
        if (AppConfig.USE_MOCK_DATA) {
            observationArray.clear()
            sqlMessage.noEntry()
            return
        }

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
            val statement2 =
                connection.prepareStatement("SELECT Key1 FROM DolgKodok WHERE TSz = ?")
            statement2.setString(1, rtsz)
            val resultSet2 = statement2.executeQuery()
            if (!resultSet2.next()) {
                sqlMessage.sendMessage("Hibás kód $rtsz")
            } else {
                val code2 = resultSet2.getString("Key1")
                if (code == code2) {
                    val date = SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                    val statement =
                        connection.prepareStatement("""UPDATE MsvData Set Statusz = ?, LatogatasIdeje = ? where ID = ?""")
                    statement.setInt(1, status)
                    statement.setString(2, date)
                    statement.setInt(3, id)
                    statement.executeUpdate()
                    observationArray.clear()
                    sqlMessage.noEntry()
                } else {
                    closingTime = false
                    sqlMessage.sendMessage("Nem a résztvevő húzta le a kódját!")
                }
            }
        } catch (e: Exception) {
            sqlMessage.sendMessage("Nem sikerült a frissítés! \n$e")
        }
    }

    fun checkMsvObservationNumber(id: Int): Boolean {
        if (AppConfig.USE_MOCK_DATA) {
            return observationArray.isNotEmpty()
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        val connection: Connection = DriverManager.getConnection(AppConfig.WRITE_CONNECT)
        val statement1 =
            connection.prepareStatement("SELECT * FROM MsvNotes WHERE IdData = ?")
        statement1.setInt(1, id)
        val resultSet1 = statement1.executeQuery()
        return resultSet1.next()
    }

    fun checkRabotnik(code: String): Boolean {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) return false

        val currentMsvId = MainActivity.msvNumber.toIntOrNull() ?: 0

        if (AppConfig.USE_MOCK_DATA) {
            return true
        }

        try {
            Class.forName(AppConfig.DRIVER_CLASS)
            val connection = DriverManager.getConnection(AppConfig.READ_CONNECT)

            // Step 1: Query DolgKodok to find TSz for scanned Key1 barcode
            val statementDolg = connection.prepareStatement("SELECT TSz FROM DolgKodok WHERE Key1 = ?")
            statementDolg.setString(1, trimmedCode)
            val resultSetDolg = statementDolg.executeQuery()
            val scannedTsz = if (resultSetDolg.next()) {
                resultSetDolg.getString("TSz").trim()
            } else {
                trimmedCode
            }

            // Step 2: Query MsvData where ID = currentMsvId and Statusz = 1
            val statementMsv = connection.prepareStatement("SELECT Tsz FROM MsvData WHERE ID = ? AND Statusz = 1")
            statementMsv.setInt(1, currentMsvId)
            val resultSetMsv = statementMsv.executeQuery()

            if (resultSetMsv.next()) {
                val expectedTsz = resultSetMsv.getString("Tsz")?.trim()
                if (scannedTsz.equals(expectedTsz, ignoreCase = true)) {
                    return true
                } else {
                    CoroutineScope(Main).launch {
                        sqlMessage.sendMessage("Nem a meglátogatott személy kártyája!")
                    }
                    return false
                }
            } else {
                CoroutineScope(Main).launch {
                    sqlMessage.sendMessage("Nem található aktív MSV (ID: $currentMsvId)!")
                }
                return false
            }
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                sqlMessage.sendMessage("Hiba az aláírás során $e")
            }
            return false
        }
    }

    fun checkAdminCode(code: String): Boolean {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) return false

        if (AppConfig.USE_MOCK_DATA) {

            return true;
        }

        try {
            return "5999076269532".equals(trimmedCode)
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                sqlMessage.sendMessage("Hiba az ellenőrzés során: $e")
            }
            return false
        }
    }
}
