package com.example.localjobs;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private static final String TAG = "ChatListAdapter";
    private Context context;
    private List<ChatUser> chatUsers;
    private final OnChatClickListener clickListener;

    public interface OnChatClickListener {
        void onChatClick(String receiverId);
    }

    public ChatListAdapter(Context context, List<ChatUser> chatUsers, OnChatClickListener clickListener) {
        this.context = context;
        this.chatUsers = chatUsers;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatUser chatUser = chatUsers.get(position);
        String username = chatUser.getUsername(); // Returns username (jobTitle) if jobTitle exists
        holder.chatName.setText(username);
        holder.lastMessage.setText(chatUser.getLastMessage() != null ? chatUser.getLastMessage() : "");

        // Log for debugging
        Log.d(TAG, "Binding chatUser: username=" + username + ", jobTitle=" + chatUser.getJobTitle() + ", lastMessage=" + chatUser.getLastMessage());

        holder.itemView.setOnClickListener(v -> clickListener.onChatClick(chatUser.getUserId()));
    }

    @Override
    public int getItemCount() {
        return chatUsers.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatName, lastMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}