package com.troubadorian.streamradio.client.model;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Gravity;
import android.widget.Toast;

import com.troubadorian.streamradio.client.services.IHRConfigurationStartup;
import com.troubadorian.streamradio.client.services.IHRService;
import com.troubadorian.streamradio.client.services.IHRServiceConfiguration;
import com.troubadorian.streamradio.controller.IHRActivity;
import com.troubadorian.streamradio.controller.IHRControllerSplash;
import com.troubadorian.streamradio.controller.Streamradio;
import com.troubadorian.streamradio.model.IHRBroadcaster;
import com.troubadorian.streamradio.model.IHRCity;
import com.troubadorian.streamradio.model.IHRFormat;
import com.troubadorian.streamradio.model.IHRLocal;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumItem;
import com.troubadorian.streamradio.model.IHRStation;
import com.troubadorian.streamradio.model.IHRTaggedSong;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.*;
import android.content.*;
public class IHRConfigurationClient extends IHRConnectionClient {
	private static IHRConfigurationClient	mShared;
	
	protected static final int          kConfigurationTimeoutMs = 60 * 1000;
	protected static final int          kConfigurationTimeoutMsLong = 60 * 1000;
	
	public static final int				kAccessClear = 2;
	public static final int				kAccessWrite = 1;
	public static final int				kAccessFetch = 0;
	public static final int				kAccessForce = -1;
	
	public static final long			kKeyDebugModeEnabled = 0x1aeb15fd81f08af0L;						// com.clearchannel.iheartradio.debugModeEnabled
	public static Context ctxt;
	protected static final long         kKeyAutoplay = 0xcc5a0057a096a11dL;                             // com.clearchannel.iheartradio.autoplay
	protected static final long         kKeyAutoupdateWarnInterval = 0x7692aa7b567449b2L;               // com.clearchannel.iheartradio.autoupdateWarnInterval
	protected static final long         kKeyClientID = 0xac7ef5fcf7bc2ea3L;                             // com.clearchannel.iheartradio.clientID
	protected static final long         kKeyDefaultCity = 0x29d4d56235fc445eL;                          // com.clearchannel.iheartradio.defaultCity
	protected static final long         kKeyDontWarnAboutCarrierDataCharges = 0xeee1f9a7ca12654dL;      // com.clearchannel.iheartradio.dontWarnAboutCarrierDataCharges
	protected static final long         kKeyDontWarnAboutOptimalUsage = 0xade520840e8a163eL;            // com.clearchannel.iheartradio.dontWarnAboutOptimalUsage
	protected static final long         kKeyFavorites = 0x4e61e97dea9b09ddL;                            // com.clearchannel.iheartradio.favorites
	protected static final long         kKeyLocalStationsXML = 0xd20bf0494d61727L;                      // com.clearchannel.iheartradio.localStationsXML
	protected static final long         kKeyTaggedSongs = 0x5334d89a231d5864L;                          // com.clearchannel.iheartradio.taggedSongs
	
	public boolean                      mDontWarnAboutCarrierDataCharges;
	public boolean                      mDontWarnAboutOptimalUsage;
//	protected IHRVector					mFavorites;
//	protected IHRVector					mTaggedSongs;       // pairs of { Artist, Track }
	
	protected IHRVector					mFavorited;
	protected IHRVector					mTagged;
	
	private IHRConfigurationStartup		mCacheStartup;
	private String						mCacheAutoplay;
	private List						mCacheCityNames;
	private List						mCacheFormatNames;
	private List						mCachePremiumChannels;
	private IHRStation[]				mCacheStationsByIndex;
	private Map							mCacheStationsByLetters;
	
	private String						mParametersCommon;
	private String						mParametersUnique;
	
	private IHRControllerSplash			mDelegate;
	
	private Runnable					mStop;
	private int							mPendingRequest;
	private String						mStatus;
	
	public static IHRConfigurationClient shared() {
		if ( null == mShared ) mShared = new IHRConfigurationClient();
		
		return mShared;
	}
	
	protected IHRConfigurationClient() {
		mDontWarnAboutCarrierDataCharges = IHRPreferences.getBoolean( kKeyDontWarnAboutCarrierDataCharges );
		mDontWarnAboutOptimalUsage = true;  //IHRPreferences.getBoolean( kKeyDontWarnAboutOptimalUsage, false );    // disabled per rim
		mPendingRequest = -1;
		
//		String[]				strings;
		
//		strings = IHRPreferences.getStrings( kKeyFavorites );
//		mFavorites = ( null == strings ) ? new IHRVector() : new IHRVector( strings );
		
//		strings = IHRPreferences.getStrings( kKeyTaggedSongs );
//		mTaggedSongs = ( null == strings ) ? new IHRVector() : new IHRVector( strings );
	}
	
