package com.troubadorian.streamradio.client.model;

import java.util.List;

import android.content.Context;
import android.widget.SimpleCursorAdapter;

import com.troubadorian.streamradio.controller.R;

public class IHRSongCursor extends IHRTwoLineCursor {
	final int					kResourceID = R.layout.list_row_double_line_tagged;

	@Override
	public void setContents( List list ) {
		mContents = list;	//	list of name/description pairs at alternating indices
		mCursorCount = ( null == list ) ? 0 : list.size() / 2;
	}
	
	@Override
	public String getStringForIndex( int inColumn , int inIndex ) {
		String					result = null;
		
		if ( inIndex >= 0 && inIndex < mCursorCount ) {
			result = ( inColumn < 0 || inColumn > 1 ) ? null : (String)mContents.get( inIndex * 2 + ( inColumn ^ 1 ) );	// this is gross but it works
		}
		
		return result;
	}

	public SimpleCursorAdapter newAdapter( Context inContext ) { return new SimpleCursorAdapter( inContext , kResourceID , this , kColumns , kColumnsID ); }
}
