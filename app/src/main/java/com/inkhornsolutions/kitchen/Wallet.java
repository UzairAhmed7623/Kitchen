package com.inkhornsolutions.kitchen;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.inkhornsolutions.kitchen.Common.Common;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class Wallet extends AppCompatActivity {

    private TextView tvTotalRevenue, tvWithdrawn, tvRemaining, tvPaymentRequested, tvPaymentStatusPlace, tvPaymentStatus;
    private MaterialButton btnBack, btnWithdrawn;
    private EditText etWithdrawnPayment;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private double totalRevenue = 0.0, remaining = 0.0, withdrawn = 0.0;
    private Double totalRevenueWithoutDiscount = 0.0;
    private String resName, withdrawPayment = "0.0";
    private String withdrawnFromDatabase, paymentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.myColorThird));

        tvTotalRevenue = (TextView) findViewById(R.id.tvTotalRevenue);
        tvWithdrawn = (TextView) findViewById(R.id.tvWithdrawn);
        tvRemaining = (TextView) findViewById(R.id.tvRemaining);
        btnBack = (MaterialButton) findViewById(R.id.btnBack);
        btnWithdrawn = (MaterialButton) findViewById(R.id.btnWithdrawn);
        etWithdrawnPayment = (EditText) findViewById(R.id.etWithdrawnPayment);
        tvPaymentStatusPlace = (TextView) findViewById(R.id.tvPaymentStatusPlace);
        tvPaymentStatus = (TextView) findViewById(R.id.tvPaymentStatus);
        tvPaymentRequested = (TextView) findViewById(R.id.tvPaymentRequested);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        resName = getIntent().getStringExtra("resName");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Trips");

        firebaseFirestore.collection("Restaurants").document(resName).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.exists()) {
                    String totalRevenue = documentSnapshot.getString("totalRevenue");
                    String remaining = documentSnapshot.getString("remaining");
                    withdrawnFromDatabase = documentSnapshot.getString("withdrawn");
                    paymentStatus = documentSnapshot.getString("paymentStatus");
                    String paymentRequested = documentSnapshot.getString("paymentRequested");

                    if (documentSnapshot.getString("paymentRequested") != null) {
                        tvPaymentRequested.setText(paymentRequested);
                    }
                    else {
                        tvPaymentRequested.setText("0.0");
                    }

                    tvTotalRevenue.setText(totalRevenue);

                    if (documentSnapshot.getString("remaining") != null) {
                        tvRemaining.setText(remaining);
                    }
                    else {
                        tvRemaining.setText(totalRevenue);
                    }

                    if (documentSnapshot.getString("withdrawn") != null) {
                        tvWithdrawn.setText(withdrawnFromDatabase);
                    }
                    else {
                        tvWithdrawn.setText("0.0");
                    }

                    if (Objects.equals(paymentStatus, "pending") || Objects.equals(paymentStatus, "inProcess")) {
                        btnWithdrawn.setEnabled(false);
                        tvPaymentStatusPlace.setVisibility(View.VISIBLE);
                        tvPaymentStatus.setVisibility(View.VISIBLE);
                        tvPaymentStatus.setText(paymentStatus);
                        Toast.makeText(Wallet.this, "You can make a payment request after completing current request. Thanks ", Toast.LENGTH_LONG).show();
                    }
                    else if (Objects.equals(paymentStatus, "completed")){
                        tvPaymentStatusPlace.setVisibility(View.VISIBLE);
                        tvPaymentStatus.setVisibility(View.VISIBLE);
                        tvPaymentStatus.setText(paymentStatus);
                    }
                    else {
                        btnWithdrawn.setEnabled(true);
                    }
                }
            }
        });

