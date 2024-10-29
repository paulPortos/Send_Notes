package com.group1.notamonotako.adapter

import ApiService
import TokenManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.group1.notamonotako.R
import com.group1.notamonotako.api.AccountManager
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.comments.getComments
import kotlinx.coroutines.launch


class CommentsAdapter (val context: Context, private var data: List<getComments>,  private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<CommentsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val username: TextView = view.findViewById(R.id.username)
        val message: TextView = view.findViewById(R.id.message)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_comment, parent, false)
        return ItemViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        TokenManager.init(this.context)

        val item = data[position]
        holder.username.text = item.username
        holder.message.text = item.comment
        val soundManager = SoundManager(context) // Initialize SoundManager

        val currentuserid = AccountManager.getUserId() // Ensure this returns the logged-in user's username

        holder.itemView.setOnLongClickListener {
            soundManager.playSoundEffect()
            // Check if the username of the comment matches the current user's username
            if (item.user_id == currentuserid) {
                // Allow long press and toggle checkbox visibility
                holder.btnDelete.visibility = if (holder.btnDelete.visibility == View.VISIBLE) {
                    holder.btnDelete.visibility = View.GONE
                    holder.btnDelete.isClickable = true
                    View.GONE
                } else {
                    holder.btnDelete.visibility = View.VISIBLE
                    View.VISIBLE
                }
                true // Indicate the long press was handled
            } else {
                // Show a message or take another action if the user doesn't match
                Toast.makeText(holder.itemView.context, "You can only modify your own notes.", Toast.LENGTH_SHORT).show()
                false // Indicate the long press was not handled
            }
        }

        holder.btnDelete.setOnClickListener {
           deleteItem(item.id,item.notes_id)
            soundManager.playSoundEffect()
        }
    }







    override fun getItemCount(): Int {
        return data.size
    }

    private fun deleteItem(commentId: Int, noteId: Int) {
        val token = TokenManager.getToken()
        if (token == null) {
            Toast.makeText(this.context, "Authorization token missing", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleOwner.lifecycleScope.launch {
            try {
                // Call the API to delete the comment
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = apiService.deleteComment("Bearer $token", noteId, commentId)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show()

                    // Optionally: Remove the deleted comment from the adapter's data and refresh the view
                    data = data.filter { it.id != commentId }
                    notifyDataSetChanged()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(context, "Failed to delete comment: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Catch and log any exceptions that occur during the network request
                Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace() // Log the full stack trace to understand the issue
            }
        }
    }

}
