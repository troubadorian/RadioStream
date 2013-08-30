package com.troubadorian.streamradio.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.services.IHRServiceConfiguration;
import com.troubadorian.streamradio.model.IHRBroadcaster;
import com.troubadorian.streamradio.model.IHRListener;
import com.troubadorian.streamradio.model.IHRPremiumChannel;


public class IHRControllerPremiumRegister extends IHRController implements OnClickListener, OnEditorActionListener, OnFocusChangeListener, IHRListener {
	IHRPremiumChannel			mChannel;
	EditText					mUsername;
	EditText					mPassword;
	String						mSite;
	Button						mSubmit;
	ProgressBar					mProgress;
	boolean						mValidating;
	
	@Override
	public void onNewIntent( Intent inParameters ) {
		mSite = inParameters.getStringExtra( "site" );
		mChannel = IHRConfigurationClient.shared().fetchChannel( mSite );
	}
	
	@Override
	public void prepareView() {
		
		setContentView( R.layout.premium_register );
		
		mSubmit   = (Button)mContent.findViewById( R.id.RegisterSubmit );
		mUsername = (EditText)mContent.findViewById( R.id.RegisterUsername );
		mPassword = (EditText)mContent.findViewById( R.id.RegisterPassword );
		mProgress = (ProgressBar)mContent.findViewById( R.id.RegisterProgress );
		
		mSubmit.setOnClickListener( this );
		mUsername.setOnFocusChangeListener( this );
		mPassword.setOnFocusChangeListener( this );
		mUsername.setOnEditorActionListener( this );
		mPassword.setOnEditorActionListener( this );
		
		//	could display previous username
	}
	
	public void focus( int inFocus ) {
		if ( inFocus == 0 ) {
			mUsername.clearFocus();
			mPassword.clearFocus();
			hideKeyboard( 0 );
		} else {
			EditText			edit = ( 2 == inFocus ) ? mPassword : mUsername;
			
			edit.requestFocus();
			edit.selectAll();
			showKeyboard( 0 );
		}
	}
	
	@Override
	public void finish() {
		focus( 0 );
		super.finish();
	}
	
	public void handleAcceptance( boolean inAccepted ) {
//		IHRConfigurationClient	client = IHRConfigurationClient.shared();
		
		mValidating = false;
		mProgress.setVisibility( View.GONE );
		
		if ( inAccepted /*client.siteValidate( mSite )*/ ) {
			IHRControllerActivity	activity = activity();
			
			focus( 0 );
			
			//	pop this controller and parent to reach channels then push channel
			activity.popToController( activity.ancestor( this , 2 ) );
			activity.pushControllerIntent( new Intent().putExtra( "class" , IHRControllerPremiumArchives.class.getName() ).putExtra( "site" , mSite ) );
		} else {
//			activity().displayAlert( "Invalid username or passowrd" );
			new IHRAlert( "Invalid username or password" , "OK" , null , null ) {
				@Override
				public void onClick( int inButton ) {
					if ( AlertDialog.BUTTON_POSITIVE == inButton ) focus( 2 );
				}
			};
		}
	}
	
	public void listen( String inName , IHRHashtable inDetails ) {
		if ( inName.equals( IHRServiceConfiguration.kNotifyNameSiteValidated ) ) {
			if ( mSite.equals( inDetails.stringValue( "site" , "" ) ) ) {
				handleAcceptance( inDetails.booleanValue( "accepted" , false ) );
			}
		}
	}
	
	@Override
	public void beingShown( boolean inShown ) {
		super.beingShown( inShown );
		
		if ( inShown ) {
			IHRBroadcaster.common().listenFor( IHRServiceConfiguration.kNotifyNameSiteValidated , this );
		} else {
			IHRBroadcaster.common().removeFor( IHRBroadcaster.kRemoveEveryName , this );
			
			focus( 0 );
		}
	}
	
	@Override
	public void afterShown( boolean inShown ) {
		super.afterShown( inShown );
		
		if ( inShown ) focus( 1 );
	}
	
	public void verifyCredentials() {
		IHRConfigurationClient	client = IHRConfigurationClient.shared();
		
		mValidating = true;
		mProgress.setVisibility( View.VISIBLE );
		
		client.siteAuthenticate( mSite , mUsername.getText().toString() , mPassword.getText().toString() );
		client.siteValidate( mSite );
		
		focus( 0 );
	}
	
	public void warnRequired( boolean inPassword ) {
		activity().displayAlert( "" + ( inPassword ? "password" : "username" ) + " required" );
	}
	
	public void onClick( View inView ) {
		if ( 0 == mUsername.getText().length() ) {
			warnRequired( false );
		} else if ( 0 == mPassword.getText().length() ) {
			warnRequired( true );
		} else {
			verifyCredentials();
		}
	}
	
	public boolean onEditorAction( TextView inView , int inAction , KeyEvent inEvent ) {
		boolean					result = false;
		boolean					propose = false;
		
		/*
		String[]				actions = { "unspecified" , "none" , "go" , "search" , "send" , "next" , "dont" };
		String[]				events = { "down" , "up" , "multiple" , "unknown" };
		
		Log.d( "$$ edit" , ( inView != mUsername ? inView == mPassword ? "password" : "unknown" : "username" ) + " " + actions[inAction] + " " + events[( null == inEvent ? 3 : inEvent.getAction() )] + " " + mUsername.getText().toString() + ":" + mPassword.getText().toString() );
		*/
		
		if ( null != inEvent && KeyEvent.ACTION_DOWN == inEvent.getAction() ) {
			result = true;
		} else if ( mValidating ) {
			result = true;
		} else if ( 0 == inView.getText().length() ) {
			warnRequired( inView == mPassword );
			
			result = true;
		} else if ( mUsername == inView ) {
			if ( mPassword.getText().length() > 0 ) {
				propose = true;
			} else {
				mPassword.requestFocus();
			}
			
			result = true;
		} else if ( mPassword == inView ) {
			if ( mUsername.getText().length() > 0 ) {
				propose = true;
			} else {
				mUsername.requestFocus();
			}
			
			result = true;
		}
		
		if ( propose ) {
			verifyCredentials();
		}
		
		return result;
	}
	
	public void onFocusChange( View inView , boolean inFocused ) {
		if ( null == inView ) {
			
		} else if ( mUsername == inView ) {
			mUsername.setImeActionLabel( null , mPassword.getText().length() > 0 ? EditorInfo.IME_ACTION_SEND : EditorInfo.IME_ACTION_NEXT );
		} else if ( mPassword == inView ) {
			mUsername.setImeActionLabel( null , mUsername.getText().length() > 0 ? EditorInfo.IME_ACTION_SEND : EditorInfo.IME_ACTION_NEXT );
		}
	}
	
	@Override
	public boolean wantsBanner() { return false; }
}
