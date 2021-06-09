package com.inkhornsolutions.kitchen.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;



    public MainActivityAdapter(Context context, ArrayList<ItemsModelClass> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_activity_adapter_layout, parent, false);
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
        String description = itemsModelClass.getDescription();



        holder.tvItem.setText(itemName);
        Glide.with(context).load(imageUri).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivItem);
        holder.tvItemPrice.setText("PKR"+price);
        holder.tvItemDescription.setText(description);
        holder.tvItemSchedule.setText("Available: "+ schedule);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem, tvItemPrice, tvItemSchedule, tvItemDescription;
        private ImageView ivItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = (TextView) itemView.findViewById(R.id.tvItem);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);
            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);
            tvItemDescription = (TextView) itemView.findViewById(R.id.tvItemDescription);

            ivItem = (ImageView) itemView.findViewById(R.id.ivItem);

            firebaseFirestore = FirebaseFirestore.getInstance();

        }
    }
}
