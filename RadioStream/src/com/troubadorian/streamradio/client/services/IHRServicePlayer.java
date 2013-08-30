package com.troubadorian.streamradio.client.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Handler.Callback;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRRTSP;
import com.troubadorian.streamradio.client.model.IHRRTSPDelegate;
import com.troubadorian.streamradio.model.IHRCache;
import com.troubadorian.streamradio.model.IHRHTTP;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumItem;
import com.troubadorian.streamradio.model.IHRStation;
import com.troubadorian.streamradio.model.IHRUtilities;


public class IHRServicePlayer extends PhoneStateListener implements
MediaPlayer.OnBufferingUpdateListener,
MediaPlayer.OnCompletionListener,
MediaPlayer.OnErrorListener,
MediaPlayer.OnInfoListener,
MediaPlayer.OnPreparedListener,
MediaPlayer.OnSeekCompleteListener,
IHRRTSPDelegate,
Runnable,
Callback {
	
	public final static String	kMessagePlayer = "com.clearchannel.iheartradio.player";
	public final static String	kNotifyNamePlayer = "player";
	public final static float	kVolumeSteps = 16;
	
	public final static String	kStatusIdle = "";
	public final static String	kStatusOpening = "opening";
	public final static String	kStatusPlaying = "playing";
	public final static String	kStatusStopped = "stopped";
	public final static String	kStatusFinished = "finished";
	public final static String	kStatusPrepared = "prepared";
	public final static String	kStatusBuffered = "buffered";
	public final static String	kStatusPreparing = "preparing";
	public final static String	kStatusBuffering = "buffering";
	public final static String	kStatusError = "error";
	public final static String	kStatusNoNetwork = "nonetwork";
	public final static String	kStatusConnectionFailed = "connectionfailed";
	
	public final static int		kStateIdle = 0;
	public final static int		kStateThreading = 1;	//	not a distinct state
	public final static int		kStateOpening = 1;
	public final static int		kStatePreparing = 2;
	public final static int		kStatePrepared = 3;
	public final static int		kStatePlaying = 4;
	public final static int		kStatePaused = 5;
	public final static int		kStateStopped = 6;
	public final static int		kStateFinished = 7;
	public final static int		kStateClosed = 8;
	public final static int		kStateError = 9;
	public final static int		kStateCoalesce = 10;
	
	//	IHRService.kPlayer
	public final static int		kIsPlaying = 11;
	public final static int		kIsStopped = 12;
	public final static int		kIsBuffering = 13;
	public final static int		kIsPlayRequested = 14;
	
	public static final int		kSeekPause = 18;
	public static final int		kSeekUnpause = 19;
	
	public final static int		kGetVolume = 21;
	public final static int		kGetBufferPercent = 22;
	public final static int		kGetPosition = 23;
	
	public final static int		kSetDescription = 31;
	public final static int		kSetIdentifier = 32;
	public final static int		kSetVolume = 33;
	public final static int		kSetVolumeByKeycode = 34;
	public final static int		kSetPosition = 35;
	public final static int		kSetStation = 39;
	
	public final static int		kCopyDescription = 41;
	public final static int		kCopyIdentifier = 42;
	public final static int		kCopyMetadata = 43;
	public final static int		kCopyStatus = 44;
	public final static int		kCopyURL = 45;
	public final static int		kCopyStation = 49;
	public final static int 	kStreamIsNew = 50;

	//	IHRService.kConfiguration
	//	IHRService.kPerformOnThread + IHRService.kPlayer
	public final static int		kStop = 1011;
	public final static int		kPlay = 1012;
	public final static int		kPause = 1013;
	public final static int		kTogglePlaying = 1014;
	public final static int		kRetry = 1019;
	
	public final static int		kPlayStationByLetters = 1021;
	public final static int		kPlayStation = 1022;
	public final static int		kOpenFileURL = 1023;
	public final static int		kOpenStreamURL = 1024;
	//	IHRService.kPerformOnThread + IHRService.kConfiguration
	
	IHRService					mService;
	
	protected boolean			mPlayRequested;
	protected boolean			mPlayPaused;
	protected boolean			mStreaming;
	protected float				mVolume;
	protected float				mVolumeUnmuted;
	protected int				mBufferPercent;
	protected int				mPlayState;
	protected int				mPhoneStatePaused;
	protected int				mOpenAttemptCount;
	
	protected IHRHashtable		mMetadata;
	protected String			mDescription;
	public 	  String			mIdentifier;
	protected String			mStreamURL;
	protected String			mMediaVault;
	protected String			mStatus;
	protected String			mSite;
	
	protected boolean			mIgnoreThroughputMessages;
	
	protected MediaPlayer		mPlayer;
	protected IHRRTSP			mRTSP;

	private IHRStation			mStation;


	
	private int 				mThroughput;
	
	public void debug( String inMessage ) {
		Log.d( "IHRPlayer " + Thread.currentThread().getId() , inMessage );
	}
	
	public boolean wantsForeground() {
		return ( mPlayRequested && null != mPlayer && null != mStreamURL /*&& mVolume > 0*/ && mPlayState > kStateIdle && mPlayState < kStatePaused );
	}
	
	protected synchronized void cease() {
//		debug( "cease" + ( null == mPlayer ? "" : " player" ) + ( null == mRTSP ? "" : " rtsp" ) );
		
		if ( null != mPlayer ) {
			//	prevent delegate callbacks
			mPlayer.setOnBufferingUpdateListener( null );
			mPlayer.setOnCompletionListener( null );
			mPlayer.setOnErrorListener( null );
//			mPlayer.setWakeMode( mService , 0 );	//	WAKE_LOCK permission
			mPlayer.stop();
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
		
		if ( null != mRTSP ) {
			mRTSP.close();	//	prevents delegate callbacks
			mRTSP = null;
		}
		
		if ( null != mMetadata && mStreaming ) {
			updateMetadata( null );
		}
	}

/* moved to IHRUtilities
	protected String resolveMediaVault( String inURL , String inMediaVault , String inSite ) throws IOException, InterruptedException {
		HttpURLConnection			resolver;
		boolean						done = false;
		int							attempts = 0;
		int							code = 0;
		
		InputStream					stream;
		int							length;
		byte[]						buffer;
		String						string;
		String						url;
		
		if ( null == inMediaVault ) {
			url = inURL;
		} else {
			final String			kLegalese = "NOTICE IS HEREBY GIVEN THAT THIS TEXT AND THE ALGORITHMS USED HEREIN ARE COPYRIGHT 2009 CLEAR CHANNEL BROADCASTING, INCORPORATED AND ARE INTENDED SOLELY FOR USE IN PRODUCTS DEVELOPED AND/OR AUTHORIZED BY CLEAR CHANNEL BROADCASTING, INCORPORATED. ALL OTHER USE IS EXPRESSLY FORBIDDEN. YOUR USE OF THIS TEXT AND/OR ANY ALGORITHM CONTAINED HEREIN IN ANY NON-AUTHORIZED CAPACITY CONSTITUTES ADMITTANCE OF THEFT OF CLEAR CHANNEL BROADCASTING, INCORPORATED'S INTELLECTUAL PROPERTY. VIOLATORS WILL BE PROSECUTED TO THE FULL EXTENT OF THE LAW.";
			String					encoding = "UTF-8";
			String					unique = mService.mConfiguration.deviceID();
			long					time = System.currentTimeMillis();
			
			url = inMediaVault;
			
			if ( inURL.indexOf( "?" ) < 0 ) {
//				inURL = inURL + "?platform=Android";
				inURL = inURL + "?" + mService.mConfiguration.standardPostData( true ).toString();
			}
			
			if ( null == inSite ) {
				length = url.indexOf( "site=" );
				if ( length > 0 ) inSite = url.substring( length + 5 );
				
				if ( null != inSite ) {
					length = inSite.indexOf( '&' );
					if ( length > 0 ) inSite = inSite.substring( 0 , length );
				}
			} else if ( url.indexOf( "site="+inSite ) < 0 ) {
				url = url + ( url.indexOf( "?" ) < 0 ? "?" : "&" ) + "site=" + inSite;
			}
			
			string = kLegalese + " " + unique + " " + inURL + " " + time;
			buffer = string.getBytes( encoding );
			string = IHRPlatform.SHA1String( buffer , 0 , buffer.length );
			
			url = url +
				"&client_id=" + URLEncoder.encode( unique , encoding ) +
				"&request_id=" + URLEncoder.encode( string.toLowerCase() , encoding ) +
				"&timestamp=" + time +
				"&decode_url=" + "1" +
				"&url=" + URLEncoder.encode( inURL , encoding ) +
				"";
		}
		
		do {
			resolver = (HttpURLConnection)( new URL( url ) ).openConnection();
			done = true;
			
			if ( 1 == attempts ) {
				string = IHRPremiumCredentials.shared().credentials( inSite );
				
				if ( null != string && 0 != string.length() ) {
					resolver.setRequestProperty( "Authorization" , string );
				}
			}
			
			resolver.connect();
			code = resolver.getResponseCode();
			
			if ( HttpURLConnection.HTTP_UNAUTHORIZED == code ) {
				if ( 1 == ++attempts && null != inSite ) done = false;
			} else if ( HttpURLConnection.HTTP_MOVED_PERM == code || HttpURLConnection.HTTP_MOVED_TEMP == code ) {
				url = resolver.getHeaderField( "Location" );
			} else if ( HttpURLConnection.HTTP_OK == code ) {
				length = resolver.getContentLength();
				stream = resolver.getInputStream();
				while ( stream.available() < length ) Thread.sleep( 100 );
				buffer = new byte[length];
				stream.read( buffer );
				stream.close();
				string = new String( buffer );
				
				if ( null != string ) inURL = string.trim();
			}
			
			resolver.disconnect();
		} while ( !done );
		
		return inURL;
	}
*/
	
	protected String resolveRedirects( String inURL ) throws MalformedURLException, IOException {
		HttpURLConnection			redirecter;
		boolean						done = false;
		
		do {
			redirecter = (HttpURLConnection)( new URL( inURL ) ).openConnection();
			redirecter.connect();
			
			if ( 301 == redirecter.getResponseCode() ) {
				inURL = redirecter.getHeaderField( "Location" );
			} else {
				done = true;
			}
			
			redirecter.disconnect();
		} while ( !done && null != inURL );
		
		return inURL;
	}
	
	protected synchronized void begin( String inURL , boolean inStream ) {
		String						deviceID = null;
		
//		debug( "begin " + ( inStream ? "stream " : "file " ) + inURL );

		mIgnoreThroughputMessages = true;	//	TO DO: turn off unless debugging
		mOpenAttemptCount++;
		
		IHRHTTP.setProhibitNewConnections( true );
		if (!mService.hasConnectivity()) {
			updateStatus(kStatusNoNetwork);
			return;
		}
		updateState( kStateOpening );
		
		mRTSP = null;
		mPlayer = new MediaPlayer();

		mPlayer.reset();	// force prepare() errors through the error handler

		mPlayer.setOnBufferingUpdateListener( this );
		mPlayer.setOnCompletionListener( this );
		mPlayer.setOnErrorListener( this );
//		mPlayer.setOnInfoListener( this );
//		mPlayer.setOnPreparedListener( this );
		mPlayer.setOnSeekCompleteListener( this );
//		mPlayer.setOnVideoSizeChangedListener( this );
		
		/*
	 	TODO:  1.   Connection time is still lengthy. Z100 takes 5-6 seconds to connect. Other stations (i.e. Lex and Terry in Gainesville, FL) take as long as 10-15 seconds to connect.
	 	TODO: 12.   Sometimes stations get stuck in a connecting/buffering loop and never restart.
	 	TODO: 14.   Stations will only stream every other time I start the app Ð seems to be replicable.
	 	TODO: 15.   Sometimes a station buffers for 3+minutes. (verify fix)
		*/
		
		try {
			if ( inStream ) {
				if ( null != mMediaVault && 0 != mMediaVault.length() ) {
					if ( inURL.indexOf( "?" ) < 0 ) {
						inURL = inURL + "?" + mService.mConfiguration.standardPostData( true ).toString();
					}
					
					inURL = IHRUtilities.resolveMediaVault( inURL , deviceID = mService.mConfiguration.deviceID() , mMediaVault , mSite );
				}
				
				mRTSP = new IHRRTSP( new URI( inURL ) , this , mMediaVault, deviceID, mSite );
				inURL = mRTSP.getRTSPURL();
			} else if ( inURL.toLowerCase().startsWith( "http" ) ) {
				inURL = resolveRedirects( inURL );
			}
			
			mPlayer.setWakeMode( mService , PowerManager.PARTIAL_WAKE_LOCK );	//	WAKE_LOCK permission
			mPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
			mPlayer.setVolume( mVolume , mVolume );
			mPlayer.setDataSource( inURL );

			mPlayState = kStatePreparing;
			mPlayer.prepare();
			
			if ( inStream ) {
				String			message = mRTSP.logPrepared();
				updateThroughput( message, true );
			}
			
			onPrepared( mPlayer );
			
			mOpenAttemptCount = 0;
			
			IHRHTTP.setProhibitNewConnections( false );
		} catch (UnknownHostException dns) {
			updateStatus(kStatusConnectionFailed);
		} catch ( Exception e ) {
//			debug( "abort " + ( inStream ? "stream " : "file " ) + inURL );
			
			updateThroughput( "begin " + mOpenAttemptCount + " failed " + ( null == mRTSP ? "?" : mRTSP.getBytesRead() ), false );
			
			IHRHTTP.setProhibitNewConnections( false );

			updateStateCease( kStateError , false/*mOpenAttemptCount < 3*/ );
		}
	}
	
	public void play( boolean inPlay ) {
		mPlayRequested = inPlay;
		mPhoneStatePaused = 0;	//	supersede phone state
		
		if ( null == mPlayer ) {
			if ( inPlay && null != mStreamURL && ( mPlayState > kStatePlaying || mPlayState < kStateThreading ) ) {
				mPlayState = kStateThreading;
				new Thread( this ).start();
			}
		} else if ( mPlayer.isPlaying() ) {
			if ( !inPlay ) {
				updateState( kStateStopped );
				if ( mStreaming ) {
					cease();
				} else {
					mPlayer.stop();
				}
			}
		} else if ( inPlay ) {
			if ( mPlayState < kStatePrepared ) {
				//	already opening
			} else if ( mPlayState < kStateStopped ) {
				mPlayer.start();
			} else if ( mPlayState < kStateFinished && !mStreaming ) {
				try {
					mPlayer.prepare();
					mPlayer.start();
				} catch ( Exception e ) {}
			} else {
				cease();
				mPlayState = kStateThreading;
				new Thread( this ).start();
			}
		}
		
		if ( null != mService ) mService.noticeStreaming( inPlay && mStreaming );	//	acquires wifi lock
	}
	
	public void pause( boolean inPause ) {
		if ( mStreaming ) {
			play( !inPause );
		} else if ( null != mPlayer && mPlayState > kStatePreparing && mPlayState < kStateStopped ) {
			//if ( inPause ) {
			if ( mPlayer.isPlaying() ) {
				updateState( kStatePaused );
				mPlayer.pause();
			} else {
				updateState( kStatePlaying );
				mPlayer.start();
			}
		}
	}
	
	protected void playStreamURL( String inURL , boolean inStream ) {
		if ( null == inURL ) {
			play( false );
			updateState( kStateIdle );
			
//			mStreamURL = null;
		} else if ( null != mStreamURL && inURL.equals( mStreamURL ) ) {
			if ( !mPlayRequested || mPlayState > kStateStopped ) {
				play( true );
			}
		} else {
			mPlayState = kStateIdle;
			mStreaming = inStream;
			mStreamURL = inURL;
			
			cease();
			
			if ( inStream && null != mMetadata ) {
				updateMetadata( null );
			}
			
			play( true );
		}
	}
	
	protected void playStation( List inStation ) {
		IHRStation				station = ( null == inStation ) ? null : new IHRStation( inStation );
		IHRHashtable			metadata = null;
		String					url = null;
		int						what = 0;
		
//		debug( "playStation " + station.getCallLetters() );
		
		if ( null == station || !station.isValid() ) what = 0;
		else if ( station.isStream() ) what = 1;
		else if ( station.isFile() ) what = 2;
		
		if ( what > 0 ) {
			mStation = station;
			mIdentifier = station.getCallLetters();
			mDescription = station.getName() + "\n" + station.getDescription();
			
			if ( what > 1 ) {
				mMetadata = null;
				
				url = station.get( IHRStation.kFileURL );
				metadata = new IHRHashtable();
				metadata.put( "artist" , station.get( IHRStation.kFileArtist ) );
				metadata.put( "track" , station.get( IHRStation.kFileTitle ) );
				metadata.put( "lyricsId" , station.get( IHRStation.kFileLyricsID ) );
			} else {
				url = station.getBaseStreamURL( false );
			}
		}
		
		if ( 2 != what ) {
			if ( null != url ) url += "?" + mService.mConfiguration.standardPostData( true ).toString();
		}
		
		mMediaVault = null;
		playStreamURL( url , 1 == what );
		try{
		if (mService.mConfiguration.mFirstStation.equals("")) {
			mService.mConfiguration.mFirstStation = mIdentifier;
		}
		if (mService.mConfiguration.mSessionStations.containsKey(station.getCallLetters())) {
			mService.mConfiguration.mSessionStations.put(station.getCallLetters(), mService.mConfiguration.mSessionStations.get(station.getCallLetters()) + 1);
		} else {
			mService.mConfiguration.mSessionStations.put(station.getCallLetters(), 1);
		}
		}catch(Exception err)
		{
		}
		if ( null != metadata ) updateMetadata( metadata );
	}
	
	protected void playStation( String inLetters ) {
		if ( null != mService ) playStation( mService.mConfiguration.stationByLetters( inLetters ) );
	}
	
	public void playArchive( IHRPremiumChannel inChannel , IHRPremiumItem inItem ) {
		IHRHashtable			metadata = new IHRHashtable();
		String					url;
		int						what = 0;
		
		if ( null == inItem ) {
			mStation = inChannel.getStation();
			mMediaVault = inChannel.get( IHRPremiumChannel.kMediavaultURL );
			
			what = 1;
			url = inChannel.get( IHRPremiumChannel.kStreamURL );
			
			metadata.put( "artist" , inChannel.getName() );
			metadata.put( "track" , inChannel.getDescription() );
		} else {
			IHRCache			cache = mService.mCache;
			
			mStation = inChannel.getStationForItem( inItem );
			mMediaVault = null;
			
			what = 2;
			url = inItem.getLink();
			
			//	play local file from cache if available
			if ( IHRCache.kStateAbsent != cache.stateForFileWithURL( url ) ) {
				url = /*"file://" +*/ cache.pathForURL( url );
			}
			
			metadata.put( "artist" , inItem.getName() );
			metadata.put( "track" , inChannel.getName() );
		}
		
		mSite = inChannel.getSite();
		mIdentifier = mStation.getCallLetters();
		mDescription = mStation.getName() + "\n" + mStation.getDescription();
		mMetadata = null;
		
		playStreamURL( url , 1 == what );
		updateMetadata( metadata );
	}
	
	public void updateNotification( IHRHashtable inMetadata ) {
		String[]				description = ( null == mDescription ) ? null : mDescription.split( "\n" );
		int						lines = ( null == description ) ? 0 : description.length;
		
		String					caption = null;
		String					content = null;
		
		if ( null != inMetadata ) {
			caption = (String)inMetadata.get( "track" );
			content = (String)inMetadata.get( "artist" );
		}
		
		if ( null == caption || 0 == caption.length() ) caption = ( lines > 0 ) ? description[0] : "Streamradio";
		if ( null == content || 0 == content.length() ) content = ( lines > 1 ) ? description[1] : "Now Playing";
		
		if ( null != mService ) mService.updateNotification( caption , content );
	}
	
	//	registerReceiver( this , new IntentFilter( kMessagePlayer ) );
	public Intent intent( String inAction ) {
		return new Intent( kMessagePlayer ).putExtra( "action" , inAction );
	}
	
	public void updateMetadata( IHRHashtable inMetadata ) {
		updateNotification( inMetadata );
		
		mMetadata = inMetadata;
		
		if ( null != mMetadata && null != mStation ) {
			//	for IHRXMLTrackInfo.run
//			mMetadata.put( "logo_url" , mStation.get( IHRStation.kLogoURL ) );
//			mMetadata.put( "station_url" , mStation.get( IHRStation.kStationURL ) );
			mMetadata.put( "stationid" , mStation.get( IHRStation.kStationID ) );
			mMetadata.put( "callletters" , mStation.get( IHRStation.kCallLetters ) );
		}
		
		if ( null != mService ) mService.sendBroadcast( intent( "metadata" ).putExtra( "metadata" , mMetadata ) );
	}
	
	public void updateStatus( String inStatus ) {
		mStatus = inStatus;
		
		if ( null != mService && !(inStatus.equals(kStatusConnectionFailed) || inStatus.equals(kStatusNoNetwork))) mService.sendBroadcast( intent( "status" ).putExtra( "status" , mStatus ) );
	}
	
	protected void updateState( int inState ) {
		if ( mPlayState != inState ) {
			final String[]		status = { kStatusIdle , kStatusOpening , kStatusOpening , kStatusPreparing , kStatusPlaying , kStatusStopped , kStatusStopped , kStatusFinished , kStatusStopped , kStatusError };
			
			updateStatus( status[( inState >= kStateIdle && inState <= kStateError ) ? inState : 0] );
		}
		
		mPlayState = inState;
	}
	
	protected void updateStateCease( int inState , boolean inRecoverable ) {
//		debug( "updateStateCease " + inState + " " + inRecoverable );
		
		if ( null == mPlayer ) inRecoverable = false;

		if ( mPlayState >= kStateOpening && mPlayState < kStatePrepared && ! inRecoverable ) {
			mOpenAttemptCount = 0;
		}
		
		cease();
		updateState( inState );
		
		if ( mPlayRequested && inRecoverable ) {
			updateStatus( kStatusOpening );			//	show proper feedback
			
			mService.mThreadable.remove( kRetry );
			mService.mThreadable.handle( kRetry , 500L );
		}
	}
	
	public void updateThroughput( String message, boolean setIgnoreRest ) {
		if ( mService != null && ! mIgnoreThroughputMessages ) {
			mService.sendBroadcast( intent( "throughput" ).putExtra( "message", message ) );
		}
		
		try {
			mThroughput = (int) Math.round(Double.parseDouble(message.split(" ")[0]));
		} catch (NumberFormatException e) {
			mThroughput = 0;
		}
		
		if ( setIgnoreRest ) mIgnoreThroughputMessages = true;
	}
	
	public void onSetVolume( float inVolume ) {
		mVolume = inVolume;
		
		if ( mVolume > 0 ) {
			mService.preferencesPut( "player_volume" , mVolume );
		}
		
		if ( null != mPlayer ) {
			mPlayer.setVolume( mVolume , mVolume );
			mService.sendBroadcast( intent( "volume" ).putExtra( "volume" , mVolume ) );
		}
	}
	
	public void onSetVolumeByKeycode( int inKeycode ) {
		float					volume = mVolume;
		
		if ( KeyEvent.KEYCODE_MUTE == inKeycode ) {
			if ( volume > 0 ) {
				mVolumeUnmuted = volume;
				mVolume = 0;
			} else {
				mVolume = mVolumeUnmuted;
			}
			
			if ( null != mPlayer ) {
				mPlayer.setVolume( mVolume , mVolume );
				mService.sendBroadcast( intent( "volume" ).putExtra( "volume" , mVolume ) );
			}
		} else {
			//	1.0 0.5 0.25 ...
			//	0.0 0.5 0.75 ...
			
			if ( KeyEvent.KEYCODE_VOLUME_UP == inKeycode ) {
				volume += ( 1 / kVolumeSteps );
				if ( volume > 1 ) volume = 1;
				
//				volume = ( volume < 1 ) ? 1 - ( 1 - volume ) / 2 : 1;
//				volume = ( volume < 1 ) ? volume * 2 : 1;
			} else {
				volume -= ( 1 / kVolumeSteps );
				if ( volume < 0 ) volume = 0;
				
//				volume = ( volume < 1 ) ? 1 - ( 1 - volume ) * 2 : (float)( 1.0 - 1.0/65536.0 );
//				volume = ( volume > 0 ) ? 1 - ( 1 - volume ) * 2 : 0;
//				volume = ( volume > 0 ) ? volume / 2 : 0;
			}
			
			onSetVolume( volume );
		}
	}
	
	public void onCreate( IHRService inService ) {
		mService = inService;
		mVolume = inService.preferencesGet( "player_volume" , (float)1.0 );
		
		
		((TelephonyManager)mService.getSystemService( Context.TELEPHONY_SERVICE )).listen( this , PhoneStateListener.LISTEN_CALL_STATE );
	}

	public void onDestroy( IHRService inService ) {
		((TelephonyManager)mService.getSystemService( Context.TELEPHONY_SERVICE )).listen( this , PhoneStateListener.LISTEN_NONE );
		
		mService = null;
	}
	
	@Override
	public void onCallStateChanged( int inState , String inPhoneNumber ) {
		switch ( inState ) {
		case TelephonyManager.CALL_STATE_IDLE:
			if ( 0 != ( mPhoneStatePaused & 1 ) ) mPlayRequested = true;
			if ( 0 != ( mPhoneStatePaused & 2 ) ) pause( false );
			
			mPhoneStatePaused = 0;
			break;
		case TelephonyManager.CALL_STATE_RINGING:
		case TelephonyManager.CALL_STATE_OFFHOOK:
			if ( 0 == mPhoneStatePaused ) {
				if ( mPlayRequested ) {
					mPlayRequested = false;
					mPhoneStatePaused |= 1;
				}
				
				if ( kStatePlaying == mPlayState || ( null != mPlayer && mPlayer.isPlaying() ) ) {
					pause( true );
					mPhoneStatePaused |= 2;
				}
			}
			break;
		}
	}
	
	public void onBufferingUpdate( MediaPlayer mp , int percent ) {
		long now = Calendar.getInstance().getTime().getTime();
		if (null != mService && null != mService.mConfiguration) {
			if (percent < 75 && null != mStatus && !mStatus.equals(kStatusBuffering)) {		
					mService.mConfiguration.mCurrentBufferTime = now;
					mService.mConfiguration.mBufferingCount++;
					mService.mConfiguration.mStreamRebufferCount++;
			} else if (percent >= 75 && null != mStatus &&  mStatus.equals(kStatusBuffering)){
				
				if (mService.mConfiguration.mStreamIsNew) {
					mService.mConfiguration.mStreamPlayDelay = 
						Math.round((Calendar.getInstance().getTime().getTime() - mService.mConfiguration.mStreamStartTime)/1000);
					mService.mConfiguration.mStreamIsNew = false;
				}
				if (mService.mConfiguration.mCurrentBufferTime < 1)
				{
					mService.mConfiguration.mBufferingCount++;
					mService.mConfiguration.mCurrentBufferTime = now;
				}
				mService.mConfiguration.mBufferingDuration += (now - mService.mConfiguration.mCurrentBufferTime);
				if ( mService.mConfiguration.mStreamRebufferCount > 1) {
					if (mService.mConfiguration.mCurrentBufferTime > 1) {
						mService.mConfiguration.mStreamRebufferDuration += (now - mService.mConfiguration.mCurrentBufferTime);
					}
				}
				mService.mConfiguration.mCurrentBufferTime = 0; 			
				
			}
		}
//		buffering is displayed to user as on or off, not level meter
		if ( mp == mPlayer ) updateStatus( ( mBufferPercent = percent ) < 75 ? kStatusBuffering : kStatusBuffered );

	}

	public void onCompletion( MediaPlayer mp ) {
		if ( mp == mPlayer ) {
			if ( mStreaming ) updateStateCease( kStateFinished , true );
			else updateState( kStateFinished );
		}
	}

	public boolean onError( MediaPlayer mp , int what , int extra ) {
		if ( mp == mPlayer ) {
			updateStateCease( kStateError , what != MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK );
		}
		return true;	//	handled else call completion
	}

	public boolean onInfo( MediaPlayer mp , int what , int extra ) {
		return false;
	}
	
	public void onSeekComplete( MediaPlayer mp ) {
		if ( mp == mPlayer && null != IHRService.g ) {
			IHRHashtable		details = new IHRHashtable();
			
			details.put( "duration" , mp.getDuration() );
			details.put( "position" , mp.getCurrentPosition() );
			details.put( "paused" , new Boolean( !mp.isPlaying() ) );
			
			IHRService.g.mConfiguration.notifyClient( kNotifyNamePlayer , details );
		}
	}
	
	public void onPrepared( MediaPlayer mp ) {
		if ( mp == mPlayer ) {
			if ( mPlayRequested ) {
				updateState( kStatePlaying );
				mPlayer.start();
			} else {
				updateState( kStatePrepared );
			}
			
			onSeekComplete( mp );
		}
	}
	
	public void rtspClosed( IHRRTSP rtsp , Exception err ) {
		if ( rtsp == mRTSP ) updateStateCease( kStateClosed , true );
	}
	
	public void rtspMetadata( IHRRTSP rtsp , IHRHashtable metadata ) {
		boolean					equal = false;
		
		if ( rtsp != mRTSP ) {
			equal = true;		//	ignore
		} else if ( null == metadata || null == mMetadata ) {
			equal = ( metadata == mMetadata );
		} else {
			String[]			keys = { "track" , "artist" };
			int					index , count = keys.length;
			
			//	cannot use metadata.equals( mMetadata ) because mMetadata modified on update
			for ( index = 0 ; index < count ; ++index ) {
				Object			a = metadata.get(  keys[index] );
				Object			b = mMetadata.get(  keys[index] );
				
				equal = ( null == a || null == b ) ? ( a == b ) : a.equals( b );
				if ( !equal ) break;
			}
		}
		
		if ( !equal ) {
			updateMetadata( metadata );
		}
	}
	
	public void rtspThroughput( IHRRTSP rtsp, String message ) {
		updateThroughput( message, false );
	}
	
	public void run() {
		String					name = "IHRMediaPlayer";
		
		if ( null != mIdentifier ) name += " " + mIdentifier;
		
		Thread.currentThread().setName( name );
		
		//	mPlayer must be created in new thread that will be owned by mPlayer
		begin( mStreamURL , mStreaming );
	}
	
	public void pauseForSeek( boolean inPause ) {
		if ( null != mPlayer && !mStreaming && mPlayRequested && mPlayState > kStatePreparing && mPlayState < kStateClosed && mPlayer.getDuration() > 0 ) {
			if ( inPause ) {
				mPlayer.pause();
				mPlayPaused = true;
			} else if ( mPlayPaused ) {
				mPlayer.start();
				mPlayPaused = false;
			}
		}
	}
	
	public boolean handleMessage( Message inMessage ) {
		boolean					result = true;
		
		switch ( inMessage.what ) {
		case kStop: 
			play( false ); 
			break;
		case kPlay: play( true ); break;
		case kPause: pause( true ); break;
		//case kTogglePlaying: play( !mPlayRequested ); break;
		case kTogglePlaying: pause( mPlayRequested ); break;

		case kRetry: if ( mPlayRequested ) play( true ); break;
		
		case kPlayStation: playStation( ((Parcel)inMessage.obj).createStringArrayList() ); break;
		case kPlayStationByLetters: playStation( ((Parcel)inMessage.obj).readString() ); break;
		case kOpenFileURL: playStreamURL( ((Parcel)inMessage.obj).readString() , false ); break;
		case kOpenStreamURL: playStreamURL( ((Parcel)inMessage.obj).readString() , true ); break;
		
		case kSetVolume: onSetVolume( ((Parcel)inMessage.obj).readFloat() ); break;
		case kSetVolumeByKeycode: onSetVolumeByKeycode( ((Parcel)inMessage.obj).readInt() ); break;
		case kStreamIsNew: setStreamIsNew(((Parcel)inMessage.obj).readLong() ); break;
		}
		
		return result;
	}
	
	//	called on UI thread
	public boolean handleTransactions( int code , Parcel data , Parcel reply , int flags ) {
		boolean					result = true;
		
		if ( code > IHRService.kPerformOnThread ) {
			mService.mThreadable.handle( code , 0 , flags , data );
		} else switch ( code ) {
		case kIsPlaying: reply.writeInt( ( null != mPlayer && mPlayer.isPlaying() ) ? 1 : 0 ); break;
		case kIsStopped: reply.writeInt( ( null == mPlayer || !mPlayer.isPlaying() ) ? 1 : 0 ); break;
		case kIsBuffering: reply.writeInt( ( mBufferPercent < 100 ) ? 1 : 0 ); break;
		case kIsPlayRequested: reply.writeInt( mPlayRequested ? 1 : 0 ); break;
		
		case kSeekPause:
		case kSeekUnpause: pauseForSeek( kSeekPause == code ); break;
		
		case kGetVolume: reply.writeFloat( mVolume ); break;
		case kGetBufferPercent: reply.writeInt( mBufferPercent ); break;
		case kGetPosition: if ( null != mPlayer ) reply.writeIntArray( new int[] { mPlayer.getCurrentPosition() , mPlayer.getDuration() , mPlayer.isPlaying() ? 1 : 0 } ); break;
		
		case kSetStation: mStation = ( 0 == data.dataAvail() ) ? null : new IHRStation( data.createStringArrayList() ); break;
		case kSetDescription: mDescription = data.readString(); break;
		case kSetIdentifier: mIdentifier = data.readString(); break;
		case kSetVolume: onSetVolume( data.readFloat() ); break;
		case kSetVolumeByKeycode: onSetVolumeByKeycode( data.readInt() ); break;
		case kSetPosition: if ( null != mPlayer ) mPlayer.seekTo( data.readInt() ); break;
		
		case kCopyDescription: reply.writeString( mDescription ); break;
		case kCopyIdentifier: reply.writeString( mIdentifier ); break;
		case kCopyMetadata: reply.writeMap( mMetadata ); break;
		case kCopyStatus: reply.writeString( mStatus ); break;
		case kCopyURL: reply.writeString( mStreamURL ); break;
		case kCopyStation: if ( null != mStation ) reply.writeStringList( mStation ); break;
		case kStreamIsNew: setStreamIsNew(data.readLong() ); break;
		
		default: result = false;
		}
		
		return result;
	}
	
	public void setStreamIsNew(long inStartTime) {
		if (!mService.mConfiguration.mStreamIsNew)
		{
			//send tracker info from previous stream
		}
		mService.mConfiguration.mStreamIsNew  = true;
		mService.mConfiguration.mStreamStartTime = inStartTime;
		mService.mConfiguration.mStreamRebufferCount = 0;
		mService.mConfiguration.mStreamRebufferDuration = 0;
		mService.mConfiguration.mStreamPlayDelay = 0;
	}
	
	public IBinder onBind( Intent intent ) {
		if ( intent.getBooleanExtra( "player" , false ) ) {
			return new Binder() {
				@Override
				public boolean onTransact( int code , Parcel data , Parcel reply , int flags ) {
					return handleTransactions( code , data , reply , flags );
				}
			};
		} else {
			return null;
		}
	}

}
