package com.example.managementsafetyvisit.utils

import android.util.Log
import com.example.managementsafetyvisit.MainActivity.Companion.dataArray
import com.example.managementsafetyvisit.MainActivity.Companion.felelos
import com.example.managementsafetyvisit.MainActivity.Companion.read_connect
import com.example.managementsafetyvisit.data.Data
import java.sql.Connection
import java.sql.DriverManager

class Sql {
    private val TAG = "Sql"
    fun getDataByName(code: String){
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
                }
            }
        }catch (e: Exception){

        }
    }
}