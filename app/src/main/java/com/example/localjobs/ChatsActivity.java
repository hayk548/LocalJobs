package com.example.localjobs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {
    private RecyclerView chatsRecyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ChatUser> chatUsers;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        chatsRecyclerView = findViewById(R.id.chatRecyclerView);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        chatUsers = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatUsers);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatListAdapter);

        loadChats();
    }

    private void loadChats() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("chats")
                .whereEqualTo("senderId", currentUserId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("ChatsActivity", "Error loading chats", e);
                        return;
                    }
                    chatUsers.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ChatMetadata chatMetadata = doc.toObject(ChatMetadata.class);
                        if (chatMetadata != null) {
                            chatUsers.add(new ChatUser(chatMetadata.getReceiverId(), chatMetadata.getLastMessage()));
                        }
                    }
                    chatListAdapter.notifyDataSetChanged();
                });
    }



}
