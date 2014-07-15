package com.backupappmp;

import android.content.Intent;
import android.os.Bundle;

public class RegistrationActivity extends LoginActivity {
	
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setEmailViewVisible();
		setVerifyViewVisible();
		setRegistrationTextGone();
		setRegistrationButtonGone();
		setSignInGome();
		setRegistrationStartVisible();
		setTwoDeviceWarningVisible();
	}
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, LoginActivity.class));
		finish();
	}
	
	
	

}
