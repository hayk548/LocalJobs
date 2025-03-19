package com.example.localjobs;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerChat;
    private EditText editMessage;
    private ImageView buttonSend;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String senderId, receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.light_blue)); // Set status bar color
        }

        recyclerChat = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSend);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        senderId = mAuth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Create message object
        ChatMessage chatMessage = new ChatMessage(senderId, receiverId, messageText, new Date().getTime());

        // Save message to Firestore
        db.collection("messages").add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    editMessage.setText("");  // Clear input field
                    loadMessages();
                    // Update the 'chats' collection for both sender & receiver
                    updateChatMetadata(senderId, receiverId, messageText);
                    updateChatMetadata(receiverId, senderId, messageText);
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error sending message", e));
    }

    // Function to update 'chats' collection
    private void updateChatMetadata(String userId, String chatPartnerId, String lastMessage) {
        db.collection("chats")
                .document(userId + "_" + chatPartnerId)
                .set(new ChatMetadata(userId + "_" + chatPartnerId, userId, chatPartnerId, lastMessage, new Date().getTime()));
    }

    private void loadMessages() {
        db.collection("messages")
                .whereIn("senderId", Arrays.asList(senderId, receiverId))
                .whereIn("receiverId", Arrays.asList(senderId, receiverId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatActivity", "Error loading messages", e);
                        return;
                    }
                    chatMessages.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        ChatMessage chatMessage = snapshot.toObject(ChatMessage.class);
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerChat.smoothScrollToPosition(chatMessages.size() - 1);
                });
    }

}
