package com.troubadorian.streamradio.model;

import com.troubadorian.streamradio.client.model.IHRHashtable;

public interface IHRAudioStreamDelegate {
	public void audioStreamConnected( Object context, String url );
	public void audioStreamConnecting( Object context, String url );
	public void audioStreamReceivedMetadata( Object context, IHRHashtable metadata );
	public void audioStreamStopped( Object context, String errorReason );
}
