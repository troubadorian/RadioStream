package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.widget.SeekBar;

public class IHRViewSlider extends SeekBar {
	
	public static void fixBrokenSeekBar( SeekBar ioSeek ) {
		ioSeek.setThumbOffset( ioSeek.getThumbOffset() / 2 + 1 );
	}
	
	public IHRViewSlider( Context inContext ) {
		super( inContext );
		fixBrokenSeekBar( this );
	}
	
}
