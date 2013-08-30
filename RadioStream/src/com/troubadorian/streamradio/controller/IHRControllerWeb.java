package com.troubadorian.streamradio.controller;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

//	TODO: prevent redirects from opening normal browser
public class IHRControllerWeb extends IHRController {
	
	public void prepareView() {
		WebView					web = new WebView( activity() );
		
		web.setId( 236 );	//	for saving state
		web.setSaveEnabled( true );
//		web.setWebViewClient( this );
//		web.setWebChromeClient( this );
		web.setInitialScale( 0 );
		
		setContentView( web );
		
		web.requestFocusFromTouch(); //this is required to allow entering data on a webform.
	}
	
	@Override
	public void onSaveInstanceState( Bundle ioState ) {
		((WebView)mContent).saveState( ioState );
	}
	
	@Override
	public void onCreate( Bundle inState ) {
		super.onCreate( inState );
		
		prepareView();
		
		((WebView)mContent).restoreState( inState );
	}
	
	@Override
	public void onDestroy() {
		((WebView)mContent).stopLoading();
		
		super.onDestroy();
	}
	
	@Override
	public void onNewIntent( Intent intent ) {
		String					url = intent.getStringExtra( "url" );
		String					old = ((WebView)mContent).getOriginalUrl();
		
		if ( null != url && ( null == old || !url.equals( old ) ) ) {
			((WebView)mContent).loadUrl( url );
		}
	}
}
