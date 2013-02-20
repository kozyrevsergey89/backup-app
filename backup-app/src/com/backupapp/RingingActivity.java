package com.backupapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.backupapp.utils.SharedUtils;

public class RingingActivity extends Activity{

	private Ringtone ringtone;
	private String soundUriString;
	private Uri notification;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ringing_activity);
		soundUriString = SharedUtils.getFromShared(this, "SOUND_URI_STRING");
		if (soundUriString == null ) {
			notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		} else  {
			notification = Uri.parse(soundUriString);
		}
		
		ringtone = RingtoneManager.getRingtone(this, notification);
		
		AudioManager audioManager = 
			    (AudioManager)getSystemService(Context.AUDIO_SERVICE);

			audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_PLAY_SOUND);
		
		ringtone.setStreamType(AudioManager.STREAM_RING);
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
