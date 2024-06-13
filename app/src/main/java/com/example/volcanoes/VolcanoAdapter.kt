package com.example.volcanoes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VolcanoAdapter(private val list: List<Volcano>) :

    RecyclerView.Adapter<VolcanoAdapter.ViewHolder>() {
    lateinit var callback: (Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item, parent, false),
            callback
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].name
        holder.height.text = "${list[position].height} m"
        holder.currentPosition = position
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View, callback: (Int)->Unit) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.item_name)
        var height = itemView.findViewById<TextView>(R.id.item_height)
        var currentPosition = 0
        init {
            itemView.setOnClickListener {
                callback.invoke(currentPosition)
            }
        }
    }
}