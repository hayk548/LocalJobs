package com.example.localjobs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        // Check if the user has already applied for this job
        db.collection("applications")
                .whereEqualTo("jobId", job.getJobId())
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // If the user has NOT applied, bind the job data
                        holder.jobTitle.setText(job.getTitle());
                        holder.jobCategory.setText(job.getCategory());
                        holder.jobDescription.setText(job.getDescription());

                        int categoryImageResId = getCategoryImage(job.getCategory());
                        holder.jobCategoryImage.setImageResource(categoryImageResId);

                        // Set visibility of edit and delete buttons
                        if (job.getUserId() != null && job.getUserId().equals(currentUserId)) {
                            holder.editButton.setVisibility(View.VISIBLE);
                            holder.deleteButton.setVisibility(View.VISIBLE);
                        } else {
                            holder.editButton.setVisibility(View.GONE);
                            holder.deleteButton.setVisibility(View.GONE);
                        }

                        holder.chatButton.setOnClickListener(v -> {
                            String jobPosterId = job.getUserId(); // Get job creator's ID

                            if (!currentUserId.equals(jobPosterId)) { // Prevent chatting with yourself
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("receiverId", jobPosterId);
                                context.startActivity(intent);
                            }
                        });

                        holder.editButton.setOnClickListener(v -> {
                            Intent intent = new Intent(context, EditJobActivity.class);
                            intent.putExtra("jobId", job.getJobId()); // Pass the job ID to the edit activity
                            context.startActivity(intent);
                        });

                        holder.deleteButton.setOnClickListener(v -> {
                            db.collection("jobs").document(job.getJobId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Job deleted successfully!", Toast.LENGTH_SHORT).show();
                                        jobList.remove(position);  // Remove from list
                                        notifyItemRemoved(position); // Notify RecyclerView
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete job. Try again.", Toast.LENGTH_SHORT).show();
                                    });
                        });

                        // Set Apply button visibility
                        holder.applyButton.setVisibility(View.VISIBLE);
                        holder.applyButton.setOnClickListener(v -> {
                            Application jobApplication = new Application(job.getJobId(), currentUserId, System.currentTimeMillis(), "Pending");
                            db.collection("applications").add(jobApplication)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(context, "Successfully applied for the job!", Toast.LENGTH_SHORT).show();
                                        jobList.remove(position);  // Remove the applied job from the list
                                        notifyItemRemoved(position);
                                        db.collection("jobs").document(job.getJobId())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "Job deleted successfully!", Toast.LENGTH_SHORT).show();
                                                    jobList.remove(position);  // Remove from list
                                                    notifyItemRemoved(position); // Notify RecyclerView
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to delete job. Try again.", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to apply. Try again.", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    } else {
                        // If the user has already applied, remove the job from the list
                        jobList.remove(position);
                        notifyItemRemoved(position);  // Remove the job from RecyclerView
                    }
                });
    }



    @Override
    public int getItemCount() {
        return jobList.size();
    }

    private int getCategoryImage(String category) {
        if (category == null) {
            return R.drawable.default_image;
        }

        category = category.toLowerCase().trim();

        switch (category) {
            case "software development":
                return R.drawable.software_development;
            case "marketing":
                return R.drawable.marketing;
            case "design":
                return R.drawable.design;
            case "customer support":
                return R.drawable.customer_support;
            case "sales":
                return R.drawable.sales;
            case "data science":
                return R.drawable.data_science;
            case "finance":
                return R.drawable.finance;
            case "healthcare":
                return R.drawable.healthcare;
            case "education":
                return R.drawable.education;
            case "human resources":
                return R.drawable.human_resources;
            case "legal":
                return R.drawable.legal;
            case "engineering":
                return R.drawable.engineering;
            case "it, networking":
                return R.drawable.it_networking;
            case "project management":
                return R.drawable.project_management;
            case "consulting":
                return R.drawable.consulting;
            case "real estate":
                return R.drawable.real_estate;
            case "manufacturing":
                return R.drawable.manufacturing;
            case "administrative":
                return R.drawable.administrative;
            case "operations":
                return R.drawable.operations;
            case "transportation":
                return R.drawable.transportation;
            case "retail":
                return R.drawable.retail;
            case "hospitality":
                return R.drawable.hospitality;
            case "construction":
                return R.drawable.construction;
            case "security":
                return R.drawable.security;
            case "government":
                return R.drawable.government;
            case "freelancing":
                return R.drawable.freelancing;
            case "media, communications":
                return R.drawable.media_communications;
            case "event management":
                return R.drawable.event_management;
            case "science, research":
                return R.drawable.science_research;
            case "social services":
                return R.drawable.social_services;
            default:
                return R.drawable.default_image;
        }
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobCategory, jobDescription;
        ImageView jobCategoryImage;
        Button applyButton, chatButton, editButton, deleteButton;

        public JobViewHolder(View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobCategory = itemView.findViewById(R.id.jobCategory);
            jobDescription = itemView.findViewById(R.id.jobDescription);
            jobCategoryImage = itemView.findViewById(R.id.jobCategoryImage);
            applyButton = itemView.findViewById(R.id.applyButton);
            chatButton = itemView.findViewById(R.id.chatButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
