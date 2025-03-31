package com.example.localjobs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localjobs.ChatUser;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private Context context;
    private List<ChatUser> chatUsers;
    private final OnChatClickListener clickListener;

    public interface OnChatClickListener {
        void onChatClick(String receiverEmail);
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
        holder.userEmail.setText(chatUser.getUserEmail());
        holder.lastMessage.setText(chatUser.getLastMessage());
        holder.itemView.setOnClickListener(v -> clickListener.onChatClick(chatUser.getUserEmail()));
    }

    @Override
    public int getItemCount() {
        return chatUsers.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail, lastMessage;

        ChatViewHolder(View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.userEmail);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}