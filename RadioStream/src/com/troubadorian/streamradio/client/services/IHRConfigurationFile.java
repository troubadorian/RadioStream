package com.troubadorian.streamradio.client.services;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.troubadorian.streamradio.model.IHRCity;
import com.troubadorian.streamradio.model.IHRFormat;
import com.troubadorian.streamradio.model.IHRHTTP;
import com.troubadorian.streamradio.model.IHRObject;
import com.troubadorian.streamradio.model.IHRStation;
import com.troubadorian.streamradio.model.IHRXML;

public class IHRConfigurationFile extends IHRObject {
	public static final String		kVersionPrevious = "<!@>\t";
	public static final String		kVersionModified = "<!=>\t";
	
	protected static final long		kKeyCitiesFile = 0xee7f904f66bed1bL;			// com.clearchannel.iheartradio.IHRConfigFile.cities.file
	protected static final long		kKeyCitiesVersion = 0xe1478180d9107f9eL;		// com.clearchannel.iheartradio.IHRConfigFile.cities.version
	protected static final long		kKeyFormatsFile = 0x5d7a40664f40e06fL;			// com.clearchannel.iheartradio.IHRConfigFile.formats.file
	protected static final long		kKeyFormatsVersion = 0x2ec6bf514db1b646L;		// com.clearchannel.iheartradio.IHRConfigFile.formats.version
	protected static final long		kKeyPremiumFile = 0x5d7a40ec4f40e06fL;			// com.clearchannel.iheartradio.IHRConfigFile.formats.file
	protected static final long		kKeyPremiumVersion = 0x2ec6ec514db1b646L;		// com.clearchannel.iheartradio.IHRConfigFile.formats.version
	protected static final long		kKeyStationListFile = 0xd5b3555da2e5d941L;		// com.clearchannel.iheartradio.IHRConfigFile.station_list.file
	protected static final long		kKeyStationListVersion = 0x547e3060d6e71ca0L;	// com.clearchannel.iheartradio.IHRConfigFile.station_list.version
	protected static final long		kKeyLocalStationsXML = 0xd20bf0494d61727L;		// com.clearchannel.iheartradio.localStationsXML
	
	public static String baseURL( String which ) {
		String					result;
		
		result = IHRXML.kURLBase + IHRXML.sConfigFilesDirectory;
		result += which + ".php";
		
		if ( which.equals( "cities" ) || which.equals( "formats" ) || which.equals( "station_list" ) ) {
			result += "?condense=1&condenseVersion=1";
		}
		
		return result;
	}
	
	public static byte[] access( String which , String version , byte[] data ) {
		byte[]					result = null;
		String					stored = null;
		
		long					keyFile = 0;
		long					keyVersion = 0;
		
		//	"startup" never saved
		
		if ( which.equals( "local_stations" ) ) {
			keyFile = kKeyLocalStationsXML;
			keyVersion = 0x10CA157A71045L;
		} else if ( which.equals( "cities" ) ) {
			keyFile = kKeyCitiesFile;
			keyVersion = kKeyCitiesVersion;
		} else if ( which.equals( "formats" ) ) {
			keyFile = kKeyFormatsFile;
			keyVersion = kKeyFormatsVersion;
		} else if ( which.equals( "premium" ) ) {
			keyFile = kKeyPremiumFile;
			keyVersion = kKeyPremiumVersion;
		} else if ( which.equals( "station_list" ) ) {
			keyFile = kKeyStationListFile;
			keyVersion = kKeyStationListVersion;
		}
		
		if ( 0 == keyFile || null == IHRService.g ) {
			//	nothing saved
		} else if ( null == version ) {
			IHRService.g.preferencesRemove( "_" + keyFile );
			IHRService.g.preferencesRemove( "_" + keyVersion );
			
//			IHRPreferences.remove( keyFile );
//			IHRPreferences.remove( keyVersion );
		} else if ( null == data ) {
			stored = IHRService.g.preferencesGet( "_" + keyVersion , "" );
//			stored = IHRPreferences.getString( keyVersion );
			
			if ( null != stored && ( version.equals( stored ) || version.equals( kVersionPrevious ) || ( version.startsWith( kVersionModified ) && !version.equals( kVersionModified + stored ) ) ) ) {
				result = IHRService.g.preferencesCopyBytes( "_" + keyFile );
//				result = IHRPreferences.getBytes( keyFile );
			}
		} else {
			IHRService.g.preferencesPut( "_" + keyFile , data );
			IHRService.g.preferencesPut( "_" + keyVersion , version );	//	needs commit later
			
//			IHRPreferences.setBytes( keyFile, data );
//			IHRPreferences.setString( keyVersion, version );
		}
		
		return result;
	}
	
