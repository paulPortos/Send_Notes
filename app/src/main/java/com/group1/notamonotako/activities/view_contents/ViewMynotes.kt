package com.group1.notamonotako.activities.view_contents

import ApiService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.AccountManager
import com.group1.notamonotako.api.AccountManager.getEmail
import com.group1.notamonotako.api.AccountManager.getUsername
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.admin.PostToAdmin
import com.group1.notamonotako.api.requests_responses.admin.updateAdminChangesForm
import com.group1.notamonotako.api.requests_responses.notes.UpdateNotes
import com.group1.notamonotako.api.requests_responses.notes.UpdateToPublicNotes
import com.group1.notamonotako.api.requests_responses.notification.PostPendingNotification
import com.group1.notamonotako.api.requests_responses.sendNotes.SendNotesRequest
import com.group1.notamonotako.activities.main_activity.HomeActivity
import com.group1.notamonotako.api.requests_responses.notes.Note
import com.group1.notamonotako.network.NetworkManager
import com.group1.notamonotako.roomdb.Notes
import com.group1.notamonotako.roomdb.NotesDao
import com.group1.notamonotako.roomdb.NotesDatabase
import com.group1.notamonotako.roomdb.database_manager.DatabaseManager
import com.group1.notamonotako.roomdb.database_manager.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class ViewMynotes : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var Content: EditText
    private lateinit var Date: TextView
    private lateinit var deletebtn : ImageButton
    private lateinit var UpdateNotes : ImageView
    private lateinit var btnback : ImageButton
    private lateinit var sharebtn : ImageButton
    private lateinit var flDelete : FrameLayout
    private lateinit var viewBlur : View
    private lateinit var btnCancel : AppCompatButton
    private lateinit var btnDelete : AppCompatButton
    private lateinit var btnCancelShare : AppCompatButton
    private lateinit var btnShare : AppCompatButton
    private lateinit var flShare : FrameLayout
    private lateinit var soundManager: SoundManager
    private lateinit var btnSend : AppCompatButton
    private lateinit var btnSendNotes : AppCompatButton
    private lateinit var btnCancelSend: AppCompatButton
    private lateinit var flSend : FrameLayout
    private lateinit var etEmail: EditText
    private lateinit var notesDao : NotesDao
    val notesRepository by lazy {
        val notesDao = NotesDatabase.getInstance(this).dao
        NotesRepository(notesDao)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mynotes)

        Date = findViewById(R.id.date)
        etTitle = findViewById(R.id.Title)
        Content = findViewById(R.id.Contents)
        deletebtn = findViewById(R.id.deletebtn)
        btnback = findViewById(R.id.btnback)
        UpdateNotes = findViewById(R.id.Update_Notes)
        sharebtn = findViewById(R.id.sharebtn)
        flDelete = findViewById(R.id.flDelete)
        viewBlur = findViewById(R.id.viewBlur)
        btnCancel = findViewById(R.id.btnCancel)
        btnDelete = findViewById(R.id.btnDelete)
        flShare = findViewById(R.id.flShare)
        btnCancelShare = findViewById(R.id.btnCancelShare)
        btnShare = findViewById(R.id.btnShare)
        btnSend = findViewById(R.id.btnSend)
        btnSendNotes = findViewById(R.id.btnSendNotes)
        btnCancelSend = findViewById(R.id.btnCancelSend)
        flSend = findViewById(R.id.flSend)
        etEmail = findViewById(R.id.etEmail)

        soundManager = SoundManager(this) // Initialize SoundManager
        val isMuted = AccountManager.isMuted
        soundManager.updateMediaPlayerVolume(isMuted)
        val networkManager = NetworkManager
        notesDao = DatabaseManager.getNotesDao(this)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val intent = intent
        val title = intent.getStringExtra("title")
        val contents = intent.getStringExtra("contents")
        val dateString = intent.getStringExtra("date")
        val noteId = intent.getIntExtra("note_id",-1)
        val publicize = intent.getBooleanExtra("public",false) ?: false
        val toPublic = intent.getBooleanExtra("to_public", false) ?: false

        val recentTitle = title.toString()
        val recentContents = contents.toString()

        this.etTitle.setText(title)
        this.Content.setText(contents)

        if (networkManager.isNetworkAvailable(this)) {
            if (dateString != null) {
                // Define the input and output date formats
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                try {
                    val date = inputFormat.parse(dateString)
                    val formattedDate = date?.let { outputFormat.format(it) }
                    this.Date.text = formattedDate
                } catch (e: Exception) {
                    // Handle parsing exception
                    e.printStackTrace()
                    this.Date.text = "Invalid date"
                }
            }
        } else {
            this.Date.text = "(Offline Mode)"
        }

        flDelete.visibility = View.GONE
        flShare.visibility = View.GONE
        viewBlur.visibility = View.GONE

        sharebtn.setOnClickListener{
            if (networkManager.isNetworkAvailable(this)) {
                flShare.visibility = View.VISIBLE
                viewBlur.visibility = View.VISIBLE
                flShare.setOnTouchListener { _, _ -> true }
                viewBlur.setOnTouchListener { _, _ -> true }
                soundManager.playSoundEffect()
            } else {
                Toast.makeText(this, "Cannot share in offline mode.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            flShare.visibility = View.GONE
            flSend.visibility = View.VISIBLE
            viewBlur.visibility = View.VISIBLE
            flSend.setOnTouchListener { _, _ -> true }
            viewBlur.setOnTouchListener { _, _ -> true }
            soundManager.playSoundEffect()
        }

        btnCancelSend.setOnClickListener {
            flSend.visibility = View.GONE
            viewBlur.visibility = View.GONE
            soundManager.playSoundEffect()
        }

        btnSendNotes.setOnClickListener {
            flSend.visibility = View.GONE
            soundManager.playSoundEffect()
            viewBlur.visibility = View.GONE
            val sentToEmail = etEmail.text.toString().trim()
            val sendByEmail = getEmail().toString()
            if (sendByEmail.isEmpty()){
                Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_SHORT).show()
            } else {
                sendNotes(noteId, sentToEmail, sendByEmail)
            }
        }

        btnCancelShare.setOnClickListener{
            flShare.visibility = View.GONE
            viewBlur.visibility = View.GONE
            soundManager.playSoundEffect()
        }

        btnShare.setOnClickListener {
            soundManager.playSoundEffect()

            val titleString = etTitle.text.toString()
            val creatorsUsername = getUsername().toString()
            val creatorsEmail = getEmail().toString()
            val contentsString = Content.text.toString()
            val publicDefaultValue = false
            if (publicize){
                Toast.makeText(this, "Note already public.", Toast.LENGTH_SHORT).show()
            } else {
                shareNote(noteId, titleString, creatorsUsername, creatorsEmail, contentsString, publicDefaultValue, publicize)
            }
            flShare.visibility = View.INVISIBLE
            viewBlur.visibility = View.INVISIBLE
            soundManager.playSoundEffect()
        }

        deletebtn.setOnClickListener{
            soundManager.playSoundEffect()

            flDelete.visibility = View.VISIBLE
            viewBlur.visibility = View.VISIBLE
            flDelete.setOnTouchListener { _, _ -> true }
            viewBlur.setOnTouchListener { _, _ -> true }
        }

        btnDelete.setOnClickListener {
            soundManager.playSoundEffect()
            btnDelete.backgroundTintList= ContextCompat.getColorStateList(this, R.color.new_background_color)
            if (networkManager.isNetworkAvailable(this)) {
                if (noteId != -1) {
                    deleteNote(noteId)
                }
            } else {
                deleteOfflineNote(noteId)
            }
        }

        btnCancel.setOnClickListener {
            flDelete.visibility = View.GONE
            viewBlur.visibility = View.GONE
            soundManager.playSoundEffect()
        }

        btnback.setOnClickListener{
            val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
            intent.putExtra("showMyNotesFragment", true)
            startActivity(intent)
            soundManager.playSoundEffect()
        }

        UpdateNotes.setOnClickListener {
            soundManager.playSoundEffect()

            val titleString = etTitle.text.toString()
            val contentsString = Content.text.toString()
            val creatorsUsername = getUsername().toString()
            val creatorsEmail = getEmail().toString()
            if (networkManager.isNetworkAvailable(this)) {
                if (noteId != -1) {
                    if (recentTitle != titleString || recentContents != contentsString) {
                        if (publicize){
                            shareNote(noteId, titleString, creatorsUsername, creatorsEmail, contentsString, false, publicize)
                            updateNote(noteId, publicize, toPublic)
                        } else {
                            updateNote(noteId, publicize, toPublic)
                        }
                    } else {
                        Toast.makeText(this, "No changes detected.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Note ID is missing.", Toast.LENGTH_SHORT).show()
                }
            } else {
                updateOfflineNote(noteId, titleString, creatorsUsername)
            }
        }

    }

    private fun sendNotes (notesId: Int, sendTo: String, sentBy: String){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val request = SendNotesRequest(notesId, sendTo, sentBy)
                val token = TokenManager.getToken()
                val bearerToken = "Bearer $token"
                val response = apiService.sendNotes(bearerToken, request)
                if (response.isSuccessful){
                    if (response.code() == 404){
                        Toast.makeText(this@ViewMynotes, "Email is not found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ViewMynotes, "Note sent successfully", Toast.LENGTH_SHORT).show()
                    }
                } else if (response.code() == 404){
                    Toast.makeText(this@ViewMynotes, "Email is not found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ViewMynotes, "Failed to send note: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("SendNotes", "Error: ${response.code()}, Message: ${response.message()}")
                    Log.e("SendNotes", "Error body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception){
                Log.e("SendNotes", "Error: ${e.message}", e)
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException){
                Log.e("SendNotes", "Error: ${e.message}", e)
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun shareNote(notesId: Int,title: String, creatorUsername: String, creatorEmail: String, contents: String, public: Boolean, publicize: Boolean){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val postToAdmin = PostToAdmin(notesId, title, creatorUsername, creatorEmail, contents, public)
                val response = apiService.toAdmin(postToAdmin)

                if (response.isSuccessful) {
                    if (!publicize) {
                        setToPublicIntoTrue(notesId)
                        postPendingNotif(notesId, creatorEmail, "Your note $title has been shared.")
                        Toast.makeText(this@ViewMynotes, "Note shared successfully!", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("ShareNote", "Response: ${response.body()}")
                } else if(response.code() == 409){
                    Toast.makeText(this@ViewMynotes, "Note already shared and pending!", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this@ViewMynotes, "Failed to share note: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("ShareNote", "Error: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: HttpException){
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ShareNote", "Error: ${e.message}", e)
            } catch (e: IOException) {
                Toast.makeText(this@ViewMynotes, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNote(noteId: Int) {
        val token = TokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Authorization token missing!", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("DeleteNote", "Token: $token, Note ID: $noteId")

        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    apiService.deleteNote("Bearer $token", noteId)  // Ensure token is correctly formatted
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@ViewMynotes, "Note deleted successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
                    intent.putExtra("showMyNotesFragment", true)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Finish Mynotes activity to avoid returning to it
                } else {
                    Log.e("DeleteNote", "Error: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(this@ViewMynotes, "Failed to delete note: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateNote(noteId: Int, publicize: Boolean, toPublic: Boolean) {
        val token = TokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Authorization token missing!", Toast.LENGTH_SHORT).show()
            return
        }
        val updatedTitle = etTitle.text.toString()
        val updatedContent = Content.text.toString()
        if (updatedTitle.isEmpty() || updatedContent.isEmpty()) {
            Toast.makeText(this, "Title or content cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("UpdateNote", "Token: $token, Note ID: $noteId")

        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val noteRequest = UpdateNotes(title = updatedTitle, contents = updatedContent, toPublic = true, public = false)

                val response = withContext(Dispatchers.IO) {
                    apiService.updateNote("Bearer $token", noteId, noteRequest)  // Pass the updated note
                }
                if (response.isSuccessful) {
                    if (publicize){
                        Toast.makeText(this@ViewMynotes, "Note is shared and pending", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
                        intent.putExtra("showMyNotesFragment", true)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else if (toPublic){
                        val username = getUsername().toString()
                        val email = getEmail().toString()
                        updateAdminChanges(noteId, updatedTitle, updatedContent, username, email)
                        Toast.makeText(this@ViewMynotes, "Note updated and pending", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
                        intent.putExtra("showMyNotesFragment", true)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }else {
                        Toast.makeText(this@ViewMynotes, "Note updated successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
                        intent.putExtra("showMyNotesFragment", true)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@ViewMynotes, "Failed to update note: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateNote", "Error: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UpdateNote", "Error: ${e.message}", e)
            }
        }
    }
    private fun setToPublicIntoTrue(noteId: Int){
        val token = TokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Authorization token missing!", Toast.LENGTH_SHORT).show()
            return
        }
        val updatedTitle = etTitle.text.toString()
        val updatedContent = Content.text.toString()
        val toPublic = true
        if (updatedTitle.isEmpty() || updatedContent.isEmpty()) {
            Toast.makeText(this, "Title or content cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val noteRequest = UpdateToPublicNotes(title = updatedTitle, contents = updatedContent, toPublic = toPublic)
                val response = withContext(Dispatchers.IO) {
                    apiService.updateNotesToPublic("Bearer $token", noteId, noteRequest)
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun updateAdminChanges(noteId: Int,title: String,  contents: String,  creatorUsername: String, creatorEmail: String,){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val post = updateAdminChangesForm(title, contents, creatorUsername, creatorEmail)
                val response = apiService.updateAdmin(noteId, post)
                if (response.isSuccessful){
                    Log.e("UpdateAdminChanges Success", "Response: ${response.body()}")
                    Log.e("UpdateAdminChanges Success", "Response code: ${response.code()}, Message: ${response.message()}")
                } else {
                    Log.e("UpdateAdminChanges Fail", "Error: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: Exception){
                Log.e("UpdateAdminChanges", "Error: ${e.message}", e)
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException){
                Log.e("UpdateAdminChanges", "Error: ${e.message}", e)
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun postPendingNotif(notesId: Int, email: String, message: String){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val post = PostPendingNotification(notesId, email, message)
                val response = apiService.postNotePending(post)
                if (response.isSuccessful){
                    Log.e("PostPendingNotif", "Response: ${response.body()}")
                } else {
                    Log.e("PostPendingNotif", "Error: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PostPendingNotif", "Error: ${e.message}", e)
            } catch (e: HttpException){
                Toast.makeText(this@ViewMynotes, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PostPendingNotif", "Error: ${e.message}", e)
            }
        }
    }

    private fun updateOfflineNote(noteId: Int, title: String, contents: String, ) {
        lifecycleScope.launch {
            try {
                val notesData = Notes(id = noteId, title, contents = contents, toPublic = false, isPublic = false)
                val upsert = notesDao.UpsertNotes(notesData)
                Log.e("Upsert Notes", "Upsert = $upsert")
                Toast.makeText(this@ViewMynotes, "Update Success!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
                intent.putExtra("showMyNotesFragment", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception){
                Log.e("Upsert Notes", "Error upserting note: $e")
                Toast.makeText(this@ViewMynotes, "Failed Upload.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteOfflineNote(noteId: Int) {
        lifecycleScope.launch {
            try {
                notesRepository.deleteOfflineNotes(noteId)
                Toast.makeText(this@ViewMynotes, "Successfully deleted notes!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ViewMynotes, HomeActivity::class.java)
                intent.putExtra("showMyNotesFragment", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()  // Finish Mynotes activity to avoid returning to it
            } catch (e: Exception) {
                Toast.makeText(this@ViewMynotes, "ERROR DELETE OFFLINE NOTE: $e", Toast.LENGTH_SHORT).show()
                Log.e("Deldel", "$e" )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(
            "showMyNotesFragment",
            true
        )  // Pass data to indicate the Home fragment should be shown
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()  // Finish Mynotes activity to avoid going back to it
    }
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release() // Release media player when done
    }
}