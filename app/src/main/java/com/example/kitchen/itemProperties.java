package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class itemProperties extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private EditText etItemPricep, etAvailablep, etScedulep;
    private Button btnDonep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_properties);

        firebaseFirestore = FirebaseFirestore.getInstance();

        etItemPricep = (EditText) findViewById(R.id.etItemPricep);
        etAvailablep = (EditText) findViewById(R.id.etAvailablep);
        etScedulep = (EditText) findViewById(R.id.etScedulep);

        btnDonep = (Button) findViewById(R.id.btnDonep);

//        String resName = getIntent().getStringExtra("restaurant");
        String resName = "Alfredo Pizza Pasta";
        String itemName = getIntent().getStringExtra("itemName");

        Log.d("TAG", resName+" "+itemName);

        btnDonep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String price = etItemPricep.getText().toString().trim().replace("PKR", "");
                String avail = etAvailablep.getText().toString();
                String schedule = etScedulep.getText().toString();

                Log.d("TAG", resName+" "+itemName);

                if (etItemPricep.getText().toString() != null){
                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                            .document(itemName).update("price", price);
                }
                else if (etAvailablep.getText().toString() != null){
                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                            .document(itemName).update("available", avail);
                }
                else if (etScedulep.getText().toString() != null){
                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                            .document(itemName).update("schedule", schedule);
                }
                else {
                    Log.d("TAG", "Empty");
                }

                Intent intent = new Intent(itemProperties.this, MainActivity.class);
                intent.putExtra("resName", resName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

    }
}