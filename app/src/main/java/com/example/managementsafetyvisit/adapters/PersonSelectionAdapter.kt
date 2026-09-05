package com.example.managementsafetyvisit.adapters

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.config.AppConfig
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.retrofit.SendApi
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonSelectionAdapter(
    private val personList: List<Data>,
    private val listener: OnPersonClickListener
) : RecyclerView.Adapter<PersonSelectionAdapter.PersonViewHolder>() {

    interface OnPersonClickListener {
        fun onPersonClick(data: Data)
    }

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val personImage: ImageView = itemView.findViewById(R.id.person_image)
        val personName: TextView = itemView.findViewById(R.id.person_name)
        val participantName: TextView = itemView.findViewById(R.id.participant_name)
        val personLocation: TextView = itemView.findViewById(R.id.person_location)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            if (position != RecyclerView.NO_POSITION && position < personList.size) {
                listener.onPersonClick(personList[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_selection_item, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val item = personList[position]
        holder.personName.text = "${item.name} (${item.tsz})"
        holder.participantName.text = if (item.resztvevo.isNullOrEmpty()) "Nincs megadva" else item.resztvevo
        holder.personLocation.text = if (item.location.isNullOrEmpty()) "Helyszín" else item.location
        holder.personImage.setImageResource(R.mipmap.ic_launcher)

        val cleanName = item.name.replace(" ", "").replace("-", "")
        val photoName = "${cleanName}${item.tsz}.jpg"

        if (!AppConfig.USE_MOCK_DATA) {
            SendApi().getImage(photoName).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                            if (bmp != null) {
                                holder.personImage.setImageBitmap(bmp)
                            }
                        } catch (e: Exception) {
                            Log.e("PersonAdapter", "Error decoding bitmap: $e")
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("PersonAdapter", "Failed to fetch image $photoName: $t")
                }
            })
        }
    }

    override fun getItemCount(): Int = personList.size
}