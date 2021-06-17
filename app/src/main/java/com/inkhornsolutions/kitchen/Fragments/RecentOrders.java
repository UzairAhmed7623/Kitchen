package com.inkhornsolutions.kitchen.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inkhornsolutions.kitchen.MainActivity;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.adapters.RecentOrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class RecentOrders extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private WaveSwipeRefreshLayout layoutOrderFrag;
    private RecyclerView rvOrdersFrag;
    private RecentOrdersAdapter adapter;
    private String resName;
    private ArrayList<OrdersModelClass> Orders = new ArrayList<>();
    private ProgressDialog progressDialog;

    public RecentOrders() {
        // Required empty public constructor
    }

    public static RecentOrders newInstance(){
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

        layoutOrderFrag = (WaveSwipeRefreshLayout) view.findViewById(R.id.layoutOrderFrag);
        layoutOrderFrag.setColorSchemeColors(Color.WHITE, Color.WHITE);
        layoutOrderFrag.setWaveColor(getContext().getColor(R.color.myColor));
        layoutOrderFrag.setMaxDropHeight(750);
        layoutOrderFrag.setMinimumHeight(750);

        Collections.sort(Orders, new Comparator<OrdersModelClass>() {
            @Override
            public int compare(OrdersModelClass o1, OrdersModelClass o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvOrdersFrag.setLayoutManager(linearLayoutManager);
        adapter = new RecentOrdersAdapter(getActivity(), Orders);
        rvOrdersFrag.setAdapter(adapter);

        MainActivity activity = (MainActivity) getActivity();
        resName = activity.sendData();


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Results are loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

//        ordersList(resName);

        layoutOrderFrag.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                onStart();
            }
        });

        return view;
    }

    private void ordersList(String resName) {

//        Toast.makeText(getActivity(), "resName1"+resName, Toast.LENGTH_SHORT).show();

        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot value) {

                Orders.clear();
                adapter.notifyDataSetChanged();

                for (QueryDocumentSnapshot documentSnapshot : value) {
                    if (documentSnapshot.exists()) {
                        String id = documentSnapshot.getId();

                        firebaseFirestore.collection("Users").document(id).collection("Cart")
                                .whereIn("status", Arrays.asList("Pending", "Rejected", "Dispatched"))
                                .whereEqualTo("restaurantName", resName)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()){

                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        if (documentSnapshot.exists()) {

                                            String resId = documentSnapshot.getId();
                                            String orderId = documentSnapshot.getString("ID");
                                            String time = documentSnapshot.getString("Time");
                                            String resName = documentSnapshot.getString("restaurantName");
                                            String status = documentSnapshot.getString("status");
                                            String total = documentSnapshot.getString("total");
                                            Double lat = documentSnapshot.getDouble("latlng.latitude");
                                            Double lng = documentSnapshot.getDouble("latlng.longitude");

                                            OrdersModelClass ordersModelClass = new OrdersModelClass();

                                            ordersModelClass.setResId(resId);
                                            ordersModelClass.setDate(time);
                                            ordersModelClass.setResName(resName);
                                            ordersModelClass.setStatus(status);
                                            ordersModelClass.setTotalPrice(total);
                                            ordersModelClass.setLat(lat);
                                            ordersModelClass.setLng(lng);
                                            ordersModelClass.setOrderId(orderId);
                                            ordersModelClass.setUserId(id);

                                            Orders.add(ordersModelClass);
                                            adapter.notifyDataSetChanged();

                                        } else {
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }
                                progressDialog.dismiss();
                                layoutOrderFrag.setRefreshing(false);
                            }
                        });
                    }
                }
            }

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ordersList(resName);
    }
}