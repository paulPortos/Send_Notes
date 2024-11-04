package com.group1.notamonotako.activities.add_contents

import ApiService
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.AccountManager
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.notes.PostnotesRequest
import com.group1.notamonotako.activities.main_activity.HomeActivity
import com.group1.notamonotako.activities.view_contents.ViewMynotes
import com.group1.notamonotako.network.NetworkManager
import com.group1.notamonotako.roomdb.Notes
import com.group1.notamonotako.roomdb.NotesDao
import com.group1.notamonotako.roomdb.NotesDatabase
import com.group1.notamonotako.roomdb.database_manager.DatabaseManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AddNotes : AppCompatActivity() {
    private lateinit var leftButton : ImageButton
    private lateinit var doneButton : ImageButton
    private lateinit var title : EditText
    private lateinit var contents : EditText
    private lateinit var progressBar : ProgressBar
    private lateinit var soundManager : SoundManager
    private lateinit var notesDao : NotesDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        leftButton = findViewById(R.id.leftButton)
        doneButton = findViewById(R.id.doneButton)
        title  = findViewById(R.id.title)
        contents = findViewById(R.id.contents)
        progressBar = findViewById(R.id.progressBar)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        soundManager = SoundManager(this) // Initialize SoundManager
        progressBar.visibility = View.INVISIBLE
        val isMuted = AccountManager.isMuted
        soundManager.updateMediaPlayerVolume(isMuted)

        notesDao = DatabaseManager.getNotesDao(this)
        val networkManager = NetworkManager

        leftButton.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(this@AddNotes, HomeActivity::class.java)
            startActivity(intent)
            progressBar.visibility = View.VISIBLE

        }

        doneButton.setOnClickListener {
            soundManager.playSoundEffect()

            val title = title.text.toString()
            val contents = contents.text.toString()
            if (title.isEmpty() || contents.isEmpty()) {
                doneButton.isClickable=true
                // Show an error message or take appropriate action
                Toast.makeText(this, "Title and Contents must not be empty.", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    if (networkManager.isNetworkAvailable(this)) {
                        addNote(title, contents)
                    } else {
                        // Call upsertNote if offline
                        upsertNote(title, contents)
                    }
                } catch (e: Exception){
                    Log.d("OOO", "Error $e")
                }
                progressBar.visibility = View.VISIBLE
                doneButton.isClickable=false
            }
        }
    }

    private fun upsertNote(title: String, contents: String){
        lifecycleScope.launch {
            try {
                val notesData = Notes(title = title, contents = contents, toPublic = false, isPublic = false)
                val upsert = notesDao.UpsertNotes(notesData)
                Log.e("Upsert Notes", "Upsert = $upsert")

                Toast.makeText(this@AddNotes, "Note created successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@AddNotes, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception){
                Log.e("Upsert Notes", "Error upserting note: $e")
                Toast.makeText(this@AddNotes, "Failed Upload.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun addNote(title: String, contents: String) {
        lifecycleScope.launch {
            val apiService = RetrofitInstance.create(ApiService::class.java)
            val postNotes = PostnotesRequest(title = title, contents = contents, isPublic = false, to_public = false)

            try {
                val response = apiService.createNote(postNotes)

                if (response.isSuccessful) {

                    val noteId = response.body()?.id
                    val dateString = response.body()?.updated_at // Assuming this is available in the response

                    if (noteId != null) {
                        // Navigate to Mynotes activity with created note's details
                        val intent = Intent(this@AddNotes, ViewMynotes::class.java).apply {
                            putExtra("title", title)
                            putExtra("contents", contents)
                            putExtra("note_id", noteId)
                            putExtra("date", dateString)
                        }
                        Toast.makeText(this@AddNotes, "Note created successfully!", Toast.LENGTH_SHORT).show()
                        // Clear back stack and start Mynotes activity
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@AddNotes, "Note created but ID is missing.", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
                } else {
                    Toast.makeText(this@AddNotes, "Failed to create note.", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE

                }
            } catch (e: HttpException) {
                Toast.makeText(this@AddNotes, "HTTP error: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE

            } catch (e: IOException) {
                Toast.makeText(this@AddNotes, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE

            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release() // Release media player when done
    }
}
