package com.example.job_connect.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.job_connect.R
import com.example.job_connect.data.model.ApplicationModel

class ApplicationAdapter(
    private val applications: MutableList<ApplicationModel> =
        mutableListOf()
) : RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder>() {

    class ApplicationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val applicationIcon: TextView =
            itemView.findViewById(
                R.id.tvApplicationIcon
            )

        val applicationTitle: TextView =
            itemView.findViewById(
                R.id.tvApplicationTitle
            )

        val applicationCompany: TextView =
            itemView.findViewById(
                R.id.tvApplicationCompany
            )

        val applicationStatus: TextView =
            itemView.findViewById(
                R.id.tvApplicationStatus
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_application,
                parent,
                false
            )

        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ApplicationViewHolder,
        position: Int
    ) {
        val application = applications[position]

        holder.applicationTitle.text =
            application.title.ifBlank {
                "Job opportunity"
            }

        holder.applicationCompany.text =
            application.company.ifBlank {
                "Company not provided"
            }

        holder.applicationStatus.text =
            "Stage: ${application.status}"

        holder.applicationIcon.text =
            application.company
                .firstOrNull()
                ?.uppercase()
                ?: "J"

        holder.itemView.setOnClickListener {
            if (application.redirectUrl.isNotBlank()) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(application.redirectUrl)
                )

                holder.itemView.context.startActivity(
                    browserIntent
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return applications.size
    }

    fun updateApplications(
        newApplications: List<ApplicationModel>
    ) {
        applications.clear()
        applications.addAll(newApplications)
        notifyDataSetChanged()
    }
}