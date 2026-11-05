package com.example.job_connect.data.local

import androidx.room.Entity

@Entity(
    tableName = "cached_saved_jobs",
    primaryKeys = ["userId", "jobId"]
)
data class CachedJobEntity(
    val userId: String,
    val jobId: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val redirectUrl: String,
    val contractType: String?,
    val cachedAt: Long =
        System.currentTimeMillis(),

    // True when this job still needs to be uploaded to Firestore.
    val pendingSync: Boolean = false
)