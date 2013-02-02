package com.backupapp.net;

import android.os.Handler;

import com.tetra.service.rest.Response;

public abstract class AsyncCallback implements IAsyncCallback{
	
	@Override
	public void onResponse(final Response response) {
		
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				processResponse(response);
			}
		});
	}
	
	public abstract void processResponse(Response response);
}
