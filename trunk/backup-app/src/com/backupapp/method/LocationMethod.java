package com.backupapp.method;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.backupapp.method.MyLocation.LocationResult;

//how to use this class
//Location l = LocationMethod.getLocationCoordinates(context);

public class LocationMethod {
	
	private static Location resultLocation;
	
	public static Location getLocationCoordinates(Context context){
		
		LocationResult locationResult = new LocationResult() {
			
			@Override
			public void gotLocation(Location location) {
				Log.i("BACKUP", "longitude: "+location.getLongitude()+" latitude: "+location.getLatitude());
				resultLocation = location;
				
			}
		};
		
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(context, locationResult);
		
		
		return resultLocation;
	}

}
