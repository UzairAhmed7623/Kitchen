package com.inkhornsolutions.kitchen.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.inkhornsolutions.kitchen.MainActivity;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.adapters.RecentOrdersAdapter;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.Arrays;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class InProgress extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private WaveSwipeRefreshLayout layoutInProgress;
    private RecyclerView rvItemsInProgress;
    private RecentOrdersAdapter adapter;
    private String resName;
    private ArrayList<OrdersModelClass> Orders = new ArrayList<>();
    private ProgressDialog progressDialog;

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

        layoutInProgress = (WaveSwipeRefreshLayout) view.findViewById(R.id.layoutInProgress);
        layoutInProgress.setColorSchemeColors(Color.WHITE, Color.WHITE);
        layoutInProgress.setWaveColor(getContext().getColor(R.color.myColor));
        layoutInProgress.setMaxDropHeight(750);
        layoutInProgress.setMinimumHeight(750);

        rvItemsInProgress.setLayoutManager(new LinearLayoutManager(view.getContext()));

        adapter = new RecentOrdersAdapter(getActivity(), Orders);
        rvItemsInProgress.setAdapter(adapter);

        MainActivity activity = (MainActivity) getActivity();
        resName = activity.sendData();


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Results are loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ordersList(resName);

        layoutInProgress.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                ordersList(resName);
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

                                        OrdersModelClass ordersModelClass = new OrdersModelClass();

                                        ordersModelClass.setResId(resId);
                                        ordersModelClass.setDate(time);
                                        ordersModelClass.setResName(resName);
                                        ordersModelClass.setStatus(status);
                                        ordersModelClass.setTotalPrice(total);
                                        ordersModelClass.setLat(lat);
                                        ordersModelClass.setLng(lng);
                                        ordersModelClass.setOrderId(orderId);

                                        Orders.add(ordersModelClass);
                                        adapter.notifyDataSetChanged();

                                    }
                                    else {
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                }
                progressDialog.dismiss();
                layoutInProgress.setRefreshing(false);
            }

        });
    }
}