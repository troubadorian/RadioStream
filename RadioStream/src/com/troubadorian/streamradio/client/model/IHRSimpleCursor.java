package com.troubadorian.streamradio.client.model;


import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import com.troubadorian.streamradio.model.IHRObject;

public class IHRSimpleCursor extends IHRObject implements Cursor {
	protected int							mCursorIndex = 0;
	protected int							mCursorCount = 0;
	
	protected ContentObservable				mObserveContent;
	protected DataSetObservable				mObserveDataSet;
	
	protected static int getColumnIndexIn( String inName , String[] inNames ) {
		int result = -1;
		int i , c = inNames.length;
		
		for ( i = 0 ; i < c ; ++i ) {
			if ( inNames[i].equals(  inName  ) ) {
				result = i;
				break;
			}
		}
		
		return result;
	}
	
	public void close() {
		mCursorCount = -1;
		
		if ( null != mObserveContent ) {
			mObserveContent.unregisterAll();
			mObserveContent = null;
		}
		
		if ( null != mObserveDataSet ) {
			mObserveDataSet.notifyInvalidated();
			mObserveDataSet.unregisterAll();
			mObserveDataSet = null;
		}
	}
	
	public void deactivate() {
		if ( mCursorCount > 0 ) {
			mCursorCount = 0;
			
			if ( null != mObserveDataSet ) {
				mObserveDataSet.notifyInvalidated();
			}
		}
	}
	
	public void copyStringToBuffer( int arg0 , CharArrayBuffer arg1 ) { /*arg1.data = getString( arg0 ).toCharArray(); arg1.sizeCopied = arg1.data.length;*/ }
	public int getColumnCount() { return 1; }
	public int getColumnIndex( String arg0 ) { return 0; }
	public int getColumnIndexOrThrow( String arg0 ) throws IllegalArgumentException {
		int result = getColumnIndex( arg0 );
		
		//	column "_id" required by SimpleCursorAdapter
		if ( result < 0 && !arg0.equals( "_id" ) ) {
			throw new IllegalArgumentException( "" );
		}
		
		return result;
	}
	public String getColumnName( int arg0 ) { return null; }
	public String[] getColumnNames() { return null; }
	public int getCount() { return mCursorCount; }
	public int getPosition() { return mCursorIndex; }
	public boolean getWantsAllOnMoveCalls() { return false; }
	
	public byte[] getBlob( int arg0 ) { return null; }
	public Bundle getExtras() { return null; }
	public double getDouble( int arg0 ) { return 0; }
	public float getFloat( int arg0 ) { return 0; }
	public int getInt( int arg0 ) { return ( arg0 < 0 ) ? mCursorIndex : 0; }	//	_id
	public long getLong( int arg0 ) { return ( arg0 < 0 ) ? mCursorIndex : 0; }	//	_id
	public short getShort( int arg0 ) { return 0; }
	public String getString( int arg0 ) { return ""; }
	public boolean isNull( int columnIndex ) { return false; }
	
	public boolean isAfterLast() { return mCursorIndex >= mCursorCount; }
	public boolean isBeforeFirst() { return mCursorIndex < 0; }
	public boolean isClosed() { return mCursorCount < 0; }
	public boolean isFirst() { return mCursorIndex == 0; }
	public boolean isLast() { return mCursorIndex + 1 == mCursorCount; }
	
	public boolean move( int offset ) { return moveToPosition( mCursorIndex + offset ); }
	public boolean moveToFirst() { mCursorIndex = 0; return ( mCursorCount > 0 ); }
	public boolean moveToLast() { mCursorIndex = mCursorCount - 1; return ( mCursorCount > 0 ); }
	
	public boolean moveToNext() {
		if ( mCursorIndex < mCursorCount ) {
			mCursorIndex += 1;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean moveToPrevious() {
		if ( mCursorIndex < 0 ) {
			return false;
		} else {
			mCursorIndex -= 1;
			return true;
		}
	}
	
	public boolean moveToPosition( int position ) {
		boolean					result = true;
		
		if ( position > mCursorCount ) {
			position = mCursorCount;
			result = false;
		} else if ( position < -1 ) {
			position = -1;
			result = false;
		}
		
		mCursorIndex = position;
		
		return result;
	}
	
	public void registerContentObserver( ContentObserver observer ) {
		if ( null == mObserveContent ) mObserveContent = new ContentObservable();
		
		mObserveContent.registerObserver( observer );
	}
	
	public void registerDataSetObserver( DataSetObserver observer ) {
		if ( null == mObserveDataSet ) mObserveDataSet = new DataSetObservable();
		
		mObserveDataSet.registerObserver( observer );
	}
	
	public boolean requery() {
		mCursorIndex = 0;
		
		if ( null != mObserveDataSet ) {
			mObserveDataSet.notifyChanged();
		}
		
		return !( mCursorCount < 0 );
	}
	
	public Bundle respond( Bundle extras ) { return Bundle.EMPTY; }
	
	public void setNotificationUri( ContentResolver cr , Uri uri ) {}
	
	public void unregisterContentObserver( ContentObserver observer ) {
		if ( null != mObserveContent ) mObserveContent.unregisterObserver( observer );
	}
	
	public void unregisterDataSetObserver( DataSetObserver observer ) {
		if ( null != mObserveDataSet ) mObserveDataSet.unregisterObserver( observer );
	}
	
}
