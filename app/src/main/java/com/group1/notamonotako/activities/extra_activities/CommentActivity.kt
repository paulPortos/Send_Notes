package com.group1.notamonotako.activities.extra_activities

import ApiService
import TokenManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.group1.notamonotako.R
import com.group1.notamonotako.adapter.CommentsAdapter
import com.group1.notamonotako.api.AccountManager
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.comments.CommentPostRequest
import kotlinx.coroutines.launch

class CommentActivity : AppCompatActivity() {
    private lateinit var etAddComment: EditText
    private lateinit var rvcomments: RecyclerView
    private lateinit var tvComments: TextView
    private lateinit var btnClose: ImageButton
    private lateinit var commentAdapter: CommentsAdapter
    private lateinit var soundManager: SoundManager
    private lateinit var swiperefresh : SwipeRefreshLayout
    private lateinit var NoComment: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        etAddComment = findViewById(R.id.etAddComment)
        rvcomments = findViewById(R.id.rvcomments)
        tvComments = findViewById(R.id.tvComments)
        btnClose = findViewById(R.id.btnClose)
        swiperefresh = findViewById(R.id.swipeRefreshLayout)
        rvcomments.setHasFixedSize(true)
        rvcomments.layoutManager = LinearLayoutManager(this)
        NoComment = findViewById(R.id.NoComment)
        fetchComments()

        soundManager = SoundManager(this) // Initialize SoundManager
        val isMuted = AccountManager.isMuted
        soundManager.updateMediaPlayerVolume(isMuted)

        btnClose.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("new_comment", false) // Indicate no new comment was added
            setResult(RESULT_OK, resultIntent)
            // Navigate to CommentActivity directly
            finish()
            soundManager.playSoundEffect()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        swiperefresh.setOnRefreshListener {
            fetchComments()
            swiperefresh.isRefreshing = false
        }

        etAddComment.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Check if the drawableEnd was clicked
                if (event.rawX >= (etAddComment.right - etAddComment.compoundDrawables[2].bounds.width())) {
                    // Handle submission of the edit text content
                    soundManager.playSoundEffect()
                    submitComment(etAddComment.text.toString())
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun fetchComments() {
        val noteId = intent.getIntExtra("note_id", -1)
        val token = TokenManager.getToken()  // Assuming you store the token here

        if (noteId == -1 || token == null) {
            Toast.makeText(this, "Invalid note or missing token.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch comments from the API
        lifecycleScope.launch {
            try {
                NoComment.visibility = TextView.GONE
                rvcomments.visibility = RecyclerView.VISIBLE
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = apiService.getComment(noteId)

                if (response.isSuccessful) {
                    val commentsList = response.body()

                    if (commentsList != null && commentsList.isNotEmpty()) {
                        // Passing `this@CommentActivity` as the `lifecycleOwner`
                        val commentsAdapter = CommentsAdapter(this@CommentActivity, commentsList, this@CommentActivity)
                        rvcomments.adapter = commentsAdapter
                        rvcomments.visibility = RecyclerView.VISIBLE
                        NoComment.visibility = TextView.GONE
                    } else {
                        rvcomments.visibility = RecyclerView.GONE
                        NoComment.visibility = TextView.VISIBLE
                    }
                } else {
                    Log.e("FetchCommentsError", "Failed to fetch comments: ${response.code()}")
                    Toast.makeText(this@CommentActivity, "Failed to fetch comments.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FetchCommentsException", "Error: ${e.message}")
                Toast.makeText(this@CommentActivity, "Error fetching comments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitComment(comment: String) {
        if (comment.isNotBlank()) {
            val noteId = intent.getIntExtra("note_id", -1)
            val username = AccountManager.getUsername().toString() // This should be dynamic
            val token = TokenManager.getToken() // Assume token is stored in TokenManager

            if (token == null) {
                Toast.makeText(this, "Authorization token missing!", Toast.LENGTH_SHORT).show()
                return
            } else {
                lifecycleScope.launch {
                    try {
                        val apiService = RetrofitInstance.create(ApiService::class.java)
                        val commentRequest = CommentPostRequest(username = username, notes_id = noteId, comment = comment)
                        val response = apiService.postComment("Bearer $token", commentRequest)

                        if (response.isSuccessful) {
                            // Handle successful comment submission
                            etAddComment.text?.clear()
                            fetchComments() // Refresh comments
                        } else {
                            // Log and handle API failure
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("CommentError", "Failed to submit comment: $errorBody")
                            Toast.makeText(this@CommentActivity, "Failed to submit comment: $errorBody", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Handle network or other errors
                        Log.e("CommentException", "Error: ${e.message}")
                        Toast.makeText(this@CommentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please input your comment.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release() // Release media player when done
    }
    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putExtra("new_comment", false) // Indicate no new comment was added
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed() // Call the default back button behavior
    }
}
