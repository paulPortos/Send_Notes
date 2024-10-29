package com.group1.notamonotako.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group1.notamonotako.R
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.public_notes.getPublicNotes
import com.group1.notamonotako.activities.view_contents.ViewHome

class HomeAdapter(val context: Context, private var data: List<getPublicNotes>) : RecyclerView.Adapter<HomeAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val contents: TextView = view.findViewById(R.id.contents)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_home_row, parent, false)
        return ItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = data[position]
        holder.title.text = item.title
        holder.contents.text = item.contents
        val soundManager = SoundManager(context) // Initialize SoundManager

        // Get the current user's ID

        holder.itemView.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(it.context, ViewHome::class.java)
            val updatedAtDate = item.updated_at?.substringBefore("T") ?: "No Date"
            intent.putExtra("note_id", item.notesId ?: -1)
            intent.putExtra("title", item.title ?: "No Title")
            intent.putExtra("contents", item.contents ?: "No Contents")
            intent.putExtra("public", item.public ?: false)
            intent.putExtra("updated_at", updatedAtDate)
            it.context.startActivity(intent)
        }



    }

    fun setFilteredList(data: List<getPublicNotes>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
