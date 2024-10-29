package com.group1.notamonotako.views

import ApiService
import RetrofitInstance
import TokenManager
import TokenManager.clearToken
import TokenManager.getToken
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.ChangePass.ChangePasswordRequest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException

class ChangePassword : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var etOldPassword: EditText
    private lateinit var btnConfirmResetPassword: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        TokenManager.init(this)

        val soundManager = SoundManager(this) // Initialize SoundManager
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)



        if (!TokenManager.isLoggedIn()) {
            // If not logged in, redirect to SignInActivity
            val intent = Intent(this@ChangePassword, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()  // Close the HomeActivity
            return  // Prevent further execution
        }

        btnBack = findViewById(R.id.btnBack)
        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnConfirmResetPassword = findViewById(R.id.btnConfirmResetPassword)

        btnBack.setOnClickListener {
             finish()
            soundManager.playSoundEffect()
        }

        btnConfirmResetPassword.setOnClickListener {
            soundManager.playSoundEffect()
            val oldPass = etOldPassword.text.toString()
            val newPass = etNewPassword.text.toString()
            val confirmpass = etConfirmNewPassword.text.toString()
            if (newPass != confirmpass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }else if(oldPass.isEmpty() && newPass.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                changePassword(oldPass, newPass)
            }


        }
    }

    private fun changePassword(oldPass: String, newPass: String) {

        if (oldPass == newPass) {
            Toast.makeText(this@ChangePassword, "New password cannot be the same as the old password", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                btnConfirmResetPassword.isClickable = true
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = apiService.PassChange(ChangePasswordRequest(oldPass, newPass))

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ChangePassword,
                        "Password changed successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnConfirmResetPassword.isClickable = false

                    logoutUser()
                    Log.d("ChangePassword", "Success: ${response.body()?.message}")


                } else {
                    // Handle the error
                    val errorBody = response.errorBody()?.string()
                    if(response.code() == 422){
                        btnConfirmResetPassword.isClickable = true

                        // Specific message when old password doesn't match
                        Toast.makeText(
                            this@ChangePassword,
                            "Old password is incorrect. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        btnConfirmResetPassword.isClickable = true

                        Log.e("ChangePassword", "Error: $errorBody")
                        Toast.makeText(
                            this@ChangePassword,
                            "Error: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()}

                }
            } catch (e: HttpException) {
                btnConfirmResetPassword.isClickable = true

                Log.e("ChangePassword", "HttpException: ${e.message}")
                Toast.makeText(this@ChangePassword, "Something went wrong", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                btnConfirmResetPassword.isClickable = true

                Log.e("ChangePassword", "Exception: ${e.message}")
                Toast.makeText(this@ChangePassword, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun logoutUser() {
        val token = getToken() ?: run {
            Toast.makeText(this@ChangePassword, "No token found", Toast.LENGTH_SHORT).show()
            return
        }
        val apiService = RetrofitInstance.create(ApiService::class.java)
        val call = apiService.logout("Bearer $token")

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    clearToken()
                    val intent = Intent(this@ChangePassword, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {

                    Toast.makeText(
                        this@ChangePassword,
                        "Error: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {

                Toast.makeText(
                    this@ChangePassword,
                    "Network error occurred: ${t.message}",
                    Toast.LENGTH_SHORT

                ).show()
                Log.d("SettingsActivity", "Network error occurred: ${t.message}")
            }
        })
    }


}
