    package com.group1.notamonotako.activities.view_contents
    
    import ApiService
    import TokenManager
    import android.content.Intent
    import android.content.pm.ActivityInfo
    import android.os.Bundle
    import android.util.Log
    import android.view.GestureDetector
    import android.view.MotionEvent
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.lifecycle.lifecycleScope
    import com.group1.notamonotako.R
    import com.group1.notamonotako.api.AccountManager
    import com.group1.notamonotako.api.SoundManager
    import com.group1.notamonotako.api.requests_responses.flashcards.UpdateFlashcards
    import com.group1.notamonotako.activities.main_activity.HomeActivity
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import retrofit2.HttpException
    
    class ViewFlashcards : AppCompatActivity() {
        private var contentsList: MutableList<String> = mutableListOf()
        private var currentIndex: Int = 0
        private lateinit var timestamp: TextView
        private lateinit var title: EditText
        private lateinit var contents: EditText
        private lateinit var btnCheck: ImageButton //
        private lateinit var btnBack: ImageButton //
        private lateinit var btnDelete: ImageButton
        private lateinit var gestureDetector: GestureDetector
        private lateinit var soundManager: SoundManager

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_view_flashcards)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            title = findViewById(R.id.title)
            contents = findViewById(R.id.contents)
            btnCheck = findViewById(R.id.btn_check)
            btnBack = findViewById(R.id.btn_back)
            timestamp = findViewById(R.id.timestamp)
            btnDelete = findViewById(R.id.btn_delete)


            soundManager = SoundManager(this) // Initialize SoundManager
            val isMuted = AccountManager.isMuted
            soundManager.updateMediaPlayerVolume(isMuted)

            val flashcardsId = intent.getIntExtra("flashcard_id", -1)
            val flashcardTitle = intent.getStringExtra("title")
            contentsList = intent.getStringArrayListExtra("cards")?.toMutableList() ?: mutableListOf()
            val flashcardsUpdatedAt = intent.getStringExtra("updated_at")
            // Set the timestamp text
            timestamp.text = flashcardsUpdatedAt

            //Log the contentsList
            Log.d("ViewFlashcards", "Contents List: $contentsList")
    
            if (flashcardTitle != null) {
                title.setText(flashcardTitle) // Set the title if it's available
                contents.setText(contentsList[currentIndex]) // Set the first content
            } else {
                Log.e("ViewFlashcards", "No title received in intent")
            }

            gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,  // Make both e1 and e2 nullable
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 != null && e2 != null) {  // Safely check if both are non-null
                        val diffX = e2.x - e1.x
                        val diffY = e2.y - e1.y
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            // Detect left or right swipe
                            if (diffX > 0) {
                                if (currentIndex > 0) {  // Ensure currentIndex doesn't go below 0
                                    currentIndex--
                                    // Move to the previous index
                                    contents.setText(contentsList[currentIndex])  // Show the previous item for editing
                                } else {
                                    Toast.makeText(this@ViewFlashcards, "No previous item.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val content = contents.text.toString()
                                if (content.isNotBlank()) {
                                    if (currentIndex == contentsList.size) {
                                        // If currentIndex is at the end, add a new content item
                                        addToContentsList(content)
                                        contents.text.clear()  // Clear the input field after adding
                                        currentIndex++  // Move to the next index
                                    } else if (currentIndex < contentsList.size) {
                                        // If currentIndex is still within the list, move to the next item
                                        contentsList[currentIndex] = content  // Update the current item with new content
                                        currentIndex++  // Move to the next index
                                        if (currentIndex < contentsList.size) {
                                            contents.setText(contentsList[currentIndex])  // Load the next item for editing
                                        } else {
                                            contents.text.clear()  // Clear the input if no more items
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@ViewFlashcards, "Card is blank.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            return true
                        }
                    }
                    return false
                }
            })
            contents.setOnTouchListener { v, event ->
                gestureDetector.onTouchEvent(event)
                false  // Return false so EditText retains its default behavior (text editing)
            }

            btnCheck.setOnClickListener {
                soundManager.playSoundEffect()

                // Ensure to update the current index in the contentsList
                val currentContent = contents.text.toString().trim()
                if (currentIndex < contentsList.size) {
                    contentsList[currentIndex] = currentContent // Update the list with the new content
                }
                //log flashcards id
                Log.d("ViewFlashcards", "Flashcards ID: $flashcardsId")
                if(flashcardsId != -1){
                    val updatedTitle = title.text.toString()
                    val updatedContentList: MutableList<String> = contentsList.map { it.trim() }.toMutableList()
                    Log.d("ViewFlashcards", "Updated Title: $updatedTitle")
                    Log.d("ViewFlashcards", "Updated Content List: $updatedContentList")
                    updateFlashcards(flashcardsId, updatedTitle, updatedContentList)
                }
              finish()
            }

            btnBack.setOnClickListener {
                soundManager.playSoundEffect()

                finish()
            }

            btnDelete.setOnClickListener {
                soundManager.playSoundEffect()

                if(flashcardsId != -1){
                    deleteFlashcards(flashcardsId)
                }
            }
        }

        private fun updateFlashcards(flashcardsId: Int, updatedTitle: String, updatedContentList: MutableList<String>){
            val token = TokenManager.getToken()
    
            if (updatedContentList.isEmpty()) {
                Toast.makeText(this, "Content list is empty.", Toast.LENGTH_SHORT).show()
                return
            }
            if (updatedTitle.isEmpty()) {
                Toast.makeText(this, "Title is empty.", Toast.LENGTH_SHORT).show()
                return
            }
    
            val flashcardRequest = UpdateFlashcards(
                title = updatedTitle,
                cards = updatedContentList.toMutableList() // This is your List<String> or MutableList<String>
            )
    
            Log.d("UpdateFlashcards", "Token: $token, Flashcards ID: $flashcardsId")
            Log.d("UpdateFlashcards", "Updated Title: $updatedTitle")
            Log.d("UpdateFlashcards", "Updated Content List: $updatedContentList")
    
            lifecycleScope.launch{
                try {
                    val apiService = RetrofitInstance.create(ApiService::class.java)
                    val response = withContext(Dispatchers.IO){
                        apiService.updateFlashcards("Bearer $token", flashcardsId, flashcardRequest)
                    }
                    if (response.isSuccessful){
                        Log.d("UpdateFlashcards", "Response Code: ${response.code()}")
                        Toast.makeText(this@ViewFlashcards, "Flashcards updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ViewFlashcards, "Failed to update flashcards: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("UpdateFlashcards", "Error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ViewFlashcards, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateFlashcards", "Error: ${e.message}", e)
                } catch (e: HttpException) {
                    Toast.makeText(this@ViewFlashcards, "HTTP Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateFlashcards", "HTTP Error: ${e.message()}", e)
                }
            }
        }
        private fun deleteFlashcards(flashcardsId: Int){
            val token = TokenManager.getToken()
            if (token == null) {
                Toast.makeText(this, "Authorization token missing", Toast.LENGTH_SHORT).show()
                return
            }
            lifecycleScope.launch {
                try {
                    val apiService = RetrofitInstance.create(ApiService::class.java)
                    val response = withContext(Dispatchers.IO) {
                        apiService.deleteFlashcards("Bearer $token", flashcardsId)
                        }
                        if (response.isSuccessful) {
                            Toast.makeText(this@ViewFlashcards, "Flashcards deleted successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ViewFlashcards, HomeActivity::class.java)
                            intent.putExtra("showFlashcardFragment", true)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@ViewFlashcards, "Failed to delete flashcards: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteFlashcards", "Error: ${response.code()}")
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ViewFlashcards, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteFlashcards", "Error: ${e.message}", e)
                } catch (e: HttpException) {
                    e.printStackTrace()
                    Toast.makeText(this@ViewFlashcards, "HTTP Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteFlashcards", "HTTP Error: ${e.message()}", e)
                }
            }
        }
        private fun addToContentsList(contents: String) {
            contentsList.add(contents)
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            event?.let {
                gestureDetector.onTouchEvent(it)
            }
            return super.onTouchEvent(event)
        }
        override fun onDestroy() {
            super.onDestroy()
            soundManager.release() // Release media player when done
        }
    }
