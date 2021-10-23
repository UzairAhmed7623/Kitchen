package com.inkhornsolutions.kitchen.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.firestore.SetOptions;
import com.inkhornsolutions.kitchen.Items;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.ItemsModelClass;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;



    public ItemsAdapter(Context context, ArrayList<ItemsModelClass> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.items_activity_adapter_layout, parent, false);
        return new ItemsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsAdapter.ViewHolder holder, int position) {
        ItemsModelClass itemsModelClass = items.get(position);

        String resName = itemsModelClass.getResName();
        String itemName = itemsModelClass.getItemName();
        String imageUri = itemsModelClass.getImage();
        String price = itemsModelClass.getPrice();
        String available = itemsModelClass.getAvailability();
        String schedule = itemsModelClass.getSchedule();
        String description = itemsModelClass.getDescription();
        String isDODAvailable = itemsModelClass.getIsDODAvailable();

        holder.tvItem.setText(itemName);
        Glide.with(context).load(imageUri).placeholder(R.drawable.main_course).fitCenter().into(holder.ivItem);
//        Picasso.get().load(imageUri).placeholder(R.drawable.food_placeholder).fit().into(holder.ivItem);

        holder.tvItemPrice.setText("PKR"+price);
        holder.tvItemDescription.setText(description);
        holder.tvItemSchedule.setText("Available: "+ schedule);

        holder.cbDOD.setChecked(isDODAvailable.equals("yes"));

        holder.cbDOD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked){
                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(itemName)
                            .update("isDODAvailable", "yes")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toasty.info(context, "Successfully changed.", Toasty.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(context, e.getMessage(),Toasty.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(itemName)
                            .update("isDODAvailable","no")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(context, e.getMessage(),Toasty.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem, tvItemPrice, tvItemSchedule, tvItemDescription;
        private ImageView ivItem;
        private MaterialCheckBox cbDOD;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = (TextView) itemView.findViewById(R.id.tvItem);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);
            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);
            tvItemDescription = (TextView) itemView.findViewById(R.id.tvItemDescription);
            cbDOD = (MaterialCheckBox) itemView.findViewById(R.id.cbDOD);

            ivItem = (ImageView) itemView.findViewById(R.id.ivItem);

            firebaseFirestore = FirebaseFirestore.getInstance();

        }
    }
}
