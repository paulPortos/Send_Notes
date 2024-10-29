package com.group1.notamonotako.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group1.notamonotako.R
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.sendNotes.getSentNotesData
import com.group1.notamonotako.activities.view_contents.ViewSendNotes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SendNotesAdapter (val context: Context, private var data: List<getSentNotesData>) : RecyclerView.Adapter<SendNotesAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.title)
        val sentBy: TextView = view.findViewById(R.id.sent_by)
        val time: TextView = view.findViewById(R.id.time)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_sendnotes, parent, false)
        return ItemViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = data[position]
        // Original date format (ISO 8601)
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        originalFormat.timeZone = TimeZone.getTimeZone("UTC")  // Handle UTC time zone

        // Desired output format
        val targetFormat = SimpleDateFormat("MM-dd-yyyy - HH:mm", Locale.getDefault())
        targetFormat.timeZone = TimeZone.getDefault()  // Local time zone
        try {
            // Parse the original date string
            val date: Date = originalFormat.parse(item.updatedAt)
            // Format the date to the desired output
            val formattedDate = targetFormat.format(date)
            holder.time.text = formattedDate
        } catch (e: Exception) {
            Log.d("NotificationAdapter", "Error parsing date: ${e.message}")
            // If parsing fails, fallback to the original string
            holder.time.text = item.updatedAt
        }
        holder.title.text = item.title
        holder.sentBy.text = item.sentBy
        val soundManager = SoundManager(context) // Initialize SoundManager

        holder.itemView.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(it.context, ViewSendNotes::class.java)
            intent.putExtra("id", item.id ?: -1)
            intent.putExtra("note_id", item.notesId ?: -1)
            intent.putExtra("sent_by", item.sentBy ?: "No Sent By")
            intent.putExtra("title", item.title ?: "No Title")
            intent.putExtra("contents", item.contents ?: "No Contents")
            it.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return data.size
    }
}