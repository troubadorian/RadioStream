package com.troubadorian.streamradio.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Handler.Callback;
import android.telephony.TelephonyManager;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRPlatform;
import com.troubadorian.streamradio.client.model.IHRURLEncoder;
import com.troubadorian.streamradio.client.model.IHRVector;
import com.troubadorian.streamradio.model.IHRCity;
import com.troubadorian.streamradio.model.IHRFormat;
import com.troubadorian.streamradio.model.IHRHTTP;
import com.troubadorian.streamradio.model.IHRHTTPDelegate;
import com.troubadorian.streamradio.model.IHRLocal;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumChannels;
import com.troubadorian.streamradio.model.IHRPremiumCredentials;
import com.troubadorian.streamradio.model.IHRPremiumItem;
import com.troubadorian.streamradio.model.IHRStation;
import com.troubadorian.streamradio.model.IHRUtilities;
import com.troubadorian.streamradio.model.IHRXML;
import com.troubadorian.streamradio.model.IHRXMLLocalStations;

//Code added by das for showing the dialog box for location
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
//Code ends here 

public class IHRServiceConfiguration implements IHRHTTPDelegate, LocationListener, Callback {
	protected static final long			kKeyClientID = 0xac7ef5fcf7bc2ea3L;		// com.clearchannel.iheartradio.clientID
	public static final String			mApplicationVersion = "2.3";			//	also in manifest
	
	public static final String			kKeyStationsVersion = "station_list_version";
	public static final String			kKeyStationsParsed = "station_list_parsed";
	public static final String			kKeyStationsFormat = "station_list_format";
	
	public static final String			kNotifyNameEndingOfflineMode = "endingOfflineMode";
	public static final String			kNotifyNameSiteValidated = "siteValidated";
	
	//	IHRService.kConfiguration
	public static final int				kIsStartupAvailable = 101;
	public static final int				kAreFilesAvailable = 102;
	public static final int				kIsLocationAvailable = 103;
	public static final int				kIsOfflineMode = 104;
	
	public static final int				kCopyStatus = 106;
	public static final int				kCopyServerPath = 107;
	public static final int				kCopyCommonParameters = 108;
	public static final int				kCopyUniqueParameters = 109;
	
	public static final int				kConfigurationCopyStartup = 221;
	public static final int				kConfigurationCopyCities = 222;
	public static final int				kConfigurationCopyFormats = 223;
	public static final int				kConfigurationCopyStations = 224;
	public static final int				kConfigurationCopyLocal = 225;
	public static final int				kConfigurationCopyImage = 226;
	
	public static final int				kGetStationsCount = 111;
	public static final int				kCopyStation = 112;
	public static final int				kCopyStations = 113;
	public static final int				kCopyStationByLetters = 114;
	
	public static final int				kGetFormatsCount = 121;
	public static final int				kCopyFormat = 122;
	public static final int				kCopyFormats = 123;
	public static final int				kCopyFormatByName = 124;
	
	public static final int				kGetCitiesCount = 131;
	public static final int				kCopyCity = 132;
	public static final int				kCopyCities = 133;
	public static final int				kCopyCityByName = 134;
	
	public static final int				kGetPremiumCount = 141;
	public static final int				kCopyPremiumChannel = 142;
	public static final int				kCopyPremiumChannels = 143;
	public static final int				kCopyPremiumBySite = 144;
	public static final int				kCopyPremiumByName = 145;
	public static final int				kCopyPremiumChannelsString = 146;
	public static final int				kCopyPremiumItemsBySite = 147;
	public static final int				kFetchPremiumChannel = 148;
	public static final int				kFetchPremiumChannels = 149;
	
	public static final int				kSiteHasAnyAuthenticated = 191;
	public static final int				kSiteHasAuthenticated = 192;
	public static final int				kSiteValidate = 193;
	public static final int				kSiteUncache = 194;
	public static final int				kSiteDiscard = 195;
	public static final int				kSitePropose = 196;
	public static final int				kSiteHasArchives = 199;
	
	public static final int				kCopyFeatured = 161;
	public static final int				kCopyAutoplayStation = 162;
	public static final int				kCopyDefaultCity = 163;
	public static final int				kCopyLocal = 163;
	
	public static final int				kCopyStartupAdsPrefix = 171;
	public static final int				kCopyStartupAdsForPurpose = 172;
	public static final int				kCopyStartupMediaVault = 179;
	
	public static final int				kSetAutoplayStation = 181;
	
	public static final int				kCopyProgressForURL = 201;
	public static final int				kPlayPremiumItem = 202;
	public static final int				kDeletePremiumItem = 203;
	public static final int				kCachePremiumItem = 204;
	public static final int				kCachePremiumItemOrPause = 205;
	
	//	IHRService.kPerformOnThread + IHRService.kConfiguration
	public static final int				kConfigurationPerformOnThread = 1100;
	public static final int				kConfigurationFetchStartup = 1101;
	public static final int				kConfigurationFetchFiles = 1102;
	public static final int				kConfigurationFetchLocal = 1103;
	public static final int				kMonitorConnectivity = 1105;
	public static final int				kTryAutoplayStation = 1109;
	
	
	//	Binder.LAST_CALL_TRANSACTION
	
	private IHRService					mService;
	
	protected boolean					mListeningForLocation;
	protected String					mDeviceID;
	protected String					mSessionID;
	public String						mStatus;
	
	public byte[]						mCitiesData;	//	"cities", mXMLStartup.mCitiesVersion
	public byte[]						mFormatsData;	//	"formats", mXMLStartup.mFormatsVersion
	public byte[]						mStationsData;	//	"station_list", mXMLStartup.mStationListVersion
	public byte[]						mLocationData;	//	"local_stations"
	public byte[]						mStartupData;	//	"startup"
	public byte[]						mImageData;		//	mXMLStartup.mAdsSplashDartURL
	public byte[]						mPremiumData;	//	"premium"
	
	public IHRLocal						mLocal;
	public List<IHRCity>				mCities;
	public List<IHRFormat>				mFormats;
	public List<IHRStation>				mStations;
	public IHRPremiumChannels			mChannels;
	public Map<String,List>				mStationsLookup;
	
	public int							mConfiguration;
	public IHRConfigurationStartup		mXMLStartup;
	public List<String>					mFeatured;
    
	public List<String>					mTagged;
	public List<String>					mFavorited;

	
	
	public String mFirstStation;
	public long mBufferingDuration;
	public long mStreamStartTime;
	public long mStreamRebufferDuration;
	public int mStreamRebufferCount;
	public long mStreamBufferingDuration;
	public int mStreamPlayDelay;
	public String mCurrentMode;
	public long mCurrentBufferTime;
	public long mCurrentModeTime;
	public String mCurrentNetwork;
	public long mCurrentNetworkTime;
	public int mBufferingCount;
	public int mWifiDuration;
	public int mDataDuration;
	public int mOfflineDuration;
	public int mForegroundDuration;
	public int mBackgroundDuration;
	public boolean mStreamIsNew;
	
