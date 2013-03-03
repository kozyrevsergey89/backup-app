package com.backupapp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.backupapp.utils.*;

public class TermsOfUseActivity extends Activity implements OnClickListener {
	private WebView wView;
	private Button btAgree;
	private CheckBox chkAgree;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_of_use);
		btAgree = (Button) findViewById(R.id.terms_agree_btn);
		chkAgree = (CheckBox) findViewById(R.id.terms_agree_check);
		wView = (WebView) findViewById(R.id.terms_web_view);
		wView.loadUrl("file:///android_asset/terms_of_use_and_privacy_policy_mobile.html");
		btAgree.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (chkAgree.isChecked()) {
			SharedUtils.writeToShared(this, "TermsAgreed", "true");
			//Intent intent = new Intent(getApplicationContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(intent);
			finish();
		}
	}
	
	
}