package com.example.localjobs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
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
        if (!messageText.isEmpty()) {
            // Create the message object without the messageId
            ChatMessage chatMessage = new ChatMessage(senderId, receiverId, messageText, new Date().getTime());

            // Add message to Firestore
            db.collection("messages").add(chatMessage)
                    .addOnSuccessListener(documentReference -> {
                        // Log the message document ID
                        Log.d("ChatActivity", "Message sent with ID: " + documentReference.getId());
                        editMessage.setText("");  // Clear the input field after sending
                        loadMessages();           // Load the updated list of messages
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors if the message fails to send
                        Log.e("ChatActivity", "Error sending message", e);
                    });
        } else {
            Log.d("ChatActivity", "Message text is empty, not sending");
        }
    }
    private void loadMessages() {
        db.collection("messages")
                .whereIn("senderId", new ArrayList<String>() {{
                    add(senderId);
                    add(receiverId);
                }})
                .whereIn("receiverId", new ArrayList<String>() {{
                    add(receiverId);
                    add(senderId);
                }})
                .orderBy("timestamp", Query.Direction.ASCENDING)  // Ordering messages by time
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Log or handle Firestore query error
                        Log.e("ChatActivity", "Error loading messages", e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        chatMessages.clear();  // Clear the list of messages before adding new ones
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            ChatMessage chatMessage = snapshot.toObject(ChatMessage.class);
                            if (chatMessage != null) {
                                chatMessages.add(chatMessage);  // Add new messages to the list
                            }
                        }
                        chatAdapter.notifyDataSetChanged();  // Notify adapter of new data
                        recyclerChat.smoothScrollToPosition(chatMessages.size() - 1);  // Scroll to the latest message
                    }
                });
    }
}