	//update the splash screen display
	private String statusDisplay;
	private int statusCount = 0;
	private Handler mDisplayHandler = new Handler();

	//Code inserted by sriram for handling gps -- 08/13/2010
    private String mCurrentProvider;
    private Criteria mCriteria;
    private List<MyLocationListener> mListeners;
    private LocationManager mLocationMgr;
    public String latitude="";//Sriram -- 08/18/2010
      public String longitude="";//Sriram-- 08/18/2010
    public static int startvar=0;//Sriram -- 08/18/2010	
	public MyLocationListener temp=null;//Code changed by sriram on 08-27-2010
 
//Code ends here 
	// following line added by Lance - 27 sep, 2010 for endless loop fix 
		
	public boolean serviceStopped = false;
	
	public HashMap<String, Integer> mSessionStations;
	
	public Intent action( String inAction ) {
		return new Intent( "com.clearchannel.iheartradio.configuration" ).putExtra( "action" , inAction );
	}
	
	public void notifyClient( String inName , IHRHashtable inDetails ) {
		mService.sendBroadcast( action( "notification" ).putExtra( "name" , inName ).putExtra( "details" , inDetails.bundle( null ) ) );
	}
	
	public void updateAction( String inAction , String inExtra ) {
		Intent					intent = action( inAction );
		
		if ( null != inExtra ) intent.putExtra( inAction , inExtra );
		if ( null != mService ) mService.sendBroadcast( intent );
	}
	
	public void updateAction( String inAction ) {
		updateAction( inAction , null );
	}
	
	public void updateStatus( String inStatus ) {
		mStatus = inStatus.indexOf(".") > 0 ? inStatus.substring(0, inStatus.indexOf(".")) : inStatus;
		//mStatus = inStatus;
		updateAction( "status" , inStatus );
	}
	
	public void onCreate( IHRService inService ) {
		mService = inService;
		mCurrentMode = "";
		mCurrentModeTime = 0;
		mFirstStation = "";
		mBufferingDuration = 0;
		mBufferingCount = 0;
		mCurrentNetwork = "";
		mWifiDuration = 0;
		mDataDuration = 0;
		mOfflineDuration = 0;
		mForegroundDuration = 0;
		mBackgroundDuration = 0;
		mSessionStations = new HashMap<String, Integer>();
		
		deviceID();
		mSessionID = "" + ( System.currentTimeMillis() / 1000 );
		mChannels = new IHRPremiumChannels();
		
//		IHRPreferences.onCreate( this );
	}
	
	public void onDestroy() {
		mService = null;
		
//		IHRPreferences.onDestroy();
	}
	
