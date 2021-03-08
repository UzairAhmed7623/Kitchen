package com.example.kitchen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.kitchen.fragments.Login_Fragment;
import com.example.kitchen.fragments.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Button btnNext;
    private EditText etResName;

    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

//        btnNext = (Button) findViewById(R.id.btnNext);
//        etResName = (EditText) findViewById(R.id.etResName);

//        btnNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String resName = etResName.getText().toString();
//
//                Intent intent = new Intent(Login.this, MainActivity.class);
//                intent.putExtra("resName", resName);
//                startActivity(intent);
//            }
//        });


        fragmentManager = getSupportFragmentManager();

        // If savedinstnacestate is null then replace login fragment
        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frameContainer, new Login_Fragment(),
                            Utils.Login_Fragment).commit();
        }

        // On close icon click finish activity
        findViewById(R.id.close_activity).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        finish();

                    }
                });

    }

    // Replace Login Fragment with animation
    public void replaceLoginFragment() {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.frameContainer, new Login_Fragment(),
                        Utils.Login_Fragment).commit();
    }

    @Override
    public void onBackPressed() {

        // Find the tag of signup and forgot password fragment
        Fragment SignUp_Fragment = fragmentManager
                .findFragmentByTag(Utils.SignUp_Fragment);
        Fragment ForgotPassword_Fragment = fragmentManager
                .findFragmentByTag(Utils.ForgotPassword_Fragment);

        // Check if both are null or not
        // If both are not null then replace login fragment else do backpressed
        // task

        if (SignUp_Fragment != null)
            replaceLoginFragment();
        else if (ForgotPassword_Fragment != null)
            replaceLoginFragment();
        else
            super.onBackPressed();
    }

}