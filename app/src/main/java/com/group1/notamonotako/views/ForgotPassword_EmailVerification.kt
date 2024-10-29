package com.group1.notamonotako.views

import ApiService
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.group1.notamonotako.R
import com.group1.notamonotako.api.ResetOtp
import com.group1.notamonotako.api.requests_responses.forgetPassword.forgot_Password
import kotlinx.coroutines.launch
import java.io.IOException

class ForgotPassword_EmailVerification : AppCompatActivity() {
    private lateinit var btnBack : ImageButton
    private lateinit var etEmail : EditText
    private lateinit var etOTP : EditText
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var btnGetOTP : AppCompatButton
    private lateinit var btnConfirmVerification :AppCompatButton
    private lateinit var progressBar: ProgressBar
    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())

    // Variable to store the token received from the server
    private var OTP: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_email_verification)

        ResetOtp.init(this)

        etEmail = findViewById(R.id.etEmail)
        etOTP = findViewById(R.id.etOTP)
        textInputLayout = findViewById(R.id.textInputLayout)
        btnGetOTP = findViewById(R.id.btnGetOTP)
        btnBack = findViewById( R.id.btnBack)
        btnConfirmVerification = findViewById(R.id.btnConfirmVerification)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE
        progressBar.max = 5  // Set max value to 5 for 5 seconds
        textInputLayout.visibility = View.INVISIBLE


        btnGetOTP.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                btnConfirmVerification.isClickable = true
                val email = etEmail.text.toString().trim()
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val forgotPassword = forgot_Password(email = email)
                // Store the token received from the server

                if (email.isEmpty()){
                    Toast.makeText(this@ForgotPassword_EmailVerification, "Please enter your email", Toast.LENGTH_SHORT).show()
                    btnConfirmVerification.isClickable = true
                    progressBar.visibility = View.GONE


                }else{
                    try {

                        val response = apiService.forgotPassword(forgotPassword)
                        val responseBody = response.body()

                        OTP = responseBody?.OTP
                        OTP?.let { OtpForget ->
                            ResetOtp.saveForgotOTP(OtpForget)
                            ResetOtp.saveEmail(email)
                        }

                        if(response.isSuccessful){
                            btnGetOTP.isClickable = false
                            Toast.makeText(this@ForgotPassword_EmailVerification, "OTP sent", Toast.LENGTH_SHORT).show()
                            textInputLayout.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE

                            OTPcountdown()
                        }else if(response.code() == 404){
                            btnConfirmVerification.isClickable = true
                            progressBar.visibility = View.GONE

                            Toast.makeText(this@ForgotPassword_EmailVerification, "email not found", Toast.LENGTH_SHORT).show()
                        }else{
                            btnConfirmVerification.isClickable = true
                            progressBar.visibility = View.GONE

                            Toast.makeText(this@ForgotPassword_EmailVerification, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }   catch (e: IOException){
                        btnConfirmVerification.isClickable = true
                        progressBar.visibility = View.GONE


                        Toast.makeText(this@ForgotPassword_EmailVerification, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        btnConfirmVerification.setOnClickListener{
            val otp = etOTP.text.toString()

            if (otp.isEmpty()){
                Toast.makeText(this@ForgotPassword_EmailVerification, "Please enter your OTP", Toast.LENGTH_SHORT).show()
            }else{
               if(otp == OTP){
                   btnConfirmVerification.isClickable = false
                   val intent = Intent(this@ForgotPassword_EmailVerification, ForgotPassword_ResetPassword::class.java)
                   startActivity(intent)

               }else{
                   Toast.makeText(this@ForgotPassword_EmailVerification, "Invalid OTP, please try again", Toast.LENGTH_SHORT).show()
               }
            }

        }

        btnBack.setOnClickListener {
            val intent = Intent(this@ForgotPassword_EmailVerification, SignInActivity::class.java)
            startActivity(intent)
        }

    }
    private fun OTPcountdown(){
        btnGetOTP.isEnabled = false
        object : CountDownTimer(60000,1000){
            override fun onTick(millisUntilFinished : Long) {
                btnGetOTP.text = "${millisUntilFinished / 1000} seconds "
                progressBar.visibility = View.GONE


            }
            override fun onFinish() {
                btnGetOTP.text = "GET OTP "
                btnGetOTP.isEnabled = true
                btnGetOTP.isClickable = true
                textInputLayout.visibility = View.INVISIBLE

            }
        }.start()

    }


}