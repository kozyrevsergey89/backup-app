package com.backupapp;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MethodActivity extends Activity implements OnClickListener{

	public static final int RESULT_ENABLE = 1;
	private Button backup, restore, enableWipe;
	private DevicePolicyManager devManager;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.method_activity);
		backup = (Button)findViewById(R.id.bt_backup_vcf);
		restore = (Button)findViewById(R.id.bt_restore_vcf);
		enableWipe = (Button)findViewById(R.id.bt_enable_wipe);
		backup.setOnClickListener(this);
		restore.setOnClickListener(this);
		enableWipe.setOnClickListener(this);
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.bt_backup_vcf:
			break;
		case R.id.bt_restore_vcf:
			break;
		case R.id.bt_enable_wipe:
			break;	
		default:
			break;
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Admin enabled!");
                } else {
                    Log.i("DeviceAdminSample", "Admin enable FAILED!");
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	/*
	private void getAdminRights() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                mDeviceAdminSample);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Application need admin rights for data wipe availability.");
        startActivityForResult(intent, RESULT_ENABLE);
	}
	*/
	
}
