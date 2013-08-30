package com.troubadorian.streamradio.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRPlayerClient;
import com.troubadorian.streamradio.client.model.IHRPreferences;
import com.troubadorian.streamradio.client.model.IHRThreadable;
import com.troubadorian.streamradio.client.services.IHRConfigurationStartup;
import com.troubadorian.streamradio.client.services.IHRServicePlayer;
import com.troubadorian.streamradio.client.view.IHRViewPlayer;
import com.troubadorian.streamradio.client.view.IHRViewSlider;
import com.troubadorian.streamradio.model.IHRBroadcaster;
import com.troubadorian.streamradio.model.IHRCache;
import com.troubadorian.streamradio.model.IHRListener;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumItem;
import com.troubadorian.streamradio.model.IHRStation;
import com.troubadorian.streamradio.model.IHRTimerTask;
import com.troubadorian.streamradio.model.IHRUtilities;
import com.troubadorian.streamradio.model.IHRXMLTrackInfo;


public class IHRControllerPlayer extends IHRController implements Handler.Callback {
	protected String					mCallLetters;
	protected Handler					mHandler;
	protected IHRHashtable				mMetadata;
	protected boolean					mPlayStationCalled;
	protected BroadcastReceiver			mReceiver;
	protected IHRViewPlayer				mViewPlayer;
	protected IHRViewTransport			mViewTransport;
	//protected IHRViewFavorite			mViewFavorite;
	
	protected String					mAdClickURL;

	protected long						mVolumeAssigned;
	protected boolean					mPlaying;
	protected boolean					mSeekable;
	protected boolean					mIgnoreMetadata;
	protected String					mURL;

	protected static final long			kKeyListenSecondsSinceLastGatewayAdShown = 0xaea777be16cac925L;		// com.troubadorian.streamradio.listenSecondsSinceLastGatewayAdShown
	protected static final long			kKeyLoadsSinceLastGatewayAdShown = 0xe40a6c34d491fc87L;				// com.troubadorian.streamradio.loadsSinceLastGatewayAdShown

	protected static final int			kMessageCloseLargeAd = IHRUtilities.osType( "ClLA" );
	protected static int stopflag=0;//Code inserted by das on 08-19-2010

	public IHRControllerPlayer() {
		super();
		
//		mHandler = new Handler( this );
//		
//		IHRAd.shared().mHandlerPlayer = mHandler;
	}
	
	protected void kickoffAds( boolean incrementLoadCounter ) {
		IHRHashtable					hash;
		int								listenSecondsSinceLastGatewayAdShown, loadsSinceLastGatewayAdShown;
		boolean							showGatewayAd = false;
		IHRConfigurationStartup			startup;
		IHRStation						station;
		
		if ( mCallLetters == null ) return;
		
		station = IHRConfigurationClient.shared().stationForCallLetters( mCallLetters );

		if ( station == null || station.getAdsDisabled() ) return;
		
		startup = IHRConfigurationClient.shared().copyStartup();
		
		if ( startup.mAdsGatewayDisplayTime <= 0 ) return;

		if ( startup.mAdsGatewayListenSeconds > 0 ) {
			listenSecondsSinceLastGatewayAdShown = IHRPreferences.getInteger( kKeyListenSecondsSinceLastGatewayAdShown );

			if ( listenSecondsSinceLastGatewayAdShown == 0 || listenSecondsSinceLastGatewayAdShown >= startup.mAdsGatewayListenSeconds ) {
				showGatewayAd = true;
			}
		}

		if ( ! showGatewayAd && startup.mAdsGatewayInterval > 0 ) {
			loadsSinceLastGatewayAdShown = IHRPreferences.getInteger( kKeyLoadsSinceLastGatewayAdShown );

			if ( loadsSinceLastGatewayAdShown == 0 || loadsSinceLastGatewayAdShown >= startup.mAdsGatewayInterval ) {
				showGatewayAd = true;
			} else if ( incrementLoadCounter ) {
				IHRPreferences.setInteger( kKeyLoadsSinceLastGatewayAdShown, loadsSinceLastGatewayAdShown + 1 );
			}
		}
		
//		if ( ( hash = IHRAd.shared().request( IHRAd.kAdTypePlayer, mCallLetters ) ) == null ) return;

//		displayAd( hash, showGatewayAd );
	}
	
	public void adClicked() {
		if ( mAdClickURL != null ) {
//			IHRAd.shared().report( IHRAd.kAdPurposeClickPlayerLarge );
			((IHRActivity) activity()).openWebURL( mAdClickURL );
		}
	}
	
	protected void displayAd( IHRHashtable hash, boolean showGatewayAd ) {
		Bitmap						largeBitmap, smallBitmap;
		IHRConfigurationStartup		startup;
		IHRTimerTask				task;

		mAdClickURL = (String) hash.get( "ad_url" );
		largeBitmap = (Bitmap) hash.get( "large_image" );
		smallBitmap = (Bitmap) hash.get( "small_image" );
		
		if ( smallBitmap != null ) {
			mViewPlayer.setSmallAd( smallBitmap );
//			mViewPlayer.mAdSmallBitmap = smallBitmap;
			mViewPlayer.mAdSmall.setClickable( largeBitmap != null );
			mViewPlayer.mAdSmall.setVisibility( View.VISIBLE );
			
//			IHRAd.shared().report( IHRAd.kAdPurposeImpressionPlayerSmall );
		}

		if ( largeBitmap != null ) {
			mViewPlayer.mAdLarge.setBackground( largeBitmap );
			mViewPlayer.mAdLarge.setClickable( mAdClickURL != null );
			mViewPlayer.mAdLarge.setVisibility( showGatewayAd ? View.VISIBLE : View.INVISIBLE );

			if ( showGatewayAd ) {
				task = new IHRTimerTask() {
					@Override
					public void run() {
						mHandler.sendMessage( mHandler.obtainMessage( kMessageCloseLargeAd ) );
					}
				};

				startup = IHRConfigurationClient.shared().copyStartup();
				
				new Timer().schedule( task, startup.mAdsGatewayDisplayTime * 1000 );

//				IHRAd.shared().report( IHRAd.kAdPurposeImpressionPlayerLarge );
			}
		}
	}
	
