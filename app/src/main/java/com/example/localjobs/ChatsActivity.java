package com.example.localjobs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatsActivity extends AppCompatActivity {
    private RecyclerView chatsRecyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ChatUser> chatUsers;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ImageButton homeButton, chatsButton, postJobButton, accountButton, openMapButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        // Initialize RecyclerView
        chatsRecyclerView = findViewById(R.id.chatRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase Initialization
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Chat List
        chatUsers = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatUsers);
        chatsRecyclerView.setAdapter(chatListAdapter);

        // Load chat data
        loadChats();

        // Initialize Bottom Navigation Buttons
        homeButton = findViewById(R.id.homeButton);
        chatsButton = findViewById(R.id.chatsButton);
        postJobButton = findViewById(R.id.postJobButton);
        accountButton = findViewById(R.id.accountButton);
        openMapButton = findViewById(R.id.openMapButton);

        // Set up navigation button listeners
        homeButton.setOnClickListener(v -> navigateTo(JobsActivity.class));
        chatsButton.setOnClickListener(v -> {}); // Already in ChatsActivity
        postJobButton.setOnClickListener(v -> navigateTo(PostJobActivity.class));
        accountButton.setOnClickListener(v -> navigateTo(AccountActivity.class));
        openMapButton.setOnClickListener(v -> navigateTo(MapsActivity.class));
    }

    private void loadChats() {
        String currentUserId = auth.getCurrentUser().getUid();
        List<ChatUser> tempChatList = new ArrayList<>();
        Set<String> uniqueChatUsers = new HashSet<>(); // Stores unique user IDs

        Log.d("ChatsActivity", "Loading chats...");

        db.collection("chats")
                .whereEqualTo("senderId", currentUserId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("ChatsActivity", "Error loading chats", e);
                        return;
                    }

                    Log.d("ChatsActivity", "Sender chats fetched: " + querySnapshot.getDocuments().size());

                    tempChatList.clear();
                    uniqueChatUsers.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ChatMetadata chatMetadata = doc.toObject(ChatMetadata.class);
                        if (chatMetadata != null) {
                            String receiverId = chatMetadata.getReceiverId();
                            if (!uniqueChatUsers.contains(receiverId)) {
                                uniqueChatUsers.add(receiverId);
                                tempChatList.add(new ChatUser(receiverId, chatMetadata.getLastMessage()));
                            }
                        }
                    }

                    // Load chats where the user is the receiver
                    db.collection("chats")
                            .whereEqualTo("receiverId", currentUserId)
                            .addSnapshotListener((receiverSnapshot, error) -> {
                                if (error != null) {
                                    Log.e("ChatsActivity", "Error loading receiver chats", error);
                                    return;
                                }

                                Log.d("ChatsActivity", "Receiver chats fetched: " + receiverSnapshot.getDocuments().size());

                                for (DocumentSnapshot doc : receiverSnapshot.getDocuments()) {
                                    ChatMetadata chatMetadata = doc.toObject(ChatMetadata.class);
                                    if (chatMetadata != null) {
                                        String senderId = chatMetadata.getSenderId();
                                        if (!uniqueChatUsers.contains(senderId)) {
                                            uniqueChatUsers.add(senderId);
                                            tempChatList.add(new ChatUser(senderId, chatMetadata.getLastMessage()));
                                        }
                                    }
                                }

                                // Update UI with unique chat list
                                chatUsers.clear();
                                chatUsers.addAll(tempChatList);
                                chatListAdapter.notifyDataSetChanged();
                            });
                });
    }



    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(ChatsActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }
}