	public boolean needsLoad() {
		return 0 == serviceFetchInteger( IHRServiceConfiguration.kAreFilesAvailable , Parcel.obtain() ) ||
			0 == serviceFetchInteger( IHRServiceConfiguration.kIsStartupAvailable , Parcel.obtain() );
	}
	
	
	public boolean isOffline() {
		return 0 != serviceFetchInteger( IHRServiceConfiguration.kIsOfflineMode , Parcel.obtain() );
	}
	
	
	/*
	 * url parameter list to append to other urls
	 * 
	 * inUnique true will include identifying information in parameters
	 * */
	
	public String parameters( boolean inUnique ) {
		String					result = "";
		
		try {
			if ( inUnique ) {
				if ( null == mParametersUnique ) mParametersUnique = serviceFetchString( IHRServiceConfiguration.kCopyUniqueParameters , Parcel.obtain() );
				
				result = mParametersUnique;
			} else {
				if ( null == mParametersCommon ) mParametersCommon = serviceFetchString( IHRServiceConfiguration.kCopyCommonParameters , Parcel.obtain() );
				
				result = mParametersCommon;
			}
		} catch ( Exception e ) {}
		
		return result;
	}
	
	
	
	/*
	 * support splash screen displaying status during initial loading and parsing of configuration files
	 * 
	 * splashShown starts request as needed
	 * configuration available called when startup xml fetched from server
	 * configuration complete called when stations and categories parsed
	 * configuration timeout called if the above takes too long
	 * 
	 * */
	
	protected void terminate() {
		Streamradio.g.finishActivity( 1 );
		Streamradio.g.finish();
		
//		Process.killProcess( Process.myPid() );
//		System.exit( 1 );
	}
	
	protected void networkFailed() {
		mStop = null;

		try {
			serviceTell( IHRService.kQuit , 0 );
		} catch ( Exception e ) {}
		
		if ( null == mDelegate ) {
			terminate();
		} else {
			mDelegate.informNoNetwork();
		}
	}
	
	protected void configurationTimeout() {
		mStop = null;
		
		if ( null == mDelegate ) {
			serviceTell( IHRService.kQuit , 0 );
		} else {
			mDelegate.askContinue();
		}
	}
	
	public void configurationComplete( boolean inOffline ) {
		if ( null != mDelegate ) {
			mDelegate.updateStatus( "Ready" );
			mDelegate.configurationAcquired( inOffline );
		}
	}
	
	public void configurationAvailable() {
		int						update = 0;
		
		if ( null != mStop ) {
			IHRThreadable.gMain.remove( mStop );
			mStop = null;
		}
		
		copyStartup();
		
		if ( null != mCacheStartup && null != mCacheStartup.mAndroidUpdateURL && mCacheStartup.mAndroidAutoupdateEnabled && 0 != mCacheStartup.mAndroidUpdateURL.length() && 0 < compareVersions( mCacheStartup.mAndroidVersion , IHRServiceConfiguration.mApplicationVersion ) ) {
			if ( mCacheStartup.mAndroidAutoupdateForced ) {
				update = 2;
			} else {
				int				interval = IHRPreferences.get( kKeyAutoupdateWarnInterval , 0 );
				
				if ( 0 == interval || interval >= mCacheStartup.mAndroidAutoupdateWarnInterval ) {
					interval = 1;
					update = 1;
				} else {
					interval += 1;
				}
				
				IHRPreferences.setInteger( kKeyAutoupdateWarnInterval , interval );
			}
		}
		
		if ( null == mDelegate || 0 == update ) {
			configurationUpdate( 0 );
		} else {
			mDelegate.askUpdate( mCacheStartup.mAndroidAutoupdateForced , mCacheStartup.mAndroidAutoupdateMessage );
		}
		//mDelegate.askLocationDialog();//Code changed by sriram for handling the Location popup box on 08-20-2010

	}
	
	public void configurationUpdate( int inUpdate ) {
		if ( 0 == inUpdate ) {
			if ( 0 != serviceFetchInteger( IHRServiceConfiguration.kAreFilesAvailable , Parcel.obtain() ) ) {
				configurationComplete( false );
			} else try {
				serviceTell( IHRServiceConfiguration.kConfigurationFetchFiles , 12/* attempts */ );
			} catch ( Exception e ) {}
		} else if ( 1 == inUpdate ) {
			terminate();	//	forced update ignored
		} else {
			((IHRActivity)mDelegate.activity()).openWebURL( mCacheStartup.mAndroidUpdateURL );
		}
	}
	
