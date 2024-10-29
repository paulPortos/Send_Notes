package com.group1.notamonotako.activities.extra_activities

import ApiService
import TokenManager.clearToken
import TokenManager.getToken
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.group1.notamonotako.R
import com.group1.notamonotako.api.AccountManager
import com.group1.notamonotako.activities.auth_activity.SignInActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var progressBar : ProgressBar
    private lateinit var btnsign_out : AppCompatButton
    private lateinit var sounds : Switch
    private lateinit var btnBack : ImageButton
    private lateinit var etemail : TextView
    private lateinit var etusername : TextView
    private lateinit var btnChangePassword : ImageButton
    private lateinit var btnAbout : ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        AccountManager.init(this)
        setContentView(R.layout.activity_settings)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnsign_out = findViewById(R.id.btnsign_out)
        btnBack = findViewById(R.id.btnBack)
        sounds = findViewById(R.id.sounds)
        progressBar = findViewById(R.id.progressBar)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        mediaPlayer = MediaPlayer.create(this,R.raw.soundeffects1)
        etemail = findViewById(R.id.tvemail2)
        etusername = findViewById(R.id.tvusername2)
        btnAbout = findViewById(R.id.btnAbout)

        // Making the progressbar Invisible
        progressBar.visibility = View.INVISIBLE

        btnChangePassword.setOnClickListener {
            mediaPlayer.start()
            val intent = Intent(this@SettingsActivity, ChangePassword::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
          
            progressBar.visibility = View.VISIBLE
            finish() // Remove this activity from the stack when going back
            mediaPlayer.start()


        }

        btnAbout.setOnClickListener {
            val intent = Intent(this@SettingsActivity, AboutActivity::class.java)
            startActivity(intent)
            progressBar.visibility = View.VISIBLE
            finish() // Remove this activity from the stack when going back
            mediaPlayer.start()

        }

        val soundIsMuted = AccountManager.isMuted

        // Set the initial state of the switch and media player volume
        sounds.isChecked = !soundIsMuted
        updateMediaPlayerVolume(soundIsMuted)

        // Handle switch toggle to save sound preference and update media volume
        sounds.setOnCheckedChangeListener { _, isChecked ->
            AccountManager.toggleSound()
            updateMediaPlayerVolume(AccountManager.isMuted)
        }

        etemail.text = AccountManager.getEmail()
        etusername.text = AccountManager.getUsername()

        btnsign_out.setOnClickListener {
            logoutUser()
            mediaPlayer.start()
        }
    }
    // Save the sound preference (muted or not) in SharedPreferences
    private fun updateMediaPlayerVolume(isMuted: Boolean) {
        if (isMuted) {
            mediaPlayer.setVolume(0F, 0F)
        } else {
            mediaPlayer.setVolume(1F, 1F)
        }
    }

    private fun logoutUser() {
        btnsign_out.isClickable = true
        val token = getToken() ?: run {
            Toast.makeText(this@SettingsActivity, "No token found", Toast.LENGTH_SHORT).show()
            return
        }
        val apiService = RetrofitInstance.create(ApiService::class.java)
        val call = apiService.logout("Bearer $token")

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    clearToken()
                    Toast.makeText(
                        this@SettingsActivity,
                        "Logged Out Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    progressBar.visibility = View.VISIBLE
                    btnsign_out.isClickable = false
                    
                    finish()

                } else {
                    progressBar.visibility = View.INVISIBLE
                    btnsign_out.isClickable = true

                    Toast.makeText(
                        this@SettingsActivity,
                        "Error: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                progressBar.visibility = View.INVISIBLE
                btnsign_out.isClickable = true
                Toast.makeText(
                    this@SettingsActivity,
                    "Network error occurred: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Remove this activity from the stack when back button is pressed
    }
}
