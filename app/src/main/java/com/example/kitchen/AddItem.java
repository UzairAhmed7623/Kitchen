package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class AddItem extends AppCompatActivity {

    private EditText etItemName, etItemPrice, etItemImage, etScedule, etAvailable;
    private Button btnDone;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        String resName = getIntent().getStringExtra("resName");

        etItemName = (EditText) findViewById(R.id.etItemName);
        etItemPrice = (EditText) findViewById(R.id.etItemPrice);
        etItemImage = (EditText) findViewById(R.id.etItemImage);
        etScedule = (EditText) findViewById(R.id.etScedule);
        etAvailable = (EditText) findViewById(R.id.etAvailable);
        btnDone = (Button) findViewById(R.id.btnDone);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etItemName.getText().toString();
                String price = etItemPrice.getText().toString();
                String image = etItemImage.getText().toString();
                String schedule = etScedule.getText().toString();
                String availability = etAvailable.getText().toString();

                HashMap<String, Object> newItem = new HashMap<>();
                newItem.put("price",price);
                newItem.put("imageUri",image);
                newItem.put("schedule",schedule);
                newItem.put("available",availability);

                firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(name)
                        .set(newItem, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Snackbar.make(findViewById(android.R.id.content), "You have added one item named "+name, Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                        Intent intent = new Intent(AddItem.this, MainActivity.class);
                        intent.putExtra("resName", resName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });

    }
}