package com.inkhornsolutions.kitchen.Common;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.AnimationModel;
import com.inkhornsolutions.kitchen.modelclasses.DriverGeoModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.Map;

public class Common {

    public static Map<String,DriverGeoModel> driverFound = new HashMap<>();
    public static HashMap<String, Marker> markerList = new HashMap<>();
    public static HashMap<String, AnimationModel> driverLocationSubscribe = new HashMap<String, AnimationModel>();

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {

        PendingIntent pendingIntent = null;

        if (intent != null){
            Log.d("TAG", "Intent is not null");

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            pendingIntent = pendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = "1001";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "CHJKitchen", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("CHJKitchen");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationChannel.setSound(soundUri, audioAttributes);

                notificationManager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
                    .setSmallIcon(R.drawable.kitchen_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.kitchen_icon))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

            if (pendingIntent != null){
                builder.setContentIntent(pendingIntent);
            }
            Notification notification = builder.build();
            notificationManager.notify(id, notification);
        }
        else {
            Log.d("TAG", "Intent is null");
        }
    }

    public static String buildName(String firstName, String lastName) {
        return new StringBuilder(firstName).append(" ").append(lastName).toString();
    }

    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    public static ValueAnimator valueAnimator(long duration, ValueAnimator.AnimatorUpdateListener listener){

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 100);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(listener);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.start();
        return valueAnimator;
    }

    public static Bitmap createIconWithDuration(Context context, String duration) {
        View view = LayoutInflater.from(context).inflate(R.layout.pickup_info_with_duration_windows, null);
        TextView tvDuration = (TextView) view.findViewById(R.id.tvDuration);
        tvDuration.setText(Common.getNumberFromText(duration));

        IconGenerator generator = new IconGenerator(context);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        return  generator.makeIcon();
    }

    private static String getNumberFromText(String duration) {
        return duration.substring(0,duration.indexOf(" "));
    }

}
