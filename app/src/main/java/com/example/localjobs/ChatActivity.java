package com.example.localjobs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerChat;
    private EditText editMessage;
    private ImageView buttonSend;
    private TextView typingIndicator, creatorInfoTextView;
    private ImageButton backButton, menuButton;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String senderId, receiverId, jobId, jobTitle;
    private String chatId;
    private ListenerRegistration messagesListener;
    private ListenerRegistration typingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue));
        }

        recyclerChat = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSend);
        typingIndicator = findViewById(R.id.typingIndicator);
        creatorInfoTextView = findViewById(R.id.creatorInfoTextView);
        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        senderId = mAuth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");
        jobId = getIntent().getStringExtra("jobId");
        jobTitle = getIntent().getStringExtra("jobTitle");

        if (senderId == null || receiverId == null) {
            Log.e("ChatActivity", "Error: User ID is null");
            Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatId = senderId.compareTo(receiverId) < 0 ? senderId + "_" + receiverId : receiverId + "_" + senderId;

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(v -> showCreatorInfoPopup());
        buttonSend.setOnClickListener(v -> sendMessage());

        initializeChatIfNeeded();
        setupTypingIndicator();
        loadMessages();
        fetchCreatorAndJobInfo();
    }

    private void fetchCreatorAndJobInfo() {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(chatSnapshot -> {
            String storedJobTitle = chatSnapshot.exists() ? chatSnapshot.getString("jobTitle") : null;
            if (storedJobTitle != null) {
                jobTitle = storedJobTitle; // Use stored jobTitle if available
            } else if (jobId != null && jobTitle == null) {
                // Fetch jobTitle from jobs collection if not passed and not stored
                db.collection("jobs").document(jobId)
                        .get()
                        .addOnSuccessListener(jobSnapshot -> {
                            if (jobSnapshot.exists()) {
                                jobTitle = jobSnapshot.getString("title");
                                // Update Firestore with fetched jobTitle
                                if (jobTitle != null) {
                                    chatRef.update("jobTitle", jobTitle)
                                            .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to update jobTitle", e));
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to fetch job", e));
            }

            db.collection("users").document(receiverId)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        String username = userSnapshot.exists() ? userSnapshot.getString("username") : "Unknown User";
                        if (username == null) username = "Unknown User";
                        creatorInfoTextView.setText(username + (jobTitle != null ? " - " + jobTitle : ""));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatActivity", "Failed to fetch user", e);
                        creatorInfoTextView.setText("Error Loading Info");
                    });
        }).addOnFailureListener(e -> {
            Log.e("ChatActivity", "Failed to fetch chat", e);
            creatorInfoTextView.setText("Error Loading Info");
        });
    }

    private void showCreatorInfoPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_creator_info);

        ImageView creatorImage = dialog.findViewById(R.id.creatorImage);
        TextView creatorUsername = dialog.findViewById(R.id.creatorUsername);
        TextView creatorEmail = dialog.findViewById(R.id.creatorEmail);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);

        db.collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String profileImage = documentSnapshot.getString("profileImage");

                        creatorUsername.setText(username != null ? username : "Unknown User");
                        creatorEmail.setText(mAuth.getCurrentUser().getEmail());

                        if (profileImage != null && !profileImage.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(this)
                                        .load(decodedByte)
                                        .transform(new CircleCrop())
                                        .into(creatorImage);
                            } catch (Exception e) {
                                Log.e("ChatActivity", "Error loading profile image", e);
                                creatorImage.setImageResource(R.drawable.profile_picture);
                            }
                        } else {
                            creatorImage.setImageResource(R.drawable.profile_picture);
                        }
                    } else {
                        creatorUsername.setText("Unknown User");
                        creatorEmail.setText("No email available");
                        creatorImage.setImageResource(R.drawable.profile_picture);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Failed to fetch creator info", e);
                    creatorUsername.setText("Error");
                    creatorEmail.setText("Error");
                    creatorImage.setImageResource(R.drawable.profile_picture);
                });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void initializeChatIfNeeded() {
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (!snapshot.exists()) {
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("users", Arrays.asList(senderId, receiverId));
                    chatData.put("lastMessage", "");
                    chatData.put("timestamp", new Date().getTime());
                    if (jobId != null) {
                        chatData.put("jobId", jobId);
                    }
                    if (jobTitle != null) {
                        chatData.put("jobTitle", jobTitle);
                    } else if (jobId != null) {
                        // Fetch jobTitle if not provided
                        db.collection("jobs").document(jobId)
                                .get()
                                .addOnSuccessListener(jobSnapshot -> {
                                    if (jobSnapshot.exists()) {
                                        String fetchedJobTitle = jobSnapshot.getString("title");
                                        if (fetchedJobTitle != null) {
                                            chatRef.update("jobTitle", fetchedJobTitle)
                                                    .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to update jobTitle", e));
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to fetch job", e));
                    }

                    chatRef.set(chatData)
                            .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to initialize chat", e));
                } else if (jobTitle != null && snapshot.getString("jobTitle") == null) {
                    // Update existing chat with jobTitle if not already set
                    chatRef.update("jobTitle", jobTitle)
                            .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to update jobTitle", e));
                }
            } else {
                Log.e("ChatActivity", "Error checking chat existence", task.getException());
            }
        });
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        ChatMessage chatMessage = new ChatMessage(senderId, null, receiverId, null, messageText, new Date().getTime());

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    editMessage.setText("");
                    updateTypingStatus(false);
                    updateChatMetadata(senderId, receiverId, messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Error sending message", e);
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        messagesListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatActivity", "Error loading messages", e);
                        Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        chatMessages.clear();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            ChatMessage chatMessage = snapshot.toObject(ChatMessage.class);
                            if (chatMessage != null) {
                                chatMessages.add(chatMessage);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        if (!chatMessages.isEmpty()) {
                            recyclerChat.smoothScrollToPosition(chatMessages.size() - 1);
                        }
                    }
                });
    }

    private void setupTypingIndicator() {
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTypingStatus(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        DocumentReference typingRef = db.collection("chats").document(chatId).collection("typing").document(receiverId);
        typingListener = typingRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("ChatActivity", "Error listening to typing status", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Boolean isTyping = snapshot.getBoolean("isTyping");
                typingIndicator.setVisibility(isTyping != null && isTyping ? View.VISIBLE : View.GONE);
            } else {
                typingIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void updateTypingStatus(boolean isTyping) {
        Map<String, Object> typingData = new HashMap<>();
        typingData.put("isTyping", isTyping);
        db.collection("chats")
                .document(chatId)
                .collection("typing")
                .document(senderId)
                .set(typingData)
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error updating typing status", e));
    }

    private void updateChatMetadata(String senderId, String receiverId, String lastMessage) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("users", Arrays.asList(senderId, receiverId));
        metadata.put("lastMessage", lastMessage);
        metadata.put("timestamp", new Date().getTime());
        if (jobId != null) {
            metadata.put("jobId", jobId);
        }
        if (jobTitle != null) {
            metadata.put("jobTitle", jobTitle);
        }

        db.collection("chats")
                .document(chatId)
                .set(metadata)
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error updating metadata", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) messagesListener.remove();
        if (typingListener != null) typingListener.remove();
        updateTypingStatus(false);
    }
}