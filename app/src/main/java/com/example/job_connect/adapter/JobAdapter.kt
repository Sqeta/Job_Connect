package com.example.job_connect.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.job_connect.JobDetailsActivity
import com.example.job_connect.R
import com.example.job_connect.data.model.Job

class JobAdapter(
    private val jobs: MutableList<Job> = mutableListOf(),
    private val showSaveButton: Boolean = true,
    private val onSaveClick: (Job) -> Unit = {}
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    class JobViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val jobIcon: TextView =
            itemView.findViewById(R.id.tvJobIcon)

        val jobTitle: TextView =
            itemView.findViewById(R.id.tvJobTitle)

        val company: TextView =
            itemView.findViewById(R.id.tvCompany)

        val location: TextView =
            itemView.findViewById(R.id.tvLocation)

        val jobType: TextView =
            itemView.findViewById(R.id.tvJobType)

        val saveJobButton: TextView =
            itemView.findViewById(R.id.btnSaveJob)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_job,
                parent,
                false
            )

        return JobViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int
    ) {
        val job = jobs[position]

        holder.jobTitle.text =
            job.title.ifBlank {
                "Job opportunity"
            }

        holder.company.text =
            job.companyName()

        holder.location.text =
            job.locationName()

        holder.jobType.text = job.contractType
            ?.replace("_", " ")
            ?.replaceFirstChar { character ->
                character.uppercase()
            }
            ?: "Job opportunity"

        holder.jobIcon.text = job.companyName()
            .firstOrNull()
            ?.uppercase()
            ?: "J"

        holder.saveJobButton.visibility =
            if (showSaveButton) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.saveJobButton.setOnClickListener {
            onSaveClick(job)
        }

        holder.itemView.setOnClickListener {
            openJobDetails(
                holder,
                job
            )
        }
    }

    override fun getItemCount(): Int {
        return jobs.size
    }

    fun updateJobs(newJobs: List<Job>) {
        jobs.clear()
        jobs.addAll(newJobs)
        notifyDataSetChanged()
    }

    private fun openJobDetails(
        holder: JobViewHolder,
        job: Job
    ) {
        val context = holder.itemView.context

        val detailsIntent = Intent(
            context,
            JobDetailsActivity::class.java
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_JOB_ID,
            job.id
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_TITLE,
            job.title
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_COMPANY,
            job.companyName()
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_LOCATION,
            job.locationName()
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_CONTRACT_TYPE,
            job.contractType ?: "Job opportunity"
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_DESCRIPTION,
            job.description
        )

        detailsIntent.putExtra(
            JobDetailsActivity.EXTRA_REDIRECT_URL,
            job.redirectUrl
        )

        context.startActivity(detailsIntent)
    }
}