	public void configurationContinue( int inFinish ) {
		if ( 0 == inFinish ) {
			long				time = mDontWarnAboutCarrierDataCharges ? kConfigurationTimeoutMs : kConfigurationTimeoutMsLong;
			
			mStop = new Runnable() { public void run() { configurationTimeout(); } };
			IHRThreadable.gMain.handle( mStop , time );
		} else {
			try {
				serviceTell( IHRService.kQuit , 0 );
			} catch ( Exception e ) {}
			
			if ( null == mDelegate ) {
				terminate();
			} else {
				mDelegate.informFinished();
			}
		}
	}
	
	public void configurationOptimized( int inAlways ) {
		if ( inAlways > 0 ) IHRPreferences.setBoolean( kKeyDontWarnAboutOptimalUsage, mDontWarnAboutOptimalUsage = true );
	}
	
	public void configurationBegin( int inSource /* 1 = staging, 2 = development */ ) {
		configurationContinue( 0 );		//	start timer
		
		if ( null != mDelegate ) {
			mDelegate.configurationQuerying();
		}
		
		if ( isConnected() ) {
			serviceTell( IHRServiceConfiguration.kConfigurationFetchStartup , inSource );
		} else {
			mPendingRequest = inSource;
		}
		
		if ( null == mDelegate || mDontWarnAboutOptimalUsage ) {
			configurationOptimized( 0 );
		} else {
			mDelegate.askOptimization();
		}
	}
	
	public void splashShown() {
		if ( 0 != serviceFetchInteger( IHRServiceConfiguration.kIsStartupAvailable , Parcel.obtain() ) ) {
			configurationAvailable();
		} else if ( null == mDelegate || !( IHRPlatform.isSimulator() || IHRPreferences.getBoolean( kKeyDebugModeEnabled ) ) ) {
			configurationBegin( 0 );
		} else {
			//	TODO: ask configuration
			configurationBegin( 0 );
			
//			mDelegate.askConfiguration();
		}
	}
	
	/*
	 * support miscellaneous saved settings
	 * 
	 * favorites list
	 * tagged songs list
	 * autoplay station
	 * default city
	 * 
	 * */
	
	public IHRVector accessFavorites( int inAction ) {
		String					key = "favorites";
		
		if ( inAction > kAccessFetch ) {
			if ( null != mFavorited ) {
				if ( inAction > kAccessWrite ) mFavorited.clear();
				
				serviceWritePreference( key , mFavorited );
				IHRPreferences.write( key , (List)mFavorited );
			}
		} else {
			if ( null == mFavorited || inAction < kAccessFetch ) mFavorited = (IHRVector)IHRPreferences.copyStringsInto( key , new IHRVector() );
			if ( null == mFavorited ) mFavorited = serviceFetchPreference( key );
			if ( null == mFavorited ) mFavorited = new IHRVector();
		}
		
		return mFavorited;
	}
	
	public int locateFavorited( String inFavorited ) {
		return ( null == inFavorited ) ? -1 : accessFavorites( kAccessFetch ).indexOf( inFavorited );
	}
	
	public IHRVector accessTagged( int inAction ) {
		String					key = "tagged";
		
		if ( inAction > kAccessFetch ) {
			if ( null != mTagged ) {
				if ( inAction > kAccessWrite ) mTagged.clear();
				
				serviceWritePreference( key , mTagged );
				IHRPreferences.write( key , (List)mTagged );
			}
		} else {
			if ( null == mTagged || inAction < kAccessFetch ) mTagged = (IHRVector)IHRPreferences.copyStringsInto( key , new IHRVector() );
			if ( null == mTagged ) mTagged = serviceFetchPreference( key );
			if ( null == mTagged ) mTagged = new IHRVector();
		}
		
		return mTagged;
	}
	
	public int locateTagged( String inArtist , String inTrack ) {
		int						result = -1;
		
		if ( null != inArtist && null != inTrack ) {
			IHRVector			tagged = accessTagged( kAccessFetch );
			int					index , count = tagged.size();
			
			for ( index = 0 ; index < count ; index += 2 ) {
				if ( inArtist.equals( tagged.get( index ) ) && inTrack.equals( tagged.get( index + 1 ) ) ) {
					result = index / 2;
					break;
				}
			}
		}
		
		return result;
	}
	
