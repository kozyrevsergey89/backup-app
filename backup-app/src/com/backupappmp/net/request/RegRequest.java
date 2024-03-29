package com.backupappmp.net.request;

import java.io.Serializable;

import com.tetra.service.rest.Request;

public class RegRequest extends Request<Serializable>{

	/**
	 * Serial uid.
	 */
	private static final long serialVersionUID = -1929338867257034121L;

	public RegRequest() {
		super();
		setHeaders("mobile_registration", "true");
		setHeaders("content-type", "application/x-www-form-urlencoded");
	}
	
	@Override
	public com.tetra.service.rest.Request.RequestType getRequestType() {
		return RequestType.POST;
	}

	@Override
	public String getUrl() {
		return "https://afgmp2014.appspot.com/signup";
	}
	
	public RegRequest setParams(final String name, final String pass, final String email, final String verify) {
		setPostEntities("username", name);
		setPostEntities("password", pass);
		setPostEntities("email", email);
		setPostEntities("verify", verify);
		return this;
	}
	
}
