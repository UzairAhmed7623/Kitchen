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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

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
    private double totalRevenue2 = 0.0, remaining = 0.0, withdrawn = 0.0;
    private Double totalRevenueWithoutDiscount = 0.0;
    private String resName, withdrawPayment = "0.0";
    private String withdrawnFromDatabase, paymentStatus;
    private String remainingPayment = "", totalRevenue = "";

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
                    totalRevenue = documentSnapshot.getString("totalRevenue");
                    remainingPayment = documentSnapshot.getString("remaining");
                    withdrawnFromDatabase = documentSnapshot.getString("withdrawn");
                    paymentStatus = documentSnapshot.getString("paymentStatus");
                    String paymentRequested = documentSnapshot.getString("paymentRequested");

                    if (documentSnapshot.getString("paymentRequested") != null) {
                        tvPaymentRequested.setText(paymentRequested);
                    }
                    else {
                        tvPaymentRequested.setText("0.0");
                    }
                    remaining = Double.parseDouble(totalRevenue) - Double.parseDouble(withdrawnFromDatabase);
                    tvRemaining.setText(String.valueOf(remaining));

                    if (documentSnapshot.getString("totalRevenue") != null) {
                        tvTotalRevenue.setText(totalRevenue);

                        btnWithdrawn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                withdrawPayment = etWithdrawnPayment.getText().toString();

                                if (!TextUtils.isEmpty(withdrawPayment)) {

                                    Log.d("payment", withdrawn + " " + withdrawPayment);

                                    if (Double.parseDouble(withdrawPayment) > remaining) {
                                        Toasty.error(Wallet.this, "You have insufficient balance to withdraw this amount.", Toasty.LENGTH_LONG).show();
                                    }
                                    else if (withdrawPayment.equals("") || Double.parseDouble(withdrawPayment) <= 0) {
                                        Toasty.error(Wallet.this, "You cannot send payment request with no amount.", Toasty.LENGTH_LONG).show();
                                    }
                                    else {
                                        withdrawn = Double.parseDouble(withdrawPayment) + Double.parseDouble(withdrawnFromDatabase);
                                        tvWithdrawn.setText(String.format("%.2f", withdrawn));

                                        Log.d("payment", withdrawn + " " + withdrawPayment + " " + withdrawnFromDatabase);

                                        remaining = Double.parseDouble(totalRevenue) - withdrawn;
                                        tvRemaining.setText(String.valueOf(remaining));

                                        HashMap<String, String> paymentData = new HashMap<>();
                                        paymentData.put("remaining", String.format("%.2f", remaining));
                                        paymentData.put("withdrawn", String.format("%.2f", withdrawn));
                                        paymentData.put("paymentStatus", "pending");
                                        paymentData.put("paymentRequested", withdrawPayment);

                                        firebaseFirestore.collection("Restaurants").document(resName)
                                                .set(paymentData, SetOptions.merge())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        tvPaymentRequested.setText(withdrawPayment);
                                                        Toasty.info(Wallet.this, "Your request has been submitted successfully.", Toasty.LENGTH_SHORT).show();
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

                                                    tvTotalRevenue.setText(totalRevenue);
                                                    tvRemaining.setText(remaining);
                                                    tvWithdrawn.setText(withdrawn);

                                                }
                                            }
                                        });
                                        etWithdrawnPayment.setText("");

                                    }
                                } else {
                                    Toasty.warning(Wallet.this, "Please enter some amount.", Toasty.LENGTH_SHORT).show();

                                }

                            }
                        });

                    }

//                    if (documentSnapshot.getString("remaining") != null) {
//                        tvRemaining.setText(remainingPayment);
//                    }
//                    else {
//                        tvRemaining.setText(totalRevenue);
//                    }

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
                        Toasty.warning(Wallet.this, "You can make a payment request after completing current request. Thanks", Toasty.LENGTH_SHORT).show();
                    }
                    else if (Objects.equals(paymentStatus, "completed")) {
                        tvPaymentStatusPlace.setVisibility(View.VISIBLE);
                        tvPaymentStatus.setVisibility(View.VISIBLE);
                        tvPaymentStatus.setText(paymentStatus);
                        tvPaymentRequested.setText("0.0");
                        btnWithdrawn.setEnabled(true);
                    }
                    else {
                        btnWithdrawn.setEnabled(true);
                    }
                }
            }
        });
    }
}