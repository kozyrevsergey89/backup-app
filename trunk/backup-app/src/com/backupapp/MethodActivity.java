package com.backupapp;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
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

import com.backupapp.method.ContactsMethod;
import com.backupapp.net.AsyncCallback;
import com.backupapp.net.AsyncRequestor;
import com.backupapp.net.request.GetFileRequest;
import com.backupapp.utils.SharedUtils;
import com.tetra.service.rest.Request;
import com.tetra.service.rest.Response;

public class MethodActivity extends Activity implements OnClickListener {

	public static final int RESULT_ENABLE = 1;
	private String userId;
	private Button backup, restore, enableWipe;
	private DevicePolicyManager devManager;
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
		ContactLader contactLader = new ContactLader();
		contactLader.execute(this);
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

	private void getAdminRights() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ""); // add
																		// device
																		// manager
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
				if (!response.getStreamString().isEmpty()) {
					Log.i("123123", response.getStreamString() + "");
					try {
						activity.url = URLEncoder.encode(response.getStreamString(), "UTF-8");
					} catch (final UnsupportedEncodingException e1) {
						Log.e("123123", e1.getMessage() + "");
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
			setHeaders("Cookie", "user_id=" + cookie);
			return this;
		}
	}
	
	private static class ContactLader extends AsyncTask<Context, Void, File> {

		@Override
		protected File doInBackground(Context... params) {
			ContactsMethod method = new ContactsMethod();
			try {
				File file = method.getVcardFile(params[0]);
				return file;
			} catch (final IOException e) {
				Log.e("123123", "Unable to get vcard file");
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(final File result) {
			super.onPostExecute(result);
			if (result != null) {
				Log.i("123123", result.getAbsolutePath());
			} else {
				Log.e("123123", "No file");
			}
		}
		
	}

}
