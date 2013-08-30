package com.troubadorian.streamradio.model;

import android.media.MediaPlayer;

abstract public class IHRAudioPlayerBase extends IHRObject {
	protected IHRInputStreamAudio			mInputStream;
	protected MediaPlayer					mPlayer;

	protected static IHRAudioPlayerBase		sSingleton;

	public IHRAudioPlayerBase() {
		mInputStream = new IHRInputStreamAudio();
	}

	
	public IHRInputStreamAudio getInputStream() { return mInputStream; }

	// abstract methods
	
	// derived class must implement:
	//
	// public static IHRAudioPlayer shared(); 

	public void start( String contentType ) {
		mInputStream.start();
	}
	
	public void stop() {
		mInputStream.stop();
	}
}
