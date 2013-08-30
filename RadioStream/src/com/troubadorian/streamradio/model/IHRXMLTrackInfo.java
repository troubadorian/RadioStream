package com.troubadorian.streamradio.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRURLEncoder;
import com.troubadorian.streamradio.client.services.IHRConfigurationFile;

public class IHRXMLTrackInfo extends IHRXMLParser implements Runnable {
	public String						mBuyURL;
	public Bitmap						mCoverArt;
	public String						mCoverArtURL;
	public Handler						mHandler;
	public String						mLyricsURL;
	public IHRHashtable					mMetadata;

	// TODO: fix this path
	protected static final String		kTrackInfoCGIBaseURL = IHRXML.kURLBase + IHRXML.sConfigFilesDirectory + "iphoneTrackXML.php?";

	public IHRXMLTrackInfo( Handler handler, IHRHashtable metadata ) {
		super();

		mHandler = handler;
		mMetadata = metadata;

		new Thread( this , "IHRXMLTrackInfo " + metadata.get( "track" ) ).start();
	}

	@Override
	public void endElement( String uri, String localName, String qName ) {
		if ( localName.equals( "buy_url" ) ) {
			mBuyURL = mContents;
		} else if ( localName.equals( "cover_art" ) ) {
			mCoverArtURL = mContents;
		} else if ( localName.equals( "lyrics_url" ) ) {
			if ( mContents.indexOf( "tid=" ) != -1 ) mLyricsURL = mContents;
		}
	}

	public void run() {
		byte[]							data;
		IHRURLEncoder					post;
		String							value;

		post = new IHRURLEncoder();
		
		post.append( IHRConfigurationClient.shared().parameters( false ) );

		if ( ( value = (String) mMetadata.get( "amgArtistId" ) ) != null ) post.append( "amgartistid", value );
		if ( ( value = (String) mMetadata.get( "amgTrackId" ) ) != null ) post.append( "amgtrackid", value );
		if ( ( value = (String) mMetadata.get( "artist" ) ) != null ) post.append( "aname", value );
		if ( ( value = (String) mMetadata.get( "callletters" ) ) != null ) post.append( "callletters", value );
		if ( ( value = (String) mMetadata.get( "cartcutId" ) ) != null ) post.append( "cartcutid", value );
		if ( ( value = (String) mMetadata.get( "itunesTrackId" ) ) != null ) post.append( "itunesid", value );
		if ( ( value = (String) mMetadata.get( "lyricsId" ) ) != null ) post.append( "lyricsid", value );
		if ( ( value = (String) mMetadata.get( "MediaBaseId" ) ) != null ) post.append( "mediabaseid", value );
		if ( ( value = (String) mMetadata.get( "song_spot" ) ) != null ) post.append( "songspot", value );
		if ( ( value = (String) mMetadata.get( "stationid" ) ) != null ) post.append( "stationid", value );
		if ( ( value = (String) mMetadata.get( "track" ) ) != null ) post.append( "tname", value );
		if ( ( value = (String) mMetadata.get( "thumbplayId" ) ) != null ) post.append( "thumbplayid", value );

		// TODO: put stationid and callletters of current station into metadata

		try {
			data = IHRHTTP.fetchSynchronous( IHRConfigurationFile.baseURL( "iphoneTrackXML" ) + "?" + post.toString() );
			
//			IHRObject.logBytes( "IHRXMLTrackInfo", "", data, 0, data.length );
			
			parse( data );
		} catch ( Exception e ) {
			return;
		}

		if ( mLyricsURL != null && mLyricsURL.length() > 0 ) mMetadata.put( "lyricsURL", mLyricsURL );

		try {
			if ( mCoverArtURL != null && mCoverArtURL.length() > 0 ) {
				if ( ( data = IHRHTTP.fetchSynchronous( mCoverArtURL ) ) != null ) {
					mCoverArt = BitmapFactory.decodeByteArray( data, 0, data.length );
				}
			}
		} catch ( Exception e ) { }

		try {
		mHandler.sendMessage( mHandler.obtainMessage( 0, this ) );
		}
		catch (Exception ex) {
		    Log.e(this.getClass().getSimpleName(), "----------------------------------" + ex.toString());
		}
	}
}
