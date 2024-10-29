package com.group1.notamonotako.views

import ApiService
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.api.AccountManager
import com.group1.notamonotako.api.requests_responses.signin.Login
import com.group1.notamonotako.api.requests_responses.signin.User
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SignInActivity : AppCompatActivity() {
    private lateinit var btnSignup: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnForgot: Button
    private lateinit var btnLoginNow: Button
    private lateinit var progressBar : ProgressBar
    private lateinit var btnSignIn : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.init(this)
        AccountManager.init(this)

        if (TokenManager.isLoggedIn()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_signin)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


        progressBar = findViewById(R.id.progressBar)

        btnSignup = findViewById(R.id.btnSignUp)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnForgot = findViewById(R.id.btnForgotPassword)
        btnLoginNow = findViewById(R.id.btnSignInNow)
        btnSignIn = findViewById(R.id.btnSignIn)






        progressBar.visibility = View.INVISIBLE

        btnLoginNow.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            //Log username and password

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@SignInActivity, "Fill up all fields", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                loginUser(email, password)
            }
        }

        btnSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            progressBar.visibility = View.VISIBLE
        }
        btnForgot.setOnClickListener {
            val intent = Intent(this@SignInActivity, ForgotPassword_EmailVerification::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            btnLoginNow.isClickable = true
            val apiService = RetrofitInstance.create(ApiService::class.java)
            val loginRequest = Login(email = email, password = password)
            //Log login request

            try {
                val response = apiService.login(loginRequest)

                if(response.code() == 404){
                    Toast.makeText(this@SignInActivity, "Email doesn't exist, Please check your email", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                    btnLoginNow.isClickable = true

                }else if(response.code() == 401){
                    Toast.makeText(this@SignInActivity,"Incorrect Password",Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                    btnLoginNow.isClickable = true

                }else if(response.code() ==403){
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@SignInActivity,"Email not yet verify",Toast.LENGTH_SHORT).show()
                }else{
                    if (response.isSuccessful) {
                        response.body()?.let { loginResponse ->
                           //gets the Token,email and username to the managers

                            TokenManager.saveToken(loginResponse.token)
                            AccountManager.saveEmail(loginResponse.user.email)
                            AccountManager.saveUsername(loginResponse.user.username)
                            AccountManager.saveUserId(loginResponse.user.id)

                            Toast.makeText(this@SignInActivity, "Logged In", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                            btnLoginNow.isClickable = false
                        }
                    } else {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@SignInActivity, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", response.errorBody().toString())
                    }
                }
            } catch (e: HttpException) {
                progressBar.visibility = View.INVISIBLE
                btnLoginNow.isClickable = true
                Toast.makeText(this@SignInActivity, "HTTP error: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@SignInActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("SignInActivity", e.message.toString())
                btnLoginNow.isClickable = true
            }
        }
    }

    override fun onBackPressed(){
        finishAffinity()
    }

}