	public void addFavorite( String callLetters ) {
		accessFavorites( kAccessFetch ).add( callLetters );
		accessFavorites( kAccessWrite );
		
//		mFavorites.addElement( callLetters );
//		IHRPreferences.write( kKeyFavorites, mFavorites.toArray( new String[mFavorites.size()] ) );
	}
	
	public boolean addTaggedSong( String artist, String track ) {
		boolean					result = false;
		
		if ( null == artist || null == track || 0 == track.length() ) return false;
		
		if ( locateTagged( artist , track ) < 0 ) {
			mTagged.add( artist );
			mTagged.add( track );
			
			accessTagged( kAccessWrite );
			
			result = true;
		}
		
		/*
		if ( isSongTagged( artist, track ) ) return false;
		
		mTaggedSongs.addElement( artist );
		mTaggedSongs.addElement( track );
		
		IHRPreferences.setString( kKeyTaggedSongs, mTaggedSongs.join( IHRPreferences.kSeparator ) );
		*/
		
		return result;
	}
	
	public String getAutoplayStation() {
		if ( null == mCacheAutoplay ) {
			mCacheAutoplay = serviceFetchString( IHRServiceConfiguration.kCopyAutoplayStation , Parcel.obtain() , ""/* default */ );
		}
		
		return mCacheAutoplay;
	}
	
	public IHRCity getDefaultCity() {
		//**
		return (IHRCity)serviceFetchStringsInto( IHRServiceConfiguration.kCopyDefaultCity , Parcel.obtain() , new IHRCity() );
		/*/
		IHRCity                         city;
		int                             i, n;
		String                          name;

		if ( ( name = IHRPreferences.getString( kKeyDefaultCity ) ) != null ) {
			for ( i = 0, n = mCities.size(); i < n; ++i ) {
				if ( ( city = (IHRCity) mCities.get( i ) ).getName().equalsIgnoreCase( name ) ) {
					return city;
				}
			}
		}

		return null;
		/**/
	}

	public IHRTaggedSong getTaggedSongAtIndex( int index ) {
		IHRTaggedSong			result = new IHRTaggedSong();
		IHRVector				tagged = accessTagged( kAccessFetch );
		
		result.mArtist = (String) tagged.elementAt( index * 2 );
		result.mTrack = (String) tagged.elementAt( index * 2 + 1 );
		
		return result;
	}

	public byte[] getLocalStationsXML() {
		return IHRPreferences.getBytes( kKeyLocalStationsXML );
	}

	public int getTaggedSongsSize() { return accessTagged( kAccessFetch ).size() / 2; }
	
	public boolean isFavorite( String callLetters ) {
		return !( locateFavorited( callLetters ) < 0 );
		/*
		int                             i, n;

		if ( callLetters == null ) return false;
		
		for ( i = 0, n = mFavorites.size(); i < n; ++i ) {
			if ( ((String) mFavorites.elementAt( i )).equalsIgnoreCase( callLetters ) ) {
				return true;
			}
		}

		return false;
		*/
	}

	public boolean isSongTagged( String artist, String track ) {
		return !( locateTagged( artist , track ) < 0 );
		/*
		int                             i, n;
		
		for ( i = 0, n = mTaggedSongs.size(); i < n; i += 2 ) {
			if ( ((String) mTaggedSongs.elementAt( i )).equals( artist ) ) {
				if ( ((String) mTaggedSongs.elementAt( i + 1 )).equals( track ) ) {
					return true;
				}
			}
		}

		return false;
		*/
	}

	public void removeAutoplayStation() {
		mCacheAutoplay = null;
		
		IHRConfigurationClient.shared().setAutoplayStation( "" );
	}

	public void removeFavorite( String callLetters ) {
		String					autoplay;
		
		if ( ( autoplay = getAutoplayStation() ) != null && autoplay.equals( callLetters ) ) {
			removeAutoplayStation();
		}
		
		accessFavorites( kAccessFetch ).remove( callLetters );
		accessFavorites( kAccessWrite );
		/*
		mFavorites.removeElement( callLetters );
		IHRPreferences.write( kKeyFavorites, mFavorites.toArray( new String[mFavorites.size()] ) );
		*/
	}

	public void removeLocalStationsXML() {
		IHRPreferences.remove( kKeyLocalStationsXML );
	}
	
	public void removeTaggedSong( int index ) {
		IHRVector				tagged = accessTagged( kAccessFetch );
		
		tagged.remove( index * 2 );
		tagged.remove( index * 2 );
		
		accessTagged( kAccessWrite );
		/*
		mTaggedSongs.remove( index * 2 );
		mTaggedSongs.remove( index * 2 );
		*/
	}

