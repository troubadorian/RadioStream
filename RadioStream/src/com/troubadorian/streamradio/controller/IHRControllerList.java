package com.troubadorian.streamradio.controller;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class IHRControllerList extends IHRController implements OnItemClickListener {
	ListView					mList;
	
	protected ListView createListView() {
		return new ListView( activity() );
	}
	
	protected void prepareListView() {
		if ( null == mList ) {
			mList = createListView();
		}
		
		if ( null != mList ) {
			mList.setLayoutParams( new LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT ) );
			mList.setOnItemClickListener( this );
		}
	}
	
	@Override
	protected void prepareView() {
		prepareListView();
		
		mList.setCacheColorHint( 0xFF000000 );
		mList.setBackgroundColor( 0xFF000000 );
		mList.setItemsCanFocus(false);
		mList.setDrawingCacheBackgroundColor( 0xFF000000 );
		
		setContentView( mList );
	}
	
	public void setListAdapter( ListAdapter inAdapter ) {
		mList.setAdapter( inAdapter );
	}
	
	@Override
	public void onSaveInstanceState( Bundle ioState ) {
		ioState.putParcelable( "list_state" , mList.onSaveInstanceState() );
	}
	
	@Override
	public void onRestoreInstanceState( Bundle inState ) {
		Parcelable				parcel = ( null == inState ) ? null : inState.getParcelable( "list_state" );
		
		if ( null != parcel ) {
			mList.onRestoreInstanceState( parcel );
		}
	}
	
	protected void onListItemClick( ListView inList , View inView , int inPosition , long inID ) {
		
	}
	
	public void onItemClick( AdapterView<?> inList , View inView , int inPosition , long inID ) {
		onListItemClick( (ListView)inList , inView , inPosition , inID );
	}
	
	static public void replaceLayout( View inView , int inViewType ) {
		ViewGroup.LayoutParams	layout = inView.getLayoutParams();
		int						height = ( null == layout ) ? ListView.LayoutParams.WRAP_CONTENT : layout.height;
		int						width = ( null == layout ) ? ListView.LayoutParams.FILL_PARENT : layout.width;
		
		inView.setLayoutParams( new ListView.LayoutParams( width , height , inViewType ) );
	}
	
	static public void assignLayout( View inView , int inHeight , int inViewType ) {
		inView.setLayoutParams( new ListView.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT , inHeight , inViewType ) );
	}
	
}
