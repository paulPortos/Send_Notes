package com.group1.notamonotako.views

import ApiService
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
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
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AddNotes : AppCompatActivity() {
    private lateinit var leftButton: ImageButton
    private lateinit var doneButton: ImageButton
    private lateinit var title: EditText
    private lateinit var contents: EditText
    private lateinit var progressBar : ProgressBar
    private lateinit var soundManager: SoundManager

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
                Toast.makeText(this, "Title and Contents must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                CreateData(title, contents)
                progressBar.visibility = View.VISIBLE
                doneButton.isClickable=false

            }
        }
    }

    private fun CreateData(title: String, contents: String) {
        lifecycleScope.launch {
            val apiService = RetrofitInstance.create(ApiService::class.java)
            val postNotes = PostnotesRequest(title = title, contents = contents, public = false, to_public = false)

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

                        Toast.makeText(this@AddNotes, "Note created successfully", Toast.LENGTH_SHORT).show()

                        // Clear back stack and start Mynotes activity
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        finish()
                    } else {
                        Toast.makeText(this@AddNotes, "Note created but ID is missing", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE

                    }
                } else {
                    Toast.makeText(this@AddNotes, "Failed to create note", Toast.LENGTH_SHORT).show()
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
