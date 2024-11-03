package com.group1.notamonotako.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.group1.notamonotako.R
import com.group1.notamonotako.activities.view_contents.ViewMynotes
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.roomdb.Notes

class MyNotesOfflineAdapter (val context: Context, private val noteList: List<Notes>) : RecyclerView.Adapter<MyNotesOfflineAdapter.NotesViewHolder>(){
    inner class NotesViewHolder(noteView: View) : RecyclerView.ViewHolder(noteView) {
        val title: TextView = noteView.findViewById(R.id.title)
        val notes : ConstraintLayout = noteView.findViewById(R.id.layout_notes)
        val contents: TextView = noteView.findViewById(R.id.contents)
        val public : TextView = noteView.findViewById(R.id.tvPublic)
    }

    override  fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : NotesViewHolder {
        val noteViewer: View = LayoutInflater.from(context).inflate(R.layout.rv_mynotes_row, parent, false)
        return NotesViewHolder(noteViewer)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val item = noteList[position]
        holder.title.text = item.title
        holder.contents.text = item.title
        val soundManager = SoundManager(context)
        holder.notes.setOnClickListener{
            soundManager.playSoundEffect()
            val intent = Intent(it.context, ViewMynotes::class.java)
            intent.putExtra("note_id", item.id)
            intent.putExtra("public", item.isPublic)
            intent.putExtra("title", item.title)
            intent.putExtra("contents", item.contents)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }
}