	public static byte[] fetchFromServer( String which , String arguments ) {
		byte[]					result = null;
		String					url = baseURL( which );
		
		if ( null != arguments ) url += ( url.indexOf( '?' ) < 0 ? "?" : "&" ) + arguments;
		
//		Log.d( which , url );
		
		try {
			result = IHRHTTP.fetchSynchronous( url );
		} catch ( Exception e ) {}
		
//		IHRObject.logBytes( "fetch" , "" , result , 0 , result.length );
		
		return result;
	}
	
	public static byte[] fetchOrLoad( String which , String version , String arguments ) {
		byte[]					result = ( null == version ) ? null : access( which , version , null );
		
		if ( null == version || !( version.equals( kVersionPrevious ) || version.startsWith( kVersionModified ) ) ) {
			if ( null == result || 0 == result.length ) {
				result = fetchFromServer( which , arguments );
				
				if ( null != result && null != version ) {
					access( which , version , result );
				}
			}
		}
		
		return result;
	}
	
	/*
	public static IHRVector parseText( String which , byte[] data , IHRHashtable inStations ) throws IOException {
		IHRVector				result = null;
		
		if ( which.equals( "cities" ) ) result = parseCities( data , inStations );
		else if ( which.equals( "formats" ) ) result = parseFormats( data , inStations );
		else if ( which.equals( "local_stations" ) ) result = parseLocal( data , inStations );
		else result = parseStationList( data );
		
		return result;
	}
	*/
	
	/*
	 * break into arrays of tab separated fields for each line
	 * */
	public static List<List<String>> bytesToStringArrays( byte[] inData , boolean inCoalesceFields ) throws UnsupportedEncodingException {
		List<List<String>>		result = new ArrayList<List<String>>();
		List<String>			line = new ArrayList<String>();
		String					string;
		int						s , i , n = inData.length;
		byte					c;
		
		s = 0;
		
		for ( i = 0 ; i < n ; ++i ) {
			c = inData[i];
			
			if ( c == '\n' || c == '\t' ) {
				if ( i > s || !inCoalesceFields ) {
					string = new String( inData , s , i - s , "UTF-8" );
					line.add( string );
				}
				
				if ( c == '\n' && line.size() > 0 ) {
					result.add( line );
					line = new ArrayList<String>();
				}
				
				s = i + 1;
			}
		}
		
		if ( null != line && line.size() > 0 ) {
			result.add( line );
		}
		
		return result;
	}
	
	/*
	 * line 1 - number of formats
	 * line X - tab separated fiels, name then stations
	 * */
	public static List<IHRFormat> arrayFromFormats( byte[] inData , Object inStations ) throws UnsupportedEncodingException {
		return IHRFormat.parseLines( bytesToStringArrays( inData , true ) , 1 , inStations );
	}
	
	/*
	 * line 1 - display national cities in cities list
	 * line 2 - display national cities in each city
	 * line 3 - number of formats
	 * line X - tab separated fiels, name, traffic url, then stations
	 * */
	public static List<IHRCity> arrayFromCities( byte[] inData , Object inStations ) throws UnsupportedEncodingException {
		return IHRCity.parseLines( bytesToStringArrays( inData , false ) , 3 , inStations );
	}
	
	/*
	 * line 1 - number of stations
	 * line 2 - tab separated field names
	 * line X - tab separated stations fields
	 * */
	public static List<IHRStation> arrayFromStations( byte[] inData ) throws UnsupportedEncodingException {
		return IHRStation.parseLines( null , bytesToStringArrays( inData , false ) , 2 );
	}
	
