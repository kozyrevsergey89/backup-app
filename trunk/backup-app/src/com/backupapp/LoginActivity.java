package com.backupapp;


import java.util.List;

import com.backupapp.method.InfoMethod;
import com.backupapp.net.AsyncCallback;
import com.backupapp.net.AsyncRequestor;
import com.backupapp.net.request.GCMRequest;
import com.backupapp.net.request.LoginRequest;
import com.backupapp.utils.SharedUtils;
import com.google.android.gcm.GCMRegistrar;
import com.tetra.service.rest.Parameter;
import com.tetra.service.rest.Response;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user.
 */
public class LoginActivity extends Activity {
	
	public static final String EXTRA_LOGIN = "backupapp.authenticatordemo.extra.LOGIN";
	public static final String SENDER_ID = "777289626036";
	public static final String COOKIE_ID = "USER_ID";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private AsyncRequestor requestTask = null;
	private LoginCallback callback = null;
	private LoginRequest loginRequest = null;
	private ServiceBroadcast broadcastReceiver;
	private GCMCallback gcmCallback = null;

	// Values for email and password at the time of the login attempt.
	private String mLogin;
	private String mPassword;

	// UI references.
	private EditText mLoginView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		//check if agreed with terms of use
		String checkStr = SharedUtils.getFromShared(this, "TermsAgreed"); 
		if ( checkStr == null) {
			Intent intent = new Intent(getApplicationContext(), TermsOfUseActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
			
			
		// Set up the login form.
		mLogin = getIntent().getStringExtra(EXTRA_LOGIN);
		mLoginView = (EditText) findViewById(R.id.login);
		mLoginView.setText(mLogin);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		gcmCallback = new GCMCallback(this);
		broadcastReceiver = new ServiceBroadcast(gcmCallback);
		registerReceiver(broadcastReceiver, new IntentFilter(GCMIntentService.MESSAGE_ACTION));
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		/*
		if (requestTask != null) {
			return;
		}
		*/

		// Reset errors.
		mLoginView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mLogin = mLoginView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mLogin)) {
			mLoginView.setError(getString(R.string.error_field_required));
			focusView = mLoginView;
			cancel = true;
		} 
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			callback = new LoginCallback(this);
			requestTask = new AsyncRequestor(callback);
			loginRequest = new LoginRequest().setLoginAndPass(mLogin, mPassword);
			
			showProgress(true);
			requestTask.execute(loginRequest);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public static class LoginCallback extends AsyncCallback {

		private Context context;
		
		public LoginCallback(final Context context) {
			super();
			this.context = context;
		}
		
		@Override
		public void processResponse(final Response response) {
			if(response.isSuccess() && !response.getCookies().isEmpty()) {
				for(Parameter cookie : response.getCookies()) {
					if ("user_id".equals(cookie.getName())) {
						SharedUtils.writeToShared(context, "user_id", cookie.getValue());
						GCMRegistrar.checkManifest(context);
						final String regId = GCMRegistrar.getRegistrationId(context);
						//if (regId.equals("")) {
							GCMRegistrar.register(context, SENDER_ID);
						//}
						/*
						else if (GCMRegistrar.isRegisteredOnServer(context)) {
							context.startActivity(new Intent(context, MethodActivity.class));
							((LoginActivity)context).showProgress(false);
							((LoginActivity)context).finish();
							context = null;
							return; 
						}
						*/
					}
				}
			} else {
				((LoginActivity)context).showProgress(false);
				Toast.makeText(context, R.string.login_error, Toast.LENGTH_SHORT).show();
				//context.startActivity(new Intent(context, MethodActivity.class));
				//((LoginActivity)context).finish();
			}
			context = null;
		}
		
	}
	
	public static class ServiceBroadcast extends BroadcastReceiver {
		
		private GCMCallback callback;
		public ServiceBroadcast(final GCMCallback callback) {
			this.callback = callback;
		}
		
		@Override
		public void onReceive(final Context context, final Intent intent) {
			GCMRegistrar.setRegisteredOnServer(context, true);
            String newMessage = intent.getExtras().getString(GCMIntentService.ID_MESSAGE);
            SharedUtils.writeToShared(context, "reg_id", newMessage);
            AsyncRequestor requestor = new AsyncRequestor(callback);
            String cookie = SharedUtils.getFromShared(context, "user_id");
            GCMRequest request = new GCMRequest().addRegId(newMessage)
            									 .addCookie(cookie)
            									 .addDeviceId(new InfoMethod(context).getImey());
            requestor.execute(request);
		}
		
	}
	
	public static class GCMCallback extends AsyncCallback {

		private LoginActivity activity;
		
		public GCMCallback(final LoginActivity activity) {
			this.activity = activity;
		}
		
		@Override
		public void processResponse(final Response response) {
			Log.i("123123", response.getResultCode() + "");
			Log.i("123123", response.getMessage() + "");
			Log.i("123123", response.getStreamString() + "");
			
			if(response.isSuccess()) {
				List<Parameter> params = response.getHeaders();
				if (params != null && !params.isEmpty()) {
					for (Parameter param : params) {
						if ("Second-User".equals(param.getName())) {
							String flag = param.getValue();
							if("true".equals(flag)) {
								SharedUtils.deleteFromShared(activity, "user_id");
								activity.showProgress(false);
								Toast.makeText(activity, R.string.second_user, Toast.LENGTH_SHORT).show();
								return;
							} else { break; }
						} else { continue; }
					}
				}
				
				
				boolean collision = false;
				boolean useflag = false;
				if (params != null && !params.isEmpty()) {
					for (Parameter param : params) {
						if ("user_collision".equals(param.getName())){
							collision = true;
							Toast.makeText(activity,
									"You have two accounts on one device",
									 Toast.LENGTH_SHORT).show();
							//break;
						} else if ("use_full".equals(param.getName())) {
							if ("true".equals(param.getValue())) { useflag = true; }
						}
					}
				}	
				
				
				if(response.isSuccess() && !collision) {
					Log.i("123123", "use_full - " + useflag);
					Intent intent = new Intent(activity, MethodActivity.class);
					intent.putExtra("use_full", useflag);
					activity.startActivity(intent);
					activity.finish();
				} else {
					Toast.makeText(activity, response.getMessage() + "", Toast.LENGTH_SHORT).show();
				}
			}
			
			activity.showProgress(false);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		//GCMRegistrar.onDestroy(getApplicationContext());
		unregisterReceiver(broadcastReceiver);
		broadcastReceiver = null;
		if (requestTask != null) {
			requestTask.cancel(true);
		}
		requestTask = null;
		callback = null;
		loginRequest = null;
		gcmCallback = null;
		super.onDestroy();
	}
	
}
