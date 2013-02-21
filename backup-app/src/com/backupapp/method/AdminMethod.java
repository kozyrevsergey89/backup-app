package com.backupapp.method;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

public class AdminMethod {
	
	public static void doWip(Context context){
		final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
	}

}
