package com.troubadorian.streamradio.client.model;

import android.content.Intent;

import com.troubadorian.streamradio.controller.IHRControllerPremiumPurchase;
import com.troubadorian.streamradio.controller.IHRControllerPremiumRegister;
import com.troubadorian.streamradio.model.IHRPremiumChannel;

public class IHRPremiumAccountsCursor extends IHROneLineCursor {
	public String				mSite;
	
	public IHRPremiumAccountsCursor( String inSite ) {
		IHRConfigurationClient	client = IHRConfigurationClient.shared();
		IHRPremiumChannel		channel = client.fetchChannel( inSite );
		IHRVector				content = new IHRVector();
		String					pitch = channel.getSalesPitch();
		
		if ( null == pitch || 0 == pitch.length() ) {
			pitch = "Take your favorite personalities with you on the go and never miss a show. Streamradio Premium offers anytime, anywhere access that accommodates your lifestyle.";
		}
		
		content.add( "Log in with your " + channel.getName() + " account" );
		content.add( "Sign up for an account" );
		content.add( pitch );
		
		mSite = inSite;
		mContents = content;
		mCursorCount = 2;
	}
	
	@Override
	public void prepareIntent( Intent intent , int inIndex ) {
		String					source , name = "";
		
		if ( inIndex == 0 ) {
			name = IHRControllerPremiumRegister.class.getName();
			source = "premium_register";
		} else {
			name = IHRControllerPremiumPurchase.class.getName();
			source = "premium_purchase";
		}
		
		intent.putExtra( "source" , source );
		intent.putExtra( "class" , name );
		intent.putExtra( "site" , mSite );
	}
	
}
