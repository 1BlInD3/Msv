package com.example.managementsafetyvisit.utils

import android.os.Bundle
import android.util.Log
import com.example.managementsafetyvisit.MainActivity.Companion.dataArray
import com.example.managementsafetyvisit.MainActivity.Companion.felelos
import com.example.managementsafetyvisit.MainActivity.Companion.msvFragment
import com.example.managementsafetyvisit.MainActivity.Companion.newPerceptionArray
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.MainActivity.Companion.perceptionFragment
import com.example.managementsafetyvisit.MainActivity.Companion.read_connect
import com.example.managementsafetyvisit.MainActivity.Companion.write_connect
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.fragment.PerceptionFragment
import java.sql.Connection
import java.sql.DriverManager
import kotlin.math.log

class Sql {
    private var updateId = 0
    private var update = false
    private val TAG = "Sql"
    fun getDataByName(code: String){
        observationArray.clear()
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(read_connect)
            val statement = connection.prepareStatement("""SELECT TextDescription FROM [Fusetech].[dbo].[DolgKodok] where Key1 = ?""")
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                felelos = ""
            }else{
                felelos = resultSet.getString("TextDescription").trim()
                val statement1 = connection.prepareStatement("""SELECT [ID],[Név],[Tsz],[FelelosSzemely],[FelelosTsz],[Resztvevo],[ResztvevoTsz],[Helyszin],[Datum],[Statusz] FROM [Fusetech].[dbo].[MsvData] where FelelosSzemely =? AND Statusz = 1""")
                statement1.setString(1, felelos)
                val resultSet1 = statement1.executeQuery()
                if(!resultSet1.next()){
                    Log.d(TAG, "getDataByName: ")
                }else{
                    val id = resultSet1.getInt("ID")
                    val name = resultSet1.getString("Név")
                    val tsz = resultSet1.getInt("Tsz")
                    val felelos = resultSet1.getString("FelelosSzemely")
                    val ftsz = resultSet1.getInt("FelelosTsz")
                    val resztvevo = resultSet1.getString("Resztvevo")
                    val rtsz = resultSet1.getInt("ResztvevoTsz")
                    val location = resultSet1.getString("Helyszin")
                    val date = resultSet1.getString("Datum")
                    val status = resultSet1.getInt("Statusz")
                    dataArray.add(Data(id,name,tsz,felelos,ftsz,resztvevo,rtsz,location,date,status))
                    val bundle = Bundle()
                    bundle.putSerializable("EMBER", dataArray)
                    val statement3 = connection.prepareStatement("""SELECT [ID],[Eszrevetel],[Tipus],[Valasz],[Intezkedes],[Azonnali],[Javito],[Datum],[Statusz] FROM [Fusetech].[dbo].[MsvNotes] WHERE IdData = ? AND Statusz > 0 order by ID""")
                    statement3.setInt(1,id)
                    val resultSet3 = statement3.executeQuery()
                    if(!resultSet3.next()){
                        Log.d(TAG, "getDataByName: Nincsenek észrevételek")
                    }else{
                        do{
                            val idM = resultSet3.getInt("ID")
                            val eszrevetel = resultSet3.getString("Eszrevetel")
                            val tipus = resultSet3.getString("Tipus")
                            val valasz = resultSet3.getString("Valasz")
                            val intezkedes = resultSet3.getString("Intezkedes")
                            val azonnali = resultSet3.getInt("Azonnali")
                            val urgent : Boolean = azonnali != 0
                            val javito = resultSet3.getString("Javito")
                            val datum = resultSet3.getString("Datum")
                            observationArray.add(ObservationData(eszrevetel,tipus,valasz,intezkedes,urgent,javito,datum,idM.toString().trim()))
                        }while (resultSet3.next())
                    }
                    msvFragment.arguments = bundle
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "getDataByName: $e")
        }
    }
    fun loadPerceptionPanel(msvCode: String){
        newPerceptionArray.clear()
        val connection1: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection1 = DriverManager.getConnection(write_connect)
            val statement1 = connection1.prepareStatement("""SELECT [ID],[IdData] FROM [Fusetech].[dbo].[MsvNotes] WHERE Statusz = 0 AND IdData = ?""")
            statement1.setInt(1,msvCode.toInt())
            val resultSet1 = statement1.executeQuery()
            if(!resultSet1.next()){
                val statement = connection1.prepareStatement("""INSERT INTO [Fusetech].[dbo].[MsvNotes] (IdData,Statusz) Values(?,?)""")
                statement.setInt(1,msvCode.toInt())
                statement.setInt(2,0)
                statement.executeUpdate()
                val statement2 = connection1.prepareStatement("""SELECT [ID],[IdData] FROM [Fusetech].[dbo].[MsvNotes] WHERE Statusz = 0 AND IdData = ?""")
                statement2.setInt(1,msvCode.toInt())
                val resultSet2 = statement2.executeQuery()
                if(!resultSet2.next()){
                    Log.d(TAG, "loadPerceptionPanel: Kurva nagy baj van")
                }else{
                    val id = resultSet2.getInt("ID")
                    newPerceptionArray.add(ObservationData("","PP","","",false,"","",id.toString().trim()))
                    val bundle = Bundle()
                    bundle.putSerializable("EMPTYARRAY", newPerceptionArray)
                    perceptionFragment.arguments = bundle
                }
            }else{
                val id = resultSet1.getInt("ID")
                newPerceptionArray.add(ObservationData("","PP","","",false,"","",id.toString().trim()))
                val bundle = Bundle()
                bundle.putSerializable("EMPTYARRAY", newPerceptionArray)
                perceptionFragment.arguments = bundle
            }
        }catch(e: Exception){
            Log.d(TAG, "loadPerceptionPanel: $e")
        }
    }

    fun saveNewPerception(perception: String?, answer: String?,measure: String?,type: String?, urgent: Boolean,corrector: String?,date: String?,id: Int) {
        var now: Int = 0
        now = if(urgent){
            1
        }else{
            0
        }
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(write_connect)
            val statement = connection.prepareStatement("""UPDATE [Fusetech].[dbo].[MsvNotes] SET Eszrevetel = ?, Tipus = ?, Valasz = ?, Intezkedes = ?, Azonnali = ?, Javito = ?, Datum = ?, Statusz = 1 WHERE ID = ? AND Statusz = 0""")
            statement.setString(1,perception)
            statement.setString(2,type)
            statement.setString(3,answer)
            statement.setString(4,measure)
            statement.setInt(5,now)
            statement.setString(6,corrector)
            statement.setString(7,date)
            statement.setInt(8,id)
            statement.executeUpdate()
            observationArray.add(ObservationData(perception,type,answer,measure,urgent,corrector,date,id.toString()))
        }catch (e: Exception){
            Log.d(TAG, "saveNewPerception: $e")
        }
    }
    fun updateExisting(perception: String?, answer: String?,measure: String?,type: String?, urgent: Boolean,corrector: String?,date: String?,id: Int){
        var now: Int = 0
        now = if(urgent){
            1
        }else{
            0
        }
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(write_connect)
            val statement = connection.prepareStatement("""UPDATE [Fusetech].[dbo].[MsvNotes] SET Eszrevetel = ?, Tipus = ?, Valasz = ?, Intezkedes = ?, Azonnali = ?, Javito = ?, Datum = ? WHERE ID = ?""")
            statement.setString(1,perception)
            statement.setString(2,type)
            statement.setString(3,answer)
            statement.setString(4,measure)
            statement.setInt(5,now)
            statement.setString(6,corrector)
            statement.setString(7,date)
            statement.setInt(8,id)
            statement.executeUpdate()
            //observationArray.add(ObservationData(perception,type,answer,measure,urgent,corrector,date,id.toString()))
            getPositionByValue(id)
            if(update){
                observationArray[updateId].perception = perception
                observationArray[updateId].type = type
                observationArray[updateId].response = answer
                observationArray[updateId].measure = measure
                observationArray[updateId].now = urgent
                observationArray[updateId].corrector = corrector
                observationArray[updateId].date = date
            }
        }catch (e: Exception){
            Log.d(TAG, "saveNewPerception: $e")
        }
    }
    fun deleteExisting(id: Int){
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(write_connect)
            val statement = connection.prepareStatement("""DELETE FROM [Fusetech].[dbo].[MsvNotes] WHERE ID = ?""")
            statement.setInt(1,id)
            statement.executeUpdate()
        }catch (e: Exception){
            Log.d(TAG, "deleteExisting: $e")
        }

    }
    private fun getPositionByValue(id: Int){
        for(i in 0 until observationArray.size){
            if(observationArray[i].id == id.toString()){
                update = true
                updateId = i
            }
        }
    }
}