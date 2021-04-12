package com.example.kitchen.Service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.kitchen.Common.Common;
import com.example.kitchen.EventBus.DeclineRequestAndRemoveTripFromDriver;
import com.example.kitchen.EventBus.DeclineRequestFromDriver;
import com.example.kitchen.EventBus.DriverAcceptTripEvent;
import com.example.kitchen.EventBus.DriverCompleteTripEvent;
import com.example.kitchen.EventBus.TimeUp;
import com.example.kitchen.Utils.UserUtils;
import com.example.kitchen.modelclasses.TokenModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

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
            String tripKey = remoteMessage.getData().get("TripKey");

            Log.d("TAG", "title: " + title + " body: " + body+" tripKey: "+tripKey);

            if (title != null){

                if (title.equals("Decline")){
                    EventBus.getDefault().postSticky(new DeclineRequestFromDriver());
                }
                else if (title.equals("DeclineAndRemoveTrip")){
                    EventBus.getDefault().postSticky(new DeclineRequestAndRemoveTripFromDriver());
                }
                else if (title.equals("DriverCompleteTrip")){
                    EventBus.getDefault().postSticky(new DriverCompleteTripEvent(tripKey));
                }
                else if (title.equals("Accept")){

                    EventBus.getDefault().postSticky(new DriverAcceptTripEvent(tripKey));

                }
                else if (title.equals("TimeOver")){

                    EventBus.getDefault().postSticky(new TimeUp(tripKey));

                }
                else {

                    Intent intent = new Intent(this, MyFirebaseService.class);

                    Common.showNotification(this, new Random().nextInt(), title, body, intent);
                }
            }

        }
    }
}