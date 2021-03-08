package com.example.kitchen.fragments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.kitchen.Login;
import com.example.kitchen.MainActivity;
import com.example.kitchen.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUp_Fragment extends Fragment implements OnClickListener {
	private static View view;
	private static EditText fullName, emailId, mobileNumber, location,
			password, confirmPassword;
	private static TextView login;
	private static Button signUpButton;
	private static CheckBox terms_conditions;
	private FirebaseFirestore firebaseFirestore;
	private FirebaseAuth firebaseAuth;

	public SignUp_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.signup_layout, container, false);
		initViews();
		setListeners();
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseFirestore = FirebaseFirestore.getInstance();
		return view;
	}

	// Initialize all views
	private void initViews() {
		fullName = (EditText) view.findViewById(R.id.fullName);
		emailId = (EditText) view.findViewById(R.id.userEmailId);
		mobileNumber = (EditText) view.findViewById(R.id.mobileNumber);
		location = (EditText) view.findViewById(R.id.location);
		password = (EditText) view.findViewById(R.id.password);
		confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
		signUpButton = (Button) view.findViewById(R.id.signUpBtn);
		login = (TextView) view.findViewById(R.id.already_user);
		terms_conditions = (CheckBox) view.findViewById(R.id.terms_conditions);

		// Setting text selector over textviews
		@SuppressLint("ResourceType") XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			login.setTextColor(csl);
			terms_conditions.setTextColor(csl);
		} catch (Exception e) {
		}
	}

	// Set Listeners
	private void setListeners() {
		signUpButton.setOnClickListener(this);
		login.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.signUpBtn:

			// Call checkValidation method
			checkValidation();
			break;

		case R.id.already_user:

			// Replace login fragment
			new Login().replaceLoginFragment();
			break;
		}

	}

	// Check Validation Method
	private void checkValidation() {

		// Get all edittext texts
		String getFullName = fullName.getText().toString();
		String getEmailId = emailId.getText().toString();
		String getMobileNumber = mobileNumber.getText().toString();
		String getLocation = location.getText().toString();
		String getPassword = password.getText().toString();
		String getConfirmPassword = confirmPassword.getText().toString();

		// Pattern match for email id
		Pattern p = Pattern.compile(Utils.regEx);
		Matcher m = p.matcher(getEmailId);

		// Check if all strings are null or not
		if (getFullName.equals("") || getFullName.length() == 0
				|| getEmailId.equals("") || getEmailId.length() == 0
				|| getMobileNumber.equals("") || getMobileNumber.length() == 0
				|| getLocation.equals("") || getLocation.length() == 0
				|| getPassword.equals("") || getPassword.length() == 0
				|| getConfirmPassword.equals("")
				|| getConfirmPassword.length() == 0)

			new CustomToast().Show_Toast(getActivity(), view, "All fields are required.");

		// Check if email id valid or not
		else if (!m.find())
			new CustomToast().Show_Toast(getActivity(), view, "Your Email Id is Invalid.");

		// Check if both password should be equal
		else if (!getConfirmPassword.equals(getPassword))
			new CustomToast().Show_Toast(getActivity(), view, "Both password doesn't match.");

		// Make sure user should check Terms and Conditions checkbox
		else if (!terms_conditions.isChecked())
			new CustomToast().Show_Toast(getActivity(), view, "Please select Terms and Conditions.");

		// Else do signup or do your stuff
		else
			Toast.makeText(getActivity(), "Do SignUp.", Toast.LENGTH_SHORT).show();

		doLogin();

	}

	private void doLogin() {
		signUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				String phone_number = "+92" + mobileNumber.getText().toString();

				if (mobileNumber.getText().toString().length() < 10) {
					new CustomToast().Show_Toast(getActivity(), view, "Please write a valid phone number!");
				}

				else {

					if (firebaseAuth.getUid() != null) {

						firebaseFirestore.collection("Users").whereEqualTo("Phone", phone_number).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
							@Override
							public void onComplete(@NonNull Task<QuerySnapshot> task) {

								if (task.isSuccessful()) {
									if (task.getResult().size() > 0) {

										new CustomToast().Show_Toast(getActivity(), view, "Phone number already registered!");
									}
								} else {

									new CustomToast().Show_Toast(getActivity(), view, task.getException().getMessage());
								}
							}
						});
					}
					else {

//						Intent SignUpIntent = new Intent(SignUp_Fragment.this, VerifyPhoneNumber.class);
//						SignUpIntent.putExtra("phone_number", phone_number);
//						startActivity(SignUpIntent);
//						getActivity().finish();
						getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

					}

				}

			}
		});

	}
}
