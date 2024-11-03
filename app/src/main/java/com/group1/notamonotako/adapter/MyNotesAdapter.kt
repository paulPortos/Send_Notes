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
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.notes.Note
import com.group1.notamonotako.activities.view_contents.ViewMynotes

class MyNotesAdapter(val context: Context, val notelist: List<Note>) : RecyclerView.Adapter<MyNotesAdapter.NotesViewHolder>() {

    inner class NotesViewHolder(noteView : View) : RecyclerView.ViewHolder(noteView) {
        val title: TextView = noteView.findViewById(R.id.title)
        val notes : ConstraintLayout = noteView.findViewById(R.id.layout_notes)
        val contents: TextView = noteView.findViewById(R.id.contents)
        val public : TextView = noteView.findViewById(R.id.tvPublic)
        val pending : TextView = noteView.findViewById(R.id.tvPending)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {

        val Noteviewer: View = LayoutInflater.from(context)
            .inflate(R.layout.rv_mynotes_row, parent, false)
        return NotesViewHolder(Noteviewer)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val item = notelist[position]
        holder.title.text = item.title
        holder.contents.text = item.contents
        val soundManager = SoundManager(context) // Initialize SoundManager
        holder.notes.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(it.context, ViewMynotes::class.java)

            intent.putExtra("public", item.public)
            intent.putExtra("title", item.title)
            intent.putExtra("contents", item.contents)
            intent.putExtra("date", item.updated_at)
            intent.putExtra("note_id",item.id)
            intent.putExtra("to_public", item.toPublic)
            it.context.startActivity(intent)
        }

        if (item.public == true && item.toPublic == false) {
            holder.public.visibility = View.VISIBLE
        } else if(item.toPublic == true && item.public == false) {
            holder.pending.visibility = View.VISIBLE
        } else{
            holder.pending.visibility = View.GONE
            holder.public.visibility = View.GONE
        }


    }

    override fun getItemCount(): Int {
        return notelist.size
    }

}
