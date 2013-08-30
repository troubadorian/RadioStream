package com.troubadorian.streamradio.controller;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRPremiumCursor;
import com.troubadorian.streamradio.client.view.IHRListView;
import com.troubadorian.streamradio.model.IHRBroadcaster;
import com.troubadorian.streamradio.model.IHRListener;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumChannels;

public class IHRControllerPremiumChannels extends IHRControllerCursorList implements IHRListener {
	
	@Override
	public ListView createListView() {
		return new IHRChannelsList( activity() );
	}
	
	@Override
	public void beingShown( boolean inShown ) {
		IHRBroadcaster			common = IHRBroadcaster.common();
		
		if ( inShown ) {
			common.listenFor( IHRPremiumChannels.kNotifyNamePremium , this );
		} else {
			common.removeFor( null , this );
		}
		
		super.beingShown( inShown );
	}
	
	public void listen( String inName , IHRHashtable inDetails ) {
		//	premium site rss has changed
		if ( inName.equals( IHRPremiumChannels.kNotifyNamePremium ) || inName.equals( "endingOfflineMode" ) ) {
			mCursor.requery();
		}
	}
	
	public class IHRChannelsList extends IHRListView {
		
		public IHRChannelsList( IHRControllerActivity inContext ) {
			super( inContext );
		}
		
		@Override
		public void addContextMenuItems( ContextMenu ioMenu , int inPosition , long inID , View inTarget ) {
			IHRConfigurationClient	client = IHRConfigurationClient.shared();
			IHRPremiumCursor	cursor = (IHRPremiumCursor)mCursor;
			IHRPremiumChannel	channel = (IHRPremiumChannel)cursor.mContents.get( inPosition );
			String				site = channel.getSite();
			
			MenuItem			item;
			int					size = ioMenu.size();
			
			//	could add generic "Open" option
			
			if ( client.siteHasAuthenticated( site ) ) {
				item = ioMenu.add( "Sign out" );
				item.setIcon( android.R.drawable.ic_media_pause );
				item.setIntent( new Intent().putExtra( "action" , "discard" ).putExtra( "site" , site ).putExtra( "forget" , true ) );
			} else {
				//	could add direct options for have/need credentials
			}
			
			if ( client.siteHasArchives( site ) ) {
				item = ioMenu.add( "Delete All Podcasts" );
				item.setIcon( android.R.drawable.ic_delete );
				item.setIntent( new Intent().putExtra( "action" , "discard" ).putExtra( "site" , site ) );
			}
			
			if ( ioMenu.size() > size ) {
				item = ioMenu.add( "Cancel" );
				item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
				item.setIntent( new Intent().putExtra( "action" , "cancel" ) );
			}
		}
	}
}
