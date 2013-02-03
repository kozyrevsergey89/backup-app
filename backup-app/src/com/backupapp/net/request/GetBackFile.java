package com.backupapp.net.request;

import java.io.Serializable;

import com.tetra.service.rest.Request;

public class GetBackFile extends Request<Serializable>{

	private static final long serialVersionUID = 70844032293693582L;

	@Override
	public com.tetra.service.rest.Request.RequestType getRequestType() {
		return RequestType.GET;
	}

	@Override
	public String getUrl() { return "http://192.168.1.236:8080//backserve"; }
	
	public GetBackFile addCookie(final String cookie) {
		setHeaders("Cookie", "user_id=" + cookie);
		return this;
	}

}
