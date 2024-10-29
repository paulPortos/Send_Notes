package com.group1.notamonotako.api

import android.content.Context
import android.media.MediaPlayer
import com.group1.notamonotako.R

class SoundManager(private val context: Context) {
    private lateinit var mediaPlayer: MediaPlayer

    init {
        mediaPlayer = MediaPlayer.create(context, R.raw.soundeffects1)
        val isMuted = AccountManager.isMuted
        updateMediaPlayerVolume(isMuted)
    }

    // Update media player volume based on mute state
    fun updateMediaPlayerVolume(isMuted: Boolean) {
        if (isMuted) {
            mediaPlayer.setVolume(0F, 0F)
        } else {
            mediaPlayer.setVolume(1F, 1F)
        }
    }

    // Play sound effect if not muted
    fun playSoundEffect() {
        if (!AccountManager.isMuted) {
            mediaPlayer.start()
        }
    }

    // Release media player resources when no longer needed
    fun release() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}