	public void removeTaggedSong( String artist, String track ) {
		int						index = locateTagged( artist , track );
		
		if ( !( index < 0 ) ) removeTaggedSong( index );
		/*
		int                             i, n, o;

		for ( i = 0, n = mTaggedSongs.size() / 2; i < n; ++i ) {
			if ( ! ((String) mTaggedSongs.elementAt( o = i * 2 )).equals( artist ) ) continue;
			if ( ! ((String) mTaggedSongs.elementAt( ++o )).equals( track ) ) continue;
			
			mTaggedSongs.removeElementAt( o-- );
			mTaggedSongs.removeElementAt( o );
			
			IHRPreferences.setString( kKeyTaggedSongs, mTaggedSongs.join( IHRPreferences.kSeparator ) );
			
			break;
		}
		*/
	}
	
	public void setAutoplayStation( String callLetters ) {
		mCacheAutoplay = callLetters;
		
		this.serviceTell( IHRServiceConfiguration.kSetAutoplayStation , callLetters );
	}

	public void setDefaultCity( String defaultCity ) {
		IHRPreferences.setString( kKeyDefaultCity, defaultCity );
		removeLocalStationsXML();
	}
	
	public void setDelegate( IHRControllerSplash delegate ) {
		mDelegate = delegate;
	}
	
	public void setDontWarnAboutCarrierDataCharges( boolean makePermanent ) {
		mDontWarnAboutCarrierDataCharges = true;
		if ( makePermanent ) IHRPreferences.setBoolean( kKeyDontWarnAboutCarrierDataCharges, true );
	}

	public void setLocalStationsXML( byte[] xml ) {
		IHRPreferences.setBytes( kKeyLocalStationsXML, xml );
	}
	
	/*
	 * Access station information in service
	 * 
	 * */
	
	public int stationCount() {
		return this.serviceFetchInteger( IHRServiceConfiguration.kGetStationsCount , Parcel.obtain() );
	}
	
	public List featured() {
		return serviceFetchStrings( IHRServiceConfiguration.kCopyFeatured , Parcel.obtain() );
	}
	
	public IHRStation stationForIndex( int index ) {
		IHRStation				result = null;
		
		if ( null == mCacheStationsByIndex ) {
			mCacheStationsByIndex = new IHRStation[stationCount()];
		} else if ( index < mCacheStationsByIndex.length ) {
			result = mCacheStationsByIndex[index];
		}
		
		if ( null == result ) {
			result = (IHRStation)serviceFetchStringsInto( IHRServiceConfiguration.kCopyStation , parcel( index ) , new IHRStation() );
			
			if ( null != result ) {
				String			key = result.getCallLetters();
				IHRStation		found = null;
				
				mCacheStationsByIndex[index] = result;
				
				if ( null == mCacheStationsByLetters ) {
					mCacheStationsByLetters = new IHRHashtable();
				} else {
					found = (IHRStation)mCacheStationsByLetters.get( key );
				}
				
				if ( null == found ) {
					mCacheStationsByLetters.put( key , result );
				}
			}
		}
		
		return result;
	}
	
	public IHRStation stationForCallLetters( String callLetters ) {
		IHRStation				result = null;
		
		if ( null == mCacheStationsByLetters ) {
			mCacheStationsByLetters = new IHRHashtable();
		} else {
			result = (IHRStation)mCacheStationsByLetters.get( callLetters );
		}
		
		if ( null == result ) {
			result = (IHRStation)serviceFetchStringsInto( IHRServiceConfiguration.kCopyStationByLetters , parcel( callLetters ) , new IHRStation() );
			
			if ( null != result ) mCacheStationsByLetters.put( callLetters , result );
		}
		
		return result;
	}
	
	public String streamForCallLetters( String inLetters , boolean inAuthenticated ) {
		String					result = null;
		IHRStation				station = stationForCallLetters( inLetters );
		
		if ( null != station ) {
			result = station.getBaseStreamURL( inAuthenticated );
		}
		
		if ( null != result ) {
			result += '?' + parameters( true );
		}
		
		return result;
	}
	
	/*
	 * Access city information from service
	 * 
	 * */
	
	public int citiesCount() {
		return this.serviceFetchInteger( IHRServiceConfiguration.kGetCitiesCount , Parcel.obtain() );
	}
	
	public List fetchCityNames() {
		if ( null == mCacheCityNames ) {
			mCacheCityNames = serviceFetchStrings( IHRServiceConfiguration.kCopyCityByName , parcel( "" ) );
		}
		
		return mCacheCityNames;
	}
	
