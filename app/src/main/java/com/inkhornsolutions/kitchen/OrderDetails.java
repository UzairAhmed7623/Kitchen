package com.inkhornsolutions.kitchen;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.kitchen.adapters.MemeberComOrders;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OrderDetails extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private String resName, resId, orderID;
    private ArrayList<OrdersModelClass> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        firebaseFirestore = FirebaseFirestore.getInstance();

        resName = getIntent().getStringExtra("resName");
        resId = getIntent().getStringExtra("resId");
        orderID = getIntent().getStringExtra("orderId");

        Toast.makeText(this, orderID, Toast.LENGTH_SHORT).show();

        firebaseFirestore.collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                String id = documentSnapshot.getId();

                                firebaseFirestore.collection("Users").document(id).collection("Cart").document(resId)
                                        .collection("Orders")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                    if (documentSnapshot.exists()) {

                                                        String id = documentSnapshot.getId();
                                                        String itemName = documentSnapshot.getString("title");
                                                        String price = documentSnapshot.getString("price");
                                                        String itemCount = documentSnapshot.getString("items_count");
                                                        String finalPrice = documentSnapshot.getString("final_price");
                                                        String pId = documentSnapshot.getString("pId");

                                                        OrdersModelClass ordersModelClass1 = new OrdersModelClass();

                                                        ordersModelClass1.setId(id);
                                                        ordersModelClass1.setItemName(itemName);
                                                        ordersModelClass1.setPrice(price);
                                                        ordersModelClass1.setItems_Count(itemCount);
                                                        ordersModelClass1.setFinalPrice(finalPrice);
                                                        ordersModelClass1.setpId(pId);

                                                        Log.d("asdfgh2", "" + id + itemName + price + itemCount + finalPrice + pId);

                                                        orders.add(ordersModelClass1);
                                                    }
                                                }
//                                              holder.rvComMember.setAdapter(new MemeberComOrders(arrayListMember));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    }
                });

    }
}

//        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//
//        firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//@Override
//public void onComplete(@NonNull Task<QuerySnapshot> task) {
//        if (task.isSuccessful()){
//        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
//        if (documentSnapshot.exists()){
//        String id = documentSnapshot.getId();
//
//        firebaseFirestore.collection("Users").document(id)
//        .collection("Cart").document(resId)
//        .update("status","In progress").addOnCompleteListener(new OnCompleteListener<Void>() {
//@Override
//public void onComplete(@NonNull Task<Void> task) {
//
//        }
//        });
//        }
//        }
//        }
//        }
//        });
//        Orders.get(position).setStatus("In progress");
//        notifyDataSetChanged();
//
//        Toast.makeText(v.getContext(), "Status updated!", Toast.LENGTH_SHORT).show();
//        holder.hideLayout.setVisibility(View.GONE);
//        holder.hideLayout2.setVisibility(View.VISIBLE);
//        }
//        });
//
//        holder.btnReject.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//@Override
//public void onComplete(@NonNull Task<QuerySnapshot> task) {
//        if (task.isSuccessful()){
//        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
//        if (documentSnapshot.exists()){
//        String id = documentSnapshot.getId();
//
//        firebaseFirestore.collection("Users").document(id)
//        .collection("Cart").document(resId)
//        .update("status","Rejected").addOnCompleteListener(new OnCompleteListener<Void>() {
//@Override
//public void onComplete(@NonNull Task<Void> task) {
//
//        }
//        });
//        }
//        }
//        }
//        }
//        });
//        Orders.remove(position);
//        notifyDataSetChanged();
//        Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show();
//        }
//        });
//
//        holder.btnRider.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        Intent intent = new Intent(context, FindDriver.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("lat", lat);
//        intent.putExtra("lng", lng);
//        intent.putExtra("orderId", orderId);
//
//        v.getContext().startActivity(intent);
//        }
//        });
//
//        holder.btnDispatch.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        Toast.makeText(context, "dispatch", Toast.LENGTH_SHORT).show();
//        }
//        });
