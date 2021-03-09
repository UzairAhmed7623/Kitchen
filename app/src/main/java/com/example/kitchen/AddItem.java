package com.example.kitchen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddItem extends AppCompatActivity {

    private EditText etItemName, etItemImage, etScedule, etAvailable;
    private Button btnDone;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        etItemName = (EditText) findViewById(R.id.etItemName);
        etItemImage = (EditText) findViewById(R.id.etItemImage);
        etScedule = (EditText) findViewById(R.id.etScedule);
        etAvailable = (EditText) findViewById(R.id.etAvailable);
        btnDone = (Button) findViewById(R.id.btnDone);



    }
}