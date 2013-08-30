package com.troubadorian.streamradio.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import android.net.ConnectivityManager;

import com.troubadorian.streamradio.client.model.IHRPlatform;

public class IHRUtilities extends IHRObject {
	public static byte[] copyBuffer( byte[] buffer, int offset, int length ) {
		byte[]					copy = new byte[ length ];
		
		System.arraycopy( buffer, offset, copy, 0, length );
		
		return copy;
	}
	
	
	public static String hexify( byte inByte , boolean inCase ) { return hexify( ( inByte & 0x00FF ), inCase ); }
	public static String hexify( int inInteger , boolean inCase ) { return Integer.toHexString( inInteger ); }
	
	public static int osType( String type ) {
		int						result;
		
		result =  ( type.charAt( 0 ) & 0xff ) << 24;
		result |= ( type.charAt( 1 ) & 0xff ) << 16;
		result |= ( type.charAt( 2 ) & 0xff ) <<  8;
		result |= ( type.charAt( 3 ) & 0xff );
		
		return result;
	}

/*
	public static boolean isUsingCarrierDataNetwork() {
		return ConnectivityManager.isNetworkTypeValid( ConnectivityManager.TYPE_MOBILE );
	}
*/
	
	public static boolean isUsingWiFi() {
		return ConnectivityManager.isNetworkTypeValid( ConnectivityManager.TYPE_WIFI );
	}
	
	public static String MD5( String text ) {
		StringBuffer			buffer;
		MessageDigest			digest;
		byte[]					data;
		int						i, n;
		
		buffer = new StringBuffer();

		try {
			digest = MessageDigest.getInstance( "MD5" );
			digest.update( text.getBytes() );
			data = digest.digest();
			
			for ( i = 0, n = data.length; i < n; ++i ) {
				buffer.append( Integer.toHexString( 0xff & data[ i ] ) );
			}
		} catch ( Exception e ) { }
		
		return buffer.toString();
	}
	
	public static byte[] randomUUID() {
		long				n;
		byte[]				result;
		UUID				uuid;
		
		result = new byte[ 16 ];
		uuid = UUID.randomUUID();
		
		n = uuid.getMostSignificantBits();

		result[  0 ] = (byte)( n >> 56 & 0xff );
		result[  1 ] = (byte)( n >> 48 & 0xff );
		result[  2 ] = (byte)( n >> 40 & 0xff );
		result[  3 ] = (byte)( n >> 32 & 0xff );
		result[  4 ] = (byte)( n >> 24 & 0xff );
		result[  5 ] = (byte)( n >> 16 & 0xff );
		result[  6 ] = (byte)( n >>  8 & 0xff );
		result[  7 ] = (byte)( n       & 0xff );
		
		n = uuid.getLeastSignificantBits();

		result[  8 ] = (byte)( n >> 56 & 0xff );
		result[  9 ] = (byte)( n >> 48 & 0xff );
		result[ 10 ] = (byte)( n >> 40 & 0xff );
		result[ 11 ] = (byte)( n >> 32 & 0xff );
		result[ 12 ] = (byte)( n >> 24 & 0xff );
		result[ 13 ] = (byte)( n >> 16 & 0xff );
		result[ 14 ] = (byte)( n >>  8 & 0xff );
		result[ 15 ] = (byte)( n       & 0xff );

		return result;
	}
	
	// support method for blocking-read of arbitrary InputStreams that will return
	// as soon as data arrives, even if the buffer is not completely filled.	
	public static int readInputStream( InputStream input, byte[] buffer, int offset, int length ) throws Exception {
		int						n, o;
		
		if ( buffer == null ) throw new NullPointerException(); 
		if ( offset < 0 || length < 0 || offset + length > buffer.length ) throw new IndexOutOfBoundsException();
		if ( length == 0 ) return 0;
		
		if ( ( n = input.read( buffer, offset++, 1 ) ) < 0 ) return -1;
		
		if ( --length > 0 && ( o = input.available() ) > 0 ) {
			n += input.read( buffer, offset, o > length ? length : o );
		}
		
		return n;
	}

