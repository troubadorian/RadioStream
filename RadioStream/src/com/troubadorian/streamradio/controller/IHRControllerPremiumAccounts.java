package com.troubadorian.streamradio.controller;

import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.model.IHRPremiumChannel;

public class IHRControllerPremiumAccounts extends IHRControllerAdapterList {
	protected String			mSite;
	protected IHRPremiumChannel	mChannel;
	
	public static final String[]kNames = {
		"I have a existing account" ,
		"I do not have an account" ,
		"Take your favorite personalities with you on the go and never miss a show. Streamradio Premium offers anytime, anywhere access that accommodates your lifestyle." ,
	};
	
	@Override
	public void onNewIntent( Intent intent ) {
		mSite = intent.getStringExtra( "site" );
		mChannel = IHRConfigurationClient.shared().fetchChannel( mSite );
//		mList.addFooterView( getView( 2 , null , null ) );
		mList.setFooterDividersEnabled( false );
		
		super.onNewIntent( intent );
	}
	
	@Override
	public int getCount() {
		return kNames.length;
	}
	
	@Override
	public int getItemViewType( int inPosition ) {
		return ( 2 == inPosition ) ? 1 : 0;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public String getString( int inPosition ) {
		String					result = kNames[inPosition];
		String					string;
		
		if ( 1 != inPosition ) {
			if ( 0 == inPosition ) {
				string = mChannel.getName();
				
				if ( null != string && 0 != string.length() ) result = result.replaceFirst( "existing" , string );
			}
			
			if ( 2 == inPosition ) {
				string = mChannel.getSalesPitch();
				
				if ( null != string && 0 != string.length() ) result = string;
			}
		}
		
		return result;
	}
	
	@Override
	public View getView( int inPosition , View inConvert , ViewGroup inParent ) {
		View					result = null;
		
		if ( inPosition < 2 ) {
			result = getViewByID( R.layout.list_row_single_line , inConvert );
			
			((TextView)result.findViewById( R.id.StationsListRowTextLine1 )).setText( getString( inPosition ) );
			
			assignLayout( result , 52 , 0 );
		} else {
			result = new TextView( activity() );
			
			((TextView)result).setTextSize( TypedValue.COMPLEX_UNIT_PX , 18 );
			((TextView)result).setText( getString( inPosition ) );
			
			assignLayout( result , ViewGroup.LayoutParams.FILL_PARENT , 1 );
		}
		
		return result;
	}
	
	@Override
	protected void onListItemClick( ListView inList , View inView , int inPosition , long inID ) {
		Class					c = null;
		
		switch ( inPosition ) {
		case 0: c = IHRControllerPremiumRegister.class; break;
		case 1: c = IHRControllerPremiumPurchase.class; break;
		}
		
		if ( null != c ) activity().pushControllerIntent( activity().pushing( c , Intent.FLAG_ACTIVITY_CLEAR_TOP ).putExtra( "site" , mSite ) );
	}
}
