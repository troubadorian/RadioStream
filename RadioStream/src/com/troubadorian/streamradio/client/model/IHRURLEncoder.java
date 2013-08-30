package com.troubadorian.streamradio.client.model;

import java.net.URLEncoder;

public class IHRURLEncoder {
	String						mData = "";
	
	public void append( String value ) { mData += value; }
	
	public void append( String key, String value ) {
		String					encoding;
		
		try {
			value = value.replace( ' ', '_' );
			//Code changed by sriram for handling the encoding
			if(key.indexOf("currentNetwork")>=0)
			encoding=value.replaceAll("'", "");
			else			
			encoding = URLEncoder.encode( value, "UTF-8" );
			//Code ends here 
			if ( mData.length() != 0 ) mData += '&';
			
			mData += key + '=';
			mData += encoding;
		} catch ( Exception e ) { }
	}
	
	@Override
	public String toString() { return mData; }
	public byte[] getBytes() { return mData.getBytes(); }
}
