package com.inkhornsolutions.kitchen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.Chat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private final Context context;
    private final List<String> msg;
    private final List<String> time;

    public MessagesAdapter(Context context, List<String> msg, List<String> time) {
        this.context = context;
        this.msg = msg;
        this.time = time;

    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        String message = msg.get(position);
        String Time = time.get(position);

//        String message = chat.getMessages().get(0).get("message").toString();

        holder.tvMessage.setText(message);

        holder.tvTime.setText(Time);
    }

    @Override
    public int getItemCount() {
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
}