	public IHRCity fetchCity( int inIndex ) {
		return (IHRCity)serviceFetchStringsInto( IHRServiceConfiguration.kCopyCity , parcel( inIndex ) , new IHRCity() );
	}
	
	public IHRCity fetchCity( String inName ) {
		return (IHRCity)serviceFetchStringsInto( IHRServiceConfiguration.kCopyCityByName , parcel( inName ) , new IHRCity() );
	}
	
	/*
	 * Access premium information from service
	 * 
	 * */
	
	public int channelsCount() {
		return this.serviceFetchInteger( IHRServiceConfiguration.kGetPremiumCount , Parcel.obtain() );
	}
	
	public List fetchPremiumChannels() {
		if ( null == mCachePremiumChannels ) {
			String				flat = null;
			
			try {
				flat = serviceFetchString( IHRServiceConfiguration.kCopyPremiumChannelsString , parcel( "" ) );
			} catch ( RemoteException e ) {}
			
			if ( null != flat ) mCachePremiumChannels = IHRPremiumChannel.fromString( flat );
//			mCachePremiumChannels = serviceFetchList( IHRServiceConfiguration.kCopyPremiumChannels , parcel( "" ) );
		}
		
		return mCachePremiumChannels;
	}
	
	public List fetchPremiumItems( String inSite ) {
		List					result = null;
		
		try { result = IHRPremiumItem.fromString( serviceFetchString( IHRServiceConfiguration.kCopyPremiumItemsBySite , parcel( inSite ) ) ); } catch ( RemoteException e ) {}
		
		return result;
	}
	
	public IHRPremiumChannel fetchChannel( int inIndex ) {
		IHRPremiumChannel		result = null;
		
		if ( null != mCachePremiumChannels && inIndex < mCachePremiumChannels.size() ) {
			result = (IHRPremiumChannel)mCachePremiumChannels.get( inIndex );
		}
		
		if ( null == result ) {
			result = (IHRPremiumChannel)serviceFetchStringsInto( IHRServiceConfiguration.kCopyPremiumChannel , parcel( inIndex ) , new IHRPremiumChannel() );
		}
		
		return result;
	}
	
	public IHRPremiumChannel fetchChannel( String inSite ) {
		IHRPremiumChannel		result = null;
		
		if ( null != mCachePremiumChannels && null != inSite ) {
			int					index , count = mCachePremiumChannels.size();
			
			for ( index = 0 ; index < count ; ++index ) {
				if ( ((IHRPremiumChannel)mCachePremiumChannels.get( index )).getSite().equalsIgnoreCase( inSite ) ) {
					result = (IHRPremiumChannel)mCachePremiumChannels.get( index );
				}
			}
		}
		
		if ( null == result ) {
			result = (IHRPremiumChannel)serviceFetchStringsInto( IHRServiceConfiguration.kCopyPremiumBySite , parcel( inSite ) , new IHRPremiumChannel() );
		}
		
		return result;
	}
	
	public boolean siteHasAnyAuthenticated() {
		return 0 != serviceFetchInteger( IHRServiceConfiguration.kSiteHasAnyAuthenticated , Parcel.obtain() );
	}
	
	public boolean siteHasAuthenticated( String inSite ) {
		return 0 != serviceFetchInteger( IHRServiceConfiguration.kSiteHasAuthenticated , parcel( inSite ) );
	}
	
	public boolean siteHasArchives( String inSite ) {
		return 0 != serviceFetchInteger( IHRServiceConfiguration.kSiteHasArchives , parcel( inSite ) );
	}
	
	public boolean siteValidate( String inSite ) {
		return 0 != serviceFetchInteger( IHRServiceConfiguration.kSiteValidate , parcel( inSite ) );
	}
	
	public void siteDiscard( String inSite , boolean inForget ) {
		serviceTell( inForget ? IHRServiceConfiguration.kSiteDiscard : IHRServiceConfiguration.kSiteUncache , inSite );
	}
	
	public void siteAuthenticate( String inSite , String inUsername , String inPassword ) {
		String[]				strings = { inSite , inUsername , inPassword };
		
		serviceTell( IHRServiceConfiguration.kSitePropose , strings );
	}
	
	public void refreshPremium( String inSite ) {
		if ( null == inSite ) serviceTell( IHRServiceConfiguration.kFetchPremiumChannels );
		else serviceTell( IHRServiceConfiguration.kFetchPremiumChannel , inSite );
	}
	
	public void playPremiumItem( ArrayList inItem , String inSite ) {
		List<String>			list = new ArrayList();
		
		if ( null != inItem ) list.addAll( inItem );
		list.add( inSite );
		
		serviceTell( IHRServiceConfiguration.kPlayPremiumItem , list );
	}
	