	public static String resolveMediaVault( String inURL , String inUniqueDeviceID , String inMediaVault , String inSite ) throws IOException, InterruptedException {
		HttpURLConnection			resolver;
//		boolean						done = false;
//		int							attempts = 0;
		int							code = 0;
		
		InputStream					stream;
		int							length;
		byte[]						buffer;
		String						string;
		String						url;
		
		if ( null == inMediaVault ) {
			url = inURL;
		} else {
			final String			kLegalese = "NOTICE IS HEREBY GIVEN THAT THIS TEXT AND THE ALGORITHMS USED HEREIN ARE COPYRIGHT 2009 CLEAR CHANNEL BROADCASTING, INCORPORATED AND ARE INTENDED SOLELY FOR USE IN PRODUCTS DEVELOPED AND/OR AUTHORIZED BY CLEAR CHANNEL BROADCASTING, INCORPORATED. ALL OTHER USE IS EXPRESSLY FORBIDDEN. YOUR USE OF THIS TEXT AND/OR ANY ALGORITHM CONTAINED HEREIN IN ANY NON-AUTHORIZED CAPACITY CONSTITUTES ADMITTANCE OF THEFT OF CLEAR CHANNEL BROADCASTING, INCORPORATED'S INTELLECTUAL PROPERTY. VIOLATORS WILL BE PROSECUTED TO THE FULL EXTENT OF THE LAW.";
			String					encoding = "UTF-8";
			String					unique = inUniqueDeviceID == null ? "NO DEVICE ID SPECIFIED" : inUniqueDeviceID;
			long					time = System.currentTimeMillis();
			
			url = inMediaVault;
			
			if ( null == inSite ) {
				length = url.indexOf( "site=" );
				if ( length > 0 ) inSite = url.substring( length + 5 );
				
				if ( null != inSite ) {
					length = inSite.indexOf( '&' );
					if ( length > 0 ) inSite = inSite.substring( 0 , length );
				}
			} else if ( url.indexOf( "site="+inSite ) < 0 ) {
				url = url + ( url.indexOf( "?" ) < 0 ? "?" : "&" ) + "site=" + inSite;
			}
			
			string = kLegalese + " " + unique + " " + inURL + " " + time;
			buffer = string.getBytes( encoding );
			string = IHRPlatform.SHA1String( buffer , 0 , buffer.length );
			
			url = url +
				"&client_id=" + URLEncoder.encode( unique , encoding ) +
				"&request_id=" + URLEncoder.encode( string.toLowerCase() , encoding ) +
				"&timestamp=" + time +
				"&decode_url=" + "1" +
				"&url=" + URLEncoder.encode( inURL , encoding ) +
				"";
		}
		
//		do {
//			System.out.println( "mediavault url is " + url );
		
			resolver = (HttpURLConnection)( new URL( url ) ).openConnection();
//			done = true;
	
			
//			if ( 1 == attempts ) {
				string = IHRPremiumCredentials.shared().credentials( inSite );
				
				if ( null != string && 0 != string.length() ) {
					resolver.setRequestProperty( "Authorization" , string );
				}
//			}
			
			resolver.connect();
			code = resolver.getResponseCode();
			
			if ( HttpURLConnection.HTTP_UNAUTHORIZED == code ) {
//				if ( 1 == ++attempts && null != inSite ) done = false;
			} else if ( HttpURLConnection.HTTP_MOVED_PERM == code || HttpURLConnection.HTTP_MOVED_TEMP == code ) {
				url = resolver.getHeaderField( "Location" );
			} else if ( HttpURLConnection.HTTP_OK == code ) {
				length = resolver.getContentLength();
				stream = resolver.getInputStream();
				while ( stream.available() < length ) Thread.sleep( 100 );
				buffer = new byte[length];
				stream.read( buffer );
				stream.close();
				string = new String( buffer );
				
				if ( null != string ) inURL = string.trim();
			}
			
			resolver.disconnect();
//		} while ( !done );
		
		return inURL;
	}

	
	public static String stringByReplacingString( String source, String search, String replace, boolean replaceAll ) {
		/**
		return ( null == source || null == search || 0 == search.length() ) ? source : replaceAll ?
			source.replaceAll( search , null == replace ) ? "" : replace ) :
			source.replaceFirst( search , null == replace ) ? "" : replace );
		/*/
		if ( source == null ) return null;
		if ( search == null || search.length() == 0 ) return source;
		if ( replace == null ) replace = "";

		String				string = "";
		int					i, j, n, o;

		n = source.length();
		o = search.length();

		for ( i = 0; i < n; ) {
			if ( ( j = source.indexOf( search, i ) ) < 0 ) break;

			string = string.concat( source.substring( i, j ) );
			string = string.concat( replace );
			i += j + o;

			if ( ! replaceAll ) break;
		}

		if ( i < n ) string = string.concat( source.substring( i ) );

		return string;
		/**/
	}
	
