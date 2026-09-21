package com.example.job_connect.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AdzunaApiClient {

    private const val BASE_URL = "https://api.adzuna.com/v1/api/"

    val service: AdzunaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdzunaApiService::class.java)
    }
}