package com.group1.notamonotako.activities.auth_activity

import ApiService
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.requests_responses.signup.RegisterRequests
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignUpActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnLoginNow: Button
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp : AppCompatButton
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var btnDone : AppCompatButton
    private lateinit var flEmail : FrameLayout
    private lateinit var tvVerify : TextView
    private lateinit var  ivEmail : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        setContentView(R.layout.activity_sign_up)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnLoginNow = findViewById(R.id.btnSignUpNow)
        btnSignIn = findViewById(R.id.btnSignIn)
        etEmail = findViewById(R.id.etEmail)
        btnDone = findViewById(R.id.btnDone)
        flEmail = findViewById(R.id.flEmailVerify)
        ivEmail = findViewById(R.id.ivEmail)
        tvVerify = findViewById(R.id.tvVerify)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE


        // Start the 5-second loop

        btnLoginNow.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "Fill up all fields", Toast.LENGTH_SHORT).show()
            } else if (password == confirmPassword) {
                if (username.length >= 5 && password.length >= 8) {
                    registerUser(username, email, password)
                    flEmail.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                } else if (username.length < 5) {
                    Toast.makeText(this@SignUpActivity, "Username must be at least 5 characters", Toast.LENGTH_SHORT).show()
                } else if (password.length < 8) {
                    Toast.makeText(this@SignUpActivity, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@SignUpActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        btnDone.setOnClickListener {
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun registerUser(username: String, email: String, password: String){
        lifecycleScope.launch {
            val apiService = RetrofitInstance.create(ApiService::class.java)
            val registerRequest = RegisterRequests(username = username, email = email, password = password)
            try {
                val response = apiService.register(registerRequest)

                if (response.isSuccessful) {
                    val registrationResponse = response.body()
                    Log.d("Registration", "Email sent successfully: $registrationResponse")
                    startProgressBarLoop()

                    btnLoginNow.isClickable = false
                } else if (response.code() == 410){
                    Log.d("Registration", "Registration failed: $response")
                    flEmail.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@SignUpActivity, "username already exists. Try another username", Toast.LENGTH_SHORT).show()
                    btnLoginNow.isClickable = true
                } else if (response.code() == 409){
                    Log.d("Registration", "Registration failed: $response")
                    flEmail.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@SignUpActivity, "email already exists. Try another email", Toast.LENGTH_SHORT).show()
                    btnLoginNow.isClickable = true

                } else if(response.code() == 422){
                    flEmail.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    Log.e("Invalid_Email", "Registration failed with code: ${response.code()} ${response.message()}")
                    Toast.makeText(this@SignUpActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
                    btnLoginNow.isClickable = true
                } else{
                    flEmail.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    Log.e("Registration", "Registration failed with code: ${response.code()} ${response.message()}")
                    Toast.makeText(this@SignUpActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                    btnLoginNow.isClickable = true
                }
            } catch(e: Exception) {
                flEmail.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                Log.e("Registrationex", "Registration failed", e)
                Toast.makeText(this@SignUpActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                btnLoginNow.isClickable = true
            } catch (e: HttpException) {
                flEmail.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                Log.e("Registrationhttp", "Registration failed", e)
                Toast.makeText(this@SignUpActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                btnLoginNow.isClickable = true
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    //block the back button of the phone
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun startProgressBarLoop() {

        Toast.makeText(this@SignUpActivity, "Email sent successfully", Toast.LENGTH_SHORT).show()

                   // Hide the progress bar after 5 seconds

                    progressBar.visibility = View.GONE  // Hide the progress bar after 5 seconds
                    tvVerify.visibility = View.VISIBLE
                    btnDone.visibility = View.VISIBLE
                    ivEmail.visibility = View.VISIBLE
                }
            }

