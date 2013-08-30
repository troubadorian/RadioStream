package com.troubadorian.streamradio.controller;

import java.util.Calendar;
import java.util.Iterator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRPlayerClient;
import com.troubadorian.streamradio.client.model.IHRPreferences;
import com.troubadorian.streamradio.client.model.IHRThreadable;
import com.troubadorian.streamradio.client.services.IHRConfigurationFile;
import com.troubadorian.streamradio.client.services.IHRService;

/*
 * Activities may be destroyed and created at unpredictable times
 * the user facing activity is more likely to be respawned
 * 
 * All global and context related data is linked from this activity
 * this activity spawns another activity to interact with the user
 * 
 * Anything that is truly global should be in a service
 * Anything that is interface global should be referenced from here
 * All services should be referenced from here
 * 
 * */

public class Streamradio extends IHRActivity implements Handler.Callback, ServiceConnection {
	public static Streamradio	g;
	
	protected IHRThreadable		mThreadable;
	//protected IHRService.MyBinder			mConnection;
	protected IBinder			mConnection;
	protected IHRService mService;
	private boolean				mPaused;
	
	private long mCurrentModeTime;
	private long mBackgroundDuration;
	private long mForegroundDuration;
	private long mApplicationStartTime;
	private String mCurrentMode;
	
	public void playingAlternateAudio( boolean inPause ) {
		if ( inPause ) {
			if ( !mPaused && IHRPlayerClient.shared().isPlayRequested() ) {
				IHRPlayerClient.shared().stop();
				mPaused = true;
			}
		} else if ( mPaused ) {
			IHRPlayerClient.shared().stop();
			mPaused = false;
		}
	}
	
	//	any message sent to IHRThreadable.gMain arrive here
	public boolean handleMessage( Message message ) {
		return false;
	}
	
	@Override
	protected void onCreate( Bundle inState ) {
		g = this;
		
		mThreadable = new IHRThreadable( this , this );
		IHRThreadable.gMain = mThreadable;

		/**
		Log.d( "Streamradio" , "Clear interface preferences" );
		IHRPreferences.clear();
		IHRPreferences.commit();
		/**/
		
		connect();
		super.onCreate( inState );

		mCurrentModeTime = 0;
		mBackgroundDuration = 0;
		mForegroundDuration = 0;
		mCurrentMode = "";
		mApplicationStartTime = Calendar.getInstance().getTime().getTime();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		long now = Calendar.getInstance().getTime().getTime();
		if ( 0 == mCurrentModeTime )
		{
			mCurrentModeTime = now;
		}
		if (!mCurrentMode.equals("foreground"))
		{
			mBackgroundDuration += (now - mCurrentModeTime);
			mCurrentMode = "foreground";
			mCurrentModeTime = now;
		}
	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		long now = Calendar.getInstance().getTime().getTime();
		if ( 0 == mCurrentModeTime )
		{
			mCurrentModeTime = now;
		}
		if (!mCurrentMode.equals("background"))
		{
			mForegroundDuration += (now - mCurrentModeTime);
			mCurrentMode = "background";
			mCurrentModeTime = now;
		}
		
		//	TODO: remove
		if ( true ) {
			int					changes = this.getChangingConfigurations();
			
			if ( changes != 0 ) Log.d( "ccc" , "configurations: " + changes );
		}

		IHRPreferences.commit();
	}
	@Override
	protected void onStop() {
		super.onStop();

	}
	
	public void onDestroy() {
		if(g == null){
			super.onDestroy();
			return;
		}

		try{
		IHRPreferences.commit();
		disconnect();
		
		IHRPlayerClient.shared().onDestroy();
		IHRConfigurationClient.shared().onDestroy();
		
		IHRThreadable.gMain = null;
		g = null;
		super.onDestroy();
		}catch(Exception err)
		{
			System.out.println("Exception err "+err.toString());
		}
	}
	
	public void onServiceConnected( ComponentName inName , IBinder inService ) {
 		if ( inName.getShortClassName().endsWith( "IHRService" ) ) {
			try {
				mConnection = inService;
				mService = ( (IHRService.MyBinder) mConnection).getService();
			} catch (Exception e) {}
			
			IHRPlayerClient.shared().onServiceConnected( inName , inService );
			IHRConfigurationClient.shared().onServiceConnected( inName , inService );
			
			serviceTell( IHRService.kRunInBackground );
		}
	}
	
	public void onServiceDisconnected( ComponentName inName ) {
		if ( inName.getShortClassName().endsWith( "IHRService" ) ) {
			mConnection = null;
			
			IHRPlayerClient.shared().onServiceDisconnected( inName );
			IHRConfigurationClient.shared().onServiceDisconnected( inName );
			
			if ( !isFinishing() ) connect();
		}
	}
	
	public void serviceTell( int inCode , Parcel inSend ) {
		if ( null != mConnection ) try { mConnection.transact( inCode , /*null == inSend ? Parcel.obtain() :*/ inSend , null , IBinder.FLAG_ONEWAY ); } catch ( Exception e ) {}
	}
	
	public void serviceTell( int inCode ) { serviceTell( inCode , Parcel.obtain() ); }
	public void serviceTell( int inCode , int inParameter ) { Parcel p = Parcel.obtain(); p.writeInt( inParameter ); serviceTell( inCode , p ); }
	public void serviceTell( int inCode , String inParameter ) { Parcel p = Parcel.obtain(); p.writeString( inParameter ); serviceTell( inCode , p ); }
	
	private void connect() {
		if ( null == mConnection ) {
			Intent intent = new Intent().setClass( this , IHRService.class );
			
			startService( intent );	//	not balanced by anything
			bindService( intent , this , Context.BIND_AUTO_CREATE );
		}
	}
	
	private void disconnect() {
		if ( null != mConnection ) {
			serviceTell( IHRService.kRunInForeground , 0/* not force */ );
			unbindService( this );
		}
	}
	
	public boolean debugModePermitted() {
		boolean					result = false;
		String					pin = null;
		byte[]					data = null;
		
		pin = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		
		if ( null != pin && 0 != pin.length() ) {
			if ( pin.equals( "000000000000000" /* emulator */ ) || pin.equals( "358279011305826" /* eric */ ) ) {
				result = true;
			} else {
				data = IHRConfigurationFile.fetchFromServer( "debug_android" , "clientType=Android" + "&pin=" + pin );
			}
		}
		
		if ( null != data && 0 != data.length && data[0] == '1' ) {
			result = true;
		}
		
		return result;
	}
}