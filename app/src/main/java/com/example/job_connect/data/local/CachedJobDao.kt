package com.example.job_connect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedJobDao {

    @Query(
        """
        SELECT * FROM cached_saved_jobs
        WHERE userId = :userId
        ORDER BY cachedAt DESC
        """
    )
    fun getSavedJobs(
        userId: String
    ): List<CachedJobEntity>

    @Query(
        """
        SELECT * FROM cached_saved_jobs
        WHERE pendingSync = 1
        ORDER BY cachedAt ASC
        """
    )
    fun getPendingSyncJobs():
            List<CachedJobEntity>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertJob(
        job: CachedJobEntity
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertJobs(
        jobs: List<CachedJobEntity>
    )

    @Query(
        """
        UPDATE cached_saved_jobs
        SET pendingSync = 0
        WHERE userId = :userId
        AND jobId = :jobId
        """
    )
    fun markJobAsSynced(
        userId: String,
        jobId: String
    )

    @Query(
        """
        DELETE FROM cached_saved_jobs
        WHERE userId = :userId
        """
    )
    fun deleteJobsForUser(
        userId: String
    )

    @Query(
        """
        SELECT COUNT(*) FROM cached_saved_jobs
        WHERE userId = :userId
        """
    )
    fun getSavedJobCount(
        userId: String
    ): Int
}