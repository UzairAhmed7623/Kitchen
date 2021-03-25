package com.example.kitchen.Service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.kitchen.Common.Common;
import com.example.kitchen.Utils.UserUtils;
import com.example.kitchen.modelclasses.TokenModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseService extends FirebaseMessagingService {

    private static final String FCM_CHANNEL_ID = "1001";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        UserUtils.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("TAG", "Message recieved from: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0){
            Log.d("TAG", "Message recieved from: " + remoteMessage.getData().toString());

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            Log.d("TAG", "Message recieved from: " + title + " " + body);

            Intent intent = new Intent(this, MyFirebaseService.class);

            Common.showNotification(this, new Random().nextInt(), title, body, intent);

        }
    }
}
