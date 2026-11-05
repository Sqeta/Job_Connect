package com.example.job_connect

import android.content.Intent
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
        setupBottomNavigation()
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
                    getString(
                        R.string.enter_job_title_error
                    )

                etKeyword.requestFocus()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                etLocation.error =
                    getString(
                        R.string.enter_city_error
                    )

                etLocation.requestFocus()
                return@setOnClickListener
            }

            hideKeyboard()
            searchJobs(keyword, location)
        }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.searchNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.searchNavSearch
        ).setOnClickListener {
            // The user is already on the Search page.
        }

        findViewById<TextView>(
            R.id.searchNavSaved
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SavedJobsActivity::class.java
                )
            )

            finish()
        }

        findViewById<TextView>(
            R.id.searchNavProfile
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )

            finish()
        }
    }

    private fun loadSearchKeyword() {
        val keyword =
            intent.getStringExtra(
                SEARCH_KEYWORD
            ).orEmpty()

        if (keyword.isNotBlank()) {
            etKeyword.setText(keyword)
            etLocation.requestFocus()

            tvSearchStatus.text =
                getString(
                    R.string.enter_location_for_jobs,
                    keyword
                )
        }
    }

    private fun searchJobs(
        keyword: String,
        location: String
    ) {
        showLoading(true)

        tvSearchStatus.text =
            getString(
                R.string.searching_jobs_in,
                keyword,
                location
            )

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
        ).enqueue(
            object : Callback<JobSearchResponse> {

                override fun onResponse(
                    call: Call<JobSearchResponse>,
                    response: Response<JobSearchResponse>
                ) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val jobs =
                            response.body()
                                ?.results
                                .orEmpty()

                        if (jobs.isEmpty()) {
                            tvSearchStatus.text =
                                getString(
                                    R.string.no_jobs_found
                                )

                            Log.d(
                                TAG,
                                "No jobs returned"
                            )
                        } else {
                            jobAdapter.updateJobs(jobs)

                            tvSearchStatus.text =
                                getString(
                                    R.string.job_opportunities_found,
                                    jobs.size
                                )

                            Log.d(
                                TAG,
                                "${jobs.size} jobs loaded successfully"
                            )
                        }
                    } else {
                        tvSearchStatus.text =
                            getString(
                                R.string.unable_load_jobs_error_code,
                                response.code()
                            )

                        Log.e(
                            TAG,
                            "API error: ${response.code()} " +
                                    response.message()
                        )
                    }
                }

                override fun onFailure(
                    call: Call<JobSearchResponse>,
                    throwable: Throwable
                ) {
                    showLoading(false)

                    tvSearchStatus.text =
                        getString(
                            R.string.connection_failed
                        )

                    Toast.makeText(
                        this@SearchActivity,
                        throwable.localizedMessage
                            ?: getString(
                                R.string.could_not_connect_adzuna
                            ),
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e(
                        TAG,
                        "Adzuna request failed",
                        throwable
                    )
                }
            }
        )
    }

    private fun saveJob(job: Job) {
        val userId =
            firebaseAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                this,
                getString(
                    R.string.sign_in_before_saving
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val documentId =
            job.id.ifBlank {
                job.redirectUrl
                    .hashCode()
                    .toString()
                    .replace("-", "n")
            }

        val savedJob = hashMapOf(
            "jobId" to documentId,
            "title" to job.title,
            "company" to job.companyName(),
            "location" to job.locationName(),
            "contractType" to (
                    job.contractType
                        ?: getString(
                            R.string.job_opportunity
                        )
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
                    getString(
                        R.string.job_saved_successfully
                    ),
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
                    getString(
                        R.string.could_not_save_job
                    ),
                    Toast.LENGTH_LONG
                ).show()

                Log.e(
                    TAG,
                    "Failed to save job",
                    exception
                )
            }
    }

    private fun showLoading(
        isLoading: Boolean
    ) {
        progressBar.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnSearchJobs.isEnabled = !isLoading

        btnSearchJobs.text =
            if (isLoading) {
                getString(R.string.searching)
            } else {
                getString(R.string.search_jobs_button)
            }
    }

    private fun hideKeyboard() {
        val keyboard =
            getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager

        keyboard.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            0
        )
    }
}