	/*
	public static IHRVector parseLocal( byte[] inData , IHRHashtable inStations ) throws IOException {
		IHRVector				result = null;
		
		return result;
	}
	
	public static IHRVector parseCities( byte[] inData , IHRHashtable inStations ) throws IOException {
		IHRVector				result;
		int						capacity, i, j, k, length, n, o, p, start;
		IHRCity					city, national = null;
		int						displayNationalStationsInCitiesList;
		int						displayNationalStationsInEachCity;
		boolean					eol;
		String					name, string, trafficUrl;
		IHRVector				stations;
		
		for ( i = 0, n = inData.length; i < n && inData[ i ] != '\n'; ++i ) ;
		
//		logBytes( "cities" , "" , inData , 0 , inData.length );
		displayNationalStationsInCitiesList = Integer.valueOf( new String( inData, 0, i, "UTF-8" ) ).intValue();
		
		for ( start = ++i; i < n && inData[ i ] != '\n'; ++i ) ;
		
		displayNationalStationsInEachCity = Integer.valueOf( new String( inData, start, i - start, "UTF-8" ) ).intValue();
		
		for ( start = ++i; i < n && inData[ i ] != '\n'; ++i ) ;
		
		capacity = Integer.valueOf( new String( inData, start, i - start, "UTF-8" ) ).intValue();

		result = new IHRVector( capacity );	// Vector of IHRCity
		
		name = null;
		stations = new IHRVector();			// Vector of call_letters
		trafficUrl = null;
		
		for ( eol = false, j = 0, start = ++i; i < n; ++i ) {
			switch ( inData[ i ] ) {
				case '\n':			eol = true;			// fall through
				case '\t': {
					if ( ( length = i - start ) > 0 ) {
						string = new String( inData, start, length, "UTF-8" );
						
						switch ( j ) {
							case 0:		name = string;						break;
							case 1:		trafficUrl = string;				break;
							default:
								if ( null == inStations || null != inStations.get( string ) ) {
									stations.addElement( string );
								}
								break;
						}
					}
					
					++j;
					start = i + 1;
					
					if ( eol ) {
						eol = false;
						j = 0;
						
						try {
							city = new IHRCity( name, trafficUrl, stations );
							if ( national == null && name.toLowerCase().equals( "national" ) ) national = city;
							else result.addElement( city  );
						} catch ( Exception e ) { }
						
						stations = new IHRVector();
					}
				} break;
			}
		}

		if ( null != national ) {
			switch ( displayNationalStationsInEachCity ) {
				case 1: {
					for ( i = 0, n = result.size(); i < n; ++i ) {
						city = (IHRCity) result.elementAt( i );
		
						for ( j = national.mCallLetters.size(); j-- > 0; ) {
							city.mCallLetters.insertElementAt( national.mCallLetters.elementAt( j ), 0 );
						}
					}
				} break;
		
				case 2: {
					for ( i = 0, n = result.size(); i < n; ++i ) {
						city = (IHRCity) result.elementAt( i );
		
						for ( j = 0, o = national.mCallLetters.size(); j < o; ++j ) {
							name = (String) national.mCallLetters.elementAt( j );
		
							for ( k = 0, p = city.mCallLetters.size(); k < p; ++k ) {
								if ( ((String) city.mCallLetters.elementAt( k )).toLowerCase().compareTo( name ) > 0 ) break;
							}
		
							city.mCallLetters.insertElementAt( name, k );
						}
					}
				} break;
		
				case 3: {
					for ( i = 0, n = result.size(); i < n; ++i ) {
						city = (IHRCity) result.elementAt( i );
		
						for ( j = 0, o = national.mCallLetters.size(); j < o; ++j ) {
							city.mCallLetters.addElement( national.mCallLetters.elementAt( j ) );
						}
					}
				} break;
			}
		
			switch ( displayNationalStationsInCitiesList ) {
				case 1:		result.insertElementAt( national, 0 );			break;		// head insert
				case 3:		result.addElement( national );					break;		// tail insert
		
				case 2: {																// alphabetical insert
					for ( i = 0, n = result.size(); i < n; ++i ) {
						if ( ((IHRCity) result.elementAt( i )).mName.toLowerCase().compareTo( "national" ) > 0 ) {
							result.insertElementAt( national, i );
							break;
						}
					}
				} break;
			}
		}
		
		return result;
	}
	
	public static IHRVector parseFormats( byte[] inData , IHRHashtable inStations ) throws IOException {
		IHRVector				result;
		int						capacity, i, j, length, n, start;
		boolean					eol;
		String					name, string;
		IHRVector				stations;
		
		for ( i = 0, n = inData.length; i < n && inData[ i ] != '\n'; ++i ) ;
		
//		logBytes( "formats" , "" , inData , 0 , inData.length );
		capacity = Integer.valueOf( new String( inData, 0, i, "UTF-8" ) ).intValue();

		result = new IHRVector( capacity );	// Vector of IHRFormat
		
		name = null;
		stations = new IHRVector();			// Vector of call_letters
		
		for ( eol = false, j = 0, start = ++i; i < n; ++i ) {
			switch ( inData[ i ] ) {
				case '\n':			eol = true;			// fall through
				case '\t': {
					if ( ( length = i - start ) > 0 ) {
						string = new String( inData, start, length, "UTF-8" );
						
						switch ( j ) {
							case 0:
								name = string;
								break;
							default:
								if ( null == inStations || null != inStations.get( string ) ) {
									stations.addElement( string );
								}
								break;
						}
					}
					
					++j;
					start = i + 1;
					
					if ( eol ) {
						eol = false;
						j = 0;
						try { result.addElement( new IHRFormat( name, stations ) ); } catch ( Exception e ) { }
						stations = new IHRVector();
					}
				} break;
			}
		}
		
		return result;
	}
	
	public static IHRVector parseStationList( byte[] inData ) throws IOException {
		IHRVector				result;
		boolean					eol;
		IHRHashtable			hash;
		int						capacity, i, j, length, n, start;
		String					key, value;
		IHRVector				keyOrder = new IHRVector();
		
		// first line is the number of stations present
		for ( i = 0, n = inData.length; i < n && inData[ i ] != '\n'; ++i ) ;
		
//		logBytes( "stations" , "" , inData , 0 , inData.length );
		capacity = Integer.valueOf( new String( inData, 0, i, "UTF-8" ) ).intValue();
		
		result = new IHRVector( capacity );
		
		// second line is the field order
		processSecondLine: {
			for ( eol = false, start = ++i; i < n; ++i ) {
				switch ( inData[ i ] ) {
					case '\n':		eol = true;			// fall through
					case '\t': {
						// no lines will be empty coming from the xml here
						length = i - start;
						key = new String( inData, start, length, "UTF-8" );
						keyOrder.addElement( key );
						start = i + 1;
						
						if ( eol ) break processSecondLine;
					} break;
				}
			}
		}
		
		// subsequent lines are stations.
		hash = new IHRHashtable();
		
		for ( eol = false, ++i, j = 0; i < n; ++i ) {
			switch ( inData [ i ] ) {
				case '\n':			eol = true;			// fall through
				case '\t': {
					if ( ( length = i - start ) > 0 ) {
						key = (String) keyOrder.elementAt( j );
						value = new String( inData, start, length, "UTF-8" );

						hash.put( key, value );
					}
					
					++j;
					start = i + 1;
					
					if ( eol ) {
						eol = false;
						j = 0;
						try { result.addElement( new IHRStation( hash ) ); } catch ( Exception e ) { }
						hash = new IHRHashtable();
					}
				} break;
			}
		}
		
		return result;
	}
	
	public static IHRHashtable reverseStationList( IHRVector inStations ) {
		IHRHashtable			result = new IHRHashtable();
		IHRStation				station;
		int						i , n = ( null == inStations ) ? 0 : inStations.size();
		
		for ( i = 0 ; i < n ; ++i ) {
			station = (IHRStation)inStations.elementAt( i );
			
			if ( null != station.mCallLetters ) {
				result.put( station.mCallLetters , station );
			}
		}
		
		return result;
	}
	*/
}