	public void cachePremiumItem( ArrayList inItem , boolean inOrPause , String inSite ) {
		List<String>			list = new ArrayList();
		
		list.addAll( inItem );
		list.add( inSite );
		
		serviceTell( inOrPause ? IHRServiceConfiguration.kCachePremiumItemOrPause : IHRServiceConfiguration.kCachePremiumItem , list );
	}
	
	public void deletePremiumItem( ArrayList inItem ) {
		serviceTell( IHRServiceConfiguration.kDeletePremiumItem , inItem );
	}
	
	public IHRHashtable progressForURL( String inURL ) {
		return serviceFetchHashtable( IHRServiceConfiguration.kCopyProgressForURL , parcel( inURL ) );
	}
	
	/*
	 * Access format information from service
	 * 
	 * */
	
	public int formatsCount() {
		return this.serviceFetchInteger( IHRServiceConfiguration.kGetFormatsCount , Parcel.obtain() );
	}
	
	public List fetchFormatNames() {
		if ( null == mCacheFormatNames ) {
			mCacheFormatNames = serviceFetchStrings( IHRServiceConfiguration.kCopyFormatByName , parcel( "" ) );
		}
		
		return mCacheFormatNames;
	}
	
	public IHRFormat fetchFormat( int inIndex ) {
		return (IHRFormat)serviceFetchStringsInto( IHRServiceConfiguration.kCopyFormat , parcel( inIndex ) , new IHRFormat() );
	}
	
	public IHRFormat fetchFormat( String inName ) {
		return (IHRFormat)serviceFetchStringsInto( IHRServiceConfiguration.kCopyFormatByName , parcel( inName ) , new IHRFormat() );
	}
	
	public String adsDartPrefix() {
		return serviceFetchString( IHRServiceConfiguration.kCopyStartupAdsPrefix , Parcel.obtain() , null );
	}
	
	public String adsPositionForPurpose( String inPurpose ) {
		return serviceFetchString( IHRServiceConfiguration.kCopyStartupAdsForPurpose , parcel( inPurpose ) , null );
	}
	
	public IHRConfigurationStartup copyStartup() {
		if ( null == mCacheStartup ) {
			byte[]				xml = serviceFetchBytes( IHRServiceConfiguration.kConfigurationCopyStartup , Parcel.obtain() );
			
			if ( null != xml && 0 != xml.length ) try {
				mCacheStartup = new IHRConfigurationStartup( xml );
			} catch ( Exception e ) {}
		}
		
		return mCacheStartup;
	}
	
	
	/*
	 * Access location information from service
	 * 
	 * */
	
	public boolean hasLocal() {
		return 0 != serviceFetchInteger( IHRServiceConfiguration.kIsLocationAvailable , Parcel.obtain() );
	}
	
	public IHRLocal fetchLocal() {
		IHRLocal				result = (IHRLocal)serviceFetchStringsInto( IHRServiceConfiguration.kCopyLocal , Parcel.obtain() , new IHRLocal() );
		
		if ( null == result ) {
			serviceTell( IHRServiceConfiguration.kConfigurationFetchLocal );
		}
		
		return result;
	}
	
	
	// compares two versions of the form [0-9]+\.[0-9]+\.[0-9]
	// returns 0 if equal, less than zero if lhs < rhs, greater than zero if lhs > rhs
	protected int compareVersions( String lhs, String rhs ) {
		char					c;
		int						i, l, lhsLength, lhsVersion, r, rhsLength, rhsVersion, start;

		if ( lhs == null && rhs == null ) return 0;
		if ( lhs == null || ( lhsLength = lhs.length() ) == 0 ) return -1;
		if ( rhs == null || ( rhsLength = rhs.length() ) == 0 ) return 1;
		
		for ( i = l = r = 0; i < 3; ++i ) {
			for ( lhsVersion = 0, start = l; l < lhsLength; ++l ) {
				if ( ( c = lhs.charAt( l ) ) < '0' || c > '9' ) break;
			}
			
			if ( start < l ) lhsVersion = Integer.valueOf( lhs.substring( start, l++ ) ).intValue();
			
			for ( rhsVersion = 0, start = r; r < rhsLength; ++r ) {
				if ( ( c = rhs.charAt( r ) ) < '0' || c > '9' ) break;
			}
			
			if ( start < r ) rhsVersion = Integer.valueOf( rhs.substring( start, r++ ) ).intValue();

			if ( lhsVersion < rhsVersion ) return -1;
			if ( lhsVersion > rhsVersion ) return 1;
		}

		return 0;
	}
	
