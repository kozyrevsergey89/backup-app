package com.backupapp.method;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

public class AdminMethod {
	
	public static void doWip(Context context){
		DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.wipeData(true ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0);
	}

}
