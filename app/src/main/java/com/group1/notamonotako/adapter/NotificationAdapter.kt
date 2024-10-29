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
import com.group1.notamonotako.api.requests_responses.notification.GetNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationAdapter(val context: Context, private var data: List<GetNotification>) : RecyclerView.Adapter<NotificationAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val notificationType: TextView = view.findViewById(R.id.notification_type)
        val time: TextView = view.findViewById(R.id.time)
        val message: TextView = view.findViewById(R.id.message)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_notification, parent, false)
        return ItemViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = data[data.size - 1 - position]
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
        holder.notificationType.text = item.notificationType
        holder.message.text = item.message
    }
    override fun getItemCount(): Int {
        return data.size
    }
}