	public void setAdLargeHidden( boolean hidden, boolean allowClose ) {
		if ( hidden ) {
			if ( mViewPlayer.mAdLarge.getVisibility() == View.VISIBLE ) {
				mViewPlayer.mAdLarge.setVisibility( View.INVISIBLE );
				mViewPlayer.mAdLarge.mClose.setVisibility( View.INVISIBLE );

				IHRPreferences.setInteger( kKeyListenSecondsSinceLastGatewayAdShown, 1 );
				IHRPreferences.setInteger( kKeyLoadsSinceLastGatewayAdShown, 1 );
			}
		} else {
			if ( mViewPlayer.mAdLarge.getVisibility() == View.INVISIBLE ) {
				mViewPlayer.mAdLarge.setVisibility( View.VISIBLE );
				mViewPlayer.mAdLarge.mClose.setVisibility( allowClose ? View.VISIBLE : View.INVISIBLE );

//				IHRAd.shared().report( IHRAd.kAdPurposeImpressionPlayerLarge );
			}
		}
	}
	
	public void showLyrics() {
		String					lyricsURL;
		
		if ( mMetadata == null ) return;
		if ( ( lyricsURL = (String) mMetadata.get( "lyricsURL" ) ) == null || lyricsURL.length() == 0 ) return;
		
		((IHRActivity) activity()).openWebURL( lyricsURL );
	}
	
	public boolean handleMessage( Message message ) {
		IHRXMLTrackInfo			info;
		
		if ( message.what == kMessageCloseLargeAd ) {
			setAdLargeHidden( true, false );
		} 
//		else if ( message.obj instanceof IHRAd ) 
//		{
//			if ( mCallLetters.equals( ((IHRAd) message.obj).mContextPlayer ) ) kickoffAds( false );
//		} 
		
		else if ( message.obj instanceof IHRXMLTrackInfo ) {
			info = (IHRXMLTrackInfo) message.obj;
			
			if ( info.mMetadata != mMetadata ) return true;
			
			mViewPlayer.setLogoBack( info.mCoverArt );
			
			if ( info.mLyricsURL != null && info.mLyricsURL.length() > 0 ) {
				mViewPlayer.setLyricsEnabled( true );
			} else {
				mViewPlayer.setLyricsEnabled( false );
			}
		}
		
		return true;
	}
	
	@Override
	public void onCreate( Bundle inState ) {
		super.onCreate( inState );
		
		setContentView( mViewPlayer = new IHRViewPlayer( this ) );
		
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive( Context context, Intent intent ) {
				Bitmap			bitmap;
				
				if ( intent.getAction().equals( IHRActivity.kActionReceivedStationLogo ) ) {
					bitmap = ((IHRActivity) activity()).getCurrentStationLogo();
					
					mViewPlayer.setLogoFront( bitmap );
				}
			}
		};
		