	protected String deviceID() {
		if ( null == mDeviceID || 0 == mDeviceID.length() ) {
			mDeviceID = mService.preferencesGet( "_" + kKeyClientID , "" );
			
			if ( null == mDeviceID || 0 == mDeviceID.length() ) {
				byte[]			bytes;
				int				i, j, k, n;
				String			s;
				
				s = ((TelephonyManager)mService.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
				if ( null == s ) s = "";
				
				s += IHRPlatform.getDeviceName();
				s += System.currentTimeMillis();
				
				bytes = s.getBytes();
				bytes = IHRPlatform.SHA1Digest( bytes , 0 , bytes.length );
				
				for ( i = 0, j = 0, s = ""; i < 5; ++i ) {
					switch ( i ) {
						case 0:                         n = 4;      break;
						case 1: case 2: case 3:         n = 2;      break;
						case 4: default:                n = 6;      break;
					}
					
					for ( k = 0; k < n; ++k ) s += IHRUtilities.hexify( bytes[ j++ ], false );
					
					if ( i < 4 ) s += '-';
				}
				
				mService.preferencesWrite( "_" + kKeyClientID , s );
				
				mDeviceID = s;
			}
		}
		
		return mDeviceID;
	}
	
/**
	private List debugFeatured() {
		IHRVector				result = new IHRVector();
		
		result.add( "! VIDEO TEST STATION" );
		mStations.add( new IHRStation( new String[] {
			"" ,																			//	kAdsDartParams
			"1" ,																			//	kAdsDisabled
			"! VIDEO TEST STATION" ,														//	kCallLetters
			"Not Fair" ,																	//	kDescription
			"0" ,																			//	kDisableSongTagging
			"" ,																			//	kFileArtist
			"" ,																			//	kFileLyricsID
			"" ,																			//	kFileTitle
			"" ,																			//	kFileURL
			"0" ,																			//	kIsTalk
			"" ,																			//	kLogoURL
			"Lily Allen" ,																	//	kName
			"" ,																			//	kStationID
			"" ,																			//	kStationURL
			"" ,																			//	kStreamURL
			"" ,																			//	kStreamURLAuthenticated
			"" ,																			//	kStreamURLFallback
			"" ,																			//	kStreamURLFallbackAuthenticated
			"" ,																			//	kTunerAddress
			"http://www.balance-software.com/clearchannel/Streamradio/media/Lily_Allen_Not_Fair_iPhone.m4v" ,	//	kVideoURL
			"http://www.balance-software.com/clearchannel/Streamradio/media/Lily_Allen_Not_Fair_iPhone.3gp"		//	kVideoURLLowBandwidth
		} ) );
	
		result.add( "! AUDIO TEST STATION" );
		mStations.add( new IHRStation( new String[] {
			"" ,																			//	kAdsDartParams
			"1" ,																			//	kAdsDisabled
			"! AUDIO TEST STATION" ,														//	kCallLetters
			"Down" ,																		//	kDescription
			"0" ,																			//	kDisableSongTagging
			"311" ,																			//	kFileArtist
			"" ,																			//	kFileLyricsID
			"Down" ,																		//	kFileTitle
			"http://www.balance-software.com/clearchannel/Streamradio/media/down.mp3" ,		//	kFileURL
			"0" ,																			//	kIsTalk
			"" ,																			//	kLogoURL
			"311" ,																			//	kName
			"" , "" , "" , "" , "" , "" , "" , "" , ""
		} ) );
		
		mStationsLookup = IHRStation.reverseStations( mStations );
		
		return result;
	}
/**/
	
	public List existingStations( List inStations , Map<String , List> inStationsLookup ) {
		List					result = null;
		
		if ( null == inStationsLookup || null == inStations ) {
			result = inStations;
		} else {
			int					i , n = inStations.size();
			
			result = new IHRVector();
			
			for ( i = 0 ; i < n ; ++i ) {
				String			letters = (String)inStations.get( i );
				
				if ( null != inStationsLookup.get( letters ) ) {
					result.add( letters );
				}
			}
		}
		
		return result;
	}
	
	public IHRURLEncoder standardPostData( boolean includeUniques ) {
		IHRURLEncoder			result;
		String					string;
		try{
		result = new IHRURLEncoder();

		if ( IHRPlatform.isBeingDebugged() ) {
			result.append( "devBuild", "1" );
		}
		
		/**	TO DO: remove debug code
		result.append( "clientType", "BlackBerry" );
		/*/
		result.append( "clientType", "Android" );
		/**/
		
		string = ((TelephonyManager)mService.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName().replace("&", "");
		if ( string != null && string.length() > 0 ) {
			//result.append( "currentNetwork", string );
			//Code changed by sriram for handling encoding
			String cnetwork=string;
			String tmpcnetwork=cnetwork.replaceAll("&", "_");
			String tmpcnetwork1=tmpcnetwork.replaceAll(" ", "_");
			result.append( "currentNetwork", tmpcnetwork1 );
						
			//code ends here 
		}

		string = IHRPlatform.getDeviceName();
		if ( string != null && string.length() > 0 ) {
			result.append( "deviceName", string );
		}

		result.append( "iheartradioVersion", mApplicationVersion );
		result.append( "osVersion", IHRPlatform.getSoftwareVersion() );

		if ( includeUniques ) {
			result.append( "deviceId", mDeviceID );
			result.append( "sessionID", mSessionID );
		}
		}catch(Exception err)
		{
			System.out.println("Exception err1");
			return null;
		}
		return result;
	}
	
	public IHRStation getAutoplayStation() {
		IHRStation				station = null;
		String					letters = null;
		String[]				strings = null;
		
		letters = mService.preferencesGet( "autoplay_letters" , "" );
		
		if ( null != letters && 0 != letters.length() ) {
			station = (IHRStation)mStationsLookup.get( letters );
			
			if ( null == station ) {
				strings = mService.preferencesCopyStrings( "autoplay_station" );
				station = ( null == strings || 0 == strings.length ) ? null : new IHRStation( strings );
			}
		}
		
		return station;
	}
	
	
	/*
	 * tagged songs getters and setters
	 * 
	 * */
	
	public void tagAccess( int inAction ) {
		String					key = "tagged";
		
		if ( inAction > 0 ) {
			if ( inAction > 1 || mTagged.size() == 0 ) mTagged = null;
			
			if ( null == mTagged ) mService.preferencesUnset( key );
			else mService.preferencesWrite( key , mTagged );
		} else {
			if ( null == mTagged || inAction < 0 ) mTagged = mService.preferencesCopyStringList( key );
			if ( null == mTagged ) mTagged = new ArrayList<String>();
		}
	}
	
	public int tagNumber() {
		return ( null == mTagged ) ? 0 : mTagged.size() / 2;
	}
	
	public int tagLocate( String[] inTagged ) {
		int						result = -1;
		int						index , count;
		
		if ( null != inTagged && 2 == inTagged.length ) {
			if ( null == mTagged ) tagAccess( 0 );
			
			count = mTagged.size();
			
			for ( index = 0 ; index < count ; index += 2 ) {
				if ( inTagged[0].equals( mTagged.get( index ) ) && inTagged[1].equals( mTagged.get( index + 1 ) ) ) {
					result = index / 2;
					break;
				}
			}
		}
		
		return result;
	}
	
	public boolean tagExists( String[] inTagged ) {
		return !( tagLocate( inTagged ) < 0 );
	}
	
	public void tagAppend( String[] inTagged ) {
		if ( null != inTagged && 2 == inTagged.length && tagLocate( inTagged ) < 0 ) {
			if ( null == mTagged ) tagAccess( 0 );
			
			mTagged.add( inTagged[0] );
			mTagged.add( inTagged[1] );
			
			tagAccess( 1 );
		}
	}
	
	public void tagRemove( int inIndex ) {
		if ( inIndex < tagNumber() ) {
			mTagged.remove( inIndex * 2 );
			mTagged.remove( inIndex * 2 );
			
			tagAccess( 1 );
		}
	}
	
	public void tagRemove( String[] inTagged ) {
		int						index = tagLocate( inTagged );
		
		if ( !( index < 0 ) ) tagRemove( index );
	}
	
	
	public void setAutoplayStation( String inLetters ) {
		IHRStation				station = ( null == inLetters || 0 == inLetters.length() ) ? null : (IHRStation)mStationsLookup.get( inLetters );
		
		if ( null == station ) {
			mService.preferencesPut( "autoplay_letters" );
			mService.preferencesPut( "autoplay_station" );
		} else {
			mService.preferencesPut( "autoplay_letters" , inLetters );
			mService.preferencesPut( "autoplay_station" , station );
		}
	}
	
	public void tryAutoplayStation() {
		IHRStation				station = getAutoplayStation();
		
		if ( null != station ) {
			if ( false == mService.mPlayer.mPlayRequested && null == mService.mPlayer.mIdentifier ) {
				mService.mPlayer.playStation( station );
			}
		}
	}
	
	public void httpFetchComplete( IHRHTTP http ) {
		mImageData = http.getDataNoThrow();
		
		if ( null != mImageData ) {
			updateAction( "splash_ad" );
		}
	}

	public LocationManager getLocationManager() {
		LocationManager			result = null;
		
		try {
			result = (LocationManager)mService.getSystemService( Context.LOCATION_SERVICE );
		} catch ( Exception e ) {}
		
		return result;
	}
	
	public String getLocationProviderName() {
		String					result = null;
		LocationManager			manager = getLocationManager();
		
		if ( null != manager ) {
			Criteria			criteria = new Criteria();
			
			criteria.setAccuracy( Criteria.ACCURACY_FINE );
			criteria.setAltitudeRequired( false );
			criteria.setBearingRequired( false );
			criteria.setCostAllowed( false );
			criteria.setPowerRequirement( Criteria.NO_REQUIREMENT );
			criteria.setSpeedRequired( false );
			
			result = manager.getBestProvider( criteria , true );
			
			if ( null == result ) result = manager.getBestProvider( criteria , false );
		}
		
		if ( null == result ) {
			result = LocationManager.GPS_PROVIDER;		//	default to fine
//			result = LocationManager.NETWORK_PROVIDER;	//	default to course
		}
		
		return result;
	}
	
	public LocationProvider getLocationProvider() {
		LocationManager			manager = getLocationManager();
		
		return ( null == manager ) ? null : manager.getProvider( getLocationProviderName() );
	}
	
	public Location getLastLocation() {
		LocationManager			manager = getLocationManager();
		
		return ( null == manager ) ? null : manager.getLastKnownLocation( getLocationProviderName() );
	}
	
	public void listenForLocation( boolean inListen , LocationListener inListener ) {
		if ( inListen != mListeningForLocation ) {
			LocationManager		manager = getLocationManager();
			
			if ( null == manager ) {
				inListen = false;
			} else if ( inListen ) {
				String			name = getLocationProviderName();
				
				if ( null == name ) {
					inListen = false;
				} else {
					manager.requestLocationUpdates( name , 60000/* milliseconds */ , 100/* meters */ , inListener );
//					manager.requestLocationUpdates( name , 0/* milliseconds */ , 0/* meters */ , inListener );
				}
			} else {
				manager.removeUpdates( inListener );
			}
			
			mListeningForLocation = inListen;
		}
	}
	//Code changed by sriram for handling location -- on 08-13-2010
	public void LocationEventChanged(Location location)
	{
		IHRURLEncoder enc=standardPostData( true );
		if(enc==null)
			return;
		String					arguments = enc.toString();
		IHRXMLLocalStations		local = null;
		byte[]					data = null;
		
		arguments += "&latitude=" + location.getLatitude();
		arguments += "&longitude=" + location.getLongitude();
		System.out.println("Arguments "+arguments);
		data = IHRConfigurationFile.fetchFromServer( "local_stations" , arguments );
		
		try {
			local = new IHRXMLLocalStations( data );
			//Code changed by sriram on 08-27-2010
			latitude=""+location.getLatitude();
			longitude=""+location.getLongitude();
			//Code ends here 			
			mLocationData = data;
			mLocal = new IHRLocal( local.mName , local.mDistance , existingStations( local.mCallLetters , mStationsLookup ) );
			mService.sendBroadcast( action( "location" ).putExtra( "latitude" , location.getLatitude() ).putExtra( "longitude" , location.getLongitude() ) );
			mLocationMgr.removeUpdates(temp);// .removeUpdates( mListeners );//Code changed by sriram on 08-27-2010
			
			System.out.println("Location File Updated "+mLocal);
			IHRConfigurationFile.access( "local_stations" , "1" , data );
			// here is the code where the populating in the screen need to be called.
			//copyLocal(true);
		} catch ( Exception e ) {
			System.out.println("Exception in Location changed "+e.toString());
		}
		
		
		
	}
	//Code ends here 
	
	public void onLocationChanged( Location location ) {
		String					arguments = standardPostData( true ).toString();
		IHRXMLLocalStations		local = null;
		byte[]					data = null;
		
		arguments += "&latitude=" + location.getLatitude();
		arguments += "&longitude=" + location.getLongitude();
		
		data = IHRConfigurationFile.fetchFromServer( "local_stations" , arguments );
		
		try {
			local = new IHRXMLLocalStations( data );
			
			mLocationData = data;
			mLocal = new IHRLocal( local.mName , local.mDistance , existingStations( local.mCallLetters , mStationsLookup ) );
			mService.sendBroadcast( action( "location" ).putExtra( "latitude" , location.getLatitude() ).putExtra( "longitude" , location.getLongitude() ) );
			
			IHRConfigurationFile.access( "local_stations" , "1" , data );
		} catch ( Exception e ) {
		}
		
		//	TODO: is it started again when locals revisited?
		//	stop listening after fix to preserve battery
		if ( location.getAccuracy() < 100.0 /* meters */ ) {
			listenForLocation( false , this );
		}
	}
	
	public void onProviderDisabled( String provider ) {
		listenForLocation( false , this );
//		listenForLocation( true , this );
	}
	
	public void onProviderEnabled( String provider ) {
	}
	
	public void onStatusChanged( String provider , int status , Bundle extras ) {
		if ( LocationProvider.OUT_OF_SERVICE == status ) {
			listenForLocation( false , this );
		} else if ( LocationProvider.TEMPORARILY_UNAVAILABLE == status ) {
			/**	DEBUG - Portland
			Location			location = new Location( provider );
			
			location.setLongitude( -122.703509 );
			location.setLatitude( 45.433922 );
			
			onLocationChanged( location );
			/**/
			
			/**	DEBUG - New York
			Location			location = new Location( provider );
			
			location.setLongitude( -73.970432 );
			location.setLatitude( 40.768842 );
			
			onLocationChanged( location );
			/**/
		}
	}
	
	public IHRFormat stationsFormat( List inStations , boolean inTrySaved ) {
		IHRFormat				result = null;
		String[]				saved = inTrySaved ? mService.preferencesCopyStrings( kKeyStationsFormat ) : null;
		
		if ( null == saved || 0 == saved.length ) {
			result = new IHRFormat();
			result.add( IHRFormat.kAll );
			
			//**
			int					index , count = inStations.size();
			
			for ( index = 0 ; index < count ; ++index ) {
				String			s = ((IHRStation)inStations.get( index )).getCallLetters();
				char			c = s.charAt( 0 );
				
				if ( c == '!' ) continue;
//				if ( c == '#' ) continue;
				
				result.add( s );
			}
			/*/
			Iterator			iterator = inStations.keySet().iterator();
			
			while ( iterator.hasNext() ) {
				String			s = (String)iterator.next();
				char			c = s.charAt( 0 );
				
				if ( c == '!' ) continue;
//				if ( c == '#' ) continue;
				
				result.add( s );
			}
			
			Collections.sort( result , new IHRStation.NameComparator( inStations ) );
			/**/
			
			mService.preferencesPut( kKeyStationsFormat , result );
		} else {
			result = new IHRFormat( saved );
		}
		
		return result;
	}
	
	public void reverseTraffic() {
		int						index , count = mCities.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			IHRCity				city = mCities.get( index );
			String				url = city.getTrafficURL();
			
			if ( null != url && 0 != url.length() ) {
				IHRStation		station = IHRStation.parseCityTraffic( city.getName() , url );
				String			letters = station.getCallLetters();
				
				if ( !mStationsLookup.containsKey( letters ) ) {
					mStations.add( station );
					mStationsLookup.put( letters , station );
				}
			}
		}
	}
	
	public void tryFetchStartup() {
		if ( null == mXMLStartup && mService.hasConnectivity() ) {
			String				arguments = standardPostData( false ).toString();
			
			try {
				mStartupData = IHRConfigurationFile.fetchFromServer( "startup" , arguments );
				
				if ( null != mStartupData ) {
					mXMLStartup = new IHRConfigurationStartup( mStartupData );
				}
			} catch ( Exception e ) {}
			
			if ( null != mXMLStartup ) {
				if ( runParseStartup( 12 ) ) {
					notifyClient( kNotifyNameEndingOfflineMode , new IHRHashtable() );
				} else {
					mXMLStartup = null;
				}
			}
		}
		
		if ( null == mXMLStartup ) {
			mService.mThreadable.handle( kMonitorConnectivity , 10*1000 );
		}
	}
	
	public void runFetchStartup( int configuration ) {
		String					arguments = standardPostData( false ).toString();
		String					label = "";
		
//		Debug.startMethodTracing( "startup" );
		
		//	to reload without asking
		mConfiguration = configuration;
		
		switch ( configuration ) {
		case 1:
			label = "Staging ";
			IHRXML.sConfigFilesDirectory = "staging/";
			break;
		case 2:
			label = "Development ";
			IHRXML.sConfigFilesDirectory = "development/";
			break;
		default:
			label = "";
			IHRXML.sConfigFilesDirectory = "production/";
			break;
		}
		
//		onStatusChanged( null , LocationProvider.TEMPORARILY_UNAVAILABLE , null );
		listenForLocation( true , this );
		
		if ( !mService.hasConnectivity() && isReadyForOffline() ) {
			mService.mThreadable.handle( kMonitorConnectivity , 15*1000 );
			updateAction( "offline" );
			return;
		} else if (!mService.hasConnectivity()) {
			updateAction( "noconnection" );
			return;
		}

		runDisplayUpdate("Fetching " + label + "Configuration");
		for ( ;  ;  ) {
			try {
				mStartupData = IHRConfigurationFile.fetchFromServer( "startup" , arguments );
				
				if ( null != mStartupData ) {
					mXMLStartup = new IHRConfigurationStartup( mStartupData );
					break;
				}
			} catch ( Exception e ) {
				mDisplayHandler.removeCallbacks(mUpdateDisplayTask);}
		}
		mDisplayHandler.removeCallbacks(mUpdateDisplayTask);
		updateAction( "startup" );
	}
	
	@SuppressWarnings("unchecked")
	public boolean runParseStartup( int inAttempts ) {
	    // following line added by Lance on 27 sep, 2010 to fix endless loop issue
	    if(serviceStopped) return false;
	    
		String					arguments = standardPostData( false ).toString();
		String					saved = null;
		boolean					try_saved = false;
		int i = 0;
		
		if ( mXMLStartup.mAdsSplashDisplayTime > 0 && mXMLStartup.mAdsSplashDartURL != null ) {
			IHRHTTP.fetchAsynchronous( mXMLStartup.mAdsSplashDartURL, this, null );
		}
		
		if ( i < inAttempts && null != mService ) {
			updateStatus( "Fetching Stations" );
			
			mStations = null;
			saved = mService.preferencesGet( kKeyStationsVersion , "" );
			if ( saved.equals( mXMLStartup.mStationListVersion ) ) {
				saved = mService.preferencesGet( kKeyStationsParsed , "" );
				
				if ( saved.length() > 0 ) {
					//	load already saved station list if versions match
					mStations = IHRStation.fromString( saved );
					try_saved = true;
				}
			}
			
			if ( null == mStations || mStations.size() < 10 ) {
				runDisplayUpdate("Fetching Stations");
				for ( i = 0; i < inAttempts; ++i ) {
					mStationsData = IHRConfigurationFile.fetchOrLoad( "station_list", mXMLStartup.mStationListVersion , arguments );
					
					if ( null != mStationsData ) {
						updateAction( "stations" ); 
						break;
					}
				}
				mDisplayHandler.removeCallbacks(mUpdateDisplayTask); 
				
				if ( null != mStationsData && null != mService ) {
					updateStatus( "Reading Stations" );
					
					try {
						mStations = IHRConfigurationFile.arrayFromStations( mStationsData );
						
						//	save already parsed station list
						mService.preferencesPut( kKeyStationsVersion , mXMLStartup.mStationListVersion );
						mService.preferencesPut( kKeyStationsParsed , IHRStation.toString( mStations ) );
					} catch ( Exception e ) {
						mStationsData = null;
					}
				}
			}
			//Code inserted by sriram for getting the gps at the start of the app -- 08-18-2010
			System.out.println("Todays request for local ");
			startvar=1;
			//initializeLocation();
			//Code ends here 	
			if ( null != mStations && null != mService ) {
				mStationsLookup = IHRStation.reverseStations( mStations );
				mFeatured = existingStations( mXMLStartup.mFeatured , mStationsLookup );
				
				/**	TO DO: not release
				mFeatured = debugFeatured();
				/**/
			}
		}
		
		if ( i < inAttempts && null != mService ) {
			updateStatus( "Fetching Cities" );
			runDisplayUpdate("Fetching Cities");
			for ( i = 0; i < inAttempts; ++i ) {
				mCitiesData = IHRConfigurationFile.fetchOrLoad( "cities", mXMLStartup.mCitiesVersion , arguments );
				
				if ( null != mCitiesData ) { 
					updateAction( "cities" ); 
					break; 
				}
			}
			mDisplayHandler.removeCallbacks(mUpdateDisplayTask); 
			
			if ( null != mCitiesData && null != mService ) {
				updateStatus( "Reading Cities" );
				
				try {
					mCities = IHRConfigurationFile.arrayFromCities( mCitiesData , mStationsLookup );
					
					if ( null != mCities ) reverseTraffic();
				} catch ( Exception e ) {
					mCitiesData = null;
				}
				
				if ( null != mLocal && null != mStationsLookup ) {
					mLocal = new IHRLocal( mLocal.getName() , mLocal.getDistance() , existingStations( mLocal.copyStationList() , mStationsLookup ) );
				}
			}
		}
		
		if ( i < inAttempts && null != mService ) {
			runDisplayUpdate("Fetching Formats");
			for ( i = 0; i < inAttempts; ++i ) {
				mFormatsData = IHRConfigurationFile.fetchOrLoad( "formats", mXMLStartup.mFormatsVersion , arguments );
				
				if ( null != mFormatsData ) {  
					updateAction( "format" ); 
					break; 
				}
			}
			mDisplayHandler.removeCallbacks(mUpdateDisplayTask);
			
			if ( null != mFormatsData && null != mService ) {
				updateStatus( "Reading Formats" );
				
				try {
					mFormats = IHRConfigurationFile.arrayFromFormats( mFormatsData , mStationsLookup );
					mFormats.add( 0 , stationsFormat( mStations/*Lookup*/ , try_saved ) );
				} catch ( Exception e ) {
					mFormatsData = null;
				}
			}
		}
		
		if ( i < inAttempts && null != mService ) {
			
			byte[]				previous = IHRConfigurationFile.fetchOrLoad( "premium", IHRConfigurationFile.kVersionModified + mXMLStartup.mPremiumVersion , null );
			
			updateStatus( "Fetching Premium" );
			
			if ( null != previous && null == mChannels.channels() ) {
				//	mChannels requires previous state to compare against current state if different
				mChannels.parseChannelsXML( previous );
			}

			runDisplayUpdate("Fetching Premium");
			for ( i = 0; i < inAttempts; ++i ) {
				mPremiumData = IHRConfigurationFile.fetchOrLoad( "premium", mXMLStartup.mPremiumVersion , arguments );
				
				if ( null != mPremiumData ) { updateAction( "premium" ); break; }
			}
			mDisplayHandler.removeCallbacks(mUpdateDisplayTask); 
			
			if ( null != mPremiumData && null != mService ) {
				updateStatus( "Reading Premium" );
				
				try {
					//	only needed if version changed
					mChannels.parseChannelsXML( mPremiumData );
					mChannels.fetch( mXMLStartup.mPremiumVersion , false );	//	load existing rss based on channel xml
					
					/**	TO DO remove
					IHRPremiumCredentials.shared().propose( "coast" , "prninteractive" , "prninteractive" , null );
					IHRPremiumCredentials.shared().accept( "coast" , true );
					IHRPremiumCredentials.shared().propose( "seanhannity" , "balance" , "balance1" , null );
					IHRPremiumCredentials.shared().accept( "seanhannity" , true );
					/**/
				} catch ( Exception e ) {
					mPremiumData = null;
				}
			}
		}
		//Code added by sriram on 09-17-2010
		int ctr=0;
		runDisplayUpdate("Fetching Local Stations");
		while(true)
		{
			//System.out.println("Inside this condition1 "+latitude+" Longitude "+longitude);
			if(ctr>9000)
			{
				break;
			}
			ctr++;
		}
		mDisplayHandler.removeCallbacks(mUpdateDisplayTask); 
//Code ends here -- sriram on 09-17-2010
		
		updateAction( "complete" );
		
		if ( i < inAttempts && null != mService ) {
			mService.mThreadable.handle( kTryAutoplayStation );
			mService.commit();
			
			mService.mCache.restoreDownloadsAfterLaunch();
		}
		
//		Debug.stopMethodTracing();
		
		return ( i < inAttempts );
	}
	
	private Runnable mUpdateDisplayTask = new Runnable() {
		
		public void run() {
			mDisplayHandler.removeCallbacks(mUpdateDisplayTask);
			switch (statusCount%4) {
			case 0:
				updateStatus( statusDisplay );
				break;
			case 1:
				updateStatus( statusDisplay + " ." );
				break;
			case 2:
				updateStatus( statusDisplay + " .." );
				break;
			case 3:
				updateStatus( statusDisplay + " ..." );
				break;
			}
			statusCount++;
			mDisplayHandler.postDelayed(mUpdateDisplayTask, 1000);
		}
	};
	
	public void runDisplayUpdate(String status) {
		statusDisplay = status;
		statusCount = 0;
		mDisplayHandler.postDelayed(mUpdateDisplayTask, 1000);
		statusCount = 0;
	}
	
	public boolean isReadyForOffline() {
		boolean					result = false;
		
		if ( null == mChannels.channels() ) {
			byte[]				previous = IHRConfigurationFile.fetchOrLoad( "premium", IHRConfigurationFile.kVersionPrevious , null );
			
			if ( null != previous ) {
				mChannels.parseChannelsXML( previous );
				mChannels.fetch( null , false );	//	load existing rss based on channel xml
			}
		}
		
		if ( null != mChannels.channels() ) {
			//	could check that authenticated channels have archives but should be implied
			result = mService.mCredentials.hasAnyAuthenticated() && mChannels.hasArchives( true );
		}
		
		return result;
	}
	//Code inserted by sriram for handling the Location stuff on 08-13-2010
	   private class MyLocationListener implements LocationListener {
	        private String name;
	        
	        MyLocationListener(String s) {
	            name = s;
	        }
	        public void onLocationChanged(Location location) {
	        	if(mCurrentProvider != null && name.equals(mCurrentProvider)) {//Code changed by sriram on 07-23-2010 for handling the issue related to gps not getting recorded.
	            	{
	            		System.out.println("Provider updating data ");
	            		uploadGPS(location);
	            	}
	            }
	        	else if(name.equals("network") && mCurrentProvider.equals("gps"))
	        	{
         		uploadGPS(location);

	        	}
	        		        	
	        }
	        public void onProviderDisabled(String provider) {
	        	System.out.println("Provider disable ===="+provider);
	        	if(mCurrentProvider != null && provider.equals(mCurrentProvider)) {
	            	{
	            		System.out.println("Provider is going to be disabled");
	            		requestGPSProvider();
	            	}
	            }
	        }

	        public void onProviderEnabled(String provider) {
	        	System.out.println("Provider Enabled ===="+provider);
	        	if(mCurrentProvider == null || !provider.equals(mCurrentProvider)) {
	            	{
	            		System.out.println("Provider is going to enabled");
	            		requestGPSProvider();
	            	}
	            }
	        }

	        public void onStatusChanged(String provider, int status, Bundle extras) {
	        	System.out.println("Status Changed in GPS "+provider+" Status "+status);
	        	if(mCurrentProvider != null && mCurrentProvider.equals(provider) && 
	                    (status == LocationProvider.TEMPORARILY_UNAVAILABLE ||
	                            status == LocationProvider.OUT_OF_SERVICE)) {
	        			if(status==LocationProvider.TEMPORARILY_UNAVAILABLE)
	        			{
	        				
	        				System.out.println("Temporarily Unavailable");
	        			}
	        			else
	        			{
	        				
	            			System.out.println("Out of Service Message");
	        			}
	        		requestGPSProvider();
	            } else if(mCurrentProvider != null && !mCurrentProvider.equals(provider) &&
	                    status == LocationProvider.AVAILABLE) {

	            	requestGPSProvider();
	            }
	        }
	        
	   }
	   private boolean requestGPSProvider() {    
		   
		      String newProvider = mLocationMgr.getBestProvider(mCriteria, true);
		      System.out.println("GPS Provider 1");
		      try{
		      if(newProvider != null && !newProvider.equals(mCurrentProvider)) {
		    	  System.out.println("Modify GPS Request");
		          // modify our GPS request
		          mCurrentProvider = newProvider;
		      } else if (newProvider != null) {
		          // we have the same GPS status
		    	  System.out.println("We have some gps status");
		          return true;
		      } else {
		    	  System.out.println("No GPS");
		          // no GPS provider
		          mCurrentProvider = null;
		    	  //newProvider=LocationManager.GPS_PROVIDER;
		    	  //mCurrentProvider =newProvider;
		      }
		      }catch(Exception err)
		      {
		    	  System.out.println("Exception in request gps "+err.toString());
		      }
		      
		      //displayLog("current GPS provider: " + mCurrentProvider);
		      return mCurrentProvider != null;
		  }

		  private void modifyGPSUpdatingInterval(long time, float distance) {
		      System.out.println("GPS Interval");

			     for(int i = 0; i < mListeners.size(); i++) {
			          MyLocationListener temp = mListeners.get(i);
			          mLocationMgr.requestLocationUpdates(temp.name, 1000,
			                  10, temp);
			      }	  
			  
		  }

		  private void uploadGPS(Location location) {
		      float Spd = location.getSpeed() * 2.23693629F; // Miles per hour
		      //speedMoved=Spd;
		      LocationEventChanged(location);

		  }
		  //Code added by sriram on 09-17-2010
		  public void getLastKnownLoc()
		  {
			  Location location;
			  try{
			  location = getLastLocation();
			  if(location !=null)
				  LocationEventChanged(location);
			  else
				  System.out.println("Last Location is null");
			  }catch(Exception err)
			  {
				  System.out.println("Last Know location error "+err.toString());
			  }
			  
		  }
		  //Code ends here 		  
		 public void initializeLocation()
		 {
//			 popupMessage();
		      mLocationMgr = (LocationManager) mService.getSystemService(Context.LOCATION_SERVICE);
		      mCriteria = new Criteria();

		      mCriteria.setAltitudeRequired(true);
		      mCriteria.setSpeedRequired(true);
		      List<String> providers = mLocationMgr.getAllProviders();
		      //List<String> providers = mLocationMgr.getProviders(mCriteria, false);
		      mListeners = new ArrayList<MyLocationListener>(providers.size());
		      for(int i = 0; i < providers.size(); i++) {
		          MyLocationListener temp = new MyLocationListener(providers.get(i));
		          mLocationMgr.requestLocationUpdates(providers.get(i), 0,10, temp);
		          mListeners.add(temp);
		      }
		      requestGPSProvider();
			 
		 }
		  //Code ends here --sriram
	
	public boolean runFetchLocal( int inUseCurrent ) {
		boolean					result = false;
		Location				location = null;
		
		if ( 0 != inUseCurrent || null == mLocal ) {
			try {
				location = getLastLocation();
			} catch ( Exception e ) {}
		}
		
		if ( null != location ) {
			onLocationChanged( location );
			result = true;
		}
		
		if ( 0 == inUseCurrent ) {
			listenForLocation( true , this );
		}
		
		return result;
	}
	
	//	running on mThread
	public boolean handleMessage( Message message ) {
		boolean					result = true;
		
		switch ( message.what ) {
		case kConfigurationFetchStartup:
			//Code inserted by sriram for getting the gps at the start of the app -- 08-27-2010
			//System.out.println("Todays request for local ");
			startvar=1;
			initializeLocation();
			//Code ends here 	
			
			runFetchStartup( ((Parcel)message.obj).readInt() ); break;
		case kConfigurationFetchFiles: runParseStartup( ((Parcel)message.obj).readInt() ); break;
		case kConfigurationFetchLocal: 
			//Code changed by sriram on 08/18/2010
			while(true)
			{
				updateStatus("Fetching Local Stations");
				//System.out.println("Inside this condition "+latitude+" Longitude "+longitude);
				if(latitude.trim().length()>0 && longitude.trim().length()>0)
					break;
			}
			//Code ends here 
			//Code changed by sriram for handling the local stations -- 08/18/2010

			if(latitude.trim().length()<=0)
				runFetchLocal( ((Parcel)message.obj).readInt() ); 
			break;
		case kMonitorConnectivity: new Thread( new Runnable() { public void run() { tryFetchStartup(); } } , "IHRConfiguration" ).start(); break;
		case kTryAutoplayStation: tryAutoplayStation(); break;
		default: result = false; break;
		}
		
		return result;
	}
	
	public List<String> locateValueInArrayByIndex( List inArray , String inValue , int inIndex ) {
		List					result = null;
		List<String>			entry;
		int						index;
		int						count = ( null == inArray ) ? 0 : inArray.size();
		
		if ( null == inValue || 0 == inValue.length() ) {
			result = new IHRVector();
			
			for ( index = 0 ; index < count ; ++index ) {
				result.add( ((List<String>)inArray.get( index )).get( inIndex ) );
			}
		} else {
			for ( index = 0 ; index < count ; ++index ) {
				entry = (List<String>)inArray.get( index );
				
				if ( inValue.equalsIgnoreCase( entry.get( inIndex ) ) ) {
					result = entry;
					break;
				}
			}
		}
		
		return result;
	}
	
	public IHRLocal copyLocal( boolean inTryCached ) {
		IHRLocal				result = mLocal;
		
		if ( null == result && inTryCached ) try {
			byte[]				xml = IHRConfigurationFile.access( "local_stations" , "1" , null );
			IHRXMLLocalStations	local = ( null == xml ) ? null : new IHRXMLLocalStations( xml );
			
			if ( null != local ) result = new IHRLocal( local.mName , local.mDistance , local.mCallLetters );
		} catch ( Exception e ) {}
		
		if ( null == result ) {
			initializeLocation();
			result=mLocal;
		}
		
		return result;
	}
	
	public List stationByLetters( String inLetters ) {
		return ( null == inLetters ) ? null : mStationsLookup.get( inLetters );
	}
	
	public int cachePremiumItem( List<String> inList , boolean inOrPause ) {
		IHRPremiumItem			item = new IHRPremiumItem( inList );
		String					site = inList.get( IHRPremiumItem.kCapacity );
		
		item.truncate();
		
		return mChannels.archiveDownload( item , inOrPause , site );
	}
	
	public void validateSite( String inSite ) {
		IHRPremiumChannel		channel = mChannels.channel( inSite );
//		byte[]					reply = null;
		
		if ( null != channel ) try {
//			reply = IHRHTTP.request( channel.get( IHRPremiumChannel.kValidateURL ) , null , inSite );
			
			IHRHTTP.fetchAsynchronous( channel.get( IHRPremiumChannel.kValidateURL ) ,
				new IHRHTTPDelegate() {
					public void httpFetchComplete( IHRHTTP http ) {
						String	site = (String)http.getContext();
						byte[]	reply = http.getDataNoThrow();
						int		value = ( null != reply && 0 != reply.length ) ? reply[reply.length - 1] - '0' : 0;
						
						mService.mCredentials.accept( site , value != 0 );
						
						notifyClient( kNotifyNameSiteValidated , new IHRHashtable( "accepted" , new Integer( value ) , "site" , site ) );
					}
				} ,
				inSite , null , inSite );
		} catch ( Exception e ) {}
		
		//	last character (should be first printing character) as digit
//		return ( null != reply && 0 != reply.length ) ? reply[reply.length - 1] - '0' : 0;
	}
	
	public void proposeSite( String[] inCredentials ) {
		int						length = ( null == inCredentials ) ? 0 : inCredentials.length;
		
		String					site     = ( length > 0 ) ? inCredentials[0] : null;
		String					username = ( length > 1 ) ? inCredentials[1] : null;
		String					password = ( length > 2 ) ? inCredentials[2] : null;
		String					expiring = ( length > 3 ) ? inCredentials[3] : null;
		
		IHRPremiumCredentials.shared().propose( site , username , password , expiring );
	}
	
	public void removeSite( String inSite , boolean inForget ) {
		mChannels.archiveRemoveSite( inSite );
		
		if ( inForget ) IHRPremiumCredentials.shared().propose( inSite , null , null , null );
	}
	
	public void playPremiumItem( List<String> inList ) {
		String					site = ( inList.size() > 0 ) ? inList.get( inList.size() - 1 ) : null;
		IHRPremiumItem			item = ( inList.size() < IHRPremiumItem.kCapacity ) ? null : new IHRPremiumItem( inList );
		IHRPremiumChannel		channel = mChannels.channel( site );
		
		if ( null == item ) {
			mService.mPlayer.playArchive( channel , null );
		} else if ( mService.mCache.available() ) {
			item.truncate();
			
			mChannels.archiveDownload( item , false , site );
			mService.mPlayer.playArchive( channel , item );
		} else {
			mService.mPlayer.playStation( channel.getStationForItem( item ) );
		}
	}
	
	public boolean handleTransactions( int code , Parcel data , Parcel reply , int flags ) {
		boolean					result = true;
		
		if ( code > IHRService.kPerformOnThread ) {
			mService.mThreadable.handle( code , 0 , flags , data );
		} else switch ( code ) {
		
		case kIsStartupAvailable: reply.writeInt( null == mXMLStartup ? 0 : 1 ); break;
		case kAreFilesAvailable: reply.writeInt( null == mFormatsData ? 0 : 1 ); break;
		case kIsLocationAvailable: reply.writeInt( null == mLocal ? 0 : 1 ); break;
		case kIsOfflineMode: reply.writeInt( null == mXMLStartup && null != mChannels.channels() ? 1 : 0 ); break;
		case kCopyStatus: reply.writeString( mStatus ); break;
		case kCopyCommonParameters: reply.writeString( standardPostData( false ).toString() ); break;
		case kCopyUniqueParameters: reply.writeString( standardPostData( true ).toString() ); break;
		case kCopyServerPath: reply.writeString( IHRXML.sConfigFilesDirectory ); break;
		
		case kConfigurationCopyStartup: reply.writeByteArray( mStartupData ); break;
		case kConfigurationCopyCities: reply.writeByteArray( mCitiesData ); break;
		case kConfigurationCopyFormats: reply.writeByteArray( mFormatsData ); break;
		case kConfigurationCopyStations: reply.writeByteArray( mStationsData ); break;
		case kConfigurationCopyLocal: reply.writeByteArray( mLocationData ); break;
		case kConfigurationCopyImage: reply.writeByteArray( mImageData ); break;
		
		case kGetStationsCount: reply.writeInt( mStations.size() ); break;
		case kCopyStation: reply.writeStringList( mStations.get( data.readInt() ) ); break;
		case kCopyStations: reply.writeList( mStations ); break;
		case kCopyStationByLetters: reply.writeStringList( stationByLetters( data.readString() ) ); break;
		
		case kGetFormatsCount: reply.writeInt( mFormats.size() ); break;
		case kCopyFormat: reply.writeStringList( mFormats.get( data.readInt() ) ); break;
		case kCopyFormats: reply.writeList( mFormats ); break;
		case kCopyFormatByName: reply.writeStringList( locateValueInArrayByIndex( mFormats , data.readString() , IHRFormat.kName ) ); break;
		
		case kGetCitiesCount: reply.writeInt( mCities.size() ); break;
		case kCopyCity: reply.writeStringList( mCities.get( data.readInt() ) ); break;
		case kCopyCities: reply.writeList( mCities ); break;
		case kCopyCityByName: reply.writeStringList( locateValueInArrayByIndex( mCities , data.readString() , IHRCity.kName ) ); break;
		
		case kGetPremiumCount: reply.writeInt( mChannels.channels().size() ); break;
		case kCopyPremiumChannel: reply.writeStringList( (IHRPremiumChannel)mChannels.channels().get( data.readInt() ) ); break;
		case kCopyPremiumChannels: reply.writeList( mChannels.channels( null == mXMLStartup ) ); break;
		case kCopyPremiumChannelsString: reply.writeString( IHRPremiumChannel.toString( mChannels.channels( null == mXMLStartup ) ) ); break;
		case kCopyPremiumBySite: reply.writeStringList( locateValueInArrayByIndex( mChannels.channels() , data.readString() , IHRPremiumChannel.kSite ) ); break;
		case kCopyPremiumByName: reply.writeStringList( locateValueInArrayByIndex( mChannels.channels() , data.readString() , IHRPremiumChannel.kName ) ); break;
		case kCopyPremiumItemsBySite: reply.writeString( IHRPremiumItem.toString( mChannels.premiumItems( data.readString() , null == mXMLStartup ) ) ); break;
		case kFetchPremiumChannel: mChannels.refresh( data.readString() , 60 * 60 * 3 ); break;
		case kFetchPremiumChannels: mChannels.refresh( null , 60 * 60 * 3 ); break;
		
		case kSiteHasAnyAuthenticated: reply.writeInt( IHRPremiumCredentials.shared().hasAnyAuthenticated() ? 1 : 0 ); break;
		case kSiteHasAuthenticated: reply.writeInt( IHRPremiumCredentials.shared().hasAuthenticated( data.readString() ) ? 1 : 0 ); break;
		case kSiteHasArchives: reply.writeInt( mChannels.hasArchives( data.readString() , false ) ? 1 : 0 ); break;
		case kSiteValidate: validateSite( data.readString() ); break;
		case kSiteDiscard:
		case kSiteUncache: removeSite( data.readString() , kSiteDiscard == code ); break;
		case kSitePropose: proposeSite( data.createStringArray() ); break;
		
		case kCopyLocal: 
			//Code changed by sriram for handling local station -- 08/18/2010
			int ctr=0;
			while(true)
			{
				updateStatus("Fetching Local Stations");
				//System.out.println("2222");
				//Code added by sriram on 09-17-2010
				ctr++;
				System.out.println("Counter = "+ctr);
				if(ctr>250)
				{
					getLastKnownLoc();
					break;
					
				}
				//Code ends here -- sriram 09/17/2010
				
				if(latitude.trim().length()>=0 && longitude.trim().length()>0)
				{
					System.out.println("Inside copy local for lat "+latitude+" Longitude "+longitude);
					if(mLocal !=null)
					{
						System.out.println("Local is populated");
						break;
					}
				}
			}
			//Code ends here 

			//reply.writeStringList( copyLocal( true ) );
			//Code changed on 09-21-2010
			if(latitude.trim().length()<=0)
			{
				System.out.println("Inside the lat less than 0");
				result=false;
				updateAction( "nogps" );
				return result;
			}
			if(latitude!=null && longitude!=null)
			{
				if(latitude.trim().length()<=0)
				{
					
				}
				else
					reply.writeStringList( copyLocal( true ) ); 
					
			}
			
			//Code ends here 			
			break;
		case kCopyFeatured: reply.writeStringList( mFeatured ); break;
		case kCopyAutoplayStation: reply.writeString( mService.preferencesGet( "autoplay_letters" , "" ) ); break;
//		case kCopyDefaultCity: reply.writeStringList(  ); break;
		
		case kSetAutoplayStation: setAutoplayStation( data.readString() ); break;
		
		case kCopyStartupAdsPrefix: reply.writeString( mXMLStartup.mAdsDartURLPrefix ); break;
		case kCopyStartupAdsForPurpose: reply.writeString( (String)mXMLStartup.mAdsPagePositions.get( data.readString() ) ); break;
		case kCopyStartupMediaVault: reply.writeString( mXMLStartup.mMediaVaultURL ); break;
		
		case kCopyProgressForURL: reply.writeMap( mService.mCache.progressForURL( data.readString() ) ); break;
		case kDeletePremiumItem: mService.mCache.delete( data.createStringArrayList().get( IHRPremiumItem.kLink ) ); break;
		case kCachePremiumItem:
		case kCachePremiumItemOrPause: cachePremiumItem( data.createStringArrayList() , code == kCachePremiumItemOrPause ); break;
		case kPlayPremiumItem: playPremiumItem( data.createStringArrayList() ); break;
		
		default: result = false; break;
		}
		
		return result;
	}
	
	public IBinder onBind( Intent intent ) {
		if ( intent.getBooleanExtra( "configuration" , false ) ) {
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
