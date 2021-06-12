package com.inkhornsolutions.kitchen.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.adapters.OrdersAdapter;
import com.inkhornsolutions.kitchen.adapters.MainActivityAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class RecentOrders extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private WaveSwipeRefreshLayout layoutOrderFrag;
    private RecyclerView rvOrdersFrag;
    private MainActivityAdapter adapter;
    private String resName;
    private List<OrdersModelClass> Orders = new ArrayList<>();
    private OrdersModelClass ordersModelClass;

    public RecentOrders() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recent_orders, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        layoutOrderFrag = (WaveSwipeRefreshLayout) view.findViewById(R.id.layoutOrderFrag);
        layoutOrderFrag.setColorSchemeColors(Color.WHITE, Color.WHITE);
        layoutOrderFrag.setWaveColor(getContext().getColor(R.color.myColor));
        layoutOrderFrag.setMaxDropHeight(750);
        layoutOrderFrag.setMinimumHeight(750);

        resName = this.getArguments().getString("resName");

        rvOrdersFrag = (RecyclerView) view.findViewById(R.id.rvOrdersFrag);
        rvOrdersFrag.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new MainActivityAdapter(getActivity(), Orders);
        rvOrdersFrag.setAdapter(adapter);


        OrdersList("Soban Fish");

        Toast.makeText(getActivity(), "resName"+resName, Toast.LENGTH_SHORT).show();

        layoutOrderFrag.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                OrdersList("Soban Fish");
            }
        });

        return view;
    }

    private void OrdersList(String resName) {

        Toast.makeText(getActivity(), "resName1"+resName, Toast.LENGTH_SHORT).show();

        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot value) {

                Orders.clear();

                for (QueryDocumentSnapshot documentSnapshot : value){
                    if (documentSnapshot.exists()){
                        String id = documentSnapshot.getId();

                        firebaseFirestore.collection("Users").document(id).collection("Cart")
                                .whereIn("status", Arrays.asList("Pending","In progress"))
                                .whereEqualTo("restaurantName", resName)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                    if (documentSnapshot.exists()){

                                        String resId = documentSnapshot.getId();
                                        String orderId = documentSnapshot.getString("ID");
                                        String time = documentSnapshot.getString("Time");
                                        String resName = documentSnapshot.getString("restaurantName");
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
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                }
                layoutOrderFrag.setRefreshing(false);
            }

        });
    }

}