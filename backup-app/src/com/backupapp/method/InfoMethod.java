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
	
	private static String phone, ip;
	private static String accCommaSeq="";
	
	public static String getAccountList(Context context){
		AccountManager am = AccountManager.get(context);
		Account[] accArray = am.getAccounts();   //we don't get VK and FB accounts this way - future TO-DO
		for (Account a : accArray){
			accCommaSeq += a.name+",";
		}
		Log.i("BACK", "account lis: "+ accCommaSeq);
		return accCommaSeq;
	}
	
	public static String getPhone(Context context){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		phone = tm.getLine1Number();
		//IN FUTURE UNLOCK 
		//String imey = tm.getdeviceId(); 
		Log.i("BACK", "phone number"+phone);
		return phone;
	}
	
	public static String getIp(Context context){
		ip = Utils.getIPAddress(true); // IPv4 plus we have many more in utils
		Log.i("BACK", "ip v4 address: "+ip);
		return ip;
	}

}
