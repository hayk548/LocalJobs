package com.example.localjobs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
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

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatUser chatUser = chatUsers.get(position);
        holder.chatName.setText(chatUser.getUsername()); // Display username
        holder.lastMessage.setText(chatUser.getLastMessage());
        holder.itemView.setOnClickListener(v -> clickListener.onChatClick(chatUser.getUserId()));
    }

    @Override
    public int getItemCount() {
        return chatUsers.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatName, lastMessage;

        ChatViewHolder(View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName); // Update ID if needed
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}