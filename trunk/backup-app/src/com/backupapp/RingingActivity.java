package com.backupapp;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RingingActivity extends Activity{

	private Ringtone ringtone; 
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ringing_activity);
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		ringtone = RingtoneManager.getRingtone(this, notification);
		Button discad = (Button) findViewById(R.id.discad_button);
		discad.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				if(ringtone != null && ringtone.isPlaying()) {
					ringtone.stop();
					ringtone = null;
				}
				finish();
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (ringtone != null) { ringtone.play(); }
	}
	
}
