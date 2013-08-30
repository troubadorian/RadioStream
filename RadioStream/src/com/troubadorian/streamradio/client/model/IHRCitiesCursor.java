package com.troubadorian.streamradio.client.model;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;

public class IHRCitiesCursor extends IHROneLineCursor {
	
	@Override
	public void prepareIntent( Intent intent , int index ) {
		if ( index >= 0 && index < mCursorCount ) {
			//	needs to look up station by letters
			intent.putExtra( "source" , "city" );
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
	
	public class IHRCitiesAdapter extends SimpleCursorAdapter implements SectionIndexer {
		String[]				mSections;
		int[]					mOffsets;
		
		public IHRCitiesAdapter( Context context , int layout , Cursor c , String[] from , int[] to ) { super( context , layout , c , from , to ); }
		
		public int getPositionForSection( int inSection ) {
			if ( null == mOffsets ) getSections();
			
			if ( inSection < 0 || null == mOffsets || 0 == mOffsets.length ) {
				return 0;
			} else if ( inSection < mOffsets.length ) {
				return mOffsets[inSection];
			} else {
				return mOffsets[mOffsets.length - 1];
			}
		}
		
		public int getSectionForPosition( int inPosition ) {
			if ( null == mOffsets ) getSections();
			
			int						result = mSections.length;
			
			while ( --result > 0 ) {
				if ( mOffsets[result] <= inPosition ) break;
			}
			
			return result;
		}
		
		public int getPositionForLetter( char inLetter ) {
			if ( null == mSections ) getSections();
			
			int						section = mSections.length;
			
			while ( --section > 0 ) {
				if ( mSections[section].charAt( 0 ) <= inLetter ) break;
			}
			
			return getPositionForSection( section );
		}
		
		public Object[] getSections() {
			if ( null == mSections ) {
				int					total = 0;
				int[]				tally = new int[27];
				int[]				where = new int[27];
				List				names = ((IHRCitiesCursor)getCursor()).mContents;
				int					index , count = ( null == names ) ? 0 : names.size();
				
				for ( index = 0 ; index < count ; ++index ) {
					String			name = (String)names.get( index );
					char			letter = name.charAt( 0 );
					int				found = 26;
					
					if ( letter >= 'A' && letter <= 'Z' ) found = letter - 'A';
					else if ( letter >= 'a' && letter <= 'z' ) found = letter - 'a';
					
					if ( tally[found]++ == 0 ) {
						++total;
						where[found] = index;
					}
				}
				
				mSections = new String[total];
				mOffsets = new int[total];
				total = 0;
				
				for ( index = 0 ; index < 27 ; ++index ) {
					if ( tally[index] > 0 ) {
						char[]		text = { (char) ( 'A' + index ) };
						
						mSections[total] = new String( text );
						mOffsets[total] = where[index];
						
						total += 1;
					}
				}
			}
			
			return mSections;
		}
		
		
		
	}
	
	@Override
	public SimpleCursorAdapter newAdapter( Context inContext ) { return new IHRCitiesAdapter( inContext , kResourceID , this , kColumns , kColumnsID ); }
	
}
