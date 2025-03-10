package com.example.localjobs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private Context context;
    private FirebaseFirestore db;

    public JobAdapter(List<Job> jobList, Context context) {
        this.jobList = jobList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }
    public void updateJobList(List<Job> updatedJobList) {
        this.jobList = updatedJobList;
        notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.jobTitle.setText(job.getTitle());
        holder.jobCategory.setText(job.getCategory());
        holder.jobDescription.setText(job.getDescription());

        // Set up Apply button
        holder.applyButton.setOnClickListener(v -> {
            // Handle job application (can navigate to a new activity or submit data)
        });

        // Set up Edit button
        holder.editButton.setOnClickListener(v -> {
            // Edit functionality (Open Edit Job Activity or directly edit)
            // Example: Open an Edit Job Activity
            Intent editIntent = new Intent(context, EditJobActivity.class);
            editIntent.putExtra("jobId", job.getJobId());
            context.startActivity(editIntent);
        });

        // Set up Delete button
        holder.deleteButton.setOnClickListener(v -> {
            // Delete job from Firestore
            db.collection("jobs").document(job.getJobId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove from the list and notify adapter
                        jobList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting job", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {

        TextView jobTitle, jobCategory, jobDescription;
        Button applyButton, editButton, deleteButton;

        public JobViewHolder(View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobCategory = itemView.findViewById(R.id.jobCategory);
            jobDescription = itemView.findViewById(R.id.jobDescription);
            applyButton = itemView.findViewById(R.id.applyButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