//        btnWithdrawn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                withdrawPayment = etWithdrawnPayment.getText().toString();
//
//                if (!TextUtils.isEmpty(withdrawPayment)) {
//                    Toast.makeText(Wallet.this, "Clicked!", Toast.LENGTH_SHORT).show();
//
//                    Log.d("payment", withdrawn + " " + withdrawPayment);
//
//                    if (Double.parseDouble(withdrawPayment) > Double.parseDouble(tvRemaining.getText().toString())){
//                        Toast.makeText(Wallet.this, "You have insufficient balance to withdraw this amount.", Toast.LENGTH_SHORT).show();
//                    }
//                    else if (withdrawnFromDatabase != null){
//                        withdrawn = Double.parseDouble(withdrawPayment) + Double.parseDouble(withdrawnFromDatabase);
//                        tvWithdrawn.setText(String.format("%.2f", withdrawn));
//                    }
//                    else {
//                        withdrawn = Double.parseDouble(withdrawPayment);
//                        tvWithdrawn.setText(String.format("%.2f", withdrawn));
//                    }
//
//                    Log.d("payment", withdrawn + " " + withdrawPayment + " " + withdrawnFromDatabase);
//
//                    remaining = Double.parseDouble(tvRemaining.getText().toString()) - Double.parseDouble(withdrawPayment);
//                    tvRemaining.setText(String.valueOf(remaining));
//
//                    HashMap<String, String> paymentData = new HashMap<>();
//                    paymentData.put("remaining", String.format("%.2f", remaining));
//                    paymentData.put("withdrawn", String.format("%.2f", withdrawn));
//                    paymentData.put("paymentStatus", "pending");
//                    paymentData.put("paymentRequested", withdrawPayment);
//
//                    firebaseFirestore.collection("Restaurants").document(resName).set(paymentData, SetOptions.merge())
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull @NotNull Task<Void> task) {
//                                    tvPaymentRequested.setText(withdrawPayment);
//                                    Toast.makeText(Wallet.this, "Your request has been submitted successfully.", Toast.LENGTH_SHORT).show();
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull @NotNull Exception e) {
//
//                                }
//                            });
//
//                    firebaseFirestore.collection("Restaurants").document(resName).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                            if (value.exists()) {
//                                String totalRevenue = value.getString("totalRevenue");
//                                String remaining = value.getString("remaining");
//                                String withdrawn = value.getString("withdrawn");
//
//                                tvTotalRevenue.setText(String.format("%.2f", totalRevenue));
//                                tvRemaining.setText(String.format("%.2f", remaining));
//                                tvWithdrawn.setText(String.format("%.2f", withdrawn));
//
//                            }
//                        }
//                    });
//                    etWithdrawnPayment.setText("");
//                }
//                else {
//                    Toast.makeText(Wallet.this, "Please enter some amount.", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

        String kitchenKey = firebaseAuth.getCurrentUser().getUid();
        String userId = "JvGrrrZUcmQAWyfBrCtfLxlYQZx1";
        String orderId = "s1jz51zxkoj9";

        String subTotal1 = "250";


        btnWithdrawn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users").document(userId)
                        .collection("Cart").whereEqualTo("ID", orderId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                for (DocumentSnapshot documentSnapshot : querySnapshot) {

                                    String promotedOrder = documentSnapshot.getString("promotedOrder");
                                    String CHJPercentage = documentSnapshot.getString("CHJPercentage");
                                    String discountPercentage = documentSnapshot.getString("discountPercentage");
                                    String subTotal = documentSnapshot.getString("subTotal");
                                    String actualPrice = documentSnapshot.getString("actualPrice");

                                    if (promotedOrder.equals("yes")){
                                        if (CHJPercentage != null && discountPercentage != null &&
                                                Double.parseDouble(CHJPercentage) == Double.parseDouble(discountPercentage)){

                                            Log.d("wallet", "totalRevenuewithoutDiscount: "+subTotal);

                                        }
                                        else if (CHJPercentage != null && discountPercentage != null && actualPrice != null &&
                                                Double.parseDouble(CHJPercentage) > Double.parseDouble(discountPercentage)){
                                            Log.d("CHJRevenue1", ""+actualPrice);

                                            double difference = Double.parseDouble(CHJPercentage) - Double.parseDouble(discountPercentage);
                                            Log.d("CHJRevenue2", ""+difference);

                                            double CHJRevenue = (Double.parseDouble(actualPrice) / 100) * difference;
                                            Log.d("CHJRevenue3", ""+CHJRevenue);

                                            double resRevenuePer = (Double.parseDouble(actualPrice) / 100) * Double.parseDouble(CHJPercentage);
                                            double resRevenue = Double.parseDouble(actualPrice) - resRevenuePer;
                                            Log.d("CHJRevenue4", ""+resRevenue);

                                        }
                                        else if (CHJPercentage != null && discountPercentage != null && actualPrice != null &&
                                                Double.parseDouble(CHJPercentage) < Double.parseDouble(discountPercentage)){
                                            Log.d("CHJRevenue1", ""+actualPrice);

                                            double difference = Double.parseDouble(discountPercentage) - Double.parseDouble(CHJPercentage);
                                            Log.d("CHJRevenue2", ""+difference);

                                            double CHJRevenueMinus = (Double.parseDouble(actualPrice) / 100) * difference;
                                            Log.d("CHJRevenue3", ""+CHJRevenueMinus);

                                            double resRevenuePer = (Double.parseDouble(actualPrice) / 100) * Double.parseDouble(CHJPercentage);
                                            double resRevenue = Double.parseDouble(actualPrice) - resRevenuePer;
                                            Log.d("CHJRevenue4", ""+resRevenue);

                                        }
                                    }

//                                firebaseFirestore.collection("Restaurants").whereEqualTo("id", kitchenKey).get()
//                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                            @Override
//                                            public void onSuccess(QuerySnapshot querySnapshot) {
//
//                                                for (DocumentSnapshot documentSnapshot1 : querySnapshot) {
//
//                                                    String kitchenId = documentSnapshot1.getId();
//                                                    String totalRevenue = documentSnapshot1.getString("totalRevenue");
//
//                                                    Log.d("total", subTotal + " " + totalRevenue + " " + kitchenId);
//
//                                                    Double total = Double.parseDouble(subTotal) + Double.parseDouble(totalRevenue);
//
//                                                    HashMap<String, Object> revenue = new HashMap<>();
//                                                    revenue.put("totalRevenue", String.valueOf(total));
//
//                                                    firebaseFirestore.collection("Restaurants").document(kitchenId)
//                                                            .update(revenue)
//                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                @Override
//                                                                public void onSuccess(Void unused) {
//
//                                                                }
//                                                            });
//                                                }
//                                            }
//                                        });
                            }
                            }
                        });
            }
        });

