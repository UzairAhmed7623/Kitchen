package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.kitchen.adapters.CompletedOrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class CompletedOrders extends AppCompatActivity {

    private TextView tvEarnings;
    private Toolbar toolbarComOrders;
    private RecyclerView rvComOrders;
    private List<OrdersModelClass> Orders = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private OrdersModelClass ordersModelClass;
    private WaveSwipeRefreshLayout layoutComOrder;
    private double sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_orders);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarComOrders = (Toolbar) findViewById(R.id.toolbarComOrders);
        setSupportActionBar(toolbarComOrders);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        String resName = getIntent().getStringExtra("resName");

        tvEarnings = (TextView) findViewById(R.id.tvEarnings);

        layoutComOrder = (WaveSwipeRefreshLayout) findViewById(R.id.layoutComOrder);

        rvComOrders = (RecyclerView) findViewById(R.id.rvComOrders);
        rvComOrders.setLayoutManager(new LinearLayoutManager(this));

        OrdersList(resName);

        layoutComOrder.setColorSchemeColors(Color.WHITE, Color.WHITE);
        layoutComOrder.setWaveColor(getColor(R.color.myColor));
        layoutComOrder.setMaxDropHeight(750);
        layoutComOrder.setMinimumHeight(750);
//        layoutOrder.setWaveColor(0xFF000000+new Random().nextInt(0xFFFFFF)); // Random color assign

        layoutComOrder.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                OrdersList(resName);
            }
        });
    }
    private void OrdersList(String resName) {
        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot value) {

                Orders.clear();

                for (QueryDocumentSnapshot documentSnapshot : value){
                    if (documentSnapshot.exists()){
                        String id = documentSnapshot.getId();

                        firebaseFirestore.collection("Users").document(id).collection("Cart")
                                .whereEqualTo("status", "Completed")
                                .whereEqualTo("restaurant name", resName)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                    if (documentSnapshot.exists()){

                                        String resId = documentSnapshot.getId();
                                        String orderId = documentSnapshot.getString("ID");
                                        String time = documentSnapshot.getString("Time");
                                        String resName = documentSnapshot.getString("restaurant name");
                                        String status = documentSnapshot.getString("status");
                                        String total = documentSnapshot.getString("total");

                                        ordersModelClass = new OrdersModelClass();

                                        ordersModelClass.setResId(resId);
                                        ordersModelClass.setDate(time);
                                        ordersModelClass.setResName(resName);
                                        ordersModelClass.setStatus(status);
                                        ordersModelClass.setTotalPrice(total);
                                        ordersModelClass.setOrderId(orderId);


                                        ArrayList<Double> totalPrice = new ArrayList<>();
                                        totalPrice.add(Double.parseDouble(total));

                                        for (int i = 0; i < totalPrice.size(); i++)
                                        {
                                            sum = sum + totalPrice.get(i);
                                        }
                                        tvEarnings.setText("PKR " + sum);

                                        Orders.add(ordersModelClass);
                                    }
                                    else {
                                        Snackbar.make(getParent().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                                rvComOrders.setAdapter(new CompletedOrdersAdapter(CompletedOrders.this, Orders));
                            }
                        });
                    }
                }
                layoutComOrder.setRefreshing(false);
            }
        });
    }

}