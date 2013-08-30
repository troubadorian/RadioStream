package com.troubadorian.streamradio.controller;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


public class IHRControllerSettings extends IHRControllerAdapterList {
	public static final String[]kNames = {
		"Streamradio" ,
		"Premium"
	};
	
	@Override
	public int getCount() {
		return kNames.length;
	}
	
	@Override
	public String getString( int inPosition ) {
		return kNames[inPosition];
	}
	
	@Override
	protected void onListItemClick( ListView inList , View inView , int inPosition , long inID ) {
		Class					c = null;
		
		switch ( inPosition ) {
		case 0: c = IHRControllerSettingsBasic.class; break;
		case 1: c = IHRControllerSettingsPremium.class; break;
		}
		
		if ( null != c ) activity().pushControllerIntent( activity().pushing( c , Intent.FLAG_ACTIVITY_CLEAR_TOP ) );
	}
	
	@Override
	public View getView( int inPosition , View inConvert , ViewGroup inParent ) {
		View					result = getViewByID( R.layout.list_row_single_line , inConvert );
		
		((TextView)result.findViewById( R.id.StationsListRowTextLine1 )).setText( getString( inPosition ) );
		
		assignLayout( result , 52 , 0 );
		
		return result;
	}
	
	//	for identification by IHRViewMain
	public class IHRViewSettings extends ListView {
		public IHRViewSettings( Context context ) { super( context ); }
	}
	
	@Override
	protected ListView createListView() {
		return new IHRViewSettings( activity() );
	}
}