//        for (String id : Common.id) {
//
//            firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
//                    .collection("Cart")
//                    .whereEqualTo("restaurantName", resName)
//                    .whereEqualTo("promotedOrder", "yes")
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot snapshots) {
//                            for (DocumentSnapshot documentSnapshot : snapshots) {
//                                if (documentSnapshot.exists()) {
//                                    String subTotal = documentSnapshot.getString("subTotal");
//                                    String CHJPercentage = documentSnapshot.getString("CHJPercentage");
//                                    String discountPercentage = documentSnapshot.getString("discountPercentage");
//                                    String promotedOrder = documentSnapshot.getString("promotedOrder");
//
//                                    if (promotedOrder != null && promotedOrder.equals("yes")) {
////                                        totalRevenue = totalRevenue + (Double.parseDouble(subTotal));
////                                        int discountedPrice = ((100 - Integer.parseInt(discountPercentage)) * Integer.parseInt(subTotal)) / 100;
//
//                                        if (CHJPercentage != null && discountPercentage != null
//                                                && Integer.parseInt(CHJPercentage) == Integer.parseInt(discountPercentage)) {
//
//                                            totalRevenuewithoutDiscount = Double.parseDouble(subTotal) * Double.parseDouble(CHJPercentage);
//                                            Log.d("wallet", "totalRevenuewithoutDiscount: "+totalRevenuewithoutDiscount);
//                                            totalRevenue += totalRevenuewithoutDiscount;
//
//                                        }
//
//
//                                    }
//                                    else {
//
////                                        totalRevenue = totalRevenue + (Integer.parseInt(subTotal)) * 0.8;
//                                    }
//                                }
//                            }
//                            Log.d("wallet1", "totalRevenue: "+totalRevenue);
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toasty.error(Wallet.this, e.getMessage(), Toasty.LENGTH_LONG).show();
//                        }
//                    });
//        }


//ye code firebase mn data insert krne k lie ha.
//        for (String id : Common.id) {
//
//            firebaseFirestore.collection("Users").document(id).collection("Cart")
//                    .whereEqualTo("restaurantName", resName)
////                    .whereNotEqualTo("promotedOrder", "yes")
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot snapshots) {
//                            for (DocumentSnapshot documentSnapshot : snapshots) {
//                                if (documentSnapshot.exists()) {
//                                    String id2 = documentSnapshot.getId();
//                                    String subTotal = documentSnapshot.getString("subTotal");
//                                    String CHJPercentage = documentSnapshot.getString("CHJPercentage");
//                                    String discountPercentage = documentSnapshot.getString("discountPercentage");
//                                    String promotedOrder = documentSnapshot.getString("promotedOrder");
//
//                                    Toast.makeText(Wallet.this, id2, Toast.LENGTH_SHORT).show();
//
//                                    HashMap<String, Object> insert = new HashMap<>();
//                                    insert.put("CHJPercentage", "20");
//                                    insert.put("discountPercentage", "20");
//
//                                    firebaseFirestore.collection("Users").document(id)
//                                            .collection("Cart").document(id2)
//                                            .set(insert, SetOptions.merge())
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    Toast.makeText(Wallet.this, "Ho gya!", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//
//                                }
//                            }
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toasty.error(Wallet.this, e.getMessage(), Toasty.LENGTH_LONG).show();
//                        }
//                    });
//        }


//        com.google.firebase.database.Query query = databaseReference.orderByChild("rider").equalTo(firebaseAuth.getCurrentUser().getUid());
//
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot snapshots : snapshot.getChildren()) {
//                        String total = snapshots.child("total").getValue(String.class);
//
//                        totalRevenue = totalRevenue + (Double.parseDouble(total) - 45);
//                    }
//                    totalRevenue *= 0.8;
//                    tvTotalRevenue.setText(String.format("%.2f", totalRevenue));
//
//                    String remaining = tvRemaining.getText().toString();
//                    if (TextUtils.isEmpty(remaining) || remaining.equals("null")) {
//                        tvRemaining.setText(String.format("%.2f", totalRevenue));
//                    }
//
//                    saveTotalInDatabase(totalRevenue);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//            }
//        });
    }

    private void saveTotalInDatabase(double totalRevenue) {
        HashMap<String, String> paymentData = new HashMap<>();
        paymentData.put("totalRevenue", String.format("%.2f", totalRevenue));

        firebaseFirestore.collection("Restaurants").document(resName).set(paymentData, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(Wallet.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}