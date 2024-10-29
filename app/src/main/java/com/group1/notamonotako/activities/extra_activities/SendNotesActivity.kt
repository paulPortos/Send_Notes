package com.group1.notamonotako.activities.extra_activities

import ApiService
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.group1.notamonotako.R
import com.group1.notamonotako.adapter.SendNotesAdapter
import com.group1.notamonotako.api.SoundManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SendNotesActivity : AppCompatActivity() {
    private lateinit var btnClose : ImageButton
    private lateinit var rvSendNotes: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var swipeRefreshSendMail : SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)

        val soundManager = SoundManager(this) // Initialize SoundManager

        btnClose = findViewById(R.id.btnClose)
        rvSendNotes = findViewById(R.id.rvSendNotes)
       swipeRefreshSendMail = findViewById(R.id.swipeRefresherMail)
        rvSendNotes.layoutManager = LinearLayoutManager(this)

        btnClose.setOnClickListener {
            soundManager.playSoundEffect()
            finish()
        }
        swipeRefreshSendMail.setOnRefreshListener {
            fetchSentNotes()
            swipeRefreshSendMail.isRefreshing = false
        }

        fetchSentNotes()
        rvSendNotes.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
    }
    private fun fetchSentNotes() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getToken()
                val bearerToken = "Bearer $token"
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = apiService.viewSentNotes(bearerToken)

                if (response.isSuccessful) {
                    val notes = response.body()
                    Log.i("SendNotesActivity", "Response successful!")
                    if (notes != null) {
                        val adopter = SendNotesAdapter(this@SendNotesActivity, notes)
                        rvSendNotes.adapter = adopter
                    }
                } else {
                    Log.e("SendNotesActivity", "Error: ${response.code()}")
                    Log.e("SendNotesActivity", "Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@SendNotesActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SendNotesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SendNotesActivity", "Exception: ${e.message}")
            } catch (e: HttpException) {
                Toast.makeText(this@SendNotesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SendNotesActivity", "HttpException: ${e.message}")
            }
        }
    }
}