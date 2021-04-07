package com.example.kitchen.Utils;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.kitchen.Common.Common;
import com.example.kitchen.EventBus.SelectPlaceEvent;
import com.example.kitchen.R;
import com.example.kitchen.Remote.IFCMService;
import com.example.kitchen.Remote.RetrofitFCMClient;
import com.example.kitchen.RequestDriverActivity;
import com.example.kitchen.modelclasses.DriverGeoModel;
import com.example.kitchen.modelclasses.FCMResponse;
import com.example.kitchen.modelclasses.FCMSendData;
import com.example.kitchen.modelclasses.TokenModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UserUtils {

	private static String id = "P5397d1k8cYDoW8dtEIOQClO8OI2";

	//Email Validation pattern
	public static final String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";

	//Fragments Tags
	public static final String Login_Fragment = "Login_Fragment";
	public static final String SignUp_Fragment = "SignUp_Fragment";
	public static final String ForgotPassword_Fragment = "ForgotPassword_Fragment";

	public static void updateToken(Context context, String token) {
		TokenModel tokenModel = new TokenModel(token);

		FirebaseDatabase db = FirebaseDatabase.getInstance();
		DatabaseReference tokens = db.getReference("Tokens");

			tokens.child(id).setValue(tokenModel)
					.addOnSuccessListener(aVoid -> {

//						Toast.makeText(context, "Token successfully submitted to database!", Toast.LENGTH_SHORT).show();


					}).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());


//		if (FirebaseAuth.getInstance().getCurrentUser() != null){
//			tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(tokenModel)
//					.addOnSuccessListener(aVoid -> {
//
//						Toast.makeText(context, "Token successfully submitted to database!", Toast.LENGTH_SHORT).show();
//
//
//					}).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
//		}
	}

    public static void sendRequestToDriver(Context context, RelativeLayout main_layout, DriverGeoModel foundDriver, SelectPlaceEvent selectPlaceEvent) {

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
					notificationdata.put("RiderKey", id);

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

					FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationdata);
					compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(fcmResponse -> {
						if (fcmResponse.getSuccess() == 0){
							compositeDisposable.clear();
							Snackbar.make(main_layout, "Failed to send request to driver!", Snackbar.LENGTH_LONG).show();

						}
					}, throwable -> {
						compositeDisposable.clear();
						Snackbar.make(main_layout, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
					}));
				}
				else {
					Snackbar.make(main_layout, "Token not found!", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
    }
}
