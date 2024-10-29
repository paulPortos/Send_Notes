package com.group1.notamonotako.activities.extra_activities

import ApiService
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.ResetOtp
import com.group1.notamonotako.api.requests_responses.forgetPassword.reset_Password
import com.group1.notamonotako.activities.auth_activity.SignInActivity
import kotlinx.coroutines.launch

class ForgotPassword_ResetPassword : AppCompatActivity() {
    private lateinit var btnBack : ImageButton
    private lateinit var etPassword :EditText
    private lateinit var etConfirmPassword : EditText
    private lateinit var btnConfirmResetPassword : AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_reset_password)

        ResetOtp.init(this)
        btnBack = findViewById( R.id.btnBack)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById( R.id.etConfirmPassword)
        btnConfirmResetPassword = findViewById(R.id.btnConfirmResetPassword)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        btnBack.setOnClickListener {
            val intent = Intent(this@ForgotPassword_ResetPassword, SignInActivity::class.java)
            startActivity(intent)
        }

        btnConfirmResetPassword.setOnClickListener {
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val OTP = ResetOtp.getOTP().toString()
            val email = ResetOtp.getEmail().toString()
            btnConfirmResetPassword.isClickable = true

            if(password.isEmpty()) {
                Toast.makeText(this@ForgotPassword_ResetPassword, "Please enter your password", Toast.LENGTH_SHORT).show()
                btnConfirmResetPassword.isClickable = true

            } else if(password == confirmPassword) {
                try {
                    lifecycleScope.launch {
                        val apiService = RetrofitInstance.create(ApiService::class.java)
                        val newPassword = reset_Password(password = password, token = OTP, email = email)
                        val response = apiService.resetPassword(newPassword)

                        if(response.code()==400){
                            Toast.makeText(this@ForgotPassword_ResetPassword, "Your new password cannot be the same as the old password", Toast.LENGTH_SHORT).show()
                        }else{
                            if(response.isSuccessful) {
                                Toast.makeText(this@ForgotPassword_ResetPassword, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                btnConfirmResetPassword.isClickable = false
                                // Navigate to SignInActivity
                                val intent = Intent(this@ForgotPassword_ResetPassword, SignInActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Prevent going back
                                startActivity(intent)

                        }}

                    }
                } catch (e: Exception) {
                    btnConfirmResetPassword.isClickable = true
                    Toast.makeText(this@ForgotPassword_ResetPassword, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnConfirmResetPassword.isClickable = true
                Toast.makeText(this@ForgotPassword_ResetPassword, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }


    }
}