package com.troubadorian.streamradio.controller;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;

public class IHRControllerSettingsAccounts extends IHRControllerSettings {
	protected String			mSite;
	
	public static final String[]kNames = {
		"Sign out" ,
		"Delete All Podcasts"
	};
	
	@Override
	public void onNewIntent( Intent intent ) {
		super.onNewIntent( intent );
		
		mSite = intent.getStringExtra( "site" );
	}
	
	@Override
	public int getCount() {
		return kNames.length;
	}
	
	@Override
	public String getString( int inPosition ) {
		return kNames[inPosition];
	}
	
	@Override
	protected void onListItemClick( ListView inList , View inView , int inPosition , long inID ) {
		IHRConfigurationClient.shared().siteDiscard( mSite , inPosition == 0 );
		
		activity().popToRoot();
	}
}
