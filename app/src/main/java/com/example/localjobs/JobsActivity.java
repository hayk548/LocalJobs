package com.example.localjobs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class JobsActivity extends AppCompatActivity {

    private RecyclerView jobsRecyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private Button postJobButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        postJobButton = findViewById(R.id.postJobButton);
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView);

        db = FirebaseFirestore.getInstance();
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);

        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobsRecyclerView.setAdapter(jobAdapter);

        loadJobs();

        postJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JobsActivity.this, PostJobActivity.class));
            }
        });
    }

    private void loadJobs() {
        db.collection("jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Job job = document.toObject(Job.class);
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();
                    }
                });
    }
}
