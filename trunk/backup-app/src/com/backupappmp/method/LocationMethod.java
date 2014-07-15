package com.backupappmp.method;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.backupappmp.method.MyLocation.LocationResult;
import com.backupappmp.net.AsyncRequestor;
import com.backupappmp.net.request.InfoRequest;

//how to use this class
//Location l = LocationMethod.getLocationCoordinates(context);

public class LocationMethod {
	
	private static Location resultLocation;
	
	public static Location getLocationCoordinates(Context context, AsyncRequestor requestor, InfoRequest request){
		
		LocationResult locationResult = new LocationResult() {
			
			@Override
			public void gotLocation(Location location) {
				Log.i("BACKUP", "longitude: "+location.getLongitude()+" latitude: "+location.getLatitude());
				resultLocation = location;
			}
		};
		
		MyLocation myLocation = new MyLocation(requestor, request);
		myLocation.getLocation(context, locationResult);
		
		
		return resultLocation;
	}

}
