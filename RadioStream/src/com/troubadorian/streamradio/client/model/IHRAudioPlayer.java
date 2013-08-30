package com.troubadorian.streamradio.client.model;

import com.troubadorian.streamradio.model.IHRAudioPlayerBase;

public class IHRAudioPlayer extends IHRAudioPlayerBase {
	public static IHRAudioPlayer shared() {
		if ( sSingleton == null ) sSingleton = new IHRAudioPlayer();
		
		return (IHRAudioPlayer) sSingleton;
	}
	
	public void start( String contentType ) { super.start( contentType ); }
	public void stop() { super.stop(); }
}
