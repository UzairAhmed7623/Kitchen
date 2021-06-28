package com.inkhornsolutions.kitchen.Utils;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.inkhornsolutions.kitchen.EventBus.SelectPlaceEvent;
import com.inkhornsolutions.kitchen.Remote.IFCMService;
import com.inkhornsolutions.kitchen.Remote.RetrofitFCMClient;
import com.inkhornsolutions.kitchen.modelclasses.DriverGeoModel;
import com.inkhornsolutions.kitchen.modelclasses.FCMSendData;
import com.inkhornsolutions.kitchen.modelclasses.TokenModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UserUtils {

	public static void updateToken(Context context, String token) {
		TokenModel tokenModel = new TokenModel(token);

		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference tokens = db.getReference("Tokens");

		if (FirebaseAuth.getInstance().getCurrentUser() != null){
			tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(tokenModel)
					.addOnSuccessListener(aVoid -> {

//						Toast.makeText(context, "Token successfully submitted to database!", Toast.LENGTH_SHORT).show();


					}).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
		}
	}

    public static void sendRequestToDriver(Context context, View view, DriverGeoModel foundDriver, SelectPlaceEvent selectPlaceEvent) {

		CompositeDisposable compositeDisposable = new CompositeDisposable();
		IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

		FirebaseDatabase.getInstance().getReference("Tokens").child(foundDriver.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()){
					TokenModel tokenModel = snapshot.getValue(TokenModel.class);

					Map<String, String> notificationdata = new HashMap<>();
					notificationdata.put("title", "RequestDriver");
					notificationdata.put("body", "This message represent for request driver action");
					notificationdata.put("RiderKey", FirebaseAuth.getInstance().getCurrentUser().getUid());

					notificationdata.put("PickupLocationString", selectPlaceEvent.getOriginString());
					notificationdata.put("PickupLocation", new StringBuilder("")
							.append(selectPlaceEvent.getOrigin().latitude)
							.append(",")
							.append(selectPlaceEvent.getOrigin().longitude)
							.toString());

					notificationdata.put("DestinationLocationString", selectPlaceEvent.getAddress());
					notificationdata.put("DestinationLocation", new StringBuilder("")
							.append(selectPlaceEvent.getDestination().latitude)
							.append(",")
							.append(selectPlaceEvent.getDestination().longitude)
							.toString());

					notificationdata.put("dropOffUserId", selectPlaceEvent.getDropOffUserId());

					FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationdata);
					compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(fcmResponse -> {
						if (fcmResponse.getSuccess() == 0){
							compositeDisposable.clear();
							Snackbar.make(view, "Failed to send request to driver!", Snackbar.LENGTH_LONG).show();

						}
					}, throwable -> {
						compositeDisposable.clear();
						Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
					}));
				}
				else {
					Snackbar.make(view, "Token not found!", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
    }

	public static void acceptOrderNotificationToCustomer(View view, Context context, String key) {
		CompositeDisposable compositeDisposable = new CompositeDisposable();
		IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

		FirebaseDatabase.getInstance()
				.getReference("Tokens")
				.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()){
					TokenModel tokenModel = snapshot.getValue(TokenModel.class);

					Map<String, String> notificationdata = new HashMap<>();
					notificationdata.put("title", "OrderAccepted");
					notificationdata.put("body", "Your order has been accepted. Your order will be deliver you within time");

					FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationdata);
					compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
							.subscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(fcmResponse -> {
								if (fcmResponse.getSuccess() == 0){
									compositeDisposable.clear();
									Snackbar.make(view, "Order message send failed!", Snackbar.LENGTH_LONG).show();
								}
								else {
									Snackbar.make(view, "Order placed!", Snackbar.LENGTH_LONG).show();
								}

							}, throwable -> {
								compositeDisposable.clear();
								Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
							}));
				}
				else {
					compositeDisposable.clear();
					Snackbar.make(view, "Token not found!", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				compositeDisposable.clear();
				Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
	}

	public static void rejectOrderNotificationToCustomer(View view, Context context, String key) {
		CompositeDisposable compositeDisposable = new CompositeDisposable();
		IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

		FirebaseDatabase.getInstance()
				.getReference("Tokens")
				.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()){
					TokenModel tokenModel = snapshot.getValue(TokenModel.class);

					Map<String, String> notificationdata = new HashMap<>();
					notificationdata.put("title", "OrderRejected");
					notificationdata.put("body", "Your order has been rejected. Please try again.");

					FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationdata);
					compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
							.subscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(fcmResponse -> {
								if (fcmResponse.getSuccess() == 0){
									compositeDisposable.clear();
									Snackbar.make(view, "Order message send failed!", Snackbar.LENGTH_LONG).show();
								}
								else {
									Snackbar.make(view, "Order placed!", Snackbar.LENGTH_LONG).show();
								}

							}, throwable -> {
								compositeDisposable.clear();
								Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
							}));
				}
				else {
					compositeDisposable.clear();
					Snackbar.make(view, "Token not found!", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				compositeDisposable.clear();
				Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
	}

	public static void dispatchOrderNotificationToCustomer(View view, Context context, String key) {
		CompositeDisposable compositeDisposable = new CompositeDisposable();
		IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

		FirebaseDatabase.getInstance()
				.getReference("Tokens")
				.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()){
					TokenModel tokenModel = snapshot.getValue(TokenModel.class);

					Map<String, String> notificationdata = new HashMap<>();
					notificationdata.put("title", "OrderDispatched");
					notificationdata.put("body", "Your order has been dispatched and will be deliver you within time");

					FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationdata);
					compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
							.subscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(fcmResponse -> {
								if (fcmResponse.getSuccess() == 0){
									compositeDisposable.clear();
									Snackbar.make(view, "Order message send failed!", Snackbar.LENGTH_LONG).show();
								}
								else {
									Snackbar.make(view, "Order placed!", Snackbar.LENGTH_LONG).show();
								}

							}, throwable -> {
								compositeDisposable.clear();
								Snackbar.make(view, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
							}));
				}
				else {
					compositeDisposable.clear();
					Snackbar.make(view, "Token not found!", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				compositeDisposable.clear();
				Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
	}

}
