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
import com.group1.notamonotako.api.requests_responses.flashcards.GetFlashcards
import com.group1.notamonotako.views.ViewFlashcards

class MyFlashcardsAdapter(
    private val context: Context,
    private var flashcardsList: List<GetFlashcards> // List of GetFlashcards
) : RecyclerView.Adapter<MyFlashcardsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val cards: TextView = view.findViewById(R.id.contents)
        val soundManager = SoundManager(context) // Initialize SoundManager

        fun bind(item: GetFlashcards) {
            title.text = item.title
            // Display cards as a comma-separated string
            cards.text = item.cards.joinToString(", ")
            itemView.setOnClickListener {
                soundManager.playSoundEffect()
                // Create an intent to navigate to ViewFlashcards
                val intent = Intent(context, ViewFlashcards::class.java).apply {
                    val updatedAtDate = item.updatedAt?.substringBefore("T")
                    putExtra("title", item.title)
                    putStringArrayListExtra("cards", ArrayList(item.cards)) // Pass cards as an ArrayList
                    putExtra("flashcard_id", item.id)
                    putExtra("updated_at", updatedAtDate)
                }
                context.startActivity(intent) // Start the activity
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_myflashcards_grid, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = flashcardsList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = flashcardsList.size
}
