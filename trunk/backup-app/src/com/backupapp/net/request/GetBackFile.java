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
	public String getUrl() { return "https://backupbackend.appspot.com/backserve"; }
	
	public GetBackFile addCookie(final String cookie) {
		setHeaders("Cookie", "user_id=" + cookie);
		return this;
	}

}
