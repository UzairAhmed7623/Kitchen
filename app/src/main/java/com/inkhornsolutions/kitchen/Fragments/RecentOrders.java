package com.inkhornsolutions.kitchen.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import com.inkhornsolutions.kitchen.adapters.RecentOrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class RecentOrders extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private SwipeRefreshLayout layoutOrderFrag;
    private RecyclerView rvOrdersFrag;
    private RecentOrdersAdapter adapter;
    private ArrayList<OrdersModelClass> Orders;
    private String resName;
    private ProgressDialog progressDialog;
    boolean hasBeenPaused = false;

    public RecentOrders() {
        // Required empty public constructor
    }

    public static RecentOrders newInstance() {
        return new RecentOrders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

        Orders = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvOrdersFrag.setLayoutManager(linearLayoutManager);
        adapter = new RecentOrdersAdapter(getActivity(), Orders);
        rvOrdersFrag.setAdapter(adapter);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {

                            if (error == null) {
                                for (DocumentSnapshot documentSnapshot : snapshots.getDocuments()) {
                                    if (documentSnapshot.exists()) {

                                        String resId = documentSnapshot.getId();
                                        String orderId = documentSnapshot.getString("ID");
                                        String time = documentSnapshot.getString("Time");
                                        String resName = documentSnapshot.getString("restaurantName");
                                        String status = documentSnapshot.getString("status");
                                        Double lat = documentSnapshot.getDouble("latlng.latitude");
                                        Double lng = documentSnapshot.getDouble("latlng.longitude");
                                        String subTotal = documentSnapshot.getString("subTotal");
                                        String total = documentSnapshot.getString("total");
                                        String promotedOrder = documentSnapshot.getString("promotedOrder");

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
                                        ordersModelClass.setResName(resName);
                                        ordersModelClass.setStatus(status);
                                        ordersModelClass.setLat(lat);
                                        ordersModelClass.setLng(lng);
                                        ordersModelClass.setOrderId(orderId);
                                        ordersModelClass.setUserId(id);

                                        if (promotedOrder != null) {
                                            ordersModelClass.setPromotedOrder(promotedOrder);
                                        } else {
                                            ordersModelClass.setPromotedOrder("no");
                                        }

                                        Orders.add(ordersModelClass);
                                        adapter.notifyDataSetChanged();

                                    }
                                    else {
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }

                                }

                                Collections.sort(Orders, new Comparator<OrdersModelClass>() {
                                    @Override
                                    public int compare(OrdersModelClass o1, OrdersModelClass o2) {
                                        return o2.getDate().compareTo(o1.getDate());
                                    }
                                });

                                progressDialog.dismiss();
                                layoutOrderFrag.setRefreshing(false);
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

        if (!hasBeenPaused) {
            Orders.clear();
//            adapter.notifyDataSetChanged();
            ordersList(resName);
            Log.d("methods", "onStart inside");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hasBeenPaused = true;

        Log.d("methods", "onPause");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("methods", "onResume");

        if(hasBeenPaused){

            ordersList(resName);
            Log.d("methods", "onResume inside");
        }
    }
}