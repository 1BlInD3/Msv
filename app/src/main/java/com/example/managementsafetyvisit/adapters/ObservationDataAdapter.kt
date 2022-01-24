package com.example.managementsafetyvisit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.data.ObservationData

class ObservationDataAdapter(private val perceptionList: ArrayList<ObservationData>, val listener: CurrentSelection):
    RecyclerView.Adapter<ObservationDataAdapter.ObservationAdapterHolder>() {
    inner class ObservationAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val perception: TextView = itemView.findViewById(R.id.perception_adapter_text)
        val type: TextView = itemView.findViewById(R.id.type_adapter_text)
        val response: TextView = itemView.findViewById(R.id.response_adapter_text)
        val measure: TextView = itemView.findViewById(R.id.measure_adapter_text)
        val urgent :CheckBox = itemView.findViewById(R.id.urgent_adapter_box)
        val corrector: TextView = itemView.findViewById(R.id.corrector_adapter_text)
        val date: TextView = itemView.findViewById(R.id.date_adapter_text)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = absoluteAdapterPosition
            if(position!=RecyclerView.NO_POSITION){
                listener.onCurrentClick(position)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationAdapterHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.perception_adapter,parent,false)
        return ObservationAdapterHolder(itemView)
    }

    override fun onBindViewHolder(holder: ObservationAdapterHolder, position: Int) {
        val currentItem = perceptionList[position]
        holder.perception.text = currentItem.perception
        holder.type.text = currentItem.type
        holder.response.text = currentItem.response
        holder.measure.text = currentItem.measure
        holder.urgent.isChecked = currentItem.now
        holder.corrector.text = currentItem.corrector
        holder.date.text = currentItem.date
    }

    override fun getItemCount() = perceptionList.size

    interface CurrentSelection{
        fun onCurrentClick(position: Int)
    }
}