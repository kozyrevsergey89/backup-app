package com.backupapp.method;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

//how to use this class
//String ip = InfoMethod.getIp();    --- no context needed
//String accountList = InfoMethod.getAccountList(context);
//String phoneNumber = InfoMethod.getPhone(context);
// in future we can extend info gathering capabilities

public class InfoMethod {
	
	private String phone, ip;
	private String accCommaSeq="";
	
	private Context context;
	
	public InfoMethod(final Context context) {
		this.context = context;
	}
	
	public String getAccountList(){
		AccountManager am = AccountManager.get(context);
		Account[] accArray = am.getAccounts();   //we don't get VK and FB accounts this way - future TO-DO
		for (Account a : accArray){
			accCommaSeq += a.name+",";
		}
		Log.i("BACK", "account lis: "+ accCommaSeq);
		return accCommaSeq;
	}
	
	public String getPhone(){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		phone = tm.getLine1Number();
		//IN FUTURE UNLOCK 
		String imey = tm.getDeviceId(); 
		Log.i("BACK", "imei:  "+imey);
		return phone;
	}
	
	public String getImey(){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		//IN FUTURE UNLOCK 
		String imey = tm.getDeviceId(); 
		Log.i("BACK", "imei:  "+imey);
		return imey;
	}
	
	public String getIp(){
		ip = Utils.getIPAddress(true); // IPv4 plus we have many more in utils
		Log.i("BACK", "ip v4 address: "+ip);
		return ip;
	}
	
	public void destroy() {
		context = null;
	}

}
