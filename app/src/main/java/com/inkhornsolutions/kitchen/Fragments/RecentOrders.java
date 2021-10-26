package com.inkhornsolutions.kitchen.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inkhornsolutions.kitchen.Common.Common;
import com.inkhornsolutions.kitchen.MainActivity;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.adapters.RecentOrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class RecentOrders extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private SwipeRefreshLayout layoutOrderFrag;
    private RecyclerView rvOrdersFrag;
    private RecentOrdersAdapter adapter;
    private ArrayList<OrdersModelClass> Orders = new ArrayList<>();
    private String resName;
    private ProgressDialog progressDialog;
    boolean hasBeenPaused = false;
    EventListener<QuerySnapshot> eventListener;
    ListenerRegistration listenerRegistration;


    public RecentOrders() {
        // Required empty public constructor
    }

    public static RecentOrders newInstance() {
        return new RecentOrders();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recent_orders, container, false);

        rvOrdersFrag = (RecyclerView) view.findViewById(R.id.rvOrdersFrag);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        MainActivity activity = (MainActivity) getActivity();
        resName = activity.sendData();

        layoutOrderFrag = (SwipeRefreshLayout) view.findViewById(R.id.layoutOrderFrag);
        layoutOrderFrag.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getContext(), R.color.myColor));
        layoutOrderFrag.setColorSchemeColors(Color.WHITE, Color.WHITE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvOrdersFrag.setLayoutManager(linearLayoutManager);
        adapter = new RecentOrdersAdapter(getActivity(), Orders);
        rvOrdersFrag.setAdapter(adapter);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ordersList(resName);

        layoutOrderFrag.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                ordersList(resName);
            }
        });

        return view;
    }

    private void ordersList(String resName) {
        Orders.clear();
//        adapter.notifyDataSetChanged();

        for (String id : Common.id) {

                        firebaseFirestore.collection("Users").document(id).collection("Cart")
                    .whereEqualTo("restaurantName", resName)
                    .whereIn("status", Arrays.asList("Pending", "Rejected", "Dispatched", "Completed"))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot){
                                if (documentSnapshot.exists()){

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
                                        } else {
                                            ordersModelClass.setSubTotal(String.valueOf(total));
                                        }
                                    } else {
                                        if (subTotal != null) {
                                            Double deductedTotal = Double.parseDouble(subTotal) * 0.8;
                                            ordersModelClass.setSubTotal(String.valueOf(deductedTotal));
                                        } else {
                                            ordersModelClass.setSubTotal(String.valueOf(total));
                                        }
                                    }

                                    ordersModelClass.setTotalPrice(total);
                                    ordersModelClass.setResId(resId);
                                    ordersModelClass.setDate(time);
                                    ordersModelClass.setTimestamp(timestamp);
                                    ordersModelClass.setResName(resName);
                                    ordersModelClass.setStatus(status);
                                    ordersModelClass.setLat(lat);
                                    ordersModelClass.setLng(lng);
                                    ordersModelClass.setOrderId(orderId);
                                    ordersModelClass.setUserId(id);
                                    ordersModelClass.setSchedule(schedule);

                                    if (promotedOrder != null) {
                                        ordersModelClass.setPromotedOrder(promotedOrder);
                                    } else {
                                        ordersModelClass.setPromotedOrder("no");
                                    }

                                    Orders.add(ordersModelClass);
//                                    adapter.notifyDataSetChanged();
                                }
                            }

                            Collections.sort(Orders, new Comparator<OrdersModelClass>() {
                                @Override
                                public int compare(OrdersModelClass o1, OrdersModelClass o2) {
                                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                                }
                            });

                            progressDialog.dismiss();
                            layoutOrderFrag.setRefreshing(false);
                            adapter.notifyDataSetChanged();
                        }
                    });

//            eventListener = new EventListener<QuerySnapshot>() {
//                @Override
//                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
//
//                    if (error != null) {
//                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//                        Log.d("firebase", error.getMessage());
//
//                        return;
//                    }
//                    Orders.clear();
////                    adapter.notifyDataSetChanged();
//
//                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
//                        if (documentSnapshot.exists()){
//
//                            String resId = documentSnapshot.getId();
//                            String orderId = documentSnapshot.getString("ID");
//                            String time = documentSnapshot.getString("Time");
//                            String resName = documentSnapshot.getString("restaurantName");
//                            Timestamp timestamp = documentSnapshot.getTimestamp("timeStamp");
//                            String status = documentSnapshot.getString("status");
//                            Double lat = documentSnapshot.getDouble("latlng.latitude");
//                            Double lng = documentSnapshot.getDouble("latlng.longitude");
//                            String subTotal = documentSnapshot.getString("subTotal");
//                            String total = documentSnapshot.getString("total");
//                            String promotedOrder = documentSnapshot.getString("promotedOrder");
//
//                            OrdersModelClass ordersModelClass = new OrdersModelClass();
//
//                            if (promotedOrder != null && promotedOrder.equals("yes")){
//                                if (subTotal != null) {
//                                    Double deductedTotal = Double.parseDouble(subTotal);
//                                    ordersModelClass.setSubTotal(String.valueOf(deductedTotal));
//                                } else {
//                                    ordersModelClass.setSubTotal(String.valueOf(total));
//                                }
//                            } else {
//                                if (subTotal != null) {
//                                    Double deductedTotal = Double.parseDouble(subTotal) * 0.8;
//                                    ordersModelClass.setSubTotal(String.valueOf(deductedTotal));
//                                } else {
//                                    ordersModelClass.setSubTotal(String.valueOf(total));
//                                }
//                            }
//
//                            ordersModelClass.setTotalPrice(total);
//                            ordersModelClass.setResId(resId);
//                            ordersModelClass.setDate(time);
//                            ordersModelClass.setTimestamp(timestamp);
//                            ordersModelClass.setResName(resName);
//                            ordersModelClass.setStatus(status);
//                            ordersModelClass.setLat(lat);
//                            ordersModelClass.setLng(lng);
//                            ordersModelClass.setOrderId(orderId);
//                            ordersModelClass.setUserId(id);
//
//                            if (promotedOrder != null) {
//                                ordersModelClass.setPromotedOrder(promotedOrder);
//                            } else {
//                                ordersModelClass.setPromotedOrder("no");
//                            }
//
//                            Orders.add(ordersModelClass);
////                                adapter.notifyDataSetChanged();
//                        }
//                    }
//
//                    Collections.sort(Orders, new Comparator<OrdersModelClass>() {
//                        @Override
//                        public int compare(OrdersModelClass o1, OrdersModelClass o2) {
//                            return o2.getTimestamp().compareTo(o1.getTimestamp());
//                        }
//                    });
//
//                    progressDialog.dismiss();
//                    layoutOrderFrag.setRefreshing(false);
//                    adapter.notifyDataSetChanged();
//
//                }
//            };
//
//            listenerRegistration = firebaseFirestore.collection("Users").document("JvGrrrZUcmQAWyfBrCtfLxlYQZx1").collection("Cart")
//                    .whereEqualTo("restaurantName", resName)
//                    .whereIn("status", Arrays.asList("Pending", "Rejected", "Dispatched", "Completed"))
//                    .addSnapshotListener(eventListener);
        }
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm", calendar).toString();

        return date;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("methods", "onStart");
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
////            ordersList(resName);
//            Log.d("methods", "onResume inside");
//        }
//    }

}