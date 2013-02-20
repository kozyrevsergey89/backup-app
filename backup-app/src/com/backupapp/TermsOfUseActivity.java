package com.backupapp;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.backupapp.utils.*;

public class TermsOfUseActivity extends Activity implements OnClickListener {
	private TextView twTermsOfUse;
	private Button btAgree;
	private CheckBox chkAgree;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_of_use);
		twTermsOfUse = (TextView) findViewById(R.id.terms_text);
		btAgree = (Button) findViewById(R.id.terms_agree_btn);
		chkAgree = (CheckBox) findViewById(R.id.terms_agree_check);
		//enable scrolling
		twTermsOfUse.setMovementMethod(new ScrollingMovementMethod());
		
		btAgree.setOnClickListener(this);
		
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (chkAgree.isChecked()) {
			SharedUtils.writeToShared(this, "TermsAgreed", "true");
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
	
}