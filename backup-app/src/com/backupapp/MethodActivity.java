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
import com.backupapp.net.AsyncCallback;
import com.backupapp.net.AsyncRequestor;
import com.backupapp.net.request.GetBackFile;
import com.backupapp.net.request.GetFileRequest;
import com.backupapp.utils.SharedUtils;
import com.tetra.service.rest.Parameter;
import com.tetra.service.rest.Request;
import com.tetra.service.rest.Response;

public class MethodActivity extends Activity implements OnClickListener {
	
	public static final int RESULT_ENABLE = 1;
	private String userId;
	private Button backup, restore, enableWipe;
	private DevicePolicyManager mDPM;
	private ComponentName mDeviceAdminSample;
    boolean mAdminActive;
	private View statusView;
	public String url; 
	

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.method_activity);
		userId = SharedUtils.getFromShared(this, "user_id");
		backup = (Button) findViewById(R.id.bt_backup_vcf);
		restore = (Button) findViewById(R.id.bt_restore_vcf);
		enableWipe = (Button) findViewById(R.id.bt_enable_wipe);
		statusView = (View) findViewById(R.id.sstatus);
		backup.setOnClickListener(this);
		restore.setOnClickListener(this);
		enableWipe.setOnClickListener(this);
		ContactLader contactLader = new ContactLader(this);
		contactLader.execute();
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
		} else {
			backup.setVisibility(View.GONE);
			restore.setVisibility(View.GONE);
			enableWipe.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.bt_backup_vcf:
			showProgress(true);
			GetFileRequest fileRequest = new GetFileRequest().addCookie(userId);
			UrlRequestCallback callback = new UrlRequestCallback(this);
			sendRequest(callback, fileRequest);
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
             } else if (resultCode == Activity.RESULT_CANCELED) {
             	//mDPM.removeActiveAdmin(mDeviceAdminSample);
                 mAdminActive = false;
                 Log.i("DeviceAdminSample", "Admin enable FAILED!");
             }
             return;
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
				Toast.makeText(context, "Contacts are ready to backup", Toast.LENGTH_SHORT).show();
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
