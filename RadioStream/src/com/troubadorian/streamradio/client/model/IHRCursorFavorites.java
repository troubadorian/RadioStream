package com.troubadorian.streamradio.client.model;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

 import com.troubadorian.streamradio.controller.R;

public class IHRCursorFavorites extends IHRStationsCursor {
	final int					kResourceID = R.layout.list_row_double_line_favorites;

	public SimpleCursorAdapter newAdapter( Context inContext ) { return new IHRFavoritesCursorAdapter( inContext , kResourceID , this , kColumns , kColumnsID ); }

	protected class IHRFavoritesCursorAdapter extends SimpleCursorAdapter {
		public IHRFavoritesCursorAdapter( Context context, int layout, Cursor cursor, String[] from, int[] to ) {
			super( context, layout, cursor, from, to );
		}
		
		public View getView( int position, View convertView, ViewGroup parent ) {
			String					autoplay, callLetters;
			boolean					isAutoplay;
			View					result;
			
			result = super.getView( position, convertView, parent );
		
			callLetters = (String) mContents.get( position );	
			
			autoplay = IHRConfigurationClient.shared().getAutoplayStation();
			isAutoplay = ! ( autoplay == null || autoplay.length() == 0 || ! autoplay.equals( callLetters ) );
			
			if ( isAutoplay ) {
				result.findViewById( R.id.AutoplayTriangle ).setVisibility( View.VISIBLE );
			}
			
			return result;
		}
	}
}
