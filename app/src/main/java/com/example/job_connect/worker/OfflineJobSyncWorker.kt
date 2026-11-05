package com.example.job_connect.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.job_connect.data.local.JobConnectDatabase
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class OfflineJobSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : Worker(
    appContext,
    workerParameters
) {

    companion object {
        private const val TAG =
            "OfflineJobSyncWorker"
    }

    override fun doWork(): Result {
        val currentUser =
            FirebaseAuth
                .getInstance()
                .currentUser

        if (currentUser == null) {
            Log.d(
                TAG,
                "No signed-in user. Sync will retry later."
            )

            return Result.retry()
        }

        val database =
            JobConnectDatabase.getDatabase(
                applicationContext
            )

        val cachedJobDao =
            database.cachedJobDao()

        val pendingJobs =
            cachedJobDao.getPendingSyncJobs()
                .filter {
                    it.userId == currentUser.uid
                }

        if (pendingJobs.isEmpty()) {
            Log.d(
                TAG,
                "There are no offline jobs waiting to sync."
            )

            return Result.success()
        }

        val firestore =
            FirebaseFirestore.getInstance()

        return try {
            pendingJobs.forEach { job ->

                val savedJob =
                    hashMapOf<String, Any?>(
                        "jobId" to job.jobId,
                        "title" to job.title,
                        "company" to job.company,
                        "location" to job.location,
                        "contractType" to job.contractType,
                        "description" to job.description,
                        "redirectUrl" to job.redirectUrl,
                        "savedAt" to
                                FieldValue.serverTimestamp(),
                        "syncedFromOffline" to true
                    )

                val uploadTask =
                    firestore
                        .collection("users")
                        .document(job.userId)
                        .collection("savedJobs")
                        .document(job.jobId)
                        .set(savedJob)

                Tasks.await(uploadTask)

                cachedJobDao.markJobAsSynced(
                    userId = job.userId,
                    jobId = job.jobId
                )

                Log.d(
                    TAG,
                    "Offline job synchronised: ${job.jobId}"
                )
            }

            Result.success()
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Offline synchronisation failed",
                exception
            )

            Result.retry()
        }
    }
}