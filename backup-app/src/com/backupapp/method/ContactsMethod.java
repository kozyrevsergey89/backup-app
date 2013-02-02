package com.backupapp.method;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactsMethod {
	
	ArrayList<String> vCard;
	Cursor cursor;
	String vfile = "backup.vcf";
	

	private void sendVcardString(Context context) throws IOException {
	        // TODO Auto-generated method stub
	        vCard = new ArrayList<String>();
	        cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
	        if(cursor!=null&&cursor.getCount()>0)
	        {
	            int i;
	            String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vfile;
	            FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);
	            cursor.moveToFirst();
	            for(i = 0;i<cursor.getCount();i++)
	            {
	                get(cursor, context);
	                //Log.d("TAG", "Contact "+(i+1)+"VcF String is"+vCard.get(i));
	                cursor.moveToNext();
	                mFileOutputStream.write(vCard.get(i).toString().getBytes());// HERE IS A PLACE, WHERE WE WRITE TO FILE - CHANGE TO SEND a FILE
	            }
	            mFileOutputStream.close();
	            cursor.close();
	        }
	        else
	        {
	            Log.d("TAG", "No Contacts in Your Phone");
	        }
	    }
	private void get(Cursor cursor2, Context context) {
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        AssetFileDescriptor fd;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");

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

	
	

}
