package com.troubadorian.streamradio.model;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRURLEncoder;
import com.troubadorian.streamradio.client.model.IHRVector;

public class IHRXMLLocalStations extends IHRXMLParser {
	public IHRVector					mCallLetters;
	public String						mDistance;
	public String						mZipCode;
	public String						mName;

	public IHRXMLLocalStations( byte[] xml ) throws Exception {
		super( xml );
	}

	public IHRXMLLocalStations( double latitude, double longitude ) throws Exception {
		super();

		byte[]							data;
		IHRURLEncoder					post;
		String							url;

		post = new IHRURLEncoder();
		
		post.append( IHRConfigurationClient.shared().parameters( true ) );

		post.append( "latitude", String.valueOf( latitude ) );
		post.append( "longitude", String.valueOf( longitude ) );

		url = IHRXML.kURLBase + IHRXML.sConfigFilesDirectory + "local_stations.php?" + post.toString();
		data = IHRHTTP.fetchSynchronous( url );

		parse( data );
		
		IHRConfigurationClient.shared().setLocalStationsXML( data );
	}

	@Override
	public void parseEnter() { mCallLetters = new IHRVector(); super.parseEnter(); }
	
	@Override
	public void endElement( String uri, String localName, String qName ) {
		if ( localName.equals( "call_letters" ) ) {
			mCallLetters.addElement( mContents );
		} else if ( localName.equals( "distance" ) ) {
			mDistance = mContents;
		} else if ( localName.equals( "zip_code" ) ) {
			mZipCode = mContents;
		} else if ( localName.equals( "name" ) ) {
			mName = mContents;
		}
		
		mContents = "";
	}
}
