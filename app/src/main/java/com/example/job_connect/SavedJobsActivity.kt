package com.example.job_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.job_connect.adapter.JobAdapter
import com.example.job_connect.data.local.CachedJobEntity
import com.example.job_connect.data.local.JobConnectDatabase
import com.example.job_connect.data.model.Job
import com.example.job_connect.data.model.JobCompany
import com.example.job_connect.data.model.JobLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SavedJobsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SavedJobsActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var localDatabase: JobConnectDatabase
    private lateinit var databaseExecutor: ExecutorService

    private lateinit var recyclerSavedJobs: RecyclerView
    private lateinit var progressSavedJobs: ProgressBar
    private lateinit var tvSavedJobsStatus: TextView
    private lateinit var jobAdapter: JobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_jobs)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        localDatabase =
            JobConnectDatabase.getDatabase(
                applicationContext
            )

        databaseExecutor =
            Executors.newSingleThreadExecutor()

        initialiseViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupTrackerButton()
    }

    override fun onResume() {
        super.onResume()

        if (::jobAdapter.isInitialized) {
            loadSavedJobs()
        }
    }

    private fun initialiseViews() {
        recyclerSavedJobs =
            findViewById(R.id.recyclerSavedJobs)

        progressSavedJobs =
            findViewById(R.id.progressSavedJobs)

        tvSavedJobsStatus =
            findViewById(R.id.tvSavedJobsStatus)
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            showSaveButton = false
        )

        recyclerSavedJobs.layoutManager =
            LinearLayoutManager(this)

        recyclerSavedJobs.adapter = jobAdapter
        recyclerSavedJobs.setHasFixedSize(true)
    }

    private fun setupTrackerButton() {
        findViewById<Button>(
            R.id.btnOpenTracker
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    TrackerActivity::class.java
                )
            )
        }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(
            R.id.savedNavHome
        ).setOnClickListener {
            val homeIntent =
                Intent(this, HomeActivity::class.java)

            homeIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(homeIntent)
            finish()
        }

        findViewById<TextView>(
            R.id.savedNavSearch
        ).setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        findViewById<TextView>(
            R.id.savedNavProfile
        ).setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }
    }

    private fun loadSavedJobs() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
            return
        }

        showLoading(true)

        firestore
            .collection("users")
            .document(userId)
            .collection("savedJobs")
            .get()
            .addOnSuccessListener { querySnapshot ->

                val savedJobs =
                    querySnapshot.documents.map { document ->
                        Job(
                            id =
                                document.getString("jobId")
                                    ?: document.id,

                            title =
                                document.getString("title")
                                    ?: getString(
                                        R.string.job_opportunity
                                    ),

                            description =
                                document.getString("description")
                                    ?: "",

                            redirectUrl =
                                document.getString("redirectUrl")
                                    ?: "",

                            contractType =
                                document.getString("contractType"),

                            company =
                                JobCompany(
                                    displayName =
                                        document.getString("company")
                                            ?: getString(
                                                R.string.company_not_provided
                                            )
                                ),

                            location =
                                JobLocation(
                                    displayName =
                                        document.getString("location")
                                            ?: getString(
                                                R.string.location_not_provided
                                            )
                                )
                        )
                    }

                displaySavedJobs(
                    jobs = savedJobs,
                    loadedFromCache = false
                )

                cacheSavedJobs(
                    userId = userId,
                    jobs = savedJobs
                )

                Log.d(
                    TAG,
                    "${savedJobs.size} jobs loaded from Firebase"
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Firebase failed. Loading Room cache.",
                    exception
                )

                loadJobsFromCache(userId)
            }
    }

    private fun cacheSavedJobs(
        userId: String,
        jobs: List<Job>
    ) {
        databaseExecutor.execute {
            try {
                val cachedJobs =
                    jobs.map { job ->
                        CachedJobEntity(
                            userId = userId,
                            jobId = job.id,
                            title = job.title,
                            company =
                                job.company?.displayName
                                    ?: getString(
                                        R.string.company_not_provided
                                    ),
                            location =
                                job.location?.displayName
                                    ?: getString(
                                        R.string.location_not_provided
                                    ),
                            description = job.description,
                            redirectUrl = job.redirectUrl,
                            contractType = job.contractType
                        )
                    }

                val dao =
                    localDatabase.cachedJobDao()

                dao.deleteJobsForUser(userId)

                if (cachedJobs.isNotEmpty()) {
                    dao.insertJobs(cachedJobs)
                }

                Log.d(
                    TAG,
                    "${cachedJobs.size} jobs cached in Room"
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Failed to cache saved jobs",
                    exception
                )
            }
        }
    }

    private fun loadJobsFromCache(
        userId: String
    ) {
        databaseExecutor.execute {
            try {
                val cachedJobs =
                    localDatabase
                        .cachedJobDao()
                        .getSavedJobs(userId)

                val jobs =
                    cachedJobs.map { cachedJob ->
                        Job(
                            id = cachedJob.jobId,
                            title = cachedJob.title,
                            description =
                                cachedJob.description,
                            redirectUrl =
                                cachedJob.redirectUrl,
                            contractType =
                                cachedJob.contractType,
                            company =
                                JobCompany(
                                    displayName =
                                        cachedJob.company
                                ),
                            location =
                                JobLocation(
                                    displayName =
                                        cachedJob.location
                                )
                        )
                    }

                runOnUiThread {
                    displaySavedJobs(
                        jobs = jobs,
                        loadedFromCache = true
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Failed to load Room cache",
                    exception
                )

                runOnUiThread {
                    showLoading(false)

                    recyclerSavedJobs.visibility =
                        View.GONE

                    tvSavedJobsStatus.visibility =
                        View.VISIBLE

                    tvSavedJobsStatus.text =
                        getString(
                            R.string.could_not_load_saved_jobs
                        )

                    Toast.makeText(
                        this,
                        getString(
                            R.string.could_not_load_saved_jobs
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun displaySavedJobs(
        jobs: List<Job>,
        loadedFromCache: Boolean
    ) {
        showLoading(false)
        jobAdapter.updateJobs(jobs)

        if (jobs.isEmpty()) {
            recyclerSavedJobs.visibility =
                View.GONE

            tvSavedJobsStatus.visibility =
                View.VISIBLE

            tvSavedJobsStatus.text =
                if (loadedFromCache) {
                    getString(
                        R.string.no_cached_saved_jobs
                    )
                } else {
                    getString(
                        R.string.no_saved_jobs
                    )
                }
        } else {
            recyclerSavedJobs.visibility =
                View.VISIBLE

            tvSavedJobsStatus.visibility =
                if (loadedFromCache) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            if (loadedFromCache) {
                tvSavedJobsStatus.text =
                    getString(
                        R.string.offline_mode_saved_jobs
                    )
            }

            Log.d(
                TAG,
                "${jobs.size} saved jobs displayed"
            )
        }
    }

    private fun showLoading(
        isLoading: Boolean
    ) {
        progressSavedJobs.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (isLoading) {
            tvSavedJobsStatus.visibility =
                View.GONE

            recyclerSavedJobs.visibility =
                View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::databaseExecutor.isInitialized) {
            databaseExecutor.shutdown()
        }
    }
}