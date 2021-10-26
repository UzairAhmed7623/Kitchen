package com.inkhornsolutions.kitchen.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inkhornsolutions.kitchen.Common.Common;
import com.inkhornsolutions.kitchen.MainActivity;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.adapters.InProgressOrdersAdapter;
import com.inkhornsolutions.kitchen.adapters.RecentOrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InProgress extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private SwipeRefreshLayout layoutInProgress;
    private RecyclerView rvItemsInProgress;
    private InProgressOrdersAdapter adapter;
    private String resName;
    private ArrayList<OrdersModelClass> Orders;
    private ProgressDialog progressDialog;
    boolean hasBeenPaused = false;


    public InProgress() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_in_progress, container, false);
        rvItemsInProgress = (RecyclerView) view.findViewById(R.id.rvItemsInProgress);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        layoutInProgress = (SwipeRefreshLayout) view.findViewById(R.id.layoutInProgress);
        layoutInProgress.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getContext(), R.color.myColor));
        layoutInProgress.setColorSchemeColors(Color.WHITE, Color.WHITE);

        Orders = new ArrayList<>();
        rvItemsInProgress.setLayoutManager(new LinearLayoutManager(view.getContext()));

        adapter = new InProgressOrdersAdapter(getActivity(), Orders);
        rvItemsInProgress.setAdapter(adapter);

        MainActivity activity = (MainActivity) getActivity();
        resName = activity.sendData();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Results are loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        layoutInProgress.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                ordersList(resName);
            }
        });

        return view;
    }

    private void ordersList(String resName) {

        Orders.clear();
        adapter.notifyDataSetChanged();

        for (String id : Common.id) {

            firebaseFirestore.collection("Users").document(id).collection("Cart")
                                .whereEqualTo("status", "In progress")
                                .whereEqualTo("restaurantName", resName)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                            if (error == null) {
                                for (DocumentSnapshot documentSnapshot : snapshots.getDocuments()) {
                                    if (documentSnapshot.exists()) {
                                        String resId = documentSnapshot.getId();
                                        String orderId = documentSnapshot.getString("ID");
                                        String time = documentSnapshot.getString("Time");
                                        Timestamp timestamp = documentSnapshot.getTimestamp("timeStamp");
                                        String resName = documentSnapshot.getString("restaurantName");
                                        String status = documentSnapshot.getString("status");
                                        Double lat = documentSnapshot.getDouble("latlng.latitude");
                                        Double lng = documentSnapshot.getDouble("latlng.longitude");
                                        String subTotal = documentSnapshot.getString("subTotal");
                                        String total = documentSnapshot.getString("total");
                                        String promotedOrder = documentSnapshot.getString("promotedOrder");
                                        String schedule = documentSnapshot.getString("schedule");

                                        OrdersModelClass ordersModelClass = new OrdersModelClass();

                                        if (promotedOrder != null && promotedOrder.equals("yes")){
                                            if (subTotal != null) {
                                                Double deductedTotal = Double.parseDouble(subTotal);
                                                ordersModelClass.setSubTotal(String.valueOf(deductedTotal));
                                            }
                                            else {
                                                ordersModelClass.setSubTotal(String.valueOf(total));
                                            }
                                        }
                                        else {
                                            if (subTotal != null) {
                                                Double deductedTotal = Double.parseDouble(subTotal) * 0.8;
                                                ordersModelClass.setSubTotal(String.valueOf(deductedTotal));
                                            }
                                            else {
                                                ordersModelClass.setSubTotal(String.valueOf(total));
                                            }
                                        }

                                        ordersModelClass.setResId(resId);
                                        ordersModelClass.setDate(time);
                                        ordersModelClass.setTimestamp(timestamp);
                                        ordersModelClass.setResName(resName);
                                        ordersModelClass.setStatus(status);
                                        ordersModelClass.setTotalPrice(total);
                                        ordersModelClass.setLat(lat);
                                        ordersModelClass.setLng(lng);
                                        ordersModelClass.setOrderId(orderId);
                                        ordersModelClass.setUserId(id);
                                        ordersModelClass.setSchedule(schedule);

                                        if (promotedOrder != null){
                                            ordersModelClass.setPromotedOrder(promotedOrder);
                                        }
                                        else {
                                            ordersModelClass.setPromotedOrder("no");
                                        }

                                        Orders.add(ordersModelClass);
//                                        adapter.notifyDataSetChanged();

                                    }
                                    else {
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                Collections.sort(Orders, new Comparator<OrdersModelClass>() {
                                    @Override
                                    public int compare(OrdersModelClass o1, OrdersModelClass o2) {
                                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                                    }
                                });

                                progressDialog.dismiss();
                                layoutInProgress.setRefreshing(false);
                                adapter.notifyDataSetChanged();
                            }
                            else {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("firebase", error.getMessage());
                            }
                        }
                    });
        }



    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("methods", "onStart");

        ordersList(resName);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("methods", "onPause");

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("methods", "onResume");
//
//        if(hasBeenPaused){
//
//            ordersList(resName);
//            Log.d("methods", "onResume inside");
//        }
//    }
}