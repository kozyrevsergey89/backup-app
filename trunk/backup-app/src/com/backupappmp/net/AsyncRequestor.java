package com.backupappmp.net;

import com.tetra.service.rest.Request;
import com.tetra.service.rest.RequestMethod;
import com.tetra.service.rest.Response;

import android.os.AsyncTask;

public class AsyncRequestor extends AsyncTask<Request<?>, Void, Response>{

	private IAsyncCallback callback;
	
	public AsyncRequestor(final IAsyncCallback callback) {
		this.callback = callback;
	}
	
	@Override
	protected Response doInBackground(Request<?>... params) {
		Request<?> request = params[0];
		RequestMethod requestMethod;
		Response response = new Response();
		if(request.getApiType() == -1) {
			requestMethod = new RequestMethod();
		} else {
			requestMethod = new RequestMethod(request.getApiType());
		}
		response = requestMethod.doRequset(request);
		return response;
	}
	
	@Override
	protected void onPostExecute(final Response result) {
		super.onPostExecute(result);
		if (callback != null) {
			callback.onResponse(result);
		}
	}
}
