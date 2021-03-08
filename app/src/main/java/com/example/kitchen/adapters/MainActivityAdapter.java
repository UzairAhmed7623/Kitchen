package com.example.kitchen.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kitchen.R;
import com.example.kitchen.itemProperties;
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

        String resName = itemsModelClass.getResName();
        String itemName = itemsModelClass.getItemName();
        String imageUri = itemsModelClass.getImage();
        String price = itemsModelClass.getPrice();
        String available = itemsModelClass.getAvailability();
        String schedule = itemsModelClass.getSchedule();

        holder.tvItem.setText(itemName);
        Glide.with(context).load(imageUri).into(holder.ivItem);
        holder.tvItemPrice.setText("PKR"+price);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, ""+resName+" "+itemName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, itemProperties.class);
                intent.putExtra("restaurant", resName);
                intent.putExtra("itemName", itemName);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem, tvItemPrice;
        private ImageView ivItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = (TextView) itemView.findViewById(R.id.tvItem);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);

            ivItem = (ImageView) itemView.findViewById(R.id.ivItem);

        }
    }
}
