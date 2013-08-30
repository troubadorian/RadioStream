package com.troubadorian.streamradio.client.model;

import java.util.List;

import android.content.Intent;
import android.util.Log;

import com.troubadorian.streamradio.model.IHRCity;
import com.troubadorian.streamradio.model.IHRStation;

public class IHRCityCursor extends IHRStationsCursor {
	boolean						mHasTraffic;
	
	@Override
	public void setContents( List list ) {
		String					url = (String)list.get( IHRCity.kTrafficURL );
		int						count = ( null == url ) ? 0 : list.size();
		
		mContents = list;
		mHasTraffic = !( null == url || 0 == url.length() );
		mCursorCount = count - IHRCity.kStationList;
		
		if ( mCursorCount < 0 ) mCursorCount = 0;
		if ( mHasTraffic ) mCursorCount += 1;
	}
	
	@Override
	public void prepareIntent( Intent intent , int inIndex ) {
		if ( 0 == inIndex && mHasTraffic ) {
			/**
			IHRStation			station = IHRStation.parseCityTraffic(
				(String)mContents.get( IHRCity.kName ) ,
				(String)mContents.get( IHRCity.kTrafficURL ) );
			
			//	should look up traffic in station list
			
			intent.putStringArrayListExtra( "traffic" , station );
			intent.putExtra( "station" , station.getCallLetters() );
			/*/
			intent.putExtra( "station" , IHRStation.trafficCall( (String)mContents.get( IHRCity.kName ) ) );
			/**/
		} else if ( inIndex >= 0 && inIndex < mCursorCount ) {
			inIndex += IHRCity.kStationList;
			
			// mCursorCount increase put in place to accommodate check in super.prepareIntent()
			// ...unsure of why inIndex needs to be increased by kStationList (Brian Doyle had implemented this)
			mCursorCount += IHRCity.kStationList;
			
			if ( mHasTraffic ) {
				mCursorCount -= 1;
				inIndex -= 1;
			}
			
			super.prepareIntent( intent , inIndex );
		}
	}
	
	@Override
	public String getStringForIndex( int inColumn , int inIndex ) {
		String					result = null;
		
		if ( 0 == inIndex && mHasTraffic ) {
			result = ( 0 == inColumn ) ? IHRStation.trafficName( (String)mContents.get( IHRCity.kName ) ) : IHRStation.kTrafficDetail;
		} else if ( inIndex >= 0 && inIndex < mCursorCount ) {
			inIndex += IHRCity.kStationList;
			
			if ( mHasTraffic ) {
				inIndex -= 1;
			}
			try
			{
			result = getStringForObject( inColumn , mContents.get( inIndex ) );
			}
			catch (Exception ex) {
			    Log.e(this.getClass().getSimpleName(), "------------------------------" + ex.toString());
			}
			
			
//			result = super.getStringForIndex( inColumn , inIndex );
		}
		
		if (result != null) {
		return result;
		}
		else {
		    return "Not Available";
		}
	}
	
}
