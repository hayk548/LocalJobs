package com.example.localjobs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private static final String TAG = "JobAdapter";
    private List<Job> jobList;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseFunctions functions;

    public JobAdapter(List<Job> jobList, Context context) {
        this.jobList = jobList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        // Specify the region where your Cloud Function is deployed (e.g., "us-central1")
        this.functions = FirebaseFunctions.getInstance("us-central1"); // Change to your region if different
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

        // Set common job details
        holder.jobTitle.setText(job.getTitle());
        holder.jobCategory.setText(job.getCategory());
        holder.jobDescription.setText(job.getDescription());
        holder.jobLocation.setText(job.getLocation() != null ? job.getLocation() : "Unknown Location");
        int categoryImageResId = getCategoryImage(job.getCategory());
        holder.jobCategoryImage.setImageResource(categoryImageResId);

        // Check if the user has already applied for this job
        db.collection("applications")
                .whereEqualTo("jobId", job.getJobId())
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // User has not applied yet
                        if (job.getUserId() != null && job.getUserId().equals(currentUserId)) {
                            // Job is mine: Show settings icon with dropdown
                            holder.settingsButton.setVisibility(View.VISIBLE);
                            holder.applyButton.setVisibility(View.GONE);
                            holder.chatButton.setVisibility(View.GONE);

                            holder.settingsButton.setOnClickListener(v -> {
                                PopupMenu popup = new PopupMenu(context, holder.settingsButton);
                                popup.getMenuInflater().inflate(R.menu.job_options_menu, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.menu_edit) {
                                        Intent intent = new Intent(context, EditJobActivity.class);
                                        intent.putExtra("jobId", job.getJobId());
                                        context.startActivity(intent);
                                        return true;
                                    } else if (item.getItemId() == R.id.menu_delete) {
                                        new AlertDialog.Builder(context)
                                                .setTitle("Delete Job")
                                                .setMessage("Are you sure you want to delete '" + job.getTitle() + "'? This action cannot be undone.")
                                                .setPositiveButton("Delete", (dialog, which) -> {
                                                    db.collection("jobs").document(job.getJobId())
                                                            .delete()
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(context, "Job deleted successfully!", Toast.LENGTH_SHORT).show();
                                                                jobList.remove(position);
                                                                notifyItemRemoved(position);
                                                                notifyItemRangeChanged(position, jobList.size());
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(context, "Failed to delete job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .setNegativeButton("Cancel", null)
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                        return true;
                                    }
                                    return false;
                                });
                                popup.show();
                            });
                        } else {
                            // Job is not mine: Show small Apply and Chat buttons
                            holder.settingsButton.setVisibility(View.GONE);
                            holder.applyButton.setVisibility(View.VISIBLE);
                            holder.chatButton.setVisibility(View.VISIBLE);

                            holder.chatButton.setOnClickListener(v -> {
                                String jobPosterId = job.getUserId();
                                if (!currentUserId.equals(jobPosterId)) {
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("receiverId", jobPosterId);
                                    intent.putExtra("jobId", job.getJobId());
                                    context.startActivity(intent);
                                } else {
                                    Toast.makeText(context, "You cannot chat with yourself", Toast.LENGTH_SHORT).show();
                                }
                            });

                            holder.applyButton.setOnClickListener(v -> {
                                // Fetch applicant details from Firestore
                                db.collection("users").document(currentUserId).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                // Get username from User class
                                                User applicant = documentSnapshot.toObject(User.class);
                                                String applicantUsername = applicant != null && applicant.getUsername() != null ? applicant.getUsername() : "Unknown";
                                                // Get email from FirebaseAuth
                                                String applicantEmail = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "Unknown";

                                                // Fetch job creator's FCM token
                                                db.collection("users").document(job.getUserId()).get()
                                                        .addOnSuccessListener(creatorSnapshot -> {
                                                            if (creatorSnapshot.exists()) {
                                                                String creatorFcmToken = creatorSnapshot.getString("fcmToken");
                                                                if (creatorFcmToken != null && !creatorFcmToken.isEmpty()) {
                                                                    // Proceed with application logic first
                                                                    Application jobApplication = new Application(
                                                                            job.getJobId(),
                                                                            currentUserId,
                                                                            System.currentTimeMillis(),
                                                                            "Pending",
                                                                            job.getTitle(),
                                                                            job.getUserId()
                                                                    );
                                                                    db.collection("applications").add(jobApplication)
                                                                            .addOnSuccessListener(documentReference -> {
                                                                                Toast.makeText(context, "Successfully applied for the job!", Toast.LENGTH_SHORT).show();
                                                                                // Send notification to job creator
                                                                                sendNotificationToCreator(
                                                                                        creatorFcmToken,
                                                                                        job.getTitle(),
                                                                                        applicantUsername,
                                                                                        applicantEmail,
                                                                                        job.getJobId()
                                                                                );
                                                                                // Remove job from list
                                                                                jobList.remove(position);
                                                                                notifyItemRemoved(position);
                                                                                // Comment out job deletion to allow multiple applicants
                                                                                /*
                                                                                db.collection("jobs").document(job.getJobId())
                                                                                        .delete()
                                                                                        .addOnSuccessListener(aVoid -> {
                                                                                            Toast.makeText(context, "Job deleted successfully!", Toast.LENGTH_SHORT).show();
                                                                                        })
                                                                                        .addOnFailureListener(e -> {
                                                                                            Toast.makeText(context, "Failed to delete job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                        });
                                                                                */
                                                                            })
                                                                            .addOnFailureListener(e -> {
                                                                                Toast.makeText(context, "Failed to apply: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                Log.e(TAG, "Failed to save application", e);
                                                                            });
                                                                } else {
                                                                    Toast.makeText(context, "Job creator's notification token not found.", Toast.LENGTH_SHORT).show();
                                                                    Log.w(TAG, "FCM token missing for user: " + job.getUserId());
                                                                }
                                                            } else {
                                                                Toast.makeText(context, "Job creator not found.", Toast.LENGTH_SHORT).show();
                                                                Log.w(TAG, "Creator document missing for user: " + job.getUserId());
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Error fetching job creator: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "Failed to fetch creator document", e);
                                                        });
                                            } else {
                                                Toast.makeText(context, "Failed to fetch applicant details.", Toast.LENGTH_SHORT).show();
                                                Log.w(TAG, "Applicant document missing for user: " + currentUserId);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Error fetching applicant: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Failed to fetch applicant document", e);
                                        });
                            });
                        }
                    } else {
                        // User has already applied: Hide the item
                        jobList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
    }

    private void sendNotificationToCreator(String creatorFcmToken, String jobTitle, String applicantUsername, String applicantEmail, String jobId) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", creatorFcmToken);
        data.put("title", "New Application for " + jobTitle);
        data.put("body", applicantUsername + " (" + applicantEmail + ") has applied for your job.");
        data.put("jobId", jobId);

        Log.d(TAG, "Sending notification to token: " + creatorFcmToken + " for job: " + jobId);

        functions.getHttpsCallable("sendNotification")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {
                    Log.d(TAG, "Notification sent successfully for job: " + jobId);
                    Toast.makeText(context, "Notification sent to job creator.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to send notification for job: " + jobId, e);
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
        TextView jobTitle, jobCategory, jobDescription, jobLocation;
        ImageView jobCategoryImage;
        Button applyButton, chatButton;
        ImageButton settingsButton;

        public JobViewHolder(View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobCategory = itemView.findViewById(R.id.jobCategory);
            jobDescription = itemView.findViewById(R.id.jobDescription);
            jobLocation = itemView.findViewById(R.id.jobLocation);
            jobCategoryImage = itemView.findViewById(R.id.jobCategoryImage);
            applyButton = itemView.findViewById(R.id.applyButton);
            chatButton = itemView.findViewById(R.id.chatButton);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}