package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;

import com.inkhornsolutions.kitchen.adapters.OrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class Orders extends AppCompatActivity {

    private Toolbar toolbarOrders;
    private RecyclerView rvOrders;
    private List<OrdersModelClass> Orders = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    OrdersModelClass ordersModelClass;
    WaveSwipeRefreshLayout layoutOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarOrders = (Toolbar) findViewById(R.id.toolbarOrders);
        setSupportActionBar(toolbarOrders);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        String resName = getIntent().getStringExtra("resName");

        layoutOrder = (WaveSwipeRefreshLayout) findViewById(R.id.layoutOrder);

        rvOrders = (RecyclerView) findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        OrdersList(resName);

        layoutOrder.setColorSchemeColors(Color.WHITE, Color.WHITE);
        layoutOrder.setWaveColor(getColor(R.color.myColor));
        layoutOrder.setMaxDropHeight(750);
        layoutOrder.setMinimumHeight(750);
//        layoutOrder.setWaveColor(0xFF000000+new Random().nextInt(0xFFFFFF)); // Random color assign

        layoutOrder.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
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
                                .whereIn("status", Arrays.asList("Pending","In progress"))
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
                                        Double lat = documentSnapshot.getDouble("latlng.latitude");
                                        Double lng = documentSnapshot.getDouble("latlng.longitude");

                                        ordersModelClass = new OrdersModelClass();

                                        ordersModelClass.setResId(resId);
                                        ordersModelClass.setDate(time);
                                        ordersModelClass.setResName(resName);
                                        ordersModelClass.setStatus(status);
                                        ordersModelClass.setTotalPrice(total);
                                        ordersModelClass.setLat(lat);
                                        ordersModelClass.setLng(lng);
                                        ordersModelClass.setOrderId(orderId);

                                        Orders.add(ordersModelClass);


                                    }
                                    else {
                                        Snackbar.make(getParent().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                rvOrders.setAdapter(new OrdersAdapter(Orders.this, Orders));
                            }
                        });
                    }
                }
                layoutOrder.setRefreshing(false);
            }

        });
    }
}