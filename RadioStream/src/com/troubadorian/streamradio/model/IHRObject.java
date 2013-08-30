package com.troubadorian.streamradio.model;

import android.util.Log;

public class IHRObject {
	protected boolean					mLogDisabled;
	
	public static synchronized void logBytes( String tag , String prefix , byte[] bytes , int offset , int length ) {
		char			c, raw[], text[];
		int				i, j, n, o;
		String			r, t;
		
		i = ( null == bytes ) ? 0 : bytes.length;
		if ( length < 0 ) length += i;
		if ( offset < 0 ) offset += i;
		if ( offset < 0 ) offset = 0;
		if ( offset + length > i ) length = i - offset;
		
		if ( length <= 0 ) return;
		if ( null == prefix ) prefix = "";
		if ( null == tag ) tag = "Bytes";
		
		j = o = 0;
		raw = new char[ 3 * 16 ];
		text = new char[ 16 ];
		
		for ( ; length > 0; length -= n ) {
			n = length >= 16 ? 16 : length;
			
			for ( i = 0; i < n; ++i, ++j ) {
				if ( ( c = (char) ( bytes[ offset + j ] >> 4 & 0xf ) ) <= 9 ) c += '0'; else c += 'A' - 10;
				raw[ i * 3 ] = c;
				
				if ( ( c = (char) ( bytes[ offset + j ] & 0xf ) ) <= 9 ) c += '0'; else c += 'A' - 10;
				raw[ i * 3 + 1 ] = c;
				
				raw[ i * 3 + 2 ] = ' ';
				
				if ( bytes[ offset + j ] >= ' ' && bytes[ offset + j ] <= '~' ) text[ i ] = (char) bytes[ offset + j ];
				else text[ i ] = '.';
			}
			for ( ; i < 16; ++i ) {
				raw[ i * 3 ] = raw[ i * 3 + 1 ] = raw[ i * 3 + 2 ] = ' ';
				text[ i ] = ' ';
			}
			
			r = new String( raw, 0, 48 );
			t = new String( text, 0, 16 );
			
			o += n;
			
			Log.i( tag , prefix + r + " |" + t + "|  [" + o + "]" );
		}
	}
	
	public String logName() {
		String					result = "";
		int						i;
		
		result = getClass().getName();
		
		if ( ( i = result.lastIndexOf( '.' ) + 1 ) > 0 ) {
			result = result.substring( i );
		}
		
		return result;
	}
	
	public String logName( String method ) {
		String					result = logName();
		
		if ( null != method && method.length() > 0 ) {
			result += "::" + method + "()";
		}
		
		return result;
	}
	
	
	protected void dump( String prefix, byte[] bytes ) { dump( prefix, bytes, 0, bytes.length ); }
	protected void dump( String prefix, byte[] bytes, int length ) { dump( prefix, bytes, 0, length ); }
	protected void dump( String prefix, byte[] bytes, int offset, int length ) {
		if ( logEnabled() && ! mLogDisabled ) {
			logBytes( logName( prefix ) + ": " , "" , bytes , offset , length );
		}
	}

	protected void log( String method, String message ) {
		if ( logEnabled() && ! mLogDisabled ) {
			Log.i( logName( method ) + ": ", message );
		}
	}
	
	protected boolean logEnabled() { return true; }
}
