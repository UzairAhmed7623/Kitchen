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
    private EditText etPrice, etAvai;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_properties);

        firebaseFirestore = FirebaseFirestore.getInstance();

        etPrice = (EditText) findViewById(R.id.etPrice);
        etAvai = (EditText) findViewById(R.id.etAvai);

        btnOk = (Button) findViewById(R.id.btnOk);

        String resName = getIntent().getStringExtra("restaurant");
        String itemName = getIntent().getStringExtra("itemName");

        Log.d("TAG", resName+" "+itemName);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String price = etPrice.getText().toString().trim().replace("PKR", "");
                String avail = etPrice.getText().toString();

                Log.d("TAG", resName+" "+itemName);

                firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                        .document(itemName).update("price", price);



                Intent intent = new Intent(itemProperties.this, MainActivity.class);
                intent.putExtra("resName", resName);
                startActivity(intent);
                finish();

            }
        });

    }
}