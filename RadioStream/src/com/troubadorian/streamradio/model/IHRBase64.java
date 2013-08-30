package com.troubadorian.streamradio.model;

public class IHRBase64 extends IHRObject {
	protected static final byte[]		sBase64DecodeTable = {
		99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,		// 0x00
		99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,		// 0x10
		99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 62, 99, 62, 99, 63,		// 0x20
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 99, 99, 99, 99, 99, 99,		// 0x30
		99,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,		// 0x40
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 99, 99, 99, 99, 63,		// 0x50
		99, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,		// 0x60
		41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 99, 99, 99, 99, 99		// 0x70
	};

	protected static final byte[]		sBase64EncodeTable = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};
	
	protected static final byte[]		sBase64EncodeURLTable = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
	};

	public static final int				kBase64BitsPerChar					= 6;

	public static final int				kBase64EncodingDefault				= 0;
	public static final int				kBase64EncodingCGISafe				= 1;
	public static final int				kBase64EncodingURLAndFilenameSafe	= 2;

	// default interface:
	
	public static byte base64ForBits( byte bits ) { return base64ForBits( bits, kBase64EncodingDefault ); }
	
	public static byte bitsForBase64( byte base64 ) { return sBase64DecodeTable[ base64 ]; }
	
	public static byte[] decode( byte[] base64String ) { return decode( base64String, kBase64EncodingDefault ); }
	
	public static byte[] encode( byte[] data ) { return encode( data, 0, data.length, kBase64EncodingDefault ); }
	public static byte[] encode( byte[] data, int length ) { return encode( data, 0, length, kBase64EncodingDefault ); }
	public static byte[] encode( byte[] data, int offset, int length ) { return encode( data, offset, length, kBase64EncodingDefault ); }

	public static boolean isValidBase64( byte base64 ) { return isValidBase64( base64, kBase64EncodingDefault ); }

	// encoding specific interface
	
	public static byte base64ForBits( byte bits, int base64Encoding ) {
		byte[]					eTable;

		switch ( base64Encoding ) {
			case kBase64EncodingCGISafe:
			case kBase64EncodingURLAndFilenameSafe: {
				eTable = sBase64EncodeURLTable;
			} break;

			default: {
				eTable = sBase64EncodeTable;
			} break;
		}

		return eTable[ bits & 0x3f ];
	}

	public static byte[] decode( byte[] base64String, int base64Encoding ) {
		int						i, j, k, l, n, t;
		byte					pad;
		byte[]					result;
		
		switch ( base64Encoding ) {
			case kBase64EncodingCGISafe:					pad = '.';										break;

			default:										pad = '=';										break;
		}

		if ( base64String == null ) return null;
		if ( ( n = base64String.length ) < 4 ) return null;
		
		j = 3;
		
		if ( base64String[ n - 1 ] == pad ) --j;
		if ( base64String[ n - 2 ] == pad ) --j;

		result = new byte[ 3 * ( ( n -= 4 ) / 4 ) + j ];

		for ( i = k = l = 0; i < n; i += 4 ) {
			t =  sBase64DecodeTable[ base64String[ k++ ] ] << 18;
			t |= sBase64DecodeTable[ base64String[ k++ ] ] << 12;
			t |= sBase64DecodeTable[ base64String[ k++ ] ] << 6;
			t |= sBase64DecodeTable[ base64String[ k++ ] ];

			result[ l++ ] = (byte)( t >> 16 & 0xff );
			result[ l++ ] = (byte)( t >>  8 & 0xff );
			result[ l++ ] = (byte)( t       & 0xff );
		}
		
		switch ( j ) {
			case 1: {
				t =  sBase64DecodeTable[ base64String[ k++ ] ] << 2;
				t |= sBase64DecodeTable[ base64String[ k++ ] ] >> 4;
				
				result[ l ] = (byte)( t & 0xff);
			} break;

			case 2: {
				t =  sBase64DecodeTable[ base64String[ k++ ] ] << 10;
				t |= sBase64DecodeTable[ base64String[ k++ ] ] << 4;
				t |= sBase64DecodeTable[ base64String[ k++ ] ] >> 2;
				
				result[ l++ ] = (byte)( t >> 8 & 0xff );
				result[ l ]   = (byte)( t      & 0xff );
			} break;

			case 3: {
				t =  sBase64DecodeTable[ base64String[ k++ ] ] << 18;
				t |= sBase64DecodeTable[ base64String[ k++ ] ] << 12;
				t |= sBase64DecodeTable[ base64String[ k++ ] ] << 6;
				t |= sBase64DecodeTable[ base64String[ k++ ] ];

				result[ l++ ] = (byte)( t >> 16 & 0xff );
				result[ l++ ] = (byte)( t >> 8  & 0xff );
				result[ l ]   = (byte)( t       & 0xff );
			} break;

			default:			break;
		}
		
		return result;
	}

	public static byte[] encode( byte[] data, int offset, int length, int base64Encoding ) {
		byte[]					eTable, result;
		int						i, j, k, n, t;
		byte					pad;

		if ( data == null ) throw new NullPointerException();
		if ( offset < 0 || length < 0 || offset + length > data.length ) throw new IndexOutOfBoundsException();
		if ( length == 0 ) return new byte[ 0 ];
		
		switch ( base64Encoding ) {
			case kBase64EncodingCGISafe:				eTable = sBase64EncodeURLTable;		pad = '.';		break;
			case kBase64EncodingURLAndFilenameSafe:		eTable = sBase64EncodeURLTable;		pad = '=';		break;

			default:									eTable = sBase64EncodeTable;		pad = '=';		break;
		}

		result = new byte[ 4 * ( ( n = length / 3 ) + ( length % 3 != 0 ? 1 : 0 ) ) ];

		for ( j = offset, i = k = 0; i < n; ++i ) {
			t =  ( data[ j++ ] & 0xff ) << 16;
			t |= ( data[ j++ ] & 0xff ) << 8;
			t |= ( data[ j++ ] & 0xff );

			result[ k++ ] = eTable[ t >> 18 & 0x3f ];
			result[ k++ ] = eTable[ t >> 12 & 0x3f ];
			result[ k++ ] = eTable[ t >>  6 & 0x3f ];
			result[ k++ ] = eTable[ t       & 0x3f ];
		}
		
		if ( ( i = n * 3 ) < length ) {
			t = ( data[ j++ ] & 0xff ) << 16;

			result[ k++ ] = eTable[ t >> 18 & 0x3f ];

			if ( ++i < length ) {
				t |= ( data[ j++ ] & 0xff ) << 8;

				result[ k++ ] = eTable[ t >> 12 & 0x3f ];
				result[ k++ ] = eTable[ t >>  6 & 0x3f ];
			} else {
				result[ k++ ] = eTable[ t >> 12 & 0x3f ];
				result[ k++ ] = pad;
			}
			
			result[ k ] = pad;
		}
		
		return result;
	}
	
	public static boolean isValidBase64( byte base64, int base64Encoding ) {
		if ( base64 >= 'A' && base64 <= 'Z' ) return true;
		if ( base64 >= 'a' && base64 <= 'z' ) return true;
		if ( base64 >= '0' && base64 <= '9' ) return true;
		
		switch ( base64Encoding ) {
			case kBase64EncodingCGISafe: {
				if ( base64 == '-' || base64 == '_' || base64 == '.' ) return true;
			} break;

			case kBase64EncodingURLAndFilenameSafe: {
				if ( base64 == '-' || base64 == '_' || base64 == '=' ) return true;
			} break;

			default: {
				if ( base64 == '+' || base64 == '/' || base64 == '=' ) return true;
			} break;
		}

		return false;
	}
}
