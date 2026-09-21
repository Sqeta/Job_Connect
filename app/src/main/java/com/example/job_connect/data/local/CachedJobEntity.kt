package com.example.job_connect.data.local

import androidx.room.Entity

/**
 * Represents a saved job stored locally on the user's device.
 *
 * The userId and jobId are combined as the primary key so that
 * different users can cache the same job without creating conflicts.
 */
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
    val cachedAt: Long = System.currentTimeMillis()
)