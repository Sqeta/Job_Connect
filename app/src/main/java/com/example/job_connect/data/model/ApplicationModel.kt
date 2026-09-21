package com.example.job_connect.data.model

data class ApplicationModel(
    val jobId: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val redirectUrl: String = "",
    val status: String = "Applied"
)