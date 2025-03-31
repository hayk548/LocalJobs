package com.example.localjobs;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private String senderId, senderEmail, receiverId, receiverEmail;
    private String chatId;  // Email-based chat ID

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
        senderEmail = mAuth.getCurrentUser().getEmail();  // Get sender email
        receiverEmail = getIntent().getStringExtra("receiverEmail"); // Get receiver email

        if (senderEmail == null || receiverEmail == null) {
            Log.e("ChatActivity", "Error: Email is null");
            finish();
            return;
        }

        // Generate consistent chatId using email addresses
        chatId = senderEmail.compareTo(receiverEmail) < 0 ? senderEmail + "_" + receiverEmail : receiverEmail + "_" + senderEmail;

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
        ChatMessage chatMessage = new ChatMessage(senderId, senderEmail, receiverId, receiverEmail, messageText, new Date().getTime());

        // Save message inside a subcollection
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    editMessage.setText("");
                    loadMessages();
                    updateChatMetadata(senderEmail, receiverEmail, messageText);
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error sending message", e));
    }

    private void loadMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatActivity", "Error loading messages", e);
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
                        recyclerChat.smoothScrollToPosition(chatMessages.size() - 1);
                    }
                });
    }

    private void updateChatMetadata(String userEmail, String chatPartnerEmail, String lastMessage) {
        db.collection("chats")
                .document(chatId)
                .set(new ChatMetadata(chatId, userEmail, chatPartnerEmail, lastMessage, new Date().getTime()));
    }
}
