package com.group1.notamonotako.api

import android.content.Context
import android.content.SharedPreferences

object ResetOtp {
    private const val PREFS_NAME = "user_prefs"
    private const val EMAIL_KEY = "email"
    private const val OTP_KEY = "otp"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveForgotOTP(Otp: String) {
        preferences.edit().putString(OTP_KEY, Otp).apply()
    }


    fun getOTP(): String? {
        val token = preferences.getString(OTP_KEY, null)
        return token
    }

    fun saveEmail(email:String){
        preferences.edit().putString(EMAIL_KEY, email).apply()
    }

    fun getEmail():String?{
        val email = preferences.getString(EMAIL_KEY, null)
        return email
    }

    fun clearToken() {
        preferences.edit().remove(OTP_KEY).apply()
    }

}