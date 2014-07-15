package com.backupappmp.method;

import java.util.Timer;
import java.util.TimerTask;

import com.backupappmp.net.AsyncRequestor;
import com.backupappmp.net.request.InfoRequest;
import com.backupappmp.utils.SharedUtils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MyLocation {
    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled=false;
    boolean network_enabled=false;
    private AsyncRequestor requstor;
    private InfoRequest request;
    private String userId;

    public MyLocation(final AsyncRequestor requestor, final InfoRequest request) {
    	this.request = request;
    	this.requstor = requestor;
    }
    
    public boolean getLocation(Context context, LocationResult result)
    {
    	userId = SharedUtils.getFromShared(context, "user_id");
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult=result;
        if(lm==null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //don't start listeners if no provider is enabled
        if(!gps_enabled && !network_enabled){
        	
        	return false; 
        }
        
        if(gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if(network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        timer1=new Timer();
        timer1.schedule(new GetLastLocation(), 20000);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
        	Log.i("123123", "new gps location");
            timer1.cancel();
            locationResult.gotLocation(location);
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
            sendLocation(location, true);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
        	Log.i("123123", "new network location");
            timer1.cancel();
            locationResult.gotLocation(location);
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
            sendLocation(location, true);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
             lm.removeUpdates(locationListenerGps);
             lm.removeUpdates(locationListenerNetwork);

             Location net_loc=null, gps_loc=null;
             if(gps_enabled)
                 gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             if(network_enabled)
                 net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

             //if there are both values use the latest one
             if(gps_loc!=null && net_loc!=null){
                 if(gps_loc.getTime()>net_loc.getTime()) {
                	 locationResult.gotLocation(gps_loc);
                	 Log.i("123123", "last gps location");
                	 sendLocation(gps_loc, false);
                 } else {
                	 locationResult.gotLocation(net_loc);
                	 Log.i("123123", "last network location");
                	 sendLocation(net_loc, false);
                 }
                 return;
             }

             if(gps_loc!=null){ 
            	 locationResult.gotLocation(gps_loc);
            	 Log.i("123123", "last gps location");
            	 sendLocation(gps_loc, false);
             } else if (net_loc!=null){
            	 locationResult.gotLocation(net_loc);
            	 Log.i("123123", "last gps location");
            	 sendLocation(net_loc, false);
             } else {
            	 locationResult.gotLocation(null); 
             }
             return;
        }
    }

    private void sendLocation(final Location location, final boolean last) {
    	request.addParam(location);
    	request.addCookie(userId);
    	request.addCurrentFlag(last);
    	requstor.execute(request);
    	request = null;
        requstor = null;
    }
    
    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
