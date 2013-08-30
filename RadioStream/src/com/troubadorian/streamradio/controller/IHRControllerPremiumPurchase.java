package com.troubadorian.streamradio.controller;

import java.net.URLEncoder;
import java.util.Date;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.model.IHRHTTP;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumCredentials;


public class IHRControllerPremiumPurchase extends IHRController implements OnClickListener, OnEditorActionListener {
	public static IHRHashtable	sMailed;
	
	IHRPremiumChannel			mChannel;
	Button						mButton;
	EditText					mEdit;
	String						mSite;
	
	@Override
	public void onNewIntent( Intent inParameters ) {
		mSite = inParameters.getStringExtra( "site" );
		mChannel = IHRConfigurationClient.shared().fetchChannel( mSite );
	}
	
	@Override
	public void prepareView() {
		
		setContentView( R.layout.premium_purchase );
		
		mButton = (Button)mContent.findViewById( R.id.PurchaseButton );
		mEdit = (EditText)mContent.findViewById( R.id.PurchaseRequest );
		
		mButton.setOnClickListener( this );
		mEdit.setOnEditorActionListener( this );
	}
	
	public void focus( boolean inFocus ) {
		if ( inFocus ) {
			mEdit.requestFocus();
			showKeyboard( 0 );
		} else {
			mEdit.clearFocus();
			hideKeyboard( 0 );
		}
	}
	
	@Override
	public void finish() {
		focus( false );
		super.finish();
	}
	
	@Override
	public void beingShown( boolean inShown ) {
		super.beingShown( inShown );
		
		if ( !inShown ) focus( false );
	}
	
	@Override
	public void afterShown( boolean inShown ) {
		super.afterShown( inShown );
		
		if ( inShown ) focus( true );
	}
	
	public void onClick( View inView ) {
		if ( mButton == inView ) {
			finish();
			Streamradio.g.pushWebURL( mChannel.getPurchaseURL() );
		}
	}
	
	public void request( String inAddress ) {
		String					url = mChannel.get( IHRPremiumChannel.kDelegateURL );
		String					extras = IHRConfigurationClient.shared().parameters( true );
		
		if ( null == sMailed || !sMailed.containsKey( mSite ) ) {
			url = url + "&command=requestSignupLink&" + extras + "&emailAddress=" + URLEncoder.encode( inAddress );
			
			IHRHTTP.deliver( url, null, IHRPremiumCredentials.kSiteMailerDelegate );
			
			if ( null == sMailed ) sMailed = new IHRHashtable();
			sMailed.put( mChannel.getSite() , new Date() );
		}
		
		activity().displayAlert( "Email sent" );
	}
	
	public boolean onEditorAction( TextView inView , int inAction , KeyEvent inEvent ) {
		boolean					result = false;
		
		if ( mEdit == inView ) {
			if (!validateEmail(inView))
			{
				activity().displayAlert( "Email address required" , 75,  150);
				return false;
			}
			request( inView.getText().toString() );
			finish();
			
			result = true;
		}
		
		return result;
	}
	
	@Override
	public boolean wantsBanner() { return false; }
	
	private boolean validateEmail(TextView inView)
	{
		boolean	result = true;
		
		if (inView.getText().toString().trim().equalsIgnoreCase("") 
				|| !inView.getText().toString().trim().contains("@")
				|| !inView.getText().toString().trim().contains(".")
				)
		{
			result = false;
		}
		
		return result;
	}
}
