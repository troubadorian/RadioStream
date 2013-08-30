package com.troubadorian.streamradio.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRPreferences;
import com.troubadorian.streamradio.client.view.IHRViewRandomizer;

public class IHRControllerRandomizer extends IHRController {
	private String				mDebugMode;
	
	@Override
	public void onCreate( Bundle inState ) {
		super.onCreate( inState );
		
		setContentView( new IHRViewRandomizer( this ) );
		
//		mContent.mDelegate = this;
	}
	
	public void onPlay( String inLetters ) {
		Intent					intent = new Intent();
		
		intent.putExtra( "class" , IHRControllerPlayer.class.getName() );
		intent.putExtra( "station" , inLetters );
		
		startController( intent );
	}
	
	@Override
	public boolean onKeyDown( int keyCode , KeyEvent event ) {
		if ( keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z ) {
			String				letter = String.valueOf( (char)( 'a' + keyCode - KeyEvent.KEYCODE_A ) );
			int					length;
			
			mDebugMode = ( null == mDebugMode ) ? letter : mDebugMode + letter;
			length = mDebugMode.length();
			
			if ( mDebugMode.endsWith( "debugon" ) ) {
				if ( Streamradio.g.debugModePermitted() ) {
					IHRPreferences.setBoolean( IHRConfigurationClient.kKeyDebugModeEnabled , true );
					Toast.makeText( activity() , "Debug mode enabled. Relaunch Streamradio." , Toast.LENGTH_SHORT ).show();
				}
				mDebugMode = null;
			} else if ( mDebugMode.endsWith( "debugoff" ) ) {
				Toast.makeText( activity() , "Debug mode disabled. Relaunch Streamradio." , Toast.LENGTH_SHORT ).show();
				IHRPreferences.unset( IHRConfigurationClient.kKeyDebugModeEnabled );
				mDebugMode = null;
			} else if ( length > 10 ) {
				mDebugMode = mDebugMode.substring( length - 10 );
			}
		}
		
		return super.onKeyDown( keyCode , event );
	}
	
}
