package com.backupapp.net.request;

import java.io.Serializable;

import com.tetra.service.rest.Request;

public class RegRequest extends Request<Serializable>{

	/**
	 * Serial uid.
	 */
	private static final long serialVersionUID = -1929338867257034121L;

	public RegRequest() {
		setHeaders("mobile_registration", "true");
	}
	
	@Override
	public com.tetra.service.rest.Request.RequestType getRequestType() {
		return RequestType.POST;
	}

	@Override
	public String getUrl() {
		return "https://backupbackend.appspot.com/signup";
	}
	
	public RegRequest setParams(final String name, final String pass, final String email) {
		setPostEntities("name", name);
		setPostEntities("password", pass);
		setPostEntities("email", email);
		return this;
	}
	
}
