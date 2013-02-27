	package com.backupapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.backupapp.method.BackupAdminReceiver;
import com.backupapp.method.ContactsMethod;
import com.backupapp.method.InfoMethod;
import com.backupapp.net.AsyncCallback;
import com.backupapp.net.AsyncRequestor;
import com.backupapp.net.request.AdminFlagRequest;
import com.backupapp.net.request.GCMRequest;
import com.backupapp.net.request.GetBackFile;
import com.backupapp.net.request.GetFileRequest;
import com.backupapp.utils.SharedUtils;
import com.tetra.service.rest.Parameter;
import com.tetra.service.rest.Request;
import com.tetra.service.rest.Response;

public class MethodActivity extends Activity implements OnClickListener {
	
	public static final int RESULT_ENABLE = 1, RESULT_SOUND=5;
	private String userId;
	private Button backup, restore, enableWipe, chooseSound, mapDevice;
	private DevicePolicyManager mDPM;
	private ComponentName mDeviceAdminSample;
    private boolean mAdminActive;
    private boolean useflag = false; 
	private View statusView;
	public String url, soundUriString;
	private ContactLader contactLader;
	

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.method_activity);
		userId = SharedUtils.getFromShared(this, "user_id");
		
		Intent intent = getIntent();
		if(intent != null && !intent.getExtras().isEmpty()) {
			useflag = intent.getExtras().getBoolean("use_full");
		}
		
		backup = (Button) findViewById(R.id.bt_backup_vcf);
		restore = (Button) findViewById(R.id.bt_restore_vcf);
		enableWipe = (Button) findViewById(R.id.bt_enable_wipe);
		chooseSound = (Button) findViewById(R.id.bt_choose_sound);
		mapDevice = (Button) findViewById(R.id.bt_map_device);
		statusView = (View) findViewById(R.id.sstatus);
		
		if (!useflag) {
			backup.setVisibility(View.GONE);
			enableWipe.setVisibility(View.GONE);
			chooseSound.setVisibility(View.GONE);
			mapDevice.setVisibility(View.VISIBLE);
		}
		
		backup.setOnClickListener(this);
		restore.setOnClickListener(this);
		enableWipe.setOnClickListener(this);
		chooseSound.setOnClickListener(this);
		mapDevice.setOnClickListener(this);
		contactLader = new ContactLader(this);
		//contactLader.execute();
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this, BackupAdminReceiver.class);
        mAdminActive = isActiveAdmin();
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			statusView.setVisibility(View.VISIBLE);
			statusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							statusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

		} else {
			statusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}

		if (!show) {
			backup.setVisibility(View.VISIBLE);
			restore.setVisibility(View.VISIBLE);
			enableWipe.setVisibility(View.VISIBLE);
			chooseSound.setVisibility(View.VISIBLE);
			//if (!useflag) { mapDevice.setVisibility(View.VISIBLE); }
		} else {
			backup.setVisibility(View.GONE);
			restore.setVisibility(View.GONE);
			enableWipe.setVisibility(View.GONE);
			mapDevice.setVisibility(View.GONE);
			chooseSound.setVisibility(View.GONE);
		}
	}
	
	public void sendBackupedFile(){
		GetFileRequest fileRequest = new GetFileRequest().addCookie(userId);
		UrlRequestCallback callback = new UrlRequestCallback(this);
		sendRequest(callback, fileRequest);
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.bt_backup_vcf:
			showProgress(true);
			contactLader.execute();
			//GetFileRequest fileRequest = new GetFileRequest().addCookie(userId);
			//UrlRequestCallback callback = new UrlRequestCallback(this);
			//sendRequest(callback, fileRequest);
			break;
		case R.id.bt_restore_vcf:
			showProgress(true);
			GetBackFile backGet = new GetBackFile().addCookie(userId);
			GetBackFileCallback fileCallback = new GetBackFileCallback(this);
			sendRequest(fileCallback, backGet);
			break;
		case R.id.bt_enable_wipe:
			if (!mAdminActive) { getAdminRights(); }
			else { Toast.makeText(this, 
					"The app already has the admin rights",
					Toast.LENGTH_SHORT).show(); 
			}
			break;
		case R.id.bt_choose_sound:
			Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Sound");
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
			this.startActivityForResult(intent, RESULT_SOUND);
			break;
		case R.id.bt_map_device:
			showProgress(true);
			String regId = SharedUtils.getFromShared(this, "reg_id");
			GCMRequest request = new GCMRequest()
								.addCookie(userId)
								.addRegId(regId)
								.addDeviceId(new InfoMethod(this).getImey())
								.addUseThisHeader();
			
			sendRequest(new AsyncCallback() {
				@Override
				public void processResponse(final Response response) {
					Log.i("123123", response.getResultCode() + "");
					Log.i("123123", response.getMessage() + "");
					Log.i("123123", response.getStreamString() + "");
					if (response.isSuccess()) {
						List<Parameter> headers = response.getHeaders();
						if (headers != null && !headers.isEmpty()) {
							for (Parameter param : headers) {
								if("use_full".equals(param.getName())) {
									if ("true".equals(param.getValue())) {
										useflag = true;
										Log.i("123123", "use_full - " + param.getValue());
									} else {
										Toast.makeText(getBaseContext(), R.string.not_mapped, Toast.LENGTH_SHORT).show();
									}
									showProgress(false);
									break;
								}
							}
						}
					} else {
						showProgress(false);
					}
				}
			}, request);
			//showProgress(false);
			break;
		default:
			break;
		}
	}

	/**
     * Helper to determine if we are an active admin
     */
    private boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdminSample);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RESULT_ENABLE:
             if (resultCode == Activity.RESULT_OK) {
                 Log.i("DeviceAdminSample", "Admin enabled!");
                 SharedUtils.writeToShared(this, "ADMIN", String.valueOf(isActiveAdmin()));
             } else if (resultCode == Activity.RESULT_CANCELED) {
             	//mDPM.removeActiveAdmin(mDeviceAdminSample);
                 mAdminActive = false;
                 Log.i("DeviceAdminSample", "Admin enable FAILED!");
             }
             String cookie = SharedUtils.getFromShared(this, "user_id");
             AdminFlagRequest request = new AdminFlagRequest()
             				.addCookie(cookie).setAdminFlag(isActiveAdmin());
             sendRequest(new AdminFlagCallback(), request);
             return;
		case RESULT_SOUND:
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				soundUriString = uri.toString();
				
				Log.i("SOUND", "picked sound: " + soundUriString);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				soundUriString = null;
				Toast.makeText(this, R.string.default_sound, Toast.LENGTH_SHORT).show();
			}
			SharedUtils.writeToShared(this, "SOUND_URI_STRING", soundUriString);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void getAdminRights() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Application need admin rights for data wipe availability.");
        startActivityForResult(intent, RESULT_ENABLE);
	}

	private void sendRequest(final AsyncCallback callback,
			final Request<?> request) {
		AsyncRequestor requestor = new AsyncRequestor(callback);
		requestor.execute(request);
	}
	
	private static class UrlRequestCallback extends AsyncCallback {

		private MethodActivity activity;
		public UrlRequestCallback(final MethodActivity activity) {
			this.activity = activity;
		}

		@Override
		public void processResponse(final Response response) {
			if (response.isSuccess()) {
				List<Parameter> headers = response.getHeaders();
				if(headers != null && !headers.isEmpty()) {
					for(Parameter param : headers) {
						if("backurl".equals(param.getName())){
							activity.url = param.getValue();
							break;
						}
					}
					MethodActivity.PostFile postFile = activity.getPost();
					postFile.addCookie(activity.userId);
					File file = new File(Environment.getExternalStorageDirectory(), "backup.vcf");
					postFile.addFile(file);
					PostCallback callback = new PostCallback(activity);
					activity.sendRequest(callback, postFile);
					activity = null;
				}
			} else {
				activity.showProgress(false);
				Toast.makeText(activity, R.string.no_connection, Toast.LENGTH_SHORT).show();
			}
		}

	}

	public PostFile getPost() { return new PostFile(); }
	
	private static class PostCallback extends AsyncCallback {

		private MethodActivity activity;

		public PostCallback(final MethodActivity activity) {
			this.activity = activity;
		}

		@Override
		public void processResponse(final Response response) {
			Log.i("123123", response.getMessage() + "");
			Log.i("123123", response.getResultCode() + "");
			Log.i("123123", response.getStatus().name());
			Log.i("123123", response.getStreamString() + "");
			activity.showProgress(false);
			activity = null;
		}
	}
	
	private static class AdminFlagCallback extends AsyncCallback {

		@Override
		public void processResponse(final Response response) {
			if(response.isSuccess()) {
				Log.i("123123", response.getResultCode() + "");
				Log.i("123123", response.getStreamString() + "");
				Log.i("123123", response.getMessage() + "");
			}
			
		}
		
	}
	
	public class PostFile extends Request<Serializable>{
		
		private static final long serialVersionUID = 6468307116960005184L;

		@Override
		public com.tetra.service.rest.Request.RequestType getRequestType() {
			return RequestType.MULTIPART;
		}

		@Override
		public String getUrl() {
			return MethodActivity.this.url; 
		}
		
		public PostFile addFile(final File file) {
			setFile(file);
			return this;
		}
		
		public PostFile addCookie(final String cookie) {
			//setHeaders("content-type", "application/xml");
			setHeaders("Cookie", "user_id=" + cookie);
			return this;
		}
	}
	
	private static class ContactLader extends AsyncTask<Void, Void, File> {

		private Context context;
		
		public ContactLader(final Context context) {
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			((MethodActivity)context).showProgress(true);
		}
		
		@Override
		protected File doInBackground(Void... params) {
			ContactsMethod method = new ContactsMethod();
			try {
				File file = method.getVcardFile(context);
				
				return file;
			} catch (final IOException e) {
				Log.e("123123", "Unable to get vcard file");
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(final File result) {
			super.onPostExecute(result);
			((MethodActivity)context).showProgress(false);
			if (context != null) {
				((MethodActivity)context).sendBackupedFile();
				//Toast.makeText(context, "Contacts are ready to backup", Toast.LENGTH_SHORT).show();
				context = null;
			}
			if (result != null) {
				Log.i("123123", result.getAbsolutePath());
			} else {
				Log.e("123123", "No file");
			}
		}
	}
	
	private static class GetBackFileCallback extends AsyncCallback {

		private Context context;
		private ContactsMethod method;
		
		public GetBackFileCallback(final Context context) {
			this.context = context;
			method = new ContactsMethod();
		}
		
		@Override
		public void processResponse(final Response response) {
			((MethodActivity)context).showProgress(false);
			if (response.isSuccess()) {
				String contacts = response.getStreamString();
				if (contacts != null && !contacts.isEmpty()) {
					createFile(contacts);
					method.importContacts(context);
					context = null;
					method = null;
				}
			}
		}
		
		private void createFile(final String string) {
			try {
				String path = Environment.getExternalStorageDirectory().toString() + File.separator+"backup.vcf";
				BufferedWriter out = new BufferedWriter(new FileWriter(path));
				out.write(string);
				out.close();
			} catch (final IOException e) {
				Log.e("123123", e.getMessage() + "");
			}
		}
		
	}

}