		activity().registerReceiver( mReceiver, new IntentFilter( IHRActivity.kActionReceivedStationLogo ) );
	}
	
	@Override
	public void onDestroy() {
		getContext().unregisterReceiver( mReceiver );
	}
	
	@Override
	public void onNewIntent( Intent intent ) {
		IHRStation				station = null;
		IHRPlayerClient			player = IHRPlayerClient.shared();
		long now = Calendar.getInstance().getTime().getTime();
		
		try {
			mCallLetters = intent.getStringExtra( "station" );
			mIgnoreMetadata = false;
			mSeekable = false;
			
			player.setDelegate( this );
			
			if ( mCallLetters == null || mCallLetters.length() == 0 ) {
				try { updateMetadata( player.getMetadata() ); } catch ( Exception e ) { }
				
				station = player.getStation();
				
				mCallLetters = station.getCallLetters();
			}
			//} else if ( intent.hasExtra( "traffic" ) || IHRStation.isTraffic( mCallLetters ) ) {
			if ( intent.hasExtra( "traffic" ) || IHRStation.isTraffic( mCallLetters ) ) {
				updateStatus( IHRServicePlayer.kStatusBuffering );
				
				station = IHRConfigurationClient.shared().stationForCallLetters( mCallLetters );
				
				if ( null == station && intent.hasExtra( "traffic" ) ) {
					station = new IHRStation( intent.getStringArrayListExtra( "traffic" ) );
				}
				
				if ( station != null ) {
					mPlayStationCalled = true;
					player.setStreamIsNew(now);
					player.playStation( station );
				}
				
				mViewPlayer.setLogoBack( null );
				mViewPlayer.setLogoFrontResource( R.drawable.local_traffic );
			} else if ( intent.hasExtra( "channel" ) || intent.hasExtra( "archive" ) ) {
				ArrayList		archive = intent.hasExtra( "archive" ) ? intent.getStringArrayListExtra( "archive" ) : null;
				IHRPremiumChannel	channel = IHRConfigurationClient.shared().fetchChannel( intent.getStringExtra( "site" ) );
				
				updateStatus( IHRServicePlayer.kStatusBuffering );
				
//				mViewPlayer.setLogoBack( null );
//				mViewPlayer.setLogoFront( null );
				
				mIgnoreMetadata = true;
				mPlayStationCalled = true;
				mSeekable = ( null != archive );
				mURL = ( null == archive ) ? null : (String)archive.get( IHRPremiumItem.kLink );
				IHRConfigurationClient.shared().playPremiumItem( archive , intent.getStringExtra( "site" ) );
				
				mViewPlayer.setNameAndDescription( channel.getName() ,
					null == archive ? channel.getDescription() :
						(String)archive.get( IHRPremiumItem.kTitle ) );
			} else {
				updateStatus( IHRServicePlayer.kStatusBuffering );
				
				station = IHRConfigurationClient.shared().stationForCallLetters( mCallLetters );
				if ( station != null ) {
					mPlayStationCalled = true;
					player.setStreamIsNew(now);
					player.playStation( mCallLetters );
				}
			}
			
			if ( null != station ) {
				mPlayStationCalled = false;
				mViewPlayer.setNameAndDescription( station.getName() , station.getDescription() );
			}
			
			mViewPlayer.updateSeekable( mSeekable ? 2 : 0 , mURL );
		} catch ( Exception e ) {
		}
	}
	
	@Override
	public void afterShown( boolean inShown ) {
		Bitmap				bitmap;

		if ( ! inShown ) return;
		
		if ( ( bitmap = ((IHRActivity) activity()).getCurrentStationLogo() ) != null ) {
			mViewPlayer.setLogoFront( bitmap );
		}

		kickoffAds( true );
	}
	
	@Override
	public void beingShown( boolean inShown ) {
		IHRPlayerClient			playerClient = IHRPlayerClient.shared();
		
		playerClient.setDelegate( inShown ? this : null );
		
		if ( inShown ) {
			mVolumeAssigned = 0;
			
			if ( ! mPlayStationCalled ) {
				updateMetadata( playerClient.getMetadata() );
			}

			mPlayStationCalled = false;
			//if (!(playerClient.getStatus().equalsIgnoreCase(IHRServicePlayer.kStatusNoNetwork) 
			//		|| playerClient.getStatus().equalsIgnoreCase(IHRServicePlayer.kStatusConnectionFailed))) {
				updateStatus( playerClient.getStatus() );
				updateVolume( playerClient.getVolume() );
			//} 
		}
	}
	
	// called on main thread
	public void updateMetadata( IHRHashtable inMetadata ) {
		String					artist = null, track = null;
		
		mMetadata = inMetadata;
		
		if ( null != inMetadata ) {
			artist = (String) inMetadata.get( "artist" );
			track = (String) inMetadata.get( "track" );
		}
		
		if ( null == artist ) artist = "";
		if ( null == track ) track = "";

/*
		// test metadata:
		artist = "Doobie Brothers";
		track = "Listen To The Music";
*/
		
		/*
        TODO: 1.
                 a.   Album artwork is missing in general. No Meta data either available on z100 mostly. Works fine on Radio Weezer.
        TODO: 2.    Favorite icon works sometimes and does not work other times.
                 a.   When a station is made a favorite, it no longer provides an option is remove as favorite.
                 b.   Cannot favorite any songs; no metadata available on z100 mostly. Works fine on Radio Weezer.
        TODO: 4.    The AMEX ad is not centered on the screen. ThereÕs lots of black area at the top of the ad. (go to Christina Aguilera Radio to see this ad.)
		*/
		
		mViewPlayer.setLyricsEnabled( false );
		mViewPlayer.setLogoBack( null );
		if ( !mIgnoreMetadata ) mViewPlayer.setArtistAndTrack( artist , track );
		if ( artist.length() > 0 && track.length() > 0 ) new IHRXMLTrackInfo( mHandler, inMetadata );
		
		updateTagging();
	}
	
	public void updateVolume( float inVolume ) {
		AudioManager			audio = (AudioManager)activity().getSystemService( Context.AUDIO_SERVICE );
		
		inVolume = (float)audio.getStreamVolume( AudioManager.STREAM_MUSIC ) / (float)audio.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
		
		//	wait a few seconds to ignore feedback from slider
		if ( System.currentTimeMillis() > mVolumeAssigned + 3000 ) {
			mViewPlayer.updateVolume( inVolume );
		}
	}
	
	public void updateStatus( String inStatus ) {
		String					display = "";
		int						playing = 0;
		boolean					tagging = false;
		//warnConnectionFailure();
		if ( null == inStatus ) {
			display = "Connecting...";
			playing = 1;
		} else if (inStatus.equals( IHRServicePlayer.kStatusNoNetwork )) {
			warnConnectionFailure();
			return;
		} else if (inStatus.equals( IHRServicePlayer.kStatusConnectionFailed )) {
			warnNoDNS();
			return;
		} else if ( inStatus.equals( IHRServicePlayer.kStatusIdle ) || inStatus.equals( IHRServicePlayer.kStatusPreparing ) || inStatus.equals( IHRServicePlayer.kStatusOpening ) ) {
			display = "Connecting...";
			playing = 1;
		} else if ( inStatus.equals( IHRServicePlayer.kStatusBuffering ) ) {
			display = "Buffering...";
			playing = 1;
			tagging = true;
		} else if ( inStatus.equals( IHRServicePlayer.kStatusBuffered ) || inStatus.equals( IHRServicePlayer.kStatusPlaying ) ) {
			playing = 2;
			tagging = true;
		}
		
		mViewPlayer.updatePlaying( playing );
		mViewPlayer.setInfoTextRight( display );
		
		if ( null != mViewTransport ) {
			mViewTransport.showStopped( playing > 0 ? 1 : 0 );
		}
		
		if ( tagging != mPlaying ) {
			mPlaying = tagging;
			
			updateTagging();
		}

		//New code for #1064 (part 2 of 3)
		//Part 1 in the top of IHRControllerPremiumArchives.java
		//Part 2 at line 440 in IHRControllerPlayer.java
		//Part 3 at line 40 in IHRControllerCursorList.java
		if (IHRServicePlayer.kStatusFinished.equals(inStatus) ) {
			if(IHRControllerPremiumArchives.followingIntents.size() != 0){
			  mCreationIntent = 
				IHRControllerPremiumArchives.followingIntents.remove(0);
			  onNewIntent(mCreationIntent);
			}
		}
	}

	public void updateThroughput( String message ) {
		mViewPlayer.updateThroughput( message );
	}
	
	public void togglePlay() {
		boolean					playing;

		IHRPlayerClient.shared().togglePlaying();
		
		mMetadata = null;
//		mViewPlayer.mArtist.setText( "" );
//		mViewPlayer.mTrack.setText( "" );
		//if ( !mIgnoreMetadata ) mViewPlayer.setArtistAndTrack( "" , "" );//Code commented by sriram for handling the Header disappearing
		
		//playing = IHRPlayerClient.shared().isPlayRequested();
		playing = IHRPlayerClient.shared().isPlaying();

		
		//	show expected result before receiving status
		mViewPlayer.updatePlaying( playing ? 1 : 0 );
		
		if ( null != mViewTransport ) {
			//Code inserted by sriram for handling the progress related issue on 08-19-2010
			if(playing==false)
				stopflag=1;
			else
				stopflag=0;
			//Code ends here

			mViewTransport.showStopped( playing ? 1 : 0 );
		}
	}
	
	public void assignVolume( float inVolume ) {
		//**
		AudioManager			audio = (AudioManager)activity().getSystemService( Context.AUDIO_SERVICE );
		
		audio.setStreamVolume( AudioManager.STREAM_MUSIC , (int)( audio.getStreamMaxVolume( AudioManager.STREAM_MUSIC ) * inVolume ) , AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE );
		/*/
		mVolumeAssigned = System.currentTimeMillis();
		
		IHRPlayerClient.shared().setVolume( inVolume );
		/**/
	}
	
	public void handleVolume( int inKeyCode ) {
		AudioManager			audio = (AudioManager)activity().getSystemService( Context.AUDIO_SERVICE );
		
		if ( KeyEvent.KEYCODE_MUTE == inKeyCode ) {
			audio.setStreamMute( AudioManager.STREAM_MUSIC , true );
		} else {
			audio.adjustStreamVolume( AudioManager.STREAM_MUSIC , ( KeyEvent.KEYCODE_VOLUME_UP == inKeyCode ) ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER , AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE );
		}
		
		updateVolume( 0 );
	}
	
	public void updateTagging() {
		/*
		String					artist = null;
		IHRStation				station;
		String					track = null;
		int						tag = 0;
		
		if ( mPlaying && null != mMetadata ) {
			artist = (String)mMetadata.get( "artist" );
			track = (String)mMetadata.get( "track" );
		}
		
		if ( null != artist && null != track && 0 != track.length() ) {
			tag = IHRConfigurationClient.shared().isSongTagged( artist , track ) ? 0 : 1;
		}
		
		if ( 0 != tag && null != ( station = IHRPlayerClient.shared().getStation() ) ) {
			if ( station.getTagDisabled() ) tag = 0;
			else if ( station.getIsTalk() ) tag = 0;
		}
		
		mViewPlayer.setTagging( tag );
		*/
	}
	
