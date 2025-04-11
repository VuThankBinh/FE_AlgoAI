package com.example.nckh.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nckh.R;
import com.example.nckh.model.ChatMessage;

import java.util.List;

public class ChatAdapter2 extends RecyclerView.Adapter<ChatAdapter2.ChatViewHolder> {
    private List<ChatMessage> messages;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, ChatMessage message);
    }

    public ChatAdapter2(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message2, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
//        holder.bind(message);

        if (message.isUserMessage()) {
            holder.layoutUserMessage.setVisibility(View.VISIBLE);
            holder.layoutAIMessage.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getMessage());
        } else {
            holder.layoutUserMessage.setVisibility(View.GONE);
            holder.layoutAIMessage.setVisibility(View.VISIBLE);
            holder.tvAIMessage.setText(message.getMessage());
        }
        // Chỉ cho phép click vào tin nhắn của người dùng
        holder.itemView.setClickable(message.isUserMessage());
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && message.isUserMessage()) {
                onItemClickListener.onItemClick(position, message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUserMessage;
        LinearLayout layoutAIMessage;
        TextView tvUserMessage;
        TextView tvAIMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUserMessage = itemView.findViewById(R.id.layoutUserMessage);
            layoutAIMessage = itemView.findViewById(R.id.layoutAIMessage);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvAIMessage = itemView.findViewById(R.id.tvAIMessage);
        }

        public void bind(ChatMessage message) {

        }
    }
}