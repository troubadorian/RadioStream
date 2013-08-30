package com.troubadorian.streamradio.controller;

import java.util.List;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRVector;
import com.troubadorian.streamradio.model.IHRPremiumChannel;

public class IHRControllerSettingsPremium extends IHRControllerSettings {
	
	public IHRVector channelsWithPasswords() {
		IHRVector				result = new IHRVector();
		IHRConfigurationClient	client = IHRConfigurationClient.shared();
		List					channels = client.fetchPremiumChannels();
		
		for ( Object channel : channels ) {
			if ( channel instanceof IHRPremiumChannel ) {
				if ( client.siteHasAuthenticated( ((IHRPremiumChannel)channel).getSite() ) ) {
					result.add( channel );
				}
			}
		}
		
		return result;
	}
	
	@Override
	public List getContents() {
		if ( null == mContents ) {
			mContents = channelsWithPasswords();
		}
		
		return mContents;
	}
	
	@Override
	public int getCount() {
		return getContents().size();
	}
	
	@Override
	public String getString( int inPosition ) {
		return ((IHRPremiumChannel)getItem( inPosition )).getName();
	}
	
	@Override
	protected void onListItemClick( ListView inList , View inView , int inPosition , long inID ) {
		IHRPremiumChannel		channel = (IHRPremiumChannel)getItem( inPosition );
		
		if ( null != channel ) {
			Intent				intent = activity().pushing( IHRControllerSettingsAccounts.class , Intent.FLAG_ACTIVITY_CLEAR_TOP );
			
			intent.putExtra( "site" , channel.getSite() );
			intent.putExtra( "channel" , channel );
			
			activity().pushControllerIntent( intent );
		}
	}
	
}
