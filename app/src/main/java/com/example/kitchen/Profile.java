package com.example.kitchen;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmailAddress, etPhoneNumber;
    private Button btnSave;
    private ImageButton ivAddImage;
    private CircleImageView ivProfile;
    private Uri imageUri;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

//        etFirstName = (EditText) findViewById(R.id.etFirstName);
//        etLastName = (EditText) findViewById(R.id.etLastName);
//        etEmailAddress = (EditText) findViewById(R.id.etEmailAddress);
//        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
//        btnSave = (Button) findViewById(R.id.btnSave);
//        ivAddImage = (ImageButton) findViewById(R.id.ivAddImage);
//        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);

    }
}