/*	public void doTagSong() {
		String					artist = null;
		String					track = null;
		
		if ( mPlaying && null != mMetadata ) {
			artist = (String)mMetadata.get( "artist" );
			track = (String)mMetadata.get( "track" );
		}
		
		if ( null != artist && null != track && 0 != track.length() ) {
			if ( null == mCallLetters || 0 == mCallLetters.length() ) {
				IHRConfigurationClient.shared().addTaggedSong( artist , track );
			} else {
				showFavorite( true );
			}
		}
		
		updateTagging();
	}*/
	
	@Override
	public boolean onKeyDown( int keyCode , KeyEvent event ) {
		boolean					result = true;
		
		switch ( keyCode ) {
		case KeyEvent.KEYCODE_MUTE:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			handleVolume( keyCode );
			break;
		default: result = super.onKeyDown( keyCode , event ); break;
		}
		
		return result;
	}
	
	public boolean onKeyDown_( int keyCode , KeyEvent event ) {
		return onKeyDown( keyCode , event ); 
	}
	
	@Override
	public boolean wantsBanner() { return false; }
	
/*	public void toggleFavorite() {
		if ( null == mViewFavorite ) {
			mViewFavorite = new IHRViewFavorite( this );
			//IHRFavoriteDialog fav = new IHRFavoriteDialog( this.getContext() );
			//fav.show();
		} else {
			mViewFavorite.destroy();
			mViewFavorite = null;
		}
	}*/
	
	public void showFavorite() {
		if (null != mCallLetters && mCallLetters.length() > 0) {
			IHRFavoriteDialog dialog = new IHRFavoriteDialog(this.getContext());
			dialog.show();
		}
	}
	
