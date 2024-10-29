package com.group1.notamonotako.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object AccountManager {
    private const val PREFS_NAME = "user_prefs"
    private const val EMAIL_KEY = "email"
    private const val USERNAME_KEY = "username"
    private const val USER_ID_KEY = "user_id" // Add key for user ID
    private const val SOUND_MUTED_KEY_PREFIX = "sound_muted_" // Prefix for sound muted state by user
    private lateinit var preferences: SharedPreferences
    // Initialize the AccountManager with context
    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    // Save the user's email
    fun saveEmail(email: String) {
        Log.d("AccountManager", "Saving email: $email")
        preferences.edit().putString(EMAIL_KEY, email).apply()
    }
    // Save the user's username
    fun saveUsername(username: String) {
        Log.d("AccountManager", "Saving username: $username")
        preferences.edit().putString(USERNAME_KEY, username).apply()
    }
    fun saveUserId(userId: Int) {
        Log.d("AccountManager", "Saving user ID: $userId")
        preferences.edit().putInt(USER_ID_KEY, userId).apply()
    }
    fun getUserId(): Int? {
        return if (preferences.contains(USER_ID_KEY)) {
            preferences.getInt(USER_ID_KEY, -1)
        } else {
            null
        }
    }
    // Retrieve the stored email
    fun getEmail(): String? {
        val email = preferences.getString(EMAIL_KEY, null)
        Log.d("AccountManager", "Retrieved email: $email")
        return email
    }
    // Retrieve the stored username
    fun getUsername(): String? {
        val username = preferences.getString(USERNAME_KEY, null)
        Log.d("AccountManager", "Retrieved username: $username")
        return username
    }
    private fun getSoundMutedKeyForUser(userId: Int?): String {
        return "$SOUND_MUTED_KEY_PREFIX$userId"
    }

    // Check if the sound is muted for the current user
    var isMuted: Boolean
        get() {
            val userId = getUserId()
            val soundMutedKey = getSoundMutedKeyForUser(userId)
            return preferences.getBoolean(soundMutedKey, false)
        }
        set(value) {
            val userId = getUserId()
            val soundMutedKey = getSoundMutedKeyForUser(userId)
            preferences.edit().putBoolean(soundMutedKey, value).apply()
        }

    // Toggle the sound preference for the current user
    fun toggleSound() {
        isMuted = !isMuted
    }
}