	public static int stringToUnsigned( String inString , int inStart ) {
		int						result = 0;
		int						index , count = inString.length();
		
		//	does not account for integer overflow, sign, decimal, or other bases
		for ( index = inStart ; index < count ; ++index ) {
			char				letter = inString.charAt( index );
			
			if ( letter >= '0' && letter <= '9' ) {
				result = ( result * 10 ) + ( letter - '0' );
			} else break;
		}
		
		return result;
	}
	
	public static int stringToInteger( String inString , int inStart ) {
		int						result = 0;
		char					letter = inString.charAt( inStart );
		boolean					negate = false;
		
		if ( letter == '-' ) {
			inStart += 1;
			negate = true;
		} else if ( letter == '+' ) {
			inStart += 1;
		}
		
		result = stringToUnsigned( inString , inStart );
		
		return negate ? -result : result;
	}
	
	public static int stringCompareHonoringUnsigned( String inA , String inB ) {
		int						i , n , an , bn;
		int						ai = 0 , bi = 0;
		char					al , bl;
		
		an = inA.length();
		bn = inB.length();
		n = ( an < bn ) ? an : bn;
		
		for ( i = 0 ; i < n ; ++i ) {
			al = inA.charAt( i );
			bl = inB.charAt( i );
			
			if ( al >= '0' && al <= '9' && bl >= '0' && bl <= '9' ) {
				ai = stringToUnsigned( inA , i );
				bi = stringToUnsigned( inB , i );
				
				break;
			} else if ( al != bl ) {
				break;
			}
		}
		
		return ( ai > bi ) ? 1 : ( ai < bi ) ? -1 : ( i < n ) ? inA.compareTo( inB ) : ( an > bn ) ? 1 : ( an < bn ) ? -1 : 0;
	}
	
	public static String description( Object inValue , String inPrefix , String inSuffix , boolean inSkipFirst ) {
		StringBuilder			result = new StringBuilder();
		
		if ( null == inValue ) {
			result.append( ( inSkipFirst ? "" : inPrefix ) + "<null>" + inSuffix );
		} else if ( inValue instanceof Collection ) {
			result.append( ( inSkipFirst ? "" : inPrefix ) + "(\n" );
			for ( Object value : (Collection)inValue ) {
				result.append( description( value , inPrefix + "\t" , " ,\n" , false ) );
			}
			result.append( inPrefix + ")" + inSuffix + "\n" );
		} else if ( inValue instanceof Map ) {
			result.append( ( inSkipFirst ? "" : inPrefix ) + "{\n" );
			for ( Object key : ((Map)inValue).keySet() ) {
				result.append( description( key , inPrefix + "\t" , "" , false ) + " = " + description( ((Map)inValue).get( key ) , inPrefix + "\t" , " ,\n" , true ) );
			}
			result.append( inPrefix + "}" + inSuffix + "\n" );
		} else if ( inValue instanceof byte[] ) {
			byte[]				bytes = (byte[])inValue;
			
			if ( inSkipFirst ) result.append( "[\n" );
			for ( int i = 0 ; i < bytes.length ; i += 16 ) {
				result.append( inPrefix );
				result.append( Integer.toHexString( i ) );
				result.append( ":" );
				for ( int j = 0 ; j < 16 ; ++j ) result.append( " " + Integer.toHexString( bytes[i] & 0x00FF ) );
				result.append( "\n" );
			}
			result.append( ( inSkipFirst ? "]" : "" ) + inSuffix );
			result.append( "\n" );
		} else {
			result.append( ( inSkipFirst ? "" : inPrefix ) + inValue + inSuffix );
		}
		
		return result.toString();
	}
	
	public static String description( Object inValue ) {
		return description( inValue , "" , "" , false );
	}
}
