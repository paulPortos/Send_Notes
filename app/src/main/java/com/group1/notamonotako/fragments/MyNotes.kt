package com.group1.notamonotako.fragments

import ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.group1.notamonotako.R
import com.group1.notamonotako.adapter.MyNotesAdapter
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.activities.GradientText
import com.group1.notamonotako.activities.extra_activities.NotificationActivity
import com.group1.notamonotako.activities.extra_activities.SendNotesActivity
import com.group1.notamonotako.activities.extra_activities.SettingsActivity
import com.group1.notamonotako.adapter.MyNotesOfflineAdapter
import com.group1.notamonotako.network.NetworkManager
import com.group1.notamonotako.roomdb.Notes
import com.group1.notamonotako.roomdb.NotesDao
import com.group1.notamonotako.roomdb.NotesDatabase
import com.group1.notamonotako.roomdb.database_manager.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class  MyNotes : Fragment() {
    lateinit var btnSettings : ImageButton
    lateinit var myNotesAdapter: MyNotesAdapter
    lateinit var myNotesOfflineAdapter: MyNotesOfflineAdapter
    lateinit var layoutManager: LinearLayoutManager
    lateinit var rv_mynotes: RecyclerView
    private lateinit var progressBar : ProgressBar
    private lateinit var tvMyNotes : TextView
    private lateinit var btnNotification : ImageButton
    private lateinit var tvNoNotes : TextView
    private lateinit var tvNoInternet : TextView
    private lateinit var swiperefresh : SwipeRefreshLayout
    private lateinit var btnSendNotes : ImageButton
    private lateinit var notesRepository: NotesRepository
    private lateinit var notesDao: NotesDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_notes, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        rv_mynotes = view.findViewById(R.id.rv_mynotes)
        rv_mynotes.layoutManager = LinearLayoutManager(requireContext())
        btnSettings = view.findViewById(R.id.btnSettings)
        tvMyNotes = view.findViewById(R.id.tvMyNotes)
        btnNotification = view.findViewById(R.id.btnNotification)
        swiperefresh = view.findViewById(R.id.swipeRefreshMyNotes)
        tvNoNotes = view.findViewById(R.id.tvNoNotes)
        tvNoInternet = view.findViewById(R.id.tvNoInternet)
        btnSendNotes = view.findViewById(R.id.btnSendNotes)
        val soundManager = SoundManager(requireContext()) // Initialize SoundManager
        val networkManager = NetworkManager
        // Initialize notesDao and notesRepository here
        notesDao = NotesDatabase.getInstance(requireContext()).dao
        notesRepository = NotesRepository(notesDao)

        progressBar.visibility = View.INVISIBLE
        GradientText.setGradientText(tvMyNotes, requireContext())

        btnNotification.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
            Log.d("Notification", "Notification button clicked")
        }

        btnSettings.setOnClickListener {
            soundManager.playSoundEffect()
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        btnSendNotes.setOnClickListener {
            soundManager.playSoundEffect()
            if (networkManager.isNetworkAvailable(requireContext())){
                val intent = Intent(requireContext(), SendNotesActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "This feature is not available in offline mode.", Toast.LENGTH_SHORT).show()
            }
        }
        swiperefresh.setOnRefreshListener {
            if (networkManager.isNetworkAvailable(requireContext())){
                fetchNotes()
            } else {
                fetchOfflineNotes()
            }
            swiperefresh.isRefreshing = false
        }

        rv_mynotes.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.context)

        if (networkManager.isNetworkAvailable(requireContext())){
            fetchNotes()
        } else {
            Toast.makeText(requireContext(), "Offline mode", Toast.LENGTH_SHORT).show()
            fetchOfflineNotes()
        }
    return view
    }

    private fun fetchNotes() {
        lifecycleScope.launch {
            try {
                rv_mynotes.visibility = View.VISIBLE
                tvNoInternet.visibility = View.GONE
                tvNoNotes.visibility = View.GONE
                // Network call should happen on IO thread
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    apiService.getNotes().execute() // Using execute() for synchronous call
                }
                if (response.isSuccessful) {
                    val notes = response.body()

                    // Ensure fragment is still attached before updating UI
                    if (isAdded && notes != null) {
                        myNotesAdapter = MyNotesAdapter(requireContext(), notes)
                        rv_mynotes.adapter = myNotesAdapter

                        if (notes.isEmpty()) {
                            // No notes available
                            rv_mynotes.visibility = View.GONE
                            tvNoNotes.visibility = View.VISIBLE
                        }

                    } else {
                        if (isAdded) {

                            Toast.makeText(requireContext(), "No notes available", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.INVISIBLE
                        }
                    }
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to fetch notes", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "HTTP error: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE

            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("addnotes", e.message.toString())
                progressBar.visibility = View.INVISIBLE
                rv_mynotes.visibility = View.GONE
                tvNoInternet.visibility = View.VISIBLE
                tvNoNotes.visibility = View.GONE
            }
        }
    }
    private fun fetchOfflineNotes() {
        lifecycleScope.launch {
            try {
                val notesList = notesRepository.fetchOfflineNotes()
                myNotesOfflineAdapter = MyNotesOfflineAdapter(requireContext(), notesList)
                rv_mynotes.adapter = myNotesOfflineAdapter
                rv_mynotes.visibility = if (notesList.isEmpty()) View.GONE else View.VISIBLE
                tvNoNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Offline", "Error: $e")
            }
        }
    }
}



