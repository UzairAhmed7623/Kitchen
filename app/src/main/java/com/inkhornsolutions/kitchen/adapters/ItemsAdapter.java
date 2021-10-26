package com.inkhornsolutions.kitchen.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.SetOptions;
import com.inkhornsolutions.kitchen.Items;
import com.inkhornsolutions.kitchen.Profile;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.ItemsModelClass;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        String scheduled = itemsModelClass.getScheduled();

        holder.tvItem.setText(itemName);
        Glide.with(context).load(imageUri).placeholder(R.drawable.main_course).fitCenter().into(holder.ivItem);
//        Picasso.get().load(imageUri).placeholder(R.drawable.food_placeholder).fit().into(holder.ivItem);

        holder.tvItemPrice.setText("PKR"+price);
        holder.tvItemDescription.setText(description);
        holder.tvItemSchedule.setText("Available: "+ schedule);

        holder.cbDOD.setChecked(isDODAvailable.equals("yes"));
        holder.cbUFG.setChecked(scheduled.equals("1"));

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

        holder.cbUFG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked){

                    View view = LayoutInflater.from(context).inflate(R.layout.edit_details, null);
                    EditText editText = (EditText) view.findViewById(R.id.editText);
                    EditText editText2 = (EditText) view.findViewById(R.id.editText2);
                    TextInputLayout TextInputLayout2 = (TextInputLayout) view.findViewById(R.id.TextInputLayout2);
                    Button btnAdd = (Button) view.findViewById(R.id.btnAdd);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(view);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    alertDialog.show();

                    TextInputLayout2.setVisibility(View.GONE);
                    editText2.setVisibility(View.GONE);

                    editText.setHint("Write your cooking time in hours");

                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText2.setInputType(InputType.TYPE_CLASS_TEXT);

                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String time = editText.getText().toString();

                            if (TextUtils.isEmpty(time)){
                                editText.setError("Please write your first name");
                            }
                            else {
                                holder.tvTime.setText(time + " Hours");

                                firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(itemName)
                                        .update("scheduled", time)
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

                                alertDialog.dismiss();
                            }

                        }
                    });
                }
                else {
                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(itemName)
                            .update("scheduled","0")
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
        private TextView tvItem, tvItemPrice, tvItemSchedule, tvItemDescription, tvTime;
        private ImageView ivItem;
        private MaterialCheckBox cbDOD, cbUFG;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = (TextView) itemView.findViewById(R.id.tvItem);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);
            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);
            tvItemDescription = (TextView) itemView.findViewById(R.id.tvItemDescription);
            cbDOD = (MaterialCheckBox) itemView.findViewById(R.id.cbDOD);
            cbUFG = (MaterialCheckBox) itemView.findViewById(R.id.cbUFG);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);

            ivItem = (ImageView) itemView.findViewById(R.id.ivItem);

            firebaseFirestore = FirebaseFirestore.getInstance();

        }
    }
}
