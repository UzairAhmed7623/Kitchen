package com.inkhornsolutions.kitchen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.inkhornsolutions.kitchen.modelclasses.IsUpdateReady;
import com.inkhornsolutions.kitchen.modelclasses.UrlResponce;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Splash extends AppCompatActivity {

    private ProgressBar progress_circular;
    private String currentVersion = "";
    private SweetAlertDialog sweetAlertDialog;
    private AlertDialog.Builder alertDialog;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progress_circular = (ProgressBar) findViewById(R.id.progress_circular);

        progress_circular.setVisibility(View.VISIBLE);

        allWork();
    }

    private void allWork() {
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Log.d("vCode", "play: "+currentVersion);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new IsUpdateReady("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=en", new UrlResponce() {
            @Override
            public void onReceived(String playVersion) {
                Log.d("vCode", "play: "+playVersion);

                if (Float.parseFloat(currentVersion) < Float.parseFloat(playVersion)){
                    progress_circular.setVisibility(View.GONE);

                    View view = getLayoutInflater().inflate(R.layout.custom_network_dialog, null);
                    MaterialButton btnUpdate = (MaterialButton) view.findViewById(R.id.btnUpdate);
                    MaterialButton btnCancel = (MaterialButton) view.findViewById(R.id.btnCancel);

                    alertDialog = new AlertDialog.Builder(Splash.this);
                    alertDialog.setView(view);

                    dialog = alertDialog.create();
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                            startActivity(intent);
                        }
                    });

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    dialog.show();
                }
                else {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                Intent intent = new Intent(Splash.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                            else {
                                Intent intent = new Intent(Splash.this, Login.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }            }
                    },3000);
                }
            }
        }).execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        allWork();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog  != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}