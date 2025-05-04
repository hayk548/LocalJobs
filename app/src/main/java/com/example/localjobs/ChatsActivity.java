package com.example.localjobs;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ChatsActivity extends AppCompatActivity {
    private RecyclerView chatsRecyclerView;
    private EditText searchBar;
    private ChatListAdapter chatListAdapter;
    private List<ChatUser> chatUsers;
    private List<ChatUser> filteredChatUsers;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageButton homeButton, chatsButton, postJobButton, accountButton, openMapButton;
    private ListenerRegistration chatsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue));
        }

        chatsRecyclerView = findViewById(R.id.chatRecyclerView);
        searchBar = findViewById(R.id.search_bar);
        homeButton = findViewById(R.id.homeButton);
        chatsButton = findViewById(R.id.chatsButton);
        postJobButton = findViewById(R.id.postJobButton);
        accountButton = findViewById(R.id.accountButton);
        openMapButton = findViewById(R.id.openMapButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        chatUsers = new ArrayList<>();
        filteredChatUsers = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, filteredChatUsers, receiverId -> startChatActivity(receiverId, null));
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatListAdapter);

        loadChats();
        setupSearchBar();

        homeButton.setOnClickListener(v -> navigateTo(JobsActivity.class));
        chatsButton.setOnClickListener(v -> {});
        postJobButton.setOnClickListener(v -> navigateTo(PostJobActivity.class));
        accountButton.setOnClickListener(v -> navigateTo(AccountActivity.class));
        openMapButton.setOnClickListener(v -> navigateTo(MapsActivity2.class));
    }

    private void startChatActivity(String receiverId, String jobId) {
        Intent intent = new Intent(ChatsActivity.this, ChatActivity.class);
        intent.putExtra("receiverId", receiverId);
        if (jobId != null) {
            intent.putExtra("jobId", jobId);
        }
        startActivity(intent);
    }

    private void loadChats() {
        String currentUserId = auth.getCurrentUser().getUid();
        if (currentUserId == null) {
            Log.e("ChatsActivity", "Current user ID is null");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        chatsListener = db.collection("chats")
                .whereArrayContains("users", currentUserId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("ChatsActivity", "Error loading chats", e);
                        Toast.makeText(this, "Failed to load chats", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        updateChatList(querySnapshot);
                    }
                });
    }

    private void updateChatList(QuerySnapshot querySnapshot) {
        String currentUserId = auth.getCurrentUser().getUid();
        Set<String> uniqueChatUsers = new HashSet<>();
        chatUsers.clear();

        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            List<String> users = (List<String>) doc.get("users");
            if (users == null || users.size() < 2) continue;

            String otherUserId = users.get(0).equals(currentUserId) ? users.get(1) : users.get(0);
            String chatId = doc.getId();
            String lastMessage = doc.getString("lastMessage");
            String jobTitle = doc.getString("jobTitle");
            String jobId = doc.getString("jobId");

            if (!uniqueChatUsers.contains(otherUserId)) {
                uniqueChatUsers.add(otherUserId);

                AtomicReference<String> usernameRef = new AtomicReference<>("Unknown User");
                db.collection("users").document(otherUserId).get()
                        .addOnSuccessListener(userDoc -> {
                            String username = userDoc.exists() ? userDoc.getString("username") : "Unknown User";
                            if (username == null) username = otherUserId;
                            usernameRef.set(username);

                            if (jobTitle != null) {
                                addChatUser(chatId, otherUserId, usernameRef.get(), lastMessage, jobTitle);
                            } else if (jobId != null) {
                                db.collection("jobs").document(jobId).get()
                                        .addOnSuccessListener(jobDoc -> {
                                            String fetchedJobTitle = jobDoc.exists() ? jobDoc.getString("title") : null;
                                            if (fetchedJobTitle != null) {
                                                db.collection("chats").document(chatId)
                                                        .update("jobTitle", fetchedJobTitle)
                                                        .addOnFailureListener(e -> Log.e("ChatsActivity", "Failed to update jobTitle", e));
                                            }
                                            addChatUser(chatId, otherUserId, usernameRef.get(), lastMessage, fetchedJobTitle);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ChatsActivity", "Error fetching job title for jobId: " + jobId, e);
                                            addChatUser(chatId, otherUserId, usernameRef.get(), lastMessage, null);
                                        });
                            } else {
                                addChatUser(chatId, otherUserId, usernameRef.get(), lastMessage, null);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ChatsActivity", "Error fetching username for userId: " + otherUserId, e);
                            addChatUser(chatId, otherUserId, otherUserId, lastMessage, jobTitle);
                        });
            }
        }
    }

    private void addChatUser(String chatId, String userId, String username, String lastMessage, String jobTitle) {
        chatUsers.add(new ChatUser(chatId, userId, username, lastMessage, jobTitle));
        filteredChatUsers.clear();
        filteredChatUsers.addAll(chatUsers);
        chatListAdapter.notifyDataSetChanged();
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterChats(String query) {
        filteredChatUsers.clear();
        if (query.isEmpty()) {
            filteredChatUsers.addAll(chatUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ChatUser user : chatUsers) {
                String usernameWithJob = user.getUsername().toLowerCase();
                if (usernameWithJob.contains(lowerQuery) ||
                        (user.getLastMessage() != null && user.getLastMessage().toLowerCase().contains(lowerQuery))) {
                    filteredChatUsers.add(user);
                }
            }
        }
        chatListAdapter.notifyDataSetChanged();
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(ChatsActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatsListener != null) {
            chatsListener.remove();
        }
    }
}