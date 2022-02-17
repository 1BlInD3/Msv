package com.example.managementsafetyvisit.utils

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.managementsafetyvisit.MainActivity
import com.example.managementsafetyvisit.MainActivity.Companion.closingTime
import com.example.managementsafetyvisit.MainActivity.Companion.dataArray
import com.example.managementsafetyvisit.MainActivity.Companion.felelos
import com.example.managementsafetyvisit.MainActivity.Companion.managerArray
import com.example.managementsafetyvisit.MainActivity.Companion.msvFragment
import com.example.managementsafetyvisit.MainActivity.Companion.newPerceptionArray
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.MainActivity.Companion.perceptionFragment
import com.example.managementsafetyvisit.MainActivity.Companion.read_connect
import com.example.managementsafetyvisit.MainActivity.Companion.rtsz
import com.example.managementsafetyvisit.MainActivity.Companion.write_connect
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.data.ManagerNames
import com.example.managementsafetyvisit.data.ObservationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.Date
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
        val connection: Connection
        val connectionWrite: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(read_connect)
            connectionWrite = DriverManager.getConnection(write_connect)
            val statementManager =
                connection.prepareStatement("""SELECT TextDescription FROM [Fusetech].[dbo].[DolgKodok] WHERE MSVStatusz = ? ORDER BY TextDescription""")
            statementManager.setString(1, "2")
            val resultManager = statementManager.executeQuery()
            if (!resultManager.next()) {
                sqlMessage.sendMessage("Nem sikerült a managereket letölteni")
                return false
            } else {
                //managerArray.add(ManagerNames(""))
                managerArray.add("")
                do {
                    val manager = resultManager.getString("TextDescription")
                    managerArray.add(manager)
                    // managerArray.add(ManagerNames(manager))
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
                    val id = resultSet1.getInt("ID")
                    val name = resultSet1.getString("Nev")
                    val tsz = resultSet1.getString("Tsz")
                    val felelos = resultSet1.getString("FelelosSzemely")
                    val ftsz = resultSet1.getString("FelelosTsz")
                    val resztvevo = resultSet1.getString("Resztvevo")
                    val rtsz = resultSet1.getString("ResztvevoTsz")
                    val location = resultSet1.getString("Helyszin")
                    val date = resultSet1.getString("Datum")
                    val status = resultSet1.getInt("Statusz")
                    val entryDate = resultSet1.getString("BelepesDatum")
                    val statement2 = connection.prepareStatement("""SELECT [Munkahely] FROM [Fusetech].[dbo].[MSV_Dolgkodok_HolDolg] where TSz = ?""")
                    statement2.setString(1,tsz)
                    val resultSet2 = statement2.executeQuery()
                    if(!resultSet2.next()){
                        sqlMessage.sendMessage("Hiba a feldolgozás során")
                        return false
                    }else{
                        val munkahely = resultSet2.getString("Munkahely")
                        if(munkahely == "GYAR"){
                            sqlMessage.sendMessage("$name nincs a gyár területén")
                            return false
                        }else if (munkahely == "-TROGGER"){
                            sqlMessage.sendMessage("$name nincs a munkahelyére bejelentkezve. Értesítsd a műszakvezetőjét!")
                            return false
                        }else{
                            val statement3 = connectionWrite.prepareStatement("""UPDATE [Fusetech].[dbo].[MsvData] SET Helyszin = ? WHERE ID = ?""")
                            statement3.setString(1,munkahely)
                            statement3.setInt(2,id)
                            statement3.executeUpdate()
                            dataArray.add(
                                Data(
                                    id,
                                    name,
                                    tsz,
                                    felelos,
                                    ftsz,
                                    resztvevo,
                                    rtsz,
                                    munkahely,
                                    date,
                                    status,
                                    entryDate
                                )
                            )
                        }
                    }
                    MainActivity.rtsz = rtsz.toString().trim()
                    val bundle = Bundle()
                    bundle.putSerializable("EMBER", dataArray)
                    val statement3 =
                        connection.prepareStatement("""SELECT [ID],[Eszrevetel],[Tipus],[Valasz],[Intezkedes],[Azonnali],[Javito],[Datum],[Statusz] FROM [Fusetech].[dbo].[MsvNotes] WHERE IdData = ? AND Statusz > 0 order by ID""")
                    statement3.setInt(1, id)
                    val resultSet3 = statement3.executeQuery()
                    if (!resultSet3.next()) {
                        Log.d(TAG, "getDataByName: Nincsenek észrevételek")
                    } else {
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
                    msvFragment.arguments = bundle
                    return true
                }
            }
        } catch (e: Exception) {
            sqlMessage.sendMessage("Nincs hálózat $e")
        }
        return false
    }

    fun loadPerceptionPanel(msvCode: String, name: String) {
        newPerceptionArray.clear()
        val connection1: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection1 = DriverManager.getConnection(write_connect)
            val statement1 =
                connection1.prepareStatement("""SELECT [ID],[IdData] FROM [Fusetech].[dbo].[MsvNotes] WHERE Statusz = 0 AND IdData = ?""")
            statement1.setInt(1, msvCode.toInt())
            val resultSet1 = statement1.executeQuery()
            if (!resultSet1.next()) {
                val statement =
                    connection1.prepareStatement("""INSERT INTO [Fusetech].[dbo].[MsvNotes] (IdData,Statusz) Values(?,?)""")
                statement.setInt(1, msvCode.toInt())
                statement.setInt(2, 0)
                statement.executeUpdate()
                val statement2 =
                    connection1.prepareStatement("""SELECT [ID],[IdData] FROM [Fusetech].[dbo].[MsvNotes] WHERE Statusz = 0 AND IdData = ?""")
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
        var now = 0
        now = if (urgent) {
            1
        } else {
            0
        }
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(write_connect)
            val statement =
                connection.prepareStatement("""UPDATE [Fusetech].[dbo].[MsvNotes] SET Eszrevetel = ?, Tipus = ?, Valasz = ?, Intezkedes = ?, Azonnali = ?, Javito = ?, Datum = ?, Statusz = ? WHERE ID = ? AND Statusz = 0""")
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
        var now = 0
        now = if (urgent) {
            1
        } else {
            0
        }
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(write_connect)
            val statement =
                connection.prepareStatement("""UPDATE [Fusetech].[dbo].[MsvNotes] SET Eszrevetel = ?, Tipus = ?, Valasz = ?, Intezkedes = ?, Azonnali = ?, Javito = ?, Datum = ?, Statusz = ? WHERE ID = ?""")
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
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(write_connect)
            val statement =
                connection.prepareStatement("""DELETE FROM [Fusetech].[dbo].[MsvNotes] WHERE ID = ?""")
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
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(write_connect)
            val statement2 =
                connection.prepareStatement("SELECT Key1 FROM [Fusetech].[dbo].[DolgKodok] WHERE TSz = ?")
            statement2.setString(1, rtsz)
            val resultSet2 = statement2.executeQuery()
            if (!resultSet2.next()) {
                sqlMessage.sendMessage("Hibás kód $rtsz")
            } else {
                val code2 = resultSet2.getString("Key1")
                if (code == code2) {
                    val date = SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                    val statement =
                        connection.prepareStatement("""UPDATE [Fusetech].[dbo].[MsvData] Set Statusz = ?, LatogatasIdeje = ? where ID = ?""")
                    statement.setInt(1, status)
                    statement.setString(2,date)
                    statement.setInt(3, id)
                    statement.executeUpdate()
                    observationArray.clear()
                    // sqlMessage.sendMessage("Az $id számú Msv lezárásra került")
                    sqlMessage.noEntry()

                } else {
                    closingTime = false
                    sqlMessage.sendMessage("Nem a résztvevő húzta le a kódját!"/*"Van kód de valami nem jó $code és $code2"*/)
                }
            }
        } catch (e: Exception) {
            sqlMessage.sendMessage("Nem sikerült a frissítés! \n$e")
        }
    }
    fun checkMsvObservationNumber(id: Int): Boolean{
        val connection: Connection = DriverManager.getConnection(write_connect)
        val statement1 =
            connection.prepareStatement("SELECT * FROM [Fusetech].[dbo].[MsvNotes] WHERE IdData = ?")
        statement1.setInt(1, id)
        val resultSet1 = statement1.executeQuery()
        return resultSet1.next()
    }
    fun checkRabotnik(code: String): Boolean{
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(read_connect)
            val statement = connection.prepareStatement("""SELECT [TSz] FROM [Fusetech].[dbo].[DolgKodok] where Key1 = ?""")
            statement.setString(1,code)
            var tszkod = ""
            var tszMsv = ""
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                CoroutineScope(Main).launch {
                    sqlMessage.sendMessage("Nem jó a kód")
                }
                return false
            }else{
                tszkod = resultSet.getString("TSz")
                tszMsv = resultSet.getString("Tsz")
                if(tszkod == tszMsv){
                    return true
                }
            }
            return false
        }catch (e: Exception){
            CoroutineScope(Main).launch {
                sqlMessage.sendMessage("Hiba az aláírás során $e")
            }
            return false
        }
    }

}