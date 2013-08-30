package com.troubadorian.streamradio.client.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.troubadorian.streamradio.model.IHRCity;
import com.troubadorian.streamradio.model.IHRFormat;
import com.troubadorian.streamradio.model.IHRLocal;
import com.troubadorian.streamradio.model.IHRPremiumChannel;
import com.troubadorian.streamradio.model.IHRPremiumItem;
import com.troubadorian.streamradio.model.IHRStation;
import com.troubadorian.streamradio.model.IHRUtilities;


public class IHRStationsCursor extends IHRTwoLineCursor {
	
	public static IHROneLineCursor cursorForIntent( Intent intent ) {
		IHROneLineCursor		cursor = null;
		ArrayList<String>		stations = ( null == intent ) ? null : intent.getStringArrayListExtra( "stations" );
		IHRConfigurationClient	client = IHRConfigurationClient.shared();
		
		if ( null == stations ) {
			String				source = ( null == intent ) ? null : intent.getStringExtra( "source" );
			String				name = null;
			
			if ( null == source || source.equals( "primary" ) ) {
				cursor = new IHRPrimaryCursor();
				cursor.setContents( client.featured() );
			} else if ( source.equals( "cities" ) ) {
				cursor = new IHRCitiesCursor();
				cursor.setContents( client.fetchCityNames() );
			} else if ( source.equals( "city" ) ) {
				IHRCity			city = null;
				
				name = intent.getStringExtra( "name" );
				if ( null != name && name.length() > 0 ) city = client.fetchCity( name );
				if ( null == city ) city = client.fetchCity( intent.getIntExtra( "index" , 0 ) );
				
				cursor = new IHRCityCursor();
				cursor.setContents( city );
			} else if ( source.equals( "channels" ) ) {
				cursor = new IHRPremiumCursor();
				cursor.setContents( client.fetchPremiumChannels() );
			} else if ( source.equals( "premium" ) ) {
				cursor = new IHRArchiveCursor( intent.getStringExtra( "site" ) );
			} else if ( source.equals( "premium_accounts" ) ) {
				cursor = new IHRPremiumAccountsCursor( intent.getStringExtra( "site" ) );
			} else if ( source.equals( "premium_register" ) ) {
//				cursor = new IHRPremiumRegisterCursor( intent.getStringExtra( "site" ) );
			} else if ( source.equals( "premium_purchase" ) ) {
//				cursor = new IHRPremiumPurchaseCursor( intent.getStringExtra( "site" ) );
			} else if ( source.equals( "formats" ) ) {
				cursor = new IHRFormatsCursor();
				cursor.setContents( client.fetchFormatNames() );
			} else if ( source.equals( "format" ) ) {
				IHRFormat		format = null;
				
				name = intent.getStringExtra( "name" );
				if ( null != name && name.length() > 0 ) format = client.fetchFormat( name );
				if ( null == format ) format = client.fetchFormat( intent.getIntExtra( "index" , 0 ) );
				
				cursor = new IHRStationsCursor();
				cursor.setContents( format.copyStationList() );
			} else if ( source.equals( "local" ) ) {
				IHRLocal		local = client.fetchLocal();
				
				if ( local != null ) {
					/**
					cursor = new IHRLocalsCursor();
					((IHRLocalsCursor)cursor).setContents( local , ( null == local ) ? null : client.fetchCity( local.getName() ) );
					/*/
					cursor = new IHRStationsCursor();
					cursor.setContents( local.copyStationList() );
					/**/
				}
			} else if ( source.equals( "favorites" ) ) {
				cursor = new IHRCursorFavorites();
				cursor.setContents( client.accessFavorites( 0 ) );
			} else if ( source.equals( "featured" ) ) {
				cursor = new IHRStationsCursor();
				cursor.setContents( client.featured() );
			} else if ( source.equals( "tagged" ) ) {
				cursor = new IHRSongCursor();
				cursor.setContents( client.accessTagged( 0 ) );
			} else {	//	featured
				cursor = new IHRStationsCursor();
				cursor.setContents( null/*client.mStations*/ );
			}
		} else {
			//	local, city, format, personalities, favorites
			cursor = new IHRStationsCursor();
			cursor.setContents( stations );
		}
		
		return cursor;
	}
	
	@Override
	public void prepareIntent( Intent intent , int index ) {
		if ( index >= 0 && index < mCursorCount ) {
			Object				object = mContents.get( index );
			
			if ( object instanceof String ) {
				IHRStation		station = IHRConfigurationClient.shared().stationForCallLetters( (String)object );
				
				if ( null == station ) {
					intent.putExtra( "source" , (String)object );
				} else if ( station.isVideo() ) {
					intent.putExtra( "video" , station.getVideoURL( !IHRUtilities.isUsingWiFi() ) );
				} else {
					intent.putExtra( "station" , station.getCallLetters() );
				}
			} else if ( object instanceof IHRPremiumChannel ) {
				intent.putExtra( "premium" , ((IHRPremiumChannel)object).getSite() );
			} else if ( object instanceof IHRStation ) {
				intent.putExtra( "station" , ((IHRStation)object).getCallLetters() );
			} else if ( object instanceof IHRFormat ) {
				intent.putExtra( "source" , "format" );
				intent.putExtra( "index" , index );
			} else if ( object instanceof IHRCity ) {
				intent.putExtra( "source" , "city" );
				intent.putExtra( "index" , index );
			}
		}
	}
	
	public String getStringForObject( int inColumn , Object inObject ) {
		String					result = null;
		
		if ( inObject instanceof String ) {
			IHRStation		station = IHRConfigurationClient.shared().stationForCallLetters( (String)inObject );
			
			if ( null == station ) {
				result = ( 0 == inColumn ) ? (String)inObject : null;
			} else {
				result = ( 0 == inColumn ) ? station.getName() : station.getDescription();
			}
		} else if ( inObject instanceof IHRPremiumChannel ) {
			result = ( 0 == inColumn ) ? ((IHRPremiumChannel)inObject).getName() : ((IHRPremiumChannel)inObject).getDescription();
		} else if ( inObject instanceof IHRPremiumItem ) {
			result = ( 0 == inColumn ) ? ((IHRPremiumItem)inObject).getName() : ((IHRPremiumItem)inObject).getDescription();
		} else if ( inObject instanceof IHRStation ) {
			result = ( 0 == inColumn ) ? ((IHRStation)inObject).getName() : ((IHRStation)inObject).getDescription();
		} else if ( inObject instanceof IHRFormat ) {
			result = ( 0 == inColumn ) ? ((IHRFormat)inObject).getName() : null;
		} else if ( inObject instanceof IHRCity ) {
			result = ( 0 == inColumn ) ? ((IHRCity)inObject).getName() : null;
		} else if ( inObject instanceof List<?> ) {
			result = ( 0 == inColumn ) ? (String)((List)inObject).get( 0 ) : null;
		}
		
		return result;
	}
	
	@Override
	public String getStringForIndex( int inColumn , int inIndex ) {
		String					result = null;
		
		if ( inIndex >= 0 && inIndex < mCursorCount ) {
			result = getStringForObject( inColumn , mContents.get( inIndex ) );
		}
		
		return result;
	}
	
}
