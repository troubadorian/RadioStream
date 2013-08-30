package com.troubadorian.streamradio.client.view;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.client.model.IHRCursorFavorites;
import com.troubadorian.streamradio.controller.IHRControllerActivity;

public class IHRViewFavorites extends IHRListView {
	
	public IHRViewFavorites( IHRControllerActivity inContext ) {
		super( inContext );
	}
	
	@Override
	public void addContextMenuItems( ContextMenu menu, ContextMenu.ContextMenuInfo menuInfo ) {
		String									autoplay, callLetters;
		IHRCursorFavorites						cursor;
		AdapterView.AdapterContextMenuInfo		info;
		boolean									isAutoplay;
		MenuItem								item;

		cursor = (IHRCursorFavorites) ((SimpleCursorAdapter) getAdapter()).getCursor();
		info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		callLetters = (String) cursor.mContents.get( info.position );
		
		autoplay = IHRConfigurationClient.shared().getAutoplayStation();
		isAutoplay = ! ( autoplay == null || autoplay.length() == 0 || ! autoplay.equals( callLetters ) );
		
		item = menu.add( isAutoplay ? "Don't play automatically" : "Play automatically" );
		item.setIntent( new Intent().putExtra( "callLetters", callLetters ).putExtra( "action", isAutoplay ? "disable" : "enable" ) );

		item = menu.add( "Play station" );
		item.setIntent( new Intent().putExtra( "callLetters", callLetters ).putExtra( "action", "play" ) );

		item = menu.add( "Delete favorite station" );
		item.setIntent( new Intent().putExtra( "callLetters", callLetters ).putExtra( "action", "delete" ) );
	
		item = menu.add( "Cancel" );
		item.setIntent( new Intent().putExtra( "action", "cancel" ) );
	}
}
