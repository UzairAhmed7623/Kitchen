package com.inkhornsolutions.kitchen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inkhornsolutions.kitchen.Utils.UserUtils;
import com.inkhornsolutions.kitchen.adapters.OrdersDetailsAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OrderDetails extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private String resName, resId, orderID, userId;
    private ArrayList<OrdersModelClass> ordersDetails = new ArrayList<>();
    private RecyclerView rvOrdersDetails;
    private TextView tvTotalPrice;
    public double allTotalPrice = 0.00;
    private MaterialButton btnAccept, btnReject, btnFindRider, btnDispatch;
    private LinearLayout layoutAcceptReject, layoutFindRiderDispatch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        firebaseFirestore = FirebaseFirestore.getInstance();

        resName = getIntent().getStringExtra("resName");
        resId = getIntent().getStringExtra("resId");
        orderID = getIntent().getStringExtra("orderId");
        userId = getIntent().getStringExtra("userId");

        rvOrdersDetails = (RecyclerView) findViewById(R.id.rvOrdersDetails);
        tvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
        btnAccept = (MaterialButton) findViewById(R.id.btnAccept);
        btnReject = (MaterialButton) findViewById(R.id.btnReject);
        btnFindRider = (MaterialButton) findViewById(R.id.btnFindRider);
        btnDispatch = (MaterialButton) findViewById(R.id.btnDispatch);
        layoutAcceptReject = (LinearLayout) findViewById(R.id.layoutAcceptReject);
        layoutFindRiderDispatch = (LinearLayout) findViewById(R.id.layoutFindRiderDispatch);

        rvOrdersDetails.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OrderDetails.this);
        rvOrdersDetails.setLayoutManager(linearLayoutManager);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users").document(userId)
                        .collection("Cart").document(resId)
                        .update("status", "In progress").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(OrderDetails.this, "Status updated!", Toast.LENGTH_SHORT).show();
                        layoutAcceptReject.setVisibility(View.GONE);
                        layoutFindRiderDispatch.setVisibility(View.VISIBLE);

                        Log.d("TAG", userId);

                        UserUtils.acceptOrderNotificationToCustomer(layoutAcceptReject, OrderDetails.this, userId);

                        Intent intent = new Intent(OrderDetails.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });

        firebaseFirestore.collection("Users").document(userId)
                .collection("Cart").document(resId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if (status.equals("Pending")) {
                            layoutAcceptReject.setVisibility(View.VISIBLE);
                            layoutFindRiderDispatch.setVisibility(View.GONE);
                        }
                        else if (status.equals("In progress")) {
                            layoutAcceptReject.setVisibility(View.GONE);
                            layoutFindRiderDispatch.setVisibility(View.VISIBLE);
                        }
                        else if (status.equals("Rejected")||status.equals("Dispatched")){
                            layoutAcceptReject.setVisibility(View.GONE);
                            layoutFindRiderDispatch.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users").document(userId)
                        .collection("Cart").document(resId)
                        .update("status", "Rejected").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(OrderDetails.this, "Status updated!", Toast.LENGTH_SHORT).show();

                        UserUtils.rejectOrderNotificationToCustomer(layoutAcceptReject, OrderDetails.this, userId);

                        Intent intent = new Intent(OrderDetails.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });

        btnFindRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetails.this, FindDriver.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("lat", lat);
//                intent.putExtra("lng", lng);
//                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        });

        btnDispatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users").document(userId)
                        .collection("Cart").document(resId)
                        .update("status", "Dispatched").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(OrderDetails.this, "Status updated!", Toast.LENGTH_SHORT).show();

                        UserUtils.dispatchOrderNotificationToCustomer(layoutAcceptReject, OrderDetails.this, userId);

                        Intent intent = new Intent(OrderDetails.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });

        firebaseFirestore.collection("Users").document(userId).collection("Cart").document(resId)
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

                                double deductedPrice = Double.parseDouble(price) * 0.8;
                                double deductedTotal = Double.parseDouble(finalPrice) * 0.8;

                                OrdersModelClass ordersModelClass1 = new OrdersModelClass();

                                ordersModelClass1.setId(id);
                                ordersModelClass1.setItemName(itemName);
                                ordersModelClass1.setPrice(String.valueOf(deductedPrice));
                                ordersModelClass1.setItems_Count(itemCount);
                                ordersModelClass1.setFinalPrice(String.valueOf(deductedTotal));
                                ordersModelClass1.setpId(pId);

                                allTotalPrice = allTotalPrice + deductedTotal;

                                Log.d("asdfgh2", "" + id + itemName + price + itemCount + finalPrice + pId);

                                ordersDetails.add(ordersModelClass1);
                            }
                        }
                        rvOrdersDetails.setAdapter(new OrdersDetailsAdapter(getApplicationContext(), ordersDetails));
                        tvTotalPrice.setText("" + allTotalPrice);
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
