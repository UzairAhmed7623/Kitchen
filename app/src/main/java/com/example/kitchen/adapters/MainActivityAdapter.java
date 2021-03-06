package com.example.kitchen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitchen.R;
import com.example.kitchen.modelclasses.ItemsModelClass;

import java.util.ArrayList;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();

    public MainActivityAdapter(Context context, ArrayList<ItemsModelClass> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_activity_adapter, parent, false);
        return new MainActivityAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityAdapter.ViewHolder holder, int position) {
        ItemsModelClass itemsModelClass = items.get(position);

        String itemName = itemsModelClass.getItemName();
        String imageUri = itemsModelClass.getImage();
        String price = itemsModelClass.getPrice();
        String available = itemsModelClass.getAvailability();
        String schedule = itemsModelClass.getSchedule();

        holder.tvItem.setText(itemName);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = (TextView) itemView.findViewById(R.id.tvItem);

        }
    }
}