/*	public void showFavorite( boolean inShow ) {
		if ( inShow == ( null == mViewFavorite ) ) toggleFavorite();
	}*/
	
/*	public boolean isShowingFavorite() {
		return ( null != mViewFavorite );
	}*/
	
	public void onFavoriteClicked() {
		IHRConfigurationClient	client = IHRConfigurationClient.shared();
		IHRStation				station = null;
		
		//String					artist = null;
		//String					track = null;
		String commercial="";
		if(mMetadata!=null)
		{
			if((String) mMetadata.get( "iscommercial" )!=null)
				commercial=(String) mMetadata.get( "iscommercial" );
		}
		//Code changed by sriram on 08-31-2010
		if(commercial!=null)
		{
			if(commercial.trim().length()>0 && commercial.equals("1"))
				return;
		}
		showFavorite();
	}	
	public void toggleTransport() {
		if ( null == mViewTransport ) {
			mViewTransport = new IHRViewTransport( this );
		} else {
			mViewTransport.destroy();
			mViewTransport = null;
		}
	}
	
	public void showTransport( boolean inShow ) {
		if ( inShow == ( null == mViewTransport ) ) toggleTransport();
	}
	
	public boolean isShowingTransport() {
		return ( null != mViewTransport );
	}
	
	public void enterSeek() {
	}
	
