package com.backupappmp.net.request;

import java.io.Serializable;

import com.tetra.service.rest.Request;

public class AdminFlagRequest extends Request<Serializable>{

	/**Generated serial UID .*/
	private static final long serialVersionUID = -8308247999857198827L;

	public AdminFlagRequest() {
		super();
		setHeaders("content-type", "application/x-www-form-urlencoded");
	}
	
	@Override
	public com.tetra.service.rest.Request.RequestType getRequestType() {
		return RequestType.POST;
	}

	@Override
	public String getUrl() {
		return "https://afgmp2014.appspot.com/isadmin";
	}
	
	public AdminFlagRequest addCookie(final String cookie) {
		setHeaders("Cookie", "user_id=" + cookie);
		return this;
	}
	
	public AdminFlagRequest setAdminFlag(final boolean flag) {
		setHeaders("admin", String.valueOf(flag));
		return this;
	}

}
