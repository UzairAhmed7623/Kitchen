package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.kitchen.adapters.OrdersAdapter;
import com.example.kitchen.modelclasses.OrdersModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.StructuredQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Orders extends AppCompatActivity {

    private Toolbar toolbarOrders;
    private RecyclerView rvOrders;
    private List<OrdersModelClass> Orders = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    OrdersModelClass ordersModelClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarOrders = (Toolbar) findViewById(R.id.toolbarOrders);
        setSupportActionBar(toolbarOrders);

        String resName = getIntent().getStringExtra("resName");

        rvOrders = (RecyclerView) findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));


        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

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

                                            Orders.add(ordersModelClass);


                                        }
                                        else {
                                            Toast.makeText(Orders.this, "Data not found!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    rvOrders.setAdapter(new OrdersAdapter(Orders.this, Orders));
                                }
                            });

                        }
                    }

            }
        });
    }
}