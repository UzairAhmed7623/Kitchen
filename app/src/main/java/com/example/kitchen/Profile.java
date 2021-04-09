package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private TextView tvFirstName, tvLastName, tvEmailAddress;
    private TextView tvName, tvMobile, tvEmail, tvAddress, tvDateOfBirth;
    private Button btnSave;
    private ImageButton ivAddImage;
    private CircleImageView ivProfile;
    private Uri imageUri;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Toolbar toolbar;
    private CoordinatorLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        tvFirstName = (TextView) findViewById(R.id.tvFirstName);
        tvLastName = (TextView) findViewById(R.id.tvLastName);
        tvEmailAddress = (TextView) findViewById(R.id.tvEmailAddress);

        tvName = (TextView) findViewById(R.id.tvName);
        tvMobile = (TextView) findViewById(R.id.tvMobile);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvDateOfBirth = (TextView) findViewById(R.id.tvDateOfBirth);
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()){
                    if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null){
                        String fName = documentSnapshot.getString("firstName");
                        String lName = documentSnapshot.getString("lastName");
                        tvFirstName.setText(fName);
                        tvLastName.setText(lName);
                        tvName.setText(fName+" "+lName);
                    }
                    if (documentSnapshot.getString("emailAddress") != null){
                        String email = documentSnapshot.getString("emailAddress");
                        tvEmailAddress.setText(email);
                        tvEmail.setText(email);
                    }
                    if (documentSnapshot.getString("phoneNumber") != null){
                        String mobile = documentSnapshot.getString("phoneNumber");
                        tvMobile.setText(mobile);
                    }
                    if (documentSnapshot.getString("address") != null){
                        String address = documentSnapshot.getString("address");
                        tvAddress.setText(address);
                    }
                    if (documentSnapshot.getString("DOB") != null){
                        String dob = documentSnapshot.getString("DOB");
                        tvDateOfBirth.setText(dob);
                    }

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        View view = LayoutInflater.from(this).inflate(R.layout.edit_details, null);
        EditText editText = (EditText) view.findViewById(R.id.editText);
        EditText editText2 = (EditText) view.findViewById(R.id.editText2);
        TextInputLayout TextInputLayout2 = (TextInputLayout) view.findViewById(R.id.TextInputLayout2);
        Button btnAdd = (Button) view.findViewById(R.id.btnAdd);

        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();


        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();
                TextInputLayout2.setVisibility(View.VISIBLE);
                editText2.setVisibility(View.VISIBLE);
                editText.setHint("Write your First Name");
                editText2.setHint("Write your Last Name");
                editText.setInputType(InputType.TYPE_CLASS_TEXT);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String firstName = editText.getText().toString();
                        String lastName = editText.getText().toString();

                        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)){
                            editText.setError("Please write your first name");
                            editText2.setError("Please write your last name");
                        }
                        else {

                            tvFirstName.setText(firstName);
                            tvLastName.setText(lastName);
                            tvName.setText(firstName+" "+lastName);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("firstName", firstName);
                            addData.put("lastName", lastName);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your name is updated successfully!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                            alertDialog.dismiss();
                        }

                    }
                });
            }
        });
        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();
                editText.setHint("Write your email");
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                TextInputLayout2.setVisibility(View.GONE);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String emailAddress = editText.getText().toString();

                        if (TextUtils.isEmpty(emailAddress)){
                            editText.setError("Please write your email");
                        }
                        else {

                            tvEmail.setText(emailAddress);
                            tvEmailAddress.setText(emailAddress);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("emailAddress", emailAddress);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your email is updated successfully!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        tvMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();
                editText.setHint("Write your phone with +92");
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                TextInputLayout2.setVisibility(View.GONE);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = editText.getText().toString();
                        if (TextUtils.isEmpty(phoneNumber)){
                            editText.setError("Please write your phone");
                        }
                        else {
                            tvMobile.setText(phoneNumber);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("phoneNumber", phoneNumber);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your phone number is updated successfully!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();
                editText.setHint("Write your address");
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                TextInputLayout2.setVisibility(View.GONE);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String address = editText.getText().toString();

                        if (TextUtils.isEmpty(address)){
                            editText.setError("Please write your address");
                        }
                        else {

                            tvAddress.setText(address);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("address", address);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your address is updated successfully!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        tvDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();
                editText.setHint("Write your date of birth");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                TextInputLayout2.setVisibility(View.GONE);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String DOB = editText.getText().toString();

                        if (TextUtils.isEmpty(DOB)){
                            editText.setError("Please write your DOB");
                        }
                        else {

                            tvDateOfBirth.setText(DOB);

                            Map<String, Object> addData = new HashMap<>();
                            addData.put("DOB", DOB);

                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(addData, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout, "Your DOB is updated successfully!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });



    }
}