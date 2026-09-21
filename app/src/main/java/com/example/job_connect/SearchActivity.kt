package com.example.job_connect

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.job_connect.adapter.JobAdapter
import com.example.job_connect.data.model.Job
import com.example.job_connect.data.model.JobSearchResponse
import com.example.job_connect.data.remote.AdzunaApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SearchActivity"
        private const val RESULTS_PER_PAGE = 20
        private const val SEARCH_KEYWORD = "SEARCH_KEYWORD"
    }

    private lateinit var etKeyword: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSearchJobs: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSearchStatus: TextView
    private lateinit var recyclerJobs: RecyclerView
    private lateinit var jobAdapter: JobAdapter

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        window.statusBarColor = getColor(R.color.black)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initialiseViews()
        setupRecyclerView()
        setupSearchButton()
        loadSearchKeyword()
    }

    private fun initialiseViews() {
        etKeyword = findViewById(R.id.etKeyword)
        etLocation = findViewById(R.id.etLocation)
        btnSearchJobs = findViewById(R.id.btnSearchJobs)
        progressBar = findViewById(R.id.progressBar)
        tvSearchStatus = findViewById(R.id.tvSearchStatus)
        recyclerJobs = findViewById(R.id.recyclerJobs)
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            onSaveClick = { selectedJob ->
                saveJob(selectedJob)
            }
        )

        recyclerJobs.layoutManager =
            LinearLayoutManager(this)

        recyclerJobs.adapter = jobAdapter
        recyclerJobs.setHasFixedSize(true)
    }

    private fun setupSearchButton() {
        btnSearchJobs.setOnClickListener {
            val keyword =
                etKeyword.text.toString().trim()

            val location =
                etLocation.text.toString().trim()

            if (keyword.isEmpty()) {
                etKeyword.error =
                    "Please enter a job title or skill"

                etKeyword.requestFocus()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                etLocation.error =
                    "Please enter a city or province"

                etLocation.requestFocus()
                return@setOnClickListener
            }

            hideKeyboard()
            searchJobs(keyword, location)
        }
    }

    private fun loadSearchKeyword() {
        val keyword = intent
            .getStringExtra(SEARCH_KEYWORD)
            .orEmpty()

        if (keyword.isNotBlank()) {
            etKeyword.setText(keyword)
            etLocation.requestFocus()

            tvSearchStatus.text =
                "Enter a location to search for $keyword jobs."
        }
    }

    private fun searchJobs(
        keyword: String,
        location: String
    ) {
        showLoading(true)

        tvSearchStatus.text =
            "Searching for $keyword jobs in $location..."

        jobAdapter.updateJobs(emptyList())

        Log.d(
            TAG,
            "Searching for jobs: $keyword in $location"
        )

        AdzunaApiClient.service.searchJobs(
            page = 1,
            appId = BuildConfig.ADZUNA_APP_ID,
            appKey = BuildConfig.ADZUNA_APP_KEY,
            resultsPerPage = RESULTS_PER_PAGE,
            keyword = keyword,
            location = location
        ).enqueue(object : Callback<JobSearchResponse> {

            override fun onResponse(
                call: Call<JobSearchResponse>,
                response: Response<JobSearchResponse>
            ) {
                showLoading(false)

                if (response.isSuccessful) {
                    val jobs =
                        response.body()?.results.orEmpty()

                    if (jobs.isEmpty()) {
                        tvSearchStatus.text =
                            "No jobs found. Try another title or location."

                        Log.d(TAG, "No jobs returned")
                    } else {
                        jobAdapter.updateJobs(jobs)

                        tvSearchStatus.text =
                            "${jobs.size} job opportunities found"

                        Log.d(
                            TAG,
                            "${jobs.size} jobs loaded successfully"
                        )
                    }
                } else {
                    tvSearchStatus.text =
                        "Unable to load jobs. Error code: ${response.code()}"

                    Log.e(
                        TAG,
                        "API error: ${response.code()} ${response.message()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<JobSearchResponse>,
                throwable: Throwable
            ) {
                showLoading(false)

                tvSearchStatus.text =
                    "Connection failed. Check your internet and try again."

                Toast.makeText(
                    this@SearchActivity,
                    throwable.localizedMessage
                        ?: "Could not connect to Adzuna",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Adzuna request failed",
                    throwable
                )
            }
        })
    }

    private fun saveJob(job: Job) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                this,
                "Please sign in before saving a job.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val documentId = job.id.ifBlank {
            job.redirectUrl.hashCode()
                .toString()
                .replace("-", "n")
        }

        val savedJob = hashMapOf(
            "jobId" to documentId,
            "title" to job.title,
            "company" to job.companyName(),
            "location" to job.locationName(),
            "contractType" to (
                    job.contractType ?: "Job opportunity"
                    ),
            "description" to job.description,
            "redirectUrl" to job.redirectUrl,
            "savedAt" to FieldValue.serverTimestamp()
        )

        firestore
            .collection("users")
            .document(userId)
            .collection("savedJobs")
            .document(documentId)
            .set(savedJob)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Job saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "Job saved to Firestore: $documentId"
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Could not save job. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to save job",
                    exception
                )
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnSearchJobs.isEnabled = !isLoading

        btnSearchJobs.text =
            if (isLoading) {
                "Searching..."
            } else {
                "Search jobs"
            }
    }

    private fun hideKeyboard() {
        val keyboard = getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager

        keyboard.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            0
        )
    }
}