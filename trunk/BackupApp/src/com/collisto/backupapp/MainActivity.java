package com.collisto.backupapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.collisto.backupapp.MyLocation.LocationResult;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	Button resultClickBtn;
	Button gpsClickBtn;
	TextView resultText, gpsText;
	ArrayList<String> vCard;
	Cursor cursor;
	String vfile = "backup.vcf";
	LocationManager locationMgr=null;
	String bestProvider;
	
	View.OnClickListener resulctClickHandler = new View.OnClickListener() {
        public void onClick(View v) {
          Log.i("BACKUP", "TEST");
          try {
			getVcardString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
      };
      
      View.OnClickListener gpsClickHandler = new View.OnClickListener() {
          public void onClick(View v) {
        	  /*//set criteria to location provider
        	  Criteria myCriteria = new Criteria();
              myCriteria.setAccuracy(Criteria.ACCURACY_HIGH);
              myCriteria.setPowerRequirement(Criteria.POWER_LOW);
              //get best provider
        	  bestProvider = locationMgr.getBestProvider(myCriteria, true);
        	  //get last know location from this provider
        	  Location currentLocation = locationMgr.getLastKnownLocation(bestProvider);
        	  gpsText.append("\n Latitude:  "+currentLocation.getLatitude()+" \n Longitude: "+currentLocation.getLongitude());
        	  Log.i("BACKUP", "\n Latitude:  "+currentLocation.getLatitude()+" \n Longitude: "+currentLocation.getLongitude());*/
        	  LocationResult locationResult = new LocationResult(){
        		    @Override
        		    public void gotLocation(Location location){
        		        //Got the location!
        		    	Log.i("BACKUP", "\n Latitude:  "+location.getLatitude()+" \n Longitude: "+location.getLongitude());
        		    }
        		};
        		MyLocation myLocation = new MyLocation();
        		myLocation.getLocation(getApplicationContext(), locationResult);
        		
        		
          }
        };
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultClickBtn = (Button) findViewById(R.id.resultClickBtn);
        resultClickBtn.setOnClickListener(resulctClickHandler);
        
        gpsClickBtn = (Button) findViewById(R.id.gpsBtn);
        gpsClickBtn.setOnClickListener(gpsClickHandler);
        
        gpsText = (TextView) findViewById(R.id.gpsText);
        /*
        locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        */
        LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        //Got the location!
		    	Log.i("BACKUP", "\n Latitude:  "+location.getLatitude()+" \n Longitude: "+location.getLongitude());
		    }
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
         
        
        
    }
    
    private void importContacts(){
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().toString() + File.separator+vfile)),"text/x-vcard"); //storage path is path of your vcf file and vFile is name of that file.
    	startActivity(intent);
    	
    }
    
    
    private void getVcardString() throws IOException {
        // TODO Auto-generated method stub
        vCard = new ArrayList<String>();
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if(cursor!=null&&cursor.getCount()>0)
        {
            int i;
            String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vfile;
            FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);
            cursor.moveToFirst();
            for(i = 0;i<cursor.getCount();i++)
            {
                get(cursor);
                //Log.d("TAG", "Contact "+(i+1)+"VcF String is"+vCard.get(i));
                cursor.moveToNext();
                mFileOutputStream.write(vCard.get(i).toString().getBytes());
            }
            mFileOutputStream.close();
            cursor.close();
        }
        else
        {
            Log.d("TAG", "No Contacts in Your Phone");
        }
    }
    
    private void get(Cursor cursor2) {
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        AssetFileDescriptor fd;
        try {
            fd = this.getContentResolver().openAssetFileDescriptor(uri, "r");

            FileInputStream fis = fd.createInputStream();
            byte[] buf = new byte[(int) fd.getDeclaredLength()];
            fis.read(buf);
            String vcardstring= new String(buf);
            vCard.add(vcardstring);
        } catch (Exception e1) 
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}


