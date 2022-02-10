package com.example.managementsafetyvisit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.data.ManagerNames
import java.util.*
import java.util.logging.StreamHandler
import kotlin.collections.ArrayList

class ManagerAdapter(context: Context, resource: Int, objects: ArrayList<out String>) :
    ArrayAdapter<String>(context,resource,objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View{
        var convertView1: View? = convertView
        convertView1 = LayoutInflater.from(parent.context).inflate(R.layout.managerek, parent, false)
        val textViewName = convertView1.findViewById<TextView>(R.id.manager_name_adapter)
        val currentItem: String? = getItem(position)
        if(currentItem != null){
            textViewName?.setText(currentItem)
        }
        return convertView1
    }

}