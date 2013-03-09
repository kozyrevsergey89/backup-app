package com.backupapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class RegistrationActivity extends LoginActivity {
	
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setEmailViewVisible();
		setVerifyViewVisible();
		setRegistrationTextGone();
		Button registerBtn = (Button) findViewById(R.id.sign_in_button);
		registerBtn.setText(R.string.register);
	}
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, LoginActivity.class));
		finish();
	}
	

}
