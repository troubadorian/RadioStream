package com.troubadorian.streamradio.client.model;

import java.util.List;

import com.troubadorian.streamradio.model.IHRCity;
import com.troubadorian.streamradio.model.IHRLocal;

public class IHRLocalsCursor extends IHRCityCursor {
	public final static String	kLoading = "Loading Local Stations...";
	
	@Override
	public void setContents( List list ) {
		mContents = list;
		mHasTraffic = false;
		mCursorCount = ( null == list ) ? 1 : list.size() - IHRLocal.kStationList;
	}
	
	public void setContents( IHRLocal inLocal , IHRCity inCity ) {
		setContents( inLocal );
		
		if ( null != inLocal && null != inCity ) {
			String				url = inCity.getTrafficURL();
			
			if ( null != url && url.length() > 24 ) {
				mContents.set( IHRCity.kTrafficURL , url );	//	kTrafficURL == kDistance
				mHasTraffic = true;
				mCursorCount += 1;
			}
		}
	}
	
	@Override
	public String getStringForIndex( int inColumn , int inIndex ) {
		String					result = null;
		
		if ( null == mContents ) {
			result = ( inColumn == 0 ) ? kLoading : null;
		} else {
			result = super.getStringForIndex( inColumn , inIndex );
		}
		
		return result;
	}
	
}
