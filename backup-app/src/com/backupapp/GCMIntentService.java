package com.backupapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.backupapp.utils.SharedUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService{

	public static final String REG_ID = "reg_id";
	public static final String ID_MESSAGE = "id_broadcast_message";
	public static final String MESSAGE_ACTION = "com.backupapp.BROADCAST_MESSAGE";
	public static final String SENDER_ID = "777289626036";
	
	public GCMIntentService() {
		super(SENDER_ID);
	}
	
	@Override
	protected void onError(final Context context, final String registrationId) {
		Log.i("123123", "onError - " + registrationId);
	}

	@Override
	protected void onMessage(final Context context, final Intent intent) {
		
	}

	@Override
	protected void onRegistered(final Context context, final String registrationId) {
		SharedUtils.writeToShared(context, REG_ID, registrationId);
		displayMessage(context, registrationId);
	}

	@Override
	protected void onUnregistered(final Context context, final String registrationId) {
		SharedUtils.writeToShared(context, REG_ID, registrationId);
		displayMessage(context, registrationId);
		if(GCMRegistrar.isRegisteredOnServer(context)) {
			//GCMRegistrar.setRegisteredOnServer(context, false);
		}
	}
	
	private void displayMessage(Context context, String message) {
        Intent intent = new Intent(MESSAGE_ACTION);
        intent.putExtra(ID_MESSAGE, message);
        context.sendBroadcast(intent);
	}

}
