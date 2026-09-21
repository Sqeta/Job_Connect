package com.example.job_connect.data.remote

import com.example.job_connect.data.model.JobSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AdzunaApiService {

    @GET("jobs/za/search/{page}")
    fun searchJobs(
        @Path("page") page: Int,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("results_per_page") resultsPerPage: Int,
        @Query("what") keyword: String,
        @Query("where") location: String,
        @Query("content-type") contentType: String = "application/json"
    ): Call<JobSearchResponse>
}