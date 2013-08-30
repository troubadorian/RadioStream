package com.troubadorian.streamradio.controller;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.VideoView;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;

public class IHRControllerVideo extends IHRController implements
	OnClickListener,
	OnCompletionListener,
	OnErrorListener,
	OnKeyListener,
	OnPreparedListener,
	OnTouchListener {
	
	VideoView					mVideo;
	ConnectivityManager			mConnectivity;
	
	boolean 					mResume;
	@Override
	protected void onCreate( Bundle inState ) {
		super.onCreate( inState );
		
		mVideo = new VideoView( activity() );
		mVideo.setOnClickListener( this );
		mVideo.setOnTouchListener( this );
		mVideo.setOnKeyListener( this );
		mVideo.setOnPreparedListener( this );
		mVideo.setOnCompletionListener( this );
		mVideo.setOnErrorListener( this );
		mVideo.setBackgroundColor( 0xFF333344 );			//	video not centered without some color
//		mVideo.setLayoutParams( new FrameLayout.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT , ViewGroup.LayoutParams.FILL_PARENT , Gravity.CENTER ) );
		
		
		setContentView( mVideo );
		
		mResume = false;
		mConnectivity = (ConnectivityManager)getContext().getSystemService( Context.CONNECTIVITY_SERVICE );
	}
	
	@Override
	protected void onDestroy() {
		mVideo.setVideoURI( null );
		mVideo.setVisibility( View.GONE );
		mVideo.stopPlayback();
		
		
		super.onDestroy();
	}
	
	@Override
	protected void onNewIntent( Intent intent ) {
		String					url = ( null == intent ) ? null : intent.getStringExtra( "video" );
		
		if ( null != url ) {
			if ( url.indexOf( '?' ) < 0 ) {
				url += "?" + IHRConfigurationClient.shared().parameters( true );
			}
			
			mVideo.setVideoURI( Uri.parse( url ) );
			mVideo.start();		
			
			mResume = true;
			if (mConnectivity.getActiveNetworkInfo().getTypeName().equalsIgnoreCase("wifi")) {
			} else if (mConnectivity.getActiveNetworkInfo().getTypeName().equalsIgnoreCase("mobile")) {
			}
			mVideo.setEnabled( true );
			mVideo.setClickable( true );
			mVideo.setKeepScreenOn( true );
		}
	}
	
	public void onClick( View inView ) {
		if ( mVideo.isPlaying() ) { 
			mVideo.pause(); 
		}
		else { 
			mVideo.start(); 
			if (mResume) {
			} else {
				if (mConnectivity.getActiveNetworkInfo().getTypeName().equalsIgnoreCase("wifi")) {
				} else if (mConnectivity.getActiveNetworkInfo().getTypeName().equalsIgnoreCase("mobile")) {
				}
			}
		}
	}
	
	public boolean onTouch( View inView , MotionEvent inEvent ) {
		boolean					result = false;
		int						action = inEvent.getAction();
		
		switch ( action ) {
		case MotionEvent.ACTION_UP: onClick( inView );	//	fall through
		case MotionEvent.ACTION_DOWN: result = true; break;
		}
		
		return result;
	}
	
	@Override
	public boolean onKeyDown( int inKey , KeyEvent inEvent ) {
		boolean					result = true;
		int						action = inEvent.getAction();
		
		switch ( inKey ) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: if ( KeyEvent.ACTION_DOWN == action ) onClick( mVideo ); break;
		case KeyEvent.KEYCODE_MEDIA_STOP: 
			if ( KeyEvent.ACTION_DOWN == action ) {
				mVideo.pause();
			}
			break;
		default: result = super.onKeyDown( inKey , inEvent ); break;
		}
		
		return result;
	}
	
	public boolean onKey( View inView , int inCode , KeyEvent inEvent ) {
		return onKeyDown( inCode , inEvent );
	}
	
	public void onCompletion( MediaPlayer mp ) {
		mVideo.setKeepScreenOn( false );
		mResume = false;
		finish();
	}
	
	public boolean onError( MediaPlayer mp , int what , int extra ) {
		mVideo.setBackgroundColor( 0xFFFF3300 );
		
		
//		onCompletion( mp );
		
		return true;
	}
	
	public void onPrepared( MediaPlayer mp ) {
		int						w = mVideo.getWidth();
		int						h = mVideo.getHeight();
		int						x = mVideo.getRootView().getWidth();
		int						y = mVideo.getRootView().getHeight();
		
//		mVideo.getRootView().setBackgroundColor( 0xFF00FF00 );
		mVideo.getRootView().setBackgroundColor( 0xFF000000 );	//	video not centered without some color
		mVideo.setBackgroundColor( 0 );
		
		if ( x > w ) mVideo.offsetLeftAndRight( ( x - w ) / 2 );
		if ( y > h ) mVideo.offsetTopAndBottom( ( y - h ) / 2 );
	}
	
}
