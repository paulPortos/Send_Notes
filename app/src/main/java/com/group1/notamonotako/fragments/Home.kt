package com.group1.notamonotako.fragments

import ApiService
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.group1.notamonotako.R
import com.group1.notamonotako.views.SettingsActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group1.notamonotako.adapter.HomeAdapter
import com.group1.notamonotako.api.SoundManager
import com.group1.notamonotako.api.requests_responses.public_notes.getPublicNotes
import com.group1.notamonotako.views.AddFlashcards
import com.group1.notamonotako.views.AddNotes
import com.group1.notamonotako.views.GradientText
import com.group1.notamonotako.views.NotificationActivity
import com.group1.notamonotako.views.SendNotesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

class Home : Fragment() {
    lateinit var btnSettings :ImageButton
    lateinit var swiperefresh : SwipeRefreshLayout
    private lateinit var progressBar : ProgressBar
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var rvhome: RecyclerView
    private lateinit var tvSendNotes : TextView
    private lateinit var svSearchView :SearchView
    private lateinit var tvNoNotes : TextView
    private lateinit var tvNoInternet : TextView
    private lateinit var  btnNotification :ImageButton
    private var data: List<getPublicNotes> = listOf()
    private var hasShownNoDataToast = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val soundManager = SoundManager(requireContext()) // Initialize SoundManager
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        btnSettings = view.findViewById(R.id.btnSettings)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
        rvhome = view.findViewById(R.id.rvhome)
        tvSendNotes = view.findViewById(R.id.tvSendNotes)
        rvhome.layoutManager = LinearLayoutManager(requireContext())
        btnNotification = view.findViewById(R.id.btnNotification)
        svSearchView = view.findViewById(R.id.svSearchView)
        swiperefresh = view.findViewById(R.id.swipeRefreshHome)
        tvNoNotes = view.findViewById(R.id.tvNoNotes)
        tvNoInternet = view.findViewById(R.id.tvNoInternet)

        svSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true

            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }


        })




        btnSettings.setOnClickListener {
            val intent = Intent(requireContext(),SettingsActivity::class.java)
            startActivity(intent)
            soundManager.playSoundEffect()


        }

        btnNotification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
            Log.d("Notification", "Notification button clicked")
            soundManager.playSoundEffect()
        }



        swiperefresh.setOnRefreshListener {
            fetchPublicNotes()
            swiperefresh.isRefreshing = false
        }

        GradientText.setGradientText(tvSendNotes, requireContext())
        rvhome.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.context)
        fetchPublicNotes()
        return view
    }

    private fun fetchPublicNotes(){
        lifecycleScope.launch {
            try {
                rvhome.visibility = View.VISIBLE
                tvNoInternet.visibility = View.GONE
                val apiService = RetrofitInstance.create(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    apiService.getPublicNotes()
                }
                if (response.isSuccessful) {
                    val publicNotes = response.body()
                    if (isAdded && publicNotes != null) {
                        if (publicNotes.isEmpty()) {
                            // No notes available
                            rvhome.visibility = View.GONE
                            tvNoNotes.visibility = View.VISIBLE
                        } else {
                            // Notes available
                            data = publicNotes // Store in data to put in datalist
                            val adapter = HomeAdapter(requireContext(), publicNotes)
                            rvhome.adapter = adapter
                            rvhome.visibility = View.VISIBLE
                            tvNoNotes.visibility = View.GONE
                        }
                    }
                }
            } catch (e: HttpException){
                Toast.makeText(requireContext(), "HTTP error: ${e.message}", Toast.LENGTH_SHORT).show()

            } catch (e: IOException){
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                rvhome.visibility = View.GONE
                tvNoInternet.visibility = View.VISIBLE

            }
        }
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    private fun filterList(query: String?) {
        if (query != null && query.isNotEmpty()) {
            val filteredList = data.filter { it.title.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) }

            if (filteredList.isEmpty() && !hasShownNoDataToast) {
                Toast.makeText(requireContext(), "No Data Found", Toast.LENGTH_SHORT).show()
                hasShownNoDataToast = true
                (rvhome.adapter as HomeAdapter).setFilteredList(emptyList())
            } else if (filteredList.isNotEmpty()) {
                hasShownNoDataToast = false
                (rvhome.adapter as HomeAdapter).setFilteredList(filteredList)
            }
        } else {
            (rvhome.adapter as HomeAdapter).setFilteredList(data)
            hasShownNoDataToast = false
        }
    }

}