	public void clearServiceCache() {
		mCacheStartup = null;
		mCacheAutoplay = null;
		mCacheCityNames = null;
		mCacheFormatNames = null;
		mCachePremiumChannels = null;
		mCacheStationsByIndex = null;
		mCacheStationsByLetters = null;
	}
	
	@Override
	public void onDestroy() {
		if ( null != mStop ) {
			IHRThreadable.gMain.remove( mStop );
			mStop = null;
		}
		
		super.onDestroy();
	}
	
	/*
	 * Internal interactions with service
	 * 
	 * */
	
	@Override
	public void onServiceConnected( ComponentName inName , IBinder inBinder ) {
		super.onServiceConnected( inName , inBinder );
		
		//	fetch remote preferences again
		accessFavorites( kAccessForce );
		accessTagged( kAccessForce );
		
		try {
			if ( mPendingRequest >= 0 ) {
				serviceTell( IHRServiceConfiguration.kConfigurationFetchStartup , mPendingRequest );
				
				mPendingRequest = -1;
			}
		} catch ( Exception e ) {}
		
		Streamradio.g.registerReceiver( this , new IntentFilter( "com.clearchannel.iheartradio.configuration" ) );
	}
	
	@Override
	public void onReceive( Context context , Intent intent ) {
		String					action = intent.getStringExtra( "action" );
		
		if ( action.equals( "notification" ) ) {
			String				name = intent.getStringExtra( "name" );
			IHRHashtable		details = new IHRHashtable();
			
			if ( intent.hasExtra( "details" ) ) {
				details.assign( intent.getBundleExtra( "details" ) );
			}
			
			if ( name.equals( "endingOfflineMode" ) ) {
				clearServiceCache();
			}
			
			IHRBroadcaster.common().notifyOnMainThread( name , details );
		} else if ( action.equals( "status" ) ) {
			if ( null != mDelegate ) mDelegate.updateStatus( intent.getStringExtra( action ) );
		} else if ( action.equals( "startup" ) ) {
			configurationAvailable();
		} else if ( action.equals( "noconnection" ) ) {
			networkFailed();
		} else if ( action.equals( "offline" ) ) {
			configurationComplete( true );
		} else if ( action.equals( "complete" ) ) {
			configurationComplete( false );
		} else if ( action.equals( "location" ) ) {
			//	notify local stations list that location is available
		}
		//Code changed on 09-21-2010
		else if(action.equals("nogps"))
		{
			System.out.println("Inside this no gps action");
			ctxt=context;
			GPSFailed();
		}		
	}
	//Code changed on 09-21-2010
	protected void GPSFailed() {
		AlertDialog altdlg;
		mStop = null;
		try{
		System.out.println("MDelegate "+mDelegate);
		}catch(Exception err)
		{
			System.out.println("M Delegate is null");
		}
		try {
			serviceTell( IHRService.kQuit , 0 );
		} catch ( Exception e ) {}
	//	if(IHRController !=null)
			
		/*if ( null == mDelegate ) {
			terminate();
		} else
		*/
		if(mDelegate==null)
		{
/*			Toast gpsMessageToast=Toast.makeText(ctxt, "No gps connection is currently available. Please try again when a gps connection is available.", Toast.LENGTH_LONG);
			gpsMessageToast.setGravity(Gravity.TOP|Gravity.LEFT, 1, 70);
			gpsMessageToast.show();
			*/
			System.out.println("Alert Dialog");
			altdlg=new AlertDialog.Builder(ctxt).create();
			altdlg.setTitle("Location Services Alert");
			altdlg.setMessage("Your device's location services cannot find your location. Please ensure that location services are enabled and a connection is available and try again.");
			altdlg.setButton("OK", new DialogInterface.OnClickListener() {

			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.cancel();
			    	  return;

			    } }); 
			altdlg.show();
		}
		if ( null != mDelegate )
		 {
			mDelegate.informNoGPS();
		}
	}

	//Code ends here 
	public void do_updateStatusOnMain( String inStatus ) {
		Runnable				runner;
		
		if ( null == inStatus || 0 == inStatus.length() ) {
			runner = new Runnable() { public void run() {
				if ( null != mDelegate ) mDelegate.configurationAcquired( false );
			} };
		} else {
			mStatus = inStatus;
			
			runner = new Runnable() { public void run() {
				if ( null != mDelegate ) mDelegate.updateStatus( mStatus );
			} };
		}
		
		IHRThreadable.gMain.handle( runner );
	}
	
}
