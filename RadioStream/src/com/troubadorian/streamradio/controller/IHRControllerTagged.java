package com.troubadorian.streamradio.controller;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.troubadorian.streamradio.client.view.IHRViewTagged;

public class IHRControllerTagged extends IHRControllerFavorites implements OnClickListener {
	
	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) {
	}
	
	@Override
	protected ListView createListView() {
		return new IHRViewTagged( activity() );
	}
	
	@Override
	protected View createHeaderView() {
		return createHeaderView( R.drawable.favorites_segment_songs_selected );
	}
	
	@Override
	public void onClick( View inView ) {
		((IHRActivity)activity()).pushFavoritesList(false);
		//finish();
	}
	
	@Override
	public boolean onKeyDown( int keyCode , KeyEvent event ) {
		boolean					result = super.onKeyDown( keyCode , event );
		
		if ( false == result ) {
			if ( KeyEvent.KEYCODE_BACK == keyCode ) {
				IHRControllerActivity	activity = activity();
				
				finish();
				
				if ( activity.topController() instanceof IHRControllerFavorites ) {
					activity.topController().finish();
				}
				
				result = true;
			}
		}
		
		return result;
	}
	
}
