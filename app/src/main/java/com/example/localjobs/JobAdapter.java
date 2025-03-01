package com.example.localjobs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_item, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        // Ensure job object is not null before accessing fields
        if (job != null) {
            holder.jobTitle.setText(job.getTitle());
            holder.jobDescription.setText(job.getDescription());
            holder.jobLocation.setText(job.getLocation());
        }
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobDescription, jobLocation;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobDescription = itemView.findViewById(R.id.jobDescription);
            jobLocation = itemView.findViewById(R.id.jobLocation);
        }
    }
}
