package com.troubadorian.streamradio.model;

import java.util.TimerTask;

public class IHRTimerTask extends TimerTask {
	public Object						mContext;
	
	public IHRTimerTask() {
		this( null );
	}
	
	public IHRTimerTask( Object context ) {
		mContext = context;
	}
	
	public void run() { }
}
