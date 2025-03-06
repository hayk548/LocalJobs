package com.example.localjobs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localjobs.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private List<Job> jobList;
    private Context context;
    private FirebaseAuth mAuth;

    public JobAdapter(List<Job> jobList, Context context) {
        this.jobList = jobList;
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        String currentUserId = mAuth.getCurrentUser().getUid();

        holder.jobTitle.setText(job.getTitle());
        holder.jobDescription.setText(job.getDescription());
        holder.jobLocation.setText(job.getLocation());
        holder.jobDate.setText(job.getDate());

        if (job.getUserId() != null && job.getUserId().equals(currentUserId)) {
            holder.deleteJobButton.setVisibility(View.VISIBLE);
            holder.editJobButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteJobButton.setVisibility(View.GONE);
            holder.editJobButton.setVisibility(View.GONE);
        }

        holder.deleteJobButton.setOnClickListener(v -> {
            deleteJob(job.getJobId(), position);
        });
        holder.editJobButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditJobActivity.class);
            intent.putExtra("job_id", job.getJobId());
            context.startActivity(intent);
        });
    }

    private void deleteJob(String jobId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("jobs").document(jobId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                    jobList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete job", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobDescription, jobLocation, jobDate;
        Button editJobButton, deleteJobButton;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobDescription = itemView.findViewById(R.id.jobDescription);
            jobLocation = itemView.findViewById(R.id.jobLocation);
            jobDate = itemView.findViewById(R.id.jobDate);
            editJobButton = itemView.findViewById(R.id.editJobButton);
            deleteJobButton = itemView.findViewById(R.id.deleteJobButton);
        }
    }
}
