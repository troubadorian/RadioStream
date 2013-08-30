package com.troubadorian.streamradio.model;

import com.troubadorian.streamradio.client.model.IHRHashtable;

public interface IHRListener {
	public void listen( String inName , IHRHashtable inDetails );
}
