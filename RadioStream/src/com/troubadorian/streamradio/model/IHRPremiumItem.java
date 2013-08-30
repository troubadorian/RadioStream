package com.troubadorian.streamradio.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRVector;



public class IHRPremiumItem extends ArrayList<String> {
	private static final long	serialVersionUID = 1L;
	
	public static final String[]kMap = {
		"guid" ,
		"link" ,
		"title" ,
		"description" };
	
	public static final int		kGUID = 0;
	public static final int		kLink = 1;
	public static final int		kTitle = 2;
	public static final int		kDescription = 3;
	public static final int		kCapacity = 4;
	
	public IHRPremiumItem() { super(); }
	//**
	public IHRPremiumItem( Collection<String> inItem ) { super( inItem ); }
	public IHRPremiumItem( String[] inItem ) { super( Arrays.asList( inItem ) ); }
	
	public void truncate() { if ( size() > kCapacity ) { removeRange( kCapacity , size() ); trimToSize(); } }
	/*/
	public IHRPremiumItem( List inItem ) { super(); copyFrom( inItem , 0 , kCapacity ); }
	/**/
	public IHRPremiumItem( List inKeys , List inValues ) { super(); applyKeysWithValues( inKeys , inValues ); }
	
	public String getGUID() { return get( kGUID ); }
	public String getLink() { return get( kLink ); }
	public String getName() { return get( kTitle ); }
	public String getDescription() { return get( kDescription ); }
	
	public boolean isValid() { return size() >= kCapacity && getLink().length() > 0; }
	
	public void applyKeysWithValues( List inKeys , List inValues ) {
		Map						map = new IHRHashtable();
		int						index , count;
		String					key , value;
		
		count = inKeys.size();
		index = inValues.size();
		if ( count > index ) count = index;
		for ( index = 0 ; index < count ; ++index ) {
			map.put( inKeys.get( index ) , inValues.get( index ) );
		}
		
		count = kMap.length;
		for ( index = 0 ; index < count ; ++index ) {
			key = kMap[index];
			value = (String)map.get( key );
			
			if ( this.size() < index ) {
				this.set( index , ( null == value ) ? "" : value );
			} else {
				this.add( ( null == value ) ? "" : value );
			}
		}
	}
	
	public void applyRSS( Map inRSS ) {
		String					guid = null , link = null , title = null , description = null;
		
		if ( null == link ) link = (String)inRSS.get( "url" );	//	podcast enclosure url
		
		if ( null == guid ) guid = (String)inRSS.get( "guid" );
		if ( null == link ) link = (String)inRSS.get( "link" );
		if ( null == title ) title = (String)inRSS.get( "title" );
		if ( null == description ) description = (String)inRSS.get( "description" );
		
		if ( null == title ) title = (String)inRSS.get( "itunes:author" );
		if ( null == description ) description = (String)inRSS.get( "itunes:summary" );
		if ( null == description ) description = (String)inRSS.get( "content:encoded" );
		if ( null == guid ) guid = (String)inRSS.get( "link" );
		if ( null == guid ) guid = link;
		
		this.add( guid );
		this.add( link );
		this.add( title );
		this.add( description );
	}
	
	public static List parseList( List inItems ) {
		IHRVector				result = new IHRVector();
		int						index , count = ( null == inItems ) ? 0 : inItems.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			result.add( new IHRPremiumItem( (List)inItems.get( index ) ) );
		}
		
		return result;
	}
	
	public static IHRPremiumItem parseLine( List inKeys , List inLine ) {
		IHRPremiumItem			result = new IHRPremiumItem( inKeys , inLine );
		
		if ( !result.isValid() ) {
			result = null;
		}
		
		return result;
	}
	
	public static List parseLines( List inKeys , List inLines , int inStart ) {
		IHRVector				result = new IHRVector();
		
		int						index , count = inLines.size();
		
		if( null == inKeys && inStart > 1 && count > 1 ) {
			inKeys = (List)inLines.get( 1 );
		}
		
		for ( index = inStart ; index < count ; ++index ) {
			List				line = (List)inLines.get( index );
			IHRPremiumItem		item = parseLine( inKeys , line );
			
			if ( null != item ) result.add( item );
		}
		
		return result;
	}
	
	public static List parseRSS( List inRSS ) {
		IHRVector				result = new IHRVector();
		IHRPremiumItem			item;
		
		int						index , count = inRSS.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			Map					channel = (Map)inRSS.get( index );
			List				items = (List)channel.get( "items" );
			int					entry , limit = items.size();
			
			for ( entry = 0 ; entry < limit ; ++entry ) {
				item = new IHRPremiumItem();
				item.applyRSS( (Map)items.get( entry ) );
				
				if ( item.isValid() ) result.add( item );
			}
		}
		
		return result;
	}
	
	public static List parseXML( byte[] inXML ) {
		List					result = null;
		IHRRSSParser			parser = new IHRRSSParser();
		
		try {
			parser.parse( inXML );
			result = parseRSS( parser._rss );
		} catch ( Exception e ) {}
		
		return result;
	}
	
	public static List fromString( String inFlattened ) {
		IHRVector				result = new IHRVector();
		IHRPremiumItem			item = new IHRPremiumItem();
		
		int						count = 0;
		int						found , start = 0;
		String					value;
		
		do {
			found = inFlattened.indexOf( '\n' , start );
			value = ( found < 0 ) ? inFlattened.substring( start ) : inFlattened.substring( start , found );
			
			item.add( value );
			start = found + 1;
			count += 1;
			
			if ( count == kCapacity ) {
				count = 0;
				result.add( item );
				item = new IHRPremiumItem();
			}
		} while ( start > 0 );
		
		return result;
	}
	
	public static String toString( List inItems ) {
		String					result = new String();
		int						index , count = ( null == inItems ) ? 0 : inItems.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			IHRPremiumItem		item = (IHRPremiumItem)inItems.get( index );
			
			for ( int i = 0 ; i < kCapacity ; ++i ) {
				String			string = item.get( i );
				
				result = result.concat( null == string ? "" : string );
				result = result.concat( "\n" );
			}
		}
		
		return result;
	}
	
}
