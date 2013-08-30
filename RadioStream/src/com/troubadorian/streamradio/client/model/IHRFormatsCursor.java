package com.troubadorian.streamradio.client.model;

import android.content.Intent;

public class IHRFormatsCursor extends IHROneLineCursor {
	
	@Override
	public void prepareIntent( Intent intent , int index ) {
		if ( index >= 0 && index < mCursorCount ) {
			//	needs to look up station by letters
			intent.putExtra( "source" , "format" );
			intent.putExtra( "index" , index );
		}
	}
	
	@Override
	public String getString( int arg0 ) {
		String					result = null;
		
		if ( mCursorIndex >= 0 && mCursorIndex < mCursorCount ) {
			result = ( arg0 == 0 ) ? (String)mContents.get( mCursorIndex ) : null;
		}
		
		return result;
	}
}
