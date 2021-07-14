package com.inkhornsolutions.kitchen;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class Wallet extends AppCompatActivity {

    private TextView tvTotalRevenue, tvWithdrawn, tvRemaining, tvPaymentRequested, tvPaymentStatusPlace, tvPaymentStatus;
    private MaterialButton btnBack, btnWithdrawn;
    private EditText etWithdrawnPayment;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private double totalRevenue = 0.0, remaining = 0.0, withdrawn = 0.0;
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
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    String totalRevenue = value.getString("totalRevenue");
                    String remaining = value.getString("remaining");
                    withdrawnFromDatabase = value.getString("withdrawn");
                    paymentStatus = value.getString("paymentStatus");
                    String paymentRequested = value.getString("paymentRequested");

                    if (value.getString("paymentRequested") != null) {
                        tvPaymentRequested.setText(paymentRequested);
                    }
                    else {
                        tvPaymentRequested.setText("0.0");
                    }

                    tvTotalRevenue.setText(String.valueOf(totalRevenue));

                    if (value.getString("remaining") != null) {
                        tvRemaining.setText(String.valueOf(remaining));
                    }
                    else {
                        tvRemaining.setText(String.valueOf(totalRevenue));
                    }

                    if (value.getString("withdrawn") != null) {
                        tvWithdrawn.setText(String.valueOf(withdrawnFromDatabase));
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
                    else {
                        btnWithdrawn.setEnabled(true);
                    }
                }
            }
        });

        btnWithdrawn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withdrawPayment = etWithdrawnPayment.getText().toString();

                if (!TextUtils.isEmpty(withdrawPayment)) {
                    Toast.makeText(Wallet.this, "Clicked!", Toast.LENGTH_SHORT).show();

                    Log.d("payment", withdrawn + " " + withdrawPayment);

                    if (withdrawnFromDatabase != null){
                        withdrawn = Double.parseDouble(withdrawPayment) + Double.parseDouble(withdrawnFromDatabase);
                        tvWithdrawn.setText(String.valueOf(withdrawn));
                    }
                    else {
                        withdrawn = Double.parseDouble(withdrawPayment);
                        tvWithdrawn.setText(String.valueOf(withdrawn));
                    }

                    Log.d("payment", withdrawn + " " + withdrawPayment + " " + withdrawnFromDatabase);

                    remaining = Double.parseDouble(tvRemaining.getText().toString()) - Double.parseDouble(withdrawPayment);
                    tvRemaining.setText(String.valueOf(remaining));

                    HashMap<String, String> paymentData = new HashMap<>();
                    paymentData.put("remaining", String.valueOf(remaining));
                    paymentData.put("withdrawn", String.valueOf(withdrawn));
                    paymentData.put("paymentStatus", "pending");
                    paymentData.put("paymentRequested", withdrawPayment);

                    firebaseFirestore.collection("Restaurants").document(resName).set(paymentData, SetOptions.merge())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    tvPaymentRequested.setText(withdrawPayment);
                                    Toast.makeText(Wallet.this, "Your request has been submitted successfully.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {

                                }
                            });

                    firebaseFirestore.collection("Restaurants").document(resName).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value.exists()) {
                                String totalRevenue = value.getString("totalRevenue");
                                String remaining = value.getString("remaining");
                                String withdrawn = value.getString("withdrawn");

                                tvTotalRevenue.setText(String.valueOf(totalRevenue));
                                tvRemaining.setText(String.valueOf(remaining));
                                tvWithdrawn.setText(String.valueOf(withdrawn));

                            }
                        }
                    });
                    etWithdrawnPayment.setText("");
                }
                else {
                    Toast.makeText(Wallet.this, "Please enter some amount.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Query query = databaseReference.orderByChild("rider").equalTo(firebaseAuth.getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshots : snapshot.getChildren()) {
                        String total = snapshots.child("total").getValue(String.class);

                        totalRevenue = totalRevenue + (Double.parseDouble(total) - 45);
                    }
                    totalRevenue *= 0.8;
                    tvTotalRevenue.setText(String.valueOf(totalRevenue));

                    String remaining = tvRemaining.getText().toString();
                    if (TextUtils.isEmpty(remaining) || remaining.equals("null")) {
                        tvRemaining.setText(String.valueOf(totalRevenue));
                    }

                    saveTotalInDatabase(totalRevenue);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void saveTotalInDatabase(double totalRevenue) {
        HashMap<String, String> paymentData = new HashMap<>();
        paymentData.put("totalRevenue", String.valueOf(totalRevenue));

        firebaseFirestore.collection("Restaurants").document(resName).set(paymentData, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
    }
}