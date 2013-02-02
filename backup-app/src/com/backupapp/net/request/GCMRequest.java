package com.backupapp.net.request;

import java.io.Serializable;

import com.tetra.service.rest.Request;

public class GCMRequest extends Request<Serializable>{

	private static final long serialVersionUID = 3629764878911787544L;

	public GCMRequest() {
		super();
		setHeaders("content-type", "application/x-www-form-urlencoded");
	}
	
	@Override
	public com.tetra.service.rest.Request.RequestType getRequestType() {
		return RequestType.POST;
	}

	@Override
	public String getUrl() {
		return "http://192.168.1.236:8080/checkgcm/register";
	}
	
	public GCMRequest addRegId(final String regId) {
		setPostEntities("regId", regId);
		return this;
	}
	
	public GCMRequest addCookie(final String cookie) {
		setHeaders("Cookie", "user_id=" + cookie);
		return this;
	}

}
