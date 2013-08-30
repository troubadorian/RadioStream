package com.troubadorian.streamradio.client.model;

public interface IHRRTSPDelegate {
	// these delegate methods may be sent at any time after the stream has been created
	// and no guarantee is made as to what thread they are called on.  thread safety is
	// the responsibility of the callee.
	public void rtspClosed( IHRRTSP rtsp, Exception err );
	
	public void rtspMetadata( IHRRTSP rtsp , IHRHashtable metadata );
	
	public void rtspThroughput( IHRRTSP rtsp, String message );
}
