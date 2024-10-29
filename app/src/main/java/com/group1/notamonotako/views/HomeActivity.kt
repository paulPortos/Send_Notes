package com.group1.notamonotako.views

import TokenManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group1.notamonotako.R
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.databinding.ActivityHomeBinding
import com.group1.notamonotako.fragments.Home
import com.group1.notamonotako.fragments.MyFlashcards
import com.group1.notamonotako.fragments.MyNotes

class HomeActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityHomeBinding
    lateinit var flashcardsTV : TextView
    lateinit var notesTV : TextView
    lateinit var flashcardsFabBtn : FloatingActionButton
    lateinit var mainFabBtn : FloatingActionButton
    lateinit var notesFabBtn : FloatingActionButton
    private lateinit var viewBlur : View
    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }
    private val rotateClockWiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise)
    }
    private val rotateAntiClockWiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_anti_clock_wise)
    }
    private val fromBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim)
    }
    private val toBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim)
    }

    private var areFabButtonsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TokenManager
        TokenManager.init(this)

        val soundManager = SoundManager(this) // Initialize SoundManager

        // Log the token to Logcat
        val token = TokenManager.getToken()
        Log.d("TOKEN", "Stored Token: $token")

        // Check if the user is logged in
        if (!TokenManager.isLoggedIn()) {
            // If not logged in, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()  // Close the HomeActivity
            return  // Prevent further execution
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        // Initialize binding first
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        notesTV =findViewById(R.id.notesTV)
        flashcardsTV =findViewById(R.id.flashcardsTV)
        notesFabBtn = findViewById(R.id.notesFabBtn)
        mainFabBtn = findViewById(R.id.mainFabBtn)
        flashcardsFabBtn =findViewById(R.id.flashcardsFabBtn)
        viewBlur = findViewById(R.id.viewBlur)
        viewBlur.visibility = View.GONE
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        // Check for intent extras to determine which fragment to show
        if (savedInstanceState == null) {
            val showMyNotesFragment = intent.getBooleanExtra("showMyNotesFragment", false)
            val showMyFlashcardFragment = intent.getBooleanExtra("showFlashcardFragment", false)
            if (showMyNotesFragment) {
                replaceFragment(MyNotes(), "Notes")
                binding.bottomNavigationView.selectedItemId = R.id.btnnotes

            } else if (showMyFlashcardFragment) { // Corrected to check for true
                replaceFragment(MyFlashcards(), "Flashcards")
                binding.bottomNavigationView.selectedItemId = R.id.btnflashcards

            } else {
                replaceFragment(Home(), "Home")
                binding.bottomNavigationView.selectedItemId = R.id.btnhome

            }
        }

        flashcardsFabBtn.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(this, AddFlashcards::class.java) // Create intent for Notes activity
            startActivity(intent)
            progressBar.visibility = View.INVISIBLE

        }

        notesFabBtn.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(this, AddNotes::class.java) // Create intent for Notes activity
            startActivity(intent)
            progressBar.visibility = View.INVISIBLE
        }

        mainFabBtn.setOnClickListener {
            progressBar.visibility = View.INVISIBLE
            if (areFabButtonsVisible) {
                shrinkFab()
                soundManager.playSoundEffect()
            } else {
                expandFab()
                soundManager.playSoundEffect()
            }
        }

        // Set up bottom navigation listener
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnhome -> replaceFragment(Home(), "Home")
                R.id.btnnotes -> replaceFragment(MyNotes(), "Notes")
                R.id.btnflashcards -> replaceFragment(MyFlashcards(), "Flashcards")

                else -> {
                }


            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {

        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentByTag(tag)

        // If the fragment is already displayed, do nothing
        if (currentFragment != null && currentFragment.isVisible) {
            return
        }

        val fragmentTransaction = fragmentManager.beginTransaction()

        // Replace the fragment and use a unique tag for each fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment, tag)

        // Always add the fragment to the back stack except for Home
        if (tag != "Home") {
            fragmentTransaction.addToBackStack(tag)
        }

        fragmentTransaction.commit()

        // Show progress bar while the fragment is loading
        progressBar.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.INVISIBLE
        }, 500)

        if (areFabButtonsVisible) {
            shrinkFab()
        }
    }
    private fun shrinkFab() {
        mainFabBtn.startAnimation(rotateAntiClockWiseFabAnim)
        notesFabBtn.startAnimation(toBottomFabAnim)
        flashcardsFabBtn.startAnimation(toBottomFabAnim)
        notesTV.startAnimation(toBottomFabAnim)
        flashcardsTV.startAnimation(toBottomFabAnim)
        areFabButtonsVisible = !areFabButtonsVisible
        viewBlur.visibility = View.GONE
    }

    private fun expandFab() {
        mainFabBtn.startAnimation(rotateClockWiseFabAnim)
        notesFabBtn.startAnimation(fromBottomFabAnim)
        flashcardsFabBtn.startAnimation(fromBottomFabAnim)
        notesTV.startAnimation(fromBottomFabAnim)
        flashcardsTV.startAnimation(fromBottomFabAnim)
        areFabButtonsVisible = !areFabButtonsVisible
        viewBlur.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.frameLayout)

        // Check if the current fragment is the Home fragment
        if (currentFragment is Home) {
            finishAffinity()
        } else {
            // If it's not Home, navigate back to Home fragment
            replaceFragment(Home(), "Home")
            binding.bottomNavigationView.selectedItemId = R.id.btnhome
        }
    }



}