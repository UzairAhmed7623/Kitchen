package com.inkhornsolutions.kitchen.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.Chat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private final Context context;
    private List<Chat> msg;

    private String currentUser;

    public ChatAdapter(Context context, List<Chat> msg) {
        this.context = context;
        this.msg = msg;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_adapter_layout_right, parent, false);
            return new ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_adapter_layout_left , parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Chat chat = msg.get(position);

        holder.tvMessage.setText(chat.getMessage());

        holder.tvTime.setText(chat.getTime());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData cData = ClipData.newPlainText("text", "["+holder.tvTime.getText().toString()+"]"
                        +"\n"
                        +holder.tvMessage.getText().toString());

                cManager.setPrimaryClip(cData);

                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (msg == null){
            msg = new ArrayList<>();
        }
        return msg.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage, tvTime;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvMessage = (TextView)itemView.findViewById(R.id.tvMessage);
            tvTime = (TextView)itemView.findViewById(R.id.tvTime);
        }
    }

    @Override
    public int getItemViewType(int position) {
        currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (msg.get(position).getSenderId().equals(currentUser)){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }
}
