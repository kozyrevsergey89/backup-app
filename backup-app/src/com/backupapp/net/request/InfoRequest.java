package com.backupapp.net.request;

import java.io.Serializable;

import com.tetra.service.rest.Request;


public class InfoRequest extends Request<Serializable>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -684946952863345251L; //generated

	public InfoRequest(){
		super();
		setHeaders("content-type", "application/x-www-form-urlencoded");
	}
	@Override
	public com.tetra.service.rest.Request.RequestType getRequestType() {
		
		return RequestType.POST;
	}

	@Override
	public String getUrl() {
		
		return "http://192.168.1.236:8080/updateuser";
	}
	
	public InfoRequest addCookie(final String cookie) {
		setHeaders("Cookie", "user_id=" + cookie);
		return this;
	}
	
	public InfoRequest addParam(final String accList, final String phone, final String ip) {
		//we can do checks for correct value here, but maybe some time later
		setPostEntities("acc", accList);
		setPostEntities("phone", phone);
		setPostEntities("ip", ip); 
		return this;
	}
	
}
