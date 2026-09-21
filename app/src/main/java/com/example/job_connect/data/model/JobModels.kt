package com.example.job_connect.data.model

import com.google.gson.annotations.SerializedName

data class JobSearchResponse(
    val count: Int = 0,
    val results: List<Job> = emptyList()
)

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val created: String = "",

    @SerializedName("redirect_url")
    val redirectUrl: String = "",

    @SerializedName("salary_min")
    val salaryMin: Double? = null,

    @SerializedName("salary_max")
    val salaryMax: Double? = null,

    @SerializedName("contract_type")
    val contractType: String? = null,

    val company: JobCompany? = null,
    val location: JobLocation? = null,
    val category: JobCategory? = null
) {

    fun companyName(): String {
        return company?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: "Company not provided"
    }

    fun locationName(): String {
        return location?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: "Location not provided"
    }
}

data class JobCompany(
    @SerializedName("display_name")
    val displayName: String = ""
)

data class JobLocation(
    @SerializedName("display_name")
    val displayName: String = ""
)

data class JobCategory(
    val label: String = ""
)