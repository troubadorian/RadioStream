package com.troubadorian.streamradio.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
/**
 * 1083 There is a possibility to start two Streamradio applications simultaneously.
 * Streamradio Activity created from non-activity will be forced to use Intent.FLAG_ACTIVITY_NEW_TASK.
 * That means every time user pressed HOME key, the activity will go background and user pressed the app icon again, 
 * a new Streamradio instance will be created. This is a TriggerActivity and it be created from IHRService (from notification) 
 * and trigger the Streamradio (so we can create Streamradio from an activity) and there is never two Streamradio instances existed simultaneously      
 *
 */
public class TriggerActivity extends Activity {
	@Override
	protected void onCreate( Bundle inState ) {
		super.onCreate(inState);
		Intent intent =  new Intent( this , Streamradio.class );

		//If one instance of this application is running, bring it to front
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		startActivity(intent);
		finish();
	}
}
