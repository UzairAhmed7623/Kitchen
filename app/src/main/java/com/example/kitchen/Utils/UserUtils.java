package com.example.kitchen.Utils;

import android.content.Context;
import android.widget.Toast;

import com.example.kitchen.modelclasses.TokenModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

						Toast.makeText(context, "Token successfully submitted to database!", Toast.LENGTH_SHORT).show();


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
	
}
