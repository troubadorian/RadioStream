package com.troubadorian.streamradio.client.view;

import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.troubadorian.streamradio.controller.IHRControllerActivity;

public class IHRListView extends ListView {
	
	public IHRListView( IHRControllerActivity inContext ) {
		super( inContext );
		
		inContext.registerForContextMenu( this );
	}
	
	public void addContextMenuItems( ContextMenu ioMenu , int inPosition , long inID , View inTarget ) {
		
	}
	
	public void addContextMenuItems( ContextMenu ioMenu , ContextMenu.ContextMenuInfo inDetails ) {
		AdapterView.AdapterContextMenuInfo	details = (AdapterView.AdapterContextMenuInfo)inDetails;
		
		addContextMenuItems( ioMenu , details.position , details.id , details.targetView );
	}
	
}
