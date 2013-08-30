package com.troubadorian.streamradio.model;


import org.xml.sax.Attributes;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRVector;

/*
 *  rss[]
 *    channels[]
 *      channel{}
 *        items[]
 *          item{}
 *            guid
 *            link
 *            title
 *            description
 *            enclosure url
 * */

public class IHRRSSParser extends IHRXMLParser {
	public IHRHashtable			_current;
	public String				_key;
	public String				_value;
	public String				_items;
	
	public IHRVector			_rss;
	public IHRVector			_channel_array;
	public IHRHashtable			_channel;
	public IHRHashtable			_item;
	
	@Override
	public void endElement( String inURL , String inName , String inQualified ) {
		if ( inName.equalsIgnoreCase( "item" ) ) {
			_channel_array.add( _item );
			_current = _channel;
			_item = null;
		} else if ( inName.equalsIgnoreCase( "channel" ) ) {
			_channel.put( null == _items ? "items" : _items , _channel_array );
			_rss.add( _channel );
			
			_channel_array = null;
			_channel = null;
			_current = null;
		} else if ( null != _current && null != _key ) {
			_current.put( _key.toLowerCase() , mContents );
			_key = null;
		}
	}
	
	@Override
	public void startElement( String inURL , String inName , String inQualified , Attributes inAttributes ) {
		boolean					applyAttributes = false;
		
		if ( inName.equalsIgnoreCase( "enclosure" ) ) {
			if ( null != _item ) applyAttributes = true;	//	assume _current is _item
		} else if ( inName.equalsIgnoreCase( "item" ) ) {
			_item = new IHRHashtable();
			_current = _item;
			
			applyAttributes = true;
		} else if ( inName.equalsIgnoreCase( "channel" ) ) {
			_channel_array = new IHRVector();
			_channel = new IHRHashtable();
			_current = _channel;
			
			applyAttributes = true;
		} else if ( inName.equalsIgnoreCase( "rss" ) ) {
			_rss = new IHRVector();
		} else if ( null != _current ) {
			_key = inName;
		}
		
		if ( applyAttributes && null != inAttributes ) {
			int					i , c = inAttributes.getLength();
			
			for ( i = 0 ; i < c ; ++i ) {
				_current.put( inAttributes.getLocalName( i ) , inAttributes.getValue( i ) );
			}
		}
		
		mContents = "";
	}
	
}