/*	public class IHRViewFavorite implements OnClickListener {
		IHRControllerPlayer			mDelegate;
		
		ViewGroup					mRoot;
		ImageView					mButtonSong;
		ImageView					mButtonStation;
		ImageView					mButtonCancel;
		ImageView					mButtonChange;
		
		String						mStation;
		String						mSongTrack;
		String						mSongArtist;
		boolean						mSongIsFavorite;
		boolean						mStationIsFavorite;
		boolean						mStationHasNoSong;
		
		public IHRViewFavorite( IHRControllerPlayer inDelegate ) {
			mStationHasNoSong = false;
			mDelegate = inDelegate;
			IHRConfigurationClient client = IHRConfigurationClient.shared();
			IHRStation station = client.stationForCallLetters(mCallLetters);
			
			if (mPlaying && null != mMetadata) {
				mSongTrack = (String)mMetadata.get( "track" );
				mSongArtist = (String)mMetadata.get( "artist" );
			} else {
				mSongTrack = "";
				mSongArtist = "";
			}
			
			if (station.getIsTalk() 
					|| station.getDescription().toLowerCase().contains("traffic")
					|| station.getName().toLowerCase().contains("traffic")
					|| null == mSongTrack || mSongTrack.trim().equals("")){
				mStationHasNoSong = true;
			}
			
			if (mStationHasNoSong )
			{
				mRoot = (ViewGroup)mDelegate.inflateLayout( R.layout.favorite_talk );
			} else {
				mRoot = (ViewGroup)mDelegate.inflateLayout( R.layout.favorite );
			}
			
			
			mStation = mCallLetters;
			
			prepare();
			
			mViewPlayer.addView( mRoot );
		}
		
		protected void destroy() {
			mViewPlayer.removeView( mRoot );
			mRoot.removeAllViews();
			mRoot = null;
			
			mButtonSong = null;
			mButtonStation = null;
			mButtonCancel = null;
			mButtonChange = null;
			
			mStation = null;
			mSongTrack = null;
			mSongArtist = null;
		}
		
		protected void prepare() {
			IHRConfigurationClient	client = IHRConfigurationClient.shared();
			
			mStationIsFavorite = client.isFavorite( mStation );
			mSongIsFavorite = client.isSongTagged( mSongArtist , mSongTrack );
			
			if (!mStationHasNoSong)
			{
				mButtonSong = (ImageView)mRoot.findViewById( R.id.FavoriteSongCheck );
				mButtonSong.setOnClickListener( this );	
				assignCheckbox( mButtonSong , mSongIsFavorite );
			}
			mButtonStation = (ImageView)mRoot.findViewById( R.id.FavoriteStationCheck );
			mButtonCancel = (ImageView)mRoot.findViewById( R.id.FavoriteCancel );
			mButtonChange = (ImageView)mRoot.findViewById( R.id.FavoriteChange );
			
			
			mButtonStation.setOnClickListener( this );
			mButtonCancel.setOnClickListener( this );
			mButtonChange.setOnClickListener( this );
			
			assignCheckbox( mButtonStation , mStationIsFavorite );
			
			enableButton( mButtonChange , false );
		}
		
		public void update() {
			Boolean					change = !(
				( !mStationHasNoSong && ((Boolean)mButtonSong.getTag()).booleanValue() == mSongIsFavorite ) &&
				//( ((Boolean)mButtonSong.getTag()).booleanValue() == mSongIsFavorite ) &&
				( ((Boolean)mButtonStation.getTag()).booleanValue() == mStationIsFavorite ) );
			
			enableButton( mButtonChange , change );
		}
		
		public void enableButton( ImageView inButton , boolean inEnable ) {
			inButton.setEnabled( inEnable );
			inButton.setAlpha( inEnable ? 255 : 127 );
		}
		
		public void assignCheckbox( ImageView inBox , boolean inChecked ) {
			inBox.setTag( Boolean.valueOf( inChecked ) );
			inBox.setImageResource( inChecked ? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked );
		}
		
		public void toggleCheckbox( ImageView inBox ) {
			assignCheckbox( inBox , !((Boolean)inBox.getTag()).booleanValue() );
			update();
		}
		
		public void assignFavorites() {
			IHRConfigurationClient	client = IHRConfigurationClient.shared();
			boolean					stationIsFavorite = ((Boolean)mButtonStation.getTag()).booleanValue();
			
			if (!mStationHasNoSong)
			{
				boolean					songIsFavorite = ((Boolean)mButtonSong.getTag()).booleanValue();

				if ( songIsFavorite != mSongIsFavorite ) {
					if ( songIsFavorite ) {
						client.addTaggedSong( mSongArtist , mSongTrack );
					} else {
						client.removeTaggedSong( mSongArtist , mSongTrack );
					}
				}
			}
			
			if ( stationIsFavorite != mStationIsFavorite ) {
				if ( stationIsFavorite ) {
					client.addFavorite( mStation );
				} else {
					client.removeFavorite( mStation );
				}
			}
			
			//	TODO: IHRPlayerFavorite.setFavorited( songIsFavorite && stationIsFavorite );
			
			showFavorite( false );
		}
		
		public void onClick( View inClicked ) {
			if ( !( inClicked instanceof ImageView ) ) ;
			else if ( !mStationHasNoSong && inClicked == mButtonSong ) toggleCheckbox( mButtonSong );
			//else if ( inClicked == mButtonSong ) toggleCheckbox( mButtonSong );
			else if ( inClicked == mButtonStation ) toggleCheckbox( mButtonStation );
			else if ( inClicked == mButtonCancel ) showFavorite( false );
			else if ( inClicked == mButtonChange ) assignFavorites();
		}
	}*/
	
	public class IHRViewTransport implements OnClickListener, OnSeekBarChangeListener, OnTouchListener, IHRListener, Runnable {
		IHRControllerPlayer			mDelegate;
		
		ViewGroup					mRoot;
		SeekBar						mSeek;
		TextView					mTimeCurrent;
		TextView					mTimeRemains;
		ImageView					mButtonReverse;
		ImageView					mButtonPlaying;
		ImageView					mButtonAdvance;
		ImageView					mButtonRestart;
		
		int							mScanning;
		int							mPosition;
		int							mDuration;
		long						mPrevious;
		long						mIdleTime;
		
		public IHRViewTransport( IHRControllerPlayer inDelegate ) {
			mDelegate = inDelegate;
			mRoot = (ViewGroup)mDelegate.inflateLayout( R.layout.transport );
			
			prepare();
			
			mViewPlayer.addView( mRoot );
		}
		
		protected void destroy() {
			mViewPlayer.removeView( mRoot );
			mRoot.removeAllViews();
			mRoot = null;
			
			mSeek = null;
			mTimeCurrent = null;
			mTimeRemains = null;
			mButtonReverse = null;
			mButtonPlaying = null;
			mButtonAdvance = null;
			mButtonRestart = null;
			
			mScanning = 0;
			mPrevious = 0;
			mPosition = 0;
			
			IHRBroadcaster.common().removeFor( null , this );
			IHRThreadable.gMain.remove( this );
		}
		
		protected void prepare() {
			IHRBroadcaster.common().listenFor( IHRServicePlayer.kNotifyNamePlayer , this );
			
			mSeek = (SeekBar)mRoot.findViewById( R.id.ControlsSeekBar );
			mButtonReverse = (ImageView)mRoot.findViewById( R.id.ControlsReverse );
			mButtonPlaying = (ImageView)mRoot.findViewById( R.id.ControlsPlaying );
			mButtonAdvance = (ImageView)mRoot.findViewById( R.id.ControlsAdvance );
			mButtonRestart = (ImageView)mRoot.findViewById( R.id.ControlsRestart );
			mTimeCurrent = (TextView)mRoot.findViewById( R.id.ControlsCurrent );
			mTimeRemains = (TextView)mRoot.findViewById( R.id.ControlsRemains );
			
			mSeek.setOnSeekBarChangeListener( this );
			mButtonReverse.setOnClickListener( this );
			mButtonPlaying.setOnClickListener( this );
			mButtonAdvance.setOnClickListener( this );
			mButtonRestart.setOnClickListener( this );
			
			mButtonReverse.setOnTouchListener( this );
			mButtonAdvance.setOnTouchListener( this );
			
			mIdleTime = System.nanoTime();
			
			IHRViewSlider.fixBrokenSeekBar( mSeek );
			
			update( true );
		}
		
		public void update( boolean inUpdatePlaying ) {
			int[]				position = IHRPlayerClient.shared().getPosition();
			
			if ( null != position && position.length > 1 && position[1] > 0 ) {
				showPosition( position[0] , position[1] );
				
				mPrevious = System.nanoTime();
				mPosition = position[0];
				mDuration = position[1];
				
				if ( inUpdatePlaying && position.length > 2 ) {
					showStopped( position[2] == 0 ? 0 : 1 );
				}
				
				IHRThreadable.gMain.handle( this , 500 );
			}
		}
		
		public void run() {
			long				now = System.nanoTime();
			
			if ( ( now - mIdleTime ) > 7L*1000000000L ) {
				mDelegate.toggleTransport();
			} else if ( mPrevious > 0 ) {
				int				interval = 500;
				int				position = mPosition + (int)( ( now - mPrevious ) / 1000000 );
				int				duration = mDuration;
				
				if ( 0 != mScanning ) {
					position += mScanning;
					interval = 1000;
					
					if ( position > duration ) position = duration;
					if ( position < 0 ) position = 0;
					
					IHRPlayerClient.shared().setPosition( position );
				}
				//showPosition( position , duration );
				if(IHRPlayerClient.shared().isPlaying()) showPosition( position , duration );
				
				if ( position < duration && null != mRoot ) {
					IHRThreadable.gMain.handle( this , interval );
				}
			}
		}
		
		public void onClick( View inClicked ) {
			mIdleTime = System.nanoTime();
			
			if ( !( inClicked instanceof ImageView ) ) ;
			else if ( inClicked == mButtonReverse ) { mScanning = -10000; run(); mScanning = 0; }
			else if ( inClicked == mButtonPlaying ) mDelegate.togglePlay();
			else if ( inClicked == mButtonAdvance ) { mScanning = 10000; run(); mScanning = 0; }
			else if ( inClicked == mButtonRestart ) IHRPlayerClient.shared().setPosition( 0 );
		}
		
		public boolean onTouch( View inTouched , MotionEvent inEvent ) {
			boolean				result = false;
			
			mIdleTime = System.nanoTime();
			
			if ( inTouched instanceof ImageView ) {
				switch ( inEvent.getAction() ) {
				case MotionEvent.ACTION_DOWN:
					mScanning = ( inTouched == mButtonReverse ) ? -10000 : 10000;
					result = true;
					run();
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					mScanning = 0;
					break;
				}
				
			}
			
			return result;
		}
		
		public void onProgressChanged( SeekBar inSeek , int inZeroToMaximum , boolean inByUser ) {
			if ( inByUser ) {
				mIdleTime = System.nanoTime();
				
				IHRPlayerClient.shared().setPosition( inZeroToMaximum );
			}
		}
		
		public void onStartTrackingTouch( SeekBar inSeek ) {
			mIdleTime = System.nanoTime();
			
			IHRPlayerClient.shared().pauseForSeek( true );
		}
		
		public void onStopTrackingTouch( SeekBar inSeek ) {
			mIdleTime = System.nanoTime();
			
			IHRPlayerClient.shared().pauseForSeek( false );
		}
		
		public void showStopped( int inStopped ) {
			mButtonPlaying.setImageResource( inStopped != 0 ? inStopped > 0 ? R.drawable.transport_pause : R.drawable.transport_stop : R.drawable.transport_play );
		}
		
		public void showProgress( int inZeroToMaximum , int inMaximum ) {
			mSeek.setMax( inMaximum );
			mSeek.setProgress( inZeroToMaximum );
		}
		
		public String timeString( int inSeconds ) {
			return String.format( "%02d:%02d" , ( inSeconds / 60 ) , ( inSeconds % 60 ) );
//			return ( inSeconds / 60 ) + ":" + ( inSeconds % 60 );
		}
		
		public void showPosition( int inPosition , int inDuration ) {
//			showProgress( inPosition , inDuration );
			
//			mTimeCurrent.setText( timeString( inPosition > 0 ? inPosition / 1000 : 0 ) + " " );
			//Code inserted by sriram for handling the progress bar on 08-19-2010
			if(stopflag==0)
			{
				showProgress( inPosition , inDuration );
				mTimeCurrent.setText( timeString( inPosition > 0 ? inPosition / 1000 : 0 ) + " " );
			}
			//Code ends here 

			mTimeRemains.setText( timeString( inDuration > 0 ? inDuration / 1000 : 0 ) + " " );
//			mTimeRemains.setText( "-" + timeString( inPosition < inDuration ? ( inDuration - inPosition ) / 1000 : 0 ) );
		}
		
		public void listen( String inName , IHRHashtable inDetails ) {
			if ( inName.equals(  IHRServicePlayer.kNotifyNamePlayer ) ) {
				int					position = inDetails.integerValue( "position" , 0 );
				int					duration = inDetails.integerValue( "duration" , 0 );
				
				if ( duration > 0 && null != mRoot ) {
					mPrevious = System.nanoTime();
					mPosition = position;
					mDuration = duration;
					showPosition( position , duration );
	//				showStopped( inDetails.booleanValue( "paused" , false ) ? 1 : 0 );
					
					IHRThreadable.gMain.handle( this , 500 );
				}
			}
			
			if ( inName.equals( IHRCache.kNotifyNameDownload ) ) {
				String				url = (String)inDetails.get( "url" );
				int					offset = inDetails.integerValue( "offset" , 0 );
				int					length = inDetails.integerValue( "length" , 0 );
				
				if ( length > 0 && offset < length && url.equals( mURL ) ) {
					mSeek.setSecondaryProgress( (int)( ( (long)offset * mSeek.getMax() ) / length ) );
				}
			}
		}
	}
	
	public class IHRFavoriteDialog extends AlertDialog implements OnClickListener {
		ImageView					mButtonSong;
		ImageView					mButtonStation;
		ImageView					mButtonCancel;
		ImageView					mButtonChange;
		View 						mView;
		
		String						mStation;
		String						mSongTrack;
		String						mSongArtist;
		boolean						mSongIsFavorite;
		boolean						mStationIsFavorite;
		boolean						mStationHasNoSong;

		protected IHRFavoriteDialog(Context context) {
			super(context);
			mStationHasNoSong = false;
			IHRConfigurationClient client = IHRConfigurationClient.shared();
			IHRStation station = client.stationForCallLetters(mCallLetters);
			
			if (mPlaying && null != mMetadata) {
				mSongTrack = (String)mMetadata.get( "track" );
				mSongArtist = (String)mMetadata.get( "artist" );
			} else {
				mSongTrack = "";
				mSongArtist = "";
			}
			
			if (station.getIsTalk() 
					|| station.getDescription().toLowerCase().contains("traffic")
					|| station.getName().toLowerCase().contains("traffic")
					|| null == mSongTrack || mSongTrack.trim().equals("")){
				mStationHasNoSong = true;
			}
			
			//if (mStationHasNoSong )
			//{
			//	mView = View.inflate(this.getContext(), R.layout.favorite_talk, null);
			//} else {
				mView = View.inflate(this.getContext(), R.layout.favorite, null);
			//}
			
			mStation = mCallLetters;
			
			prepare();
		}
		
		protected void onCreate(Bundle savedInstanceState) {
			setContentView(mView);
		}
		
		protected void prepare() {
			IHRConfigurationClient	client = IHRConfigurationClient.shared();
			
			mStationIsFavorite = client.isFavorite( mStation );
			mSongIsFavorite = client.isSongTagged( mSongArtist , mSongTrack );
			
			mButtonSong = (ImageView)mView.findViewById( R.id.FavoriteSongCheck );
			mButtonStation = (ImageView)mView.findViewById( R.id.FavoriteStationCheck );
			mButtonCancel = (ImageView)mView.findViewById( R.id.FavoriteCancel );
			mButtonChange = (ImageView)mView.findViewById( R.id.FavoriteChange );
			
			mButtonSong.setOnClickListener( this );
			mButtonStation.setOnClickListener( this );
			mButtonCancel.setOnClickListener( this );
			mButtonChange.setOnClickListener( this );
			
			assignCheckbox( mButtonSong , mSongIsFavorite );
			assignCheckbox( mButtonStation , mStationIsFavorite );
			
			if (mStationHasNoSong) enableFavorite(mButtonSong, (TextView)mView.findViewById(R.id.FavoriteSongLabel), false);
			enableButton( mButtonChange , false );
		}
		
		public void update() {
			Boolean					change = !(
				( !mStationHasNoSong && ((Boolean)mButtonSong.getTag()).booleanValue() == mSongIsFavorite ) &&
				( ((Boolean)mButtonStation.getTag()).booleanValue() == mStationIsFavorite ) );
			
			enableButton( mButtonChange , change );
		}
		
		public void enableButton( ImageView inButton , boolean inEnable ) {
			inButton.setEnabled( inEnable );
			inButton.setAlpha( inEnable ? 255 : 127 );
		}
		
		public void enableFavorite( ImageView inButton ,TextView inText, boolean inEnable ) {
			inButton.setEnabled( inEnable );
			// drawables have unexpected behaviour as outlined in the url.
			// http://android-developers.blogspot.com/2009/05/drawable-mutations.html
			Drawable button = inButton.getDrawable();

			if (inEnable) {
				button.mutate().setAlpha(255);
			} else {
				button.mutate().setAlpha(127);
			}
			inButton.setImageDrawable(button);
			inText.setTextColor(inEnable ? 0xffffffff : 0xffaaaaaa);
		}
		
		public void assignCheckbox( ImageView inBox , boolean inChecked ) {
			inBox.setTag( Boolean.valueOf( inChecked ) );
			inBox.setImageResource( inChecked ? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked );
		}
		
		public void toggleCheckbox( ImageView inBox ) {
			assignCheckbox( inBox , !((Boolean)inBox.getTag()).booleanValue() );
			update();
		}
		
		public void assignFavorites() {
			IHRConfigurationClient	client = IHRConfigurationClient.shared();
			boolean					stationIsFavorite = ((Boolean)mButtonStation.getTag()).booleanValue();
			
			if (!mStationHasNoSong)
			{
				boolean					songIsFavorite = ((Boolean)mButtonSong.getTag()).booleanValue();

				if ( songIsFavorite != mSongIsFavorite ) {
					if ( songIsFavorite ) {
						client.addTaggedSong( mSongArtist , mSongTrack );
					} else {
						client.removeTaggedSong( mSongArtist , mSongTrack );
					}
				}
			}
			
			if ( stationIsFavorite != mStationIsFavorite ) {
				if ( stationIsFavorite ) {
					client.addFavorite( mStation );
				} else {
					client.removeFavorite( mStation );
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void onClick(final View inClicked) {
			if ( !( inClicked instanceof ImageView ) ) ;
			else if ( !mStationHasNoSong && inClicked == mButtonSong ) toggleCheckbox( mButtonSong );
			else if ( inClicked == mButtonStation ) toggleCheckbox( mButtonStation );
			else if ( inClicked == mButtonCancel ) cancel(); 
			else if ( inClicked == mButtonChange ) {
				Thread t = new Thread() {
		            public void run() {
		            	assignFavorites();
		            }
		        };
		        t.start();

				dismiss();
			}
		}
		@Override
		public boolean onKeyDown( int keyCode , KeyEvent event ) {
			return onKeyDown_(keyCode, event); //hand off key down events to the parent of this dialog
		}
	}

	
	public abstract class PlayerDialog extends IHRAlert {
		public PlayerDialog( String inMessage , String inButton1 , String inButton2 , String inButton3 ) { super( inMessage , inButton1 , inButton2 , inButton3 ); }
		@Override
		public void onCancel( DialogInterface inDialog ) { cease(); /*onClick( AlertDialog.BUTTON_NEGATIVE );*/ }
	}
	
	public void warnNoDNS() {
		new PlayerDialog( "Unable to connect to the server at this time. Please try again later." , "OK" , null , null ) {
			@Override public void onClick( int inButton ) { 
				cease(); 
			}
		};
	}
	
	public PlayerDialog warnConnectionFailure() {
		return new PlayerDialog( "Unable to connect to the server. Please check your network settings." , "Quit" , null , null ) {
			@Override public void onClick( int inButton ) { 
				cease();
			}
		};
	}
}
