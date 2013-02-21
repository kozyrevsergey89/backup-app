package com.backupapp.method;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.AsyncTask;

public class AdminMethod {
	
	public static void doWip(Context context){
		final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
		//dpm.wipeData(0);
		
		/*new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(final Void... params) {
				dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
				return null;
			}
			
			@Override
			protected void onPostExecute(final Void result) {
				super.onPostExecute(result);
				dpm.wipeData(0);
			}
		}.execute();*/
		
	}

}
