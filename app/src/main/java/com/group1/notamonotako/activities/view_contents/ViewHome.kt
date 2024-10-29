package com.group1.notamonotako.activities.view_contents

import ApiService
import TokenManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.AccountManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.activities.extra_activities.CommentActivity

class ViewHome : AppCompatActivity() {
    private lateinit var btnLike : ImageButton
    private lateinit var btnDisLike : ImageButton
    private lateinit var btnComment : ImageButton
    private lateinit var flComment : FrameLayout
    private lateinit var btnback : ImageButton
    private lateinit var tvTitle : TextView
    private lateinit var tvContents : TextView
    private lateinit var tvDate : TextView
    private lateinit var tvLikeCount : TextView
    private lateinit var tvDisLikeCount : TextView
    private lateinit var tvCommentsCount : TextView
    private lateinit var soundManager: SoundManager
    private var ifLiked: Boolean = false
    private var ifDisliked: Boolean = false
    private var firstRun: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_home)
        AccountManager.init(this)
        TokenManager.init(this)

        btnback = findViewById(R.id.btnback)
        btnLike = findViewById(R.id.btnLike)
        btnDisLike = findViewById(R.id.btnDisLike)
        btnComment = findViewById(R.id.btnComment)
        tvTitle = findViewById(R.id.tvTitle)
        tvContents = findViewById(R.id.tvContents)
        tvDate = findViewById(R.id.tvDate)
        tvLikeCount = findViewById(R.id.tvLikeCount)
        tvDisLikeCount = findViewById(R.id.tvDisLikeCount)
        tvCommentsCount = findViewById(R.id.tvCommentsCount)
        soundManager = SoundManager(this) // Initialize SoundManager
        val isMuted = AccountManager.isMuted
        soundManager.updateMediaPlayerVolume(isMuted)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


        val noteId = intent.getIntExtra("note_id", -1)
        val title = intent.getStringExtra("title")
        val contents = intent.getStringExtra("contents")
        val public = intent.getBooleanExtra("public", false)
        val updatedAt = intent.getStringExtra("updated_at")
        firstRun = false
        //log all
        Log.d("ViewHometester", "noteId: $noteId, title: $title,  contents: $contents, public: $public, updatedAt: $updatedAt")
        tvTitle.text = title ?: "No title"
        tvContents.text = contents ?: "No contents"
        tvDate.text = updatedAt ?: "No date"
        fetchReactions(noteId)
        fetchSpecificNote(noteId)
        getCommentCount(noteId, tvCommentsCount)

        Log.d("ViewHome onCreate", "has_liked: $ifLiked, has_disliked: $ifDisliked")

        btnback.setOnClickListener{
            finish()
            soundManager.playSoundEffect()
        }

        btnLike.setOnClickListener {
            // Immediately update the state locally before making the API call
            ifLiked = !ifLiked
            ifDisliked = false // Reset dislike if like is clicked
            soundManager.playSoundEffect()
            updateLikeDislikeUI() // Update the UI immediately

            // Call the API to like the post
            likePostNotes(noteId)
        }

        btnDisLike.setOnClickListener {
            // Immediately update the state locally before making the API call
            ifDisliked = !ifDisliked
            ifLiked = false // Reset like if dislike is clicked
            soundManager.playSoundEffect()
            updateLikeDislikeUI() // Update the UI immediately

            // Call the API to dislike the post
            dislikePostNote(noteId)
        }

        btnComment.setOnClickListener {
            soundManager.playSoundEffect()
            // Change the button tint back to white
            btnComment.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

            // Navigate to CommentActivity directly
            val intent = Intent(this, CommentActivity::class.java)

            // Pass data to the CommentActivity if needed (e.g., note_id)
            intent.putExtra("note_id", noteId)  // Example of passing note_id

            commentActivityResultLauncher.launch(intent)

            // Add a transition animation between activities
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


    }
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release() // Release media player when done
    }


    private fun getCommentCount(noteId: Int, tvCommentsCount: TextView){
        lifecycleScope.launch {
            try{

                val token = TokenManager.getToken()
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = apiService.getCommentCountByNoteId("Bearer $token",noteId)

                if (response.isSuccessful) {
                    val commentCount = response.body()?.comment_count ?: 0
                    tvCommentsCount.text = commentCount.toString()
                    Log.d("countercomment", "Comment count: $commentCount")
                }

            }catch (e: Exception){
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "Exception: ${e.message}")
            }

        }
    }

    private val commentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // Check if the result is okay
        if (result.resultCode == RESULT_OK) {
            val noteId = intent.getIntExtra("note_id", -1)
            // Call getCommentCount to refresh the comment count after adding a comment
            getCommentCount(noteId, tvCommentsCount)
        }
    }

    private fun likePostNotes(noteId: Int){
        lifecycleScope.launch {
            try {

                val apiService = RetrofitInstance.create(ApiService::class.java)
                val token = TokenManager.getToken()
                val authToken = "Bearer $token"

                if (token != null) {
                    Log.d("ViewHome", "Token not null")
                    val response = apiService.likePost(authToken, noteId)

                    if (response.isSuccessful) {
                        // Handle successful like response
                        fetchReactions(noteId)
                        Log.d("ViewHome", "Post liked successfully.")
                    } else {
                        // Handle error response
                        Log.e("ViewHome", "Failed to like post: ${response.code()}")
                        Log.e("ViewHome", "Failed to like post: ${response.errorBody()?.string()} ")
                    }
                } else {
                    Toast.makeText(this@ViewHome, "Authentication Token is null!", Toast.LENGTH_SHORT).show()
                    Log.d("ViewHome", "Token is null")
                }
        } catch (e: Exception) {
            Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ViewHome", "Exception: ${e.message}")
        } catch (e: HttpException){
            Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ViewHome", "HttpException: ${e.message}")
            }
        }
    }

    private fun dislikePostNote(noteId: Int){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val token = TokenManager.getToken()
                val authToken = "Bearer $token"

                if (token != null) {
                    Log.d("ViewHome", "Token not null")
                    val response = apiService.dislikePost(authToken, noteId)

                    if (response.isSuccessful) {
                        fetchReactions(noteId)
                        Log.d("ViewHome", "Post dislike successfully.")
                    } else {
                        // Handle error response
                        Log.e("ViewHome", "Failed to dislike post: ${response.code()}")
                        Log.e("ViewHome", "Failed to dislike post: ${response.errorBody()?.string()} ")
                    }
                } else {
                    Log.d("ViewHome", "Token is null")
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "Exception: ${e.message}")
            } catch (e: HttpException){
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "HttpException: ${e.message}")
            }
        }
    }

    private fun fetchSpecificNote(noteId: Int){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val token = TokenManager.getToken()
                val authToken = "Bearer $token"
                val response = apiService.getSpecificNote(authToken, noteId)
                if (response.isSuccessful) {
                    val note = response.body()
                    if (note != null) {
                        ifLiked = note.has_liked
                        ifDisliked = note.has_disliked
                        updateLikeDislikeUI()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "Exception: ${e.message}")
            } catch (e: HttpException) {
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "HttpException: ${e.message}")
            }
        }
    }

    private fun updateLikeDislikeUI() {
        if (ifLiked) {
            btnLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mixcolor))
            btnDisLike.isClickable = false
        } else {
            btnLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            btnDisLike.isClickable = true
        }

        if (ifDisliked) {
            btnDisLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mixcolor))
            btnLike.isClickable = false
        } else {
            btnDisLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            btnLike.isClickable = true
        }
    }

    private fun fetchReactions(noteId: Int){
        lifecycleScope.launch {
            try {
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = apiService.getReactions(noteId)
                if (response.isSuccessful) {
                    val reactions = response.body()
                    if (reactions != null) {
                        val likes = reactions.likes
                        val dislikes = reactions.dislikes
                        tvLikeCount.text = likes.toString()
                        tvDisLikeCount.text = dislikes.toString()
                        Log.d("ViewHome", "Likes: $likes, Dislikes: $dislikes")
                    }
                } else {
                    Log.e("ViewHome", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "Exception: ${e.message}")
            } catch (e: HttpException){
                Toast.makeText(this@ViewHome, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewHome", "HttpException: ${e.message}")
            }
        }
    }

}




