package com.example.job_connect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Provides the database operations for locally cached saved jobs.
 */
@Dao
interface CachedJobDao {

    /**
     * Returns all cached jobs belonging to the selected user.
     * Newer cached jobs are shown first.
     */
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

    /**
     * Inserts jobs and replaces a job if it already exists.
     */
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertJobs(
        jobs: List<CachedJobEntity>
    )

    /**
     * Removes the user's old cached jobs before storing fresh data.
     */
    @Query(
        """
        DELETE FROM cached_saved_jobs
        WHERE userId = :userId
        """
    )
    fun deleteJobsForUser(
        userId: String
    )

    /**
     * Returns the number of cached jobs belonging to the user.
     */
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