package com.troubadorian.streamradio.controller;


import java.util.List;

import android.content.Intent;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class IHRControllerAdapterList extends IHRControllerList implements ListAdapter {
	protected DataSetObservable	mObserveDataSet;
	protected List				mContents;
	
	@Override
	public void onNewIntent( Intent intent ) {
		super.onNewIntent( intent );
		setListAdapter( this );
	}
	
	@Override
	public void onDestroy() {
		if ( null != mObserveDataSet ) {
			mObserveDataSet.unregisterAll();
			mObserveDataSet = null;
		}
		
		super.onDestroy();
	}
	
	public void reloadData() {
		mObserveDataSet.notifyChanged();
//		mObserveDataSet.notifyInvalidated();
	}
	
	@Override
	protected void onListItemClick( ListView inList , View inView , int inPosition , long inID ) {
		
	}
	
	public List getContents() {
		return mContents;
	}
	
	public boolean areAllItemsEnabled() {
		return true;
	}
	
	public boolean isEnabled( int inPosition ) {
		return true;
	}
	
	public int getCount() {
		List					contents = getContents();
		
		return ( null == contents ) ? 0 : contents.size();
	}
	
	public String getString( int inPosition ) {
		return "" + inPosition;
	}
	
	public Object getItem( int inPosition ) {
		List					contents = getContents();
		
		return ( null != contents && inPosition < contents.size() ) ? contents.get( inPosition ) : null;
	}
	
	public long getItemId( int inPosition ) {
		return inPosition;
	}
	
	public int getItemViewType( int inPosition ) {
		return 0;
	}
	
	public View getViewByID( int inID , View inExisting ) {
		return ( null == inExisting ) ? inflateLayout( inID ) : inExisting;
	}
	
	public View getViewForString( String inLabel , View inExisting ) {
		TextView				result = null;
		
		if ( inExisting instanceof TextView ) {
			result = (TextView)inExisting;
		} else {
			result = new TextView( activity() );
			
			result.setLayoutParams( new LayoutParams( ListView.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT , 0 ) );
			result.setTextSize( 24 );
		}
		
		result.setText( inLabel );
		
		return result;
	}
	
	public View getView( int inPosition , View inConvertView , ViewGroup inParent ) {
		return getViewForString( getString( inPosition ) , inConvertView );
	}
	
	public int getViewTypeCount() {
		return 1;
	}
	
	public boolean hasStableIds() {
		return true;
	}
	
	public boolean isEmpty() {
		return false;
	}
	
	public void registerDataSetObserver( DataSetObserver inObserver ) {
		if ( null == mObserveDataSet ) mObserveDataSet = new DataSetObservable();
		
		mObserveDataSet.registerObserver( inObserver );
	}
	
	public void unregisterDataSetObserver( DataSetObserver inObserver ) {
		if ( null != mObserveDataSet ) mObserveDataSet.unregisterObserver( inObserver );
	}
	
}
