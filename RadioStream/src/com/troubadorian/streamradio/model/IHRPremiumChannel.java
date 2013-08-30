package com.troubadorian.streamradio.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRVector;


/*
 *  premium{}
 *    channels[]
 *      channel{}
 *        name
 *        description
 *        site
 *        call_letters
 *        logo_url
 *        podcast_url
 *        station_url
 *        validate_url
 *        purchase_url
 *        recovery_url
 *        reminder_url
 *        authenticated_stream
 *        trial_username
 *        trial_password
 *        trial_expiring
 *        tolerance
 *        schedule{}
 *          timezone
 *          days[]
 *            day{}
 *              start
 *              duration
 *              description
 * */

public class IHRPremiumChannel extends ArrayList<String> {
	private static final long	serialVersionUID = 1L;
	
	private static IHRHashtable	sCalendars;
	
	public static final String[]kMap = {
		"name" ,
		"description" ,
		"site" ,
		"call_letters" ,
		"logo_url" ,
		"podcast_url" ,
		"station_url" ,
		"validate_url" ,
		"purchase_url" ,
		"recovery_url" ,
		"reminder_url" ,
		"mediavault_url_droid" ,
		"delegate_url" ,
		"authenticated_rtsp_stream" ,	//	"authenticated_stream" on other platforms
		"sales_pitch" ,
		"trial_username" ,
		"trial_password" ,
		"trial_expiring" ,
		"tolerance" ,
		"timezone" ,
		"day_0_start" ,
		"day_0_duration" ,
		"day_0_description" ,
		"day_1_start" ,
		"day_1_duration" ,
		"day_1_description" ,
		"day_2_start" ,
		"day_2_duration" ,
		"day_2_description" ,
		"day_3_start" ,
		"day_3_duration" ,
		"day_3_description" ,
		"day_4_start" ,
		"day_4_duration" ,
		"day_4_description" ,
		"day_5_start" ,
		"day_5_duration" ,
		"day_5_description" ,
		"day_6_start" ,
		"day_6_duration" ,
		"day_6_description"
	};
	
	//	day 0 is sunday through day 6 is saturday
	
	public static final int		kName = 0;
	public static final int		kDescription = 1;
	public static final int		kSite = 2;
	public static final int		kCallLetters = 3;
	public static final int		kLogoURL = 4;
	public static final int		kPodcastURL = 5;
	public static final int		kStationURL = 6;
	public static final int		kValidateURL = 7;
	public static final int		kPurchaseURL = 8;
	public static final int		kRecoveryURL = 9;
	public static final int		kReminderURL = 10;
	public static final int		kMediavaultURL = 11;
	public static final int		kDelegateURL = 12;
	public static final int		kStreamURL = 13;
	public static final int		kSalesPitch = 14;
	public static final int		kTrialUsername = 15;
	public static final int		kTrialPassword = 16;
	public static final int		kTrialExpiring = 17;
	public static final int		kTolerance = 18;
	public static final int		kTimezone = 19;
	public static final int		kDayStart = 20;
	public static final int		kDayDuration = 21;
	public static final int		kDayDescription = 22;
	
	public static final int		kDays = 7;
	public static final int		kDayFields = 3;
	public static final int		kDayTotal = kDays * kDayFields;
	
	//	kDayXValue = kDayValue + X * kDayFields
	//	X 0 = sunday, 1 = monday, ..., 6 = saturday
	
	public static final int		kCapacity = kDayStart + kDayTotal;
	
	public IHRPremiumChannel() { super(); }
	//**
	public IHRPremiumChannel( Collection<String> inChannel ) { super( inChannel ); }
	public IHRPremiumChannel( String[] inChannel ) { super( Arrays.asList( inChannel ) ); }
	/*/
	public IHRPremiumChannel( List inChannel ) { super(); copyFrom( inChannel , 0 , kCapacity ); }
	public IHRPremiumChannel( String[] inChannel ) { super(); copyFrom( inChannel , 0 , kCapacity ); }
	/**/
	public IHRPremiumChannel( List inKeys , List inValues ) { super(); applyKeysWithValues( inKeys , inValues ); }
	
	public String getSite() { return get( kSite ); }
	public String getName() { return get( kName ); }
	public String getSalesPitch() { return get( kSalesPitch ); }
	public String getTimezone() { return get( kTimezone ); }
	public String getDescription() { return get( kDescription ); }
	public String getLogoURL() { return get( kLogoURL ); }
	public String getPodcastURL() { return get( kPodcastURL ); }
	public String getPurchaseURL() { return get( kPurchaseURL ); }
	public String getLetters() {
		String result = get( kCallLetters );
		
		//	see IHRStation.isPremium
		if ( null == result || 0 == result.length() ) {
			result = "! PRN " + get( kSite );
		}
		
		return result;
	}
	
	
	public static String dateNow() {
		return ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ).format( new Date() );
	}
	
	public static boolean isExpired( String inExpiring ) {
		return !( null == inExpiring || 0 == inExpiring.length() || inExpiring.compareTo( dateNow() ) > 0 );
	}
	
	public String trialUsername() { return get( kTrialUsername ); }
	public String trialPassword() { return get( kTrialPassword ); }
	public String trialExpiring() { return get( kTrialExpiring ); }
	public int tolerance() { String t = get( kTolerance ); return t.length() > 0 ? Integer.parseInt( t ) : 0; }
	public boolean isValid() { return this.size() > kTimezone; }
	public boolean isExpired() { return isExpired( trialExpiring() ); }
	
	public IHRStation getStation() {
		//**
		return new IHRStation( new String[] {
				"site=" + get( kSite ) ,														//	kAdsDartParams
				"0" ,																			//	kAdsDisabled
				getLetters() ,																	//	kCallLetters
				getDescription() ,																//	kDescription
				"1" ,																			//	kDisableSongTagging
				"" ,																			//	kFileArtist
				"" ,																			//	kFileLyricsID
				getName() ,																		//	kFileTitle
				"" ,																			//	kFileURL
				"1" ,																			//	kIsTalk
				get( kLogoURL ) ,																//	kLogoURL
				get( kName ) ,																	//	kName
				getSite() ,																		//	kStationID
				get( kStationURL ) ,															//	kStationURL
				"" ,																			//	kStreamURL
				get( kStreamURL ) ,																//	kStreamURLAuthenticated
				"" ,																			//	kStreamURLFallback
				"" ,																			//	kStreamURLFallbackAuthenticated
				"" ,																			//	kTunerAddress
				"" ,																			//	kVideoURL
				""																				//	kVideoURLLowBandwidth
			} );
		/*/
		IHRStation				result = new IHRStation();
		
		result.mAdsDartParams = "site=" + get( kSite );
		result.mCallLetters = getLetters();
		result.mDisableSongTagging = true;
		result.mName = (String)get( kName );
		result.mDescription = (String)get( kDescription );
		result.mStreamURLAuthenticated = (String)get( kStreamURL );
		result.mIsTalk = true;
		result.mLogoURL = (String)get( kLogoURL );
		result.mStationID = (String)get( kSite );
		result.mStationURL = (String)get( kStationURL );
		
		return result;
		/**/
	}
	
	public IHRStation getStationForItem( IHRPremiumItem inItem ) {
		String					name = inItem.getName();
		String					description = inItem.getDescription();
		String					guid = inItem.getGUID();
		String					url = inItem.getLink();
		
		//**
		return new IHRStation( new String[] {
				"site=" + get( kSite ) ,														//	kAdsDartParams
				"0" ,																			//	kAdsDisabled
				getLetters() + ":" + guid ,														//	kCallLetters
				description ,																	//	kDescription
				"1" ,																			//	kDisableSongTagging
				description ,																	//	kFileArtist
				"" ,																			//	kFileLyricsID
				name ,																			//	kFileTitle
				url ,																			//	kFileURL
				"1" ,																			//	kIsTalk
				get( kLogoURL ) ,																//	kLogoURL
				name ,																			//	kName
				getSite() ,																		//	kStationID
				get( kStationURL ) ,															//	kStationURL
				"" ,																			//	kStreamURL
				"" ,																			//	kStreamURLAuthenticated
				"" ,																			//	kStreamURLFallback
				"" ,																			//	kStreamURLFallbackAuthenticated
				"" ,																			//	kTunerAddress
				"" ,																			//	kVideoURL
				""																				//	kVideoURLLowBandwidth
			} );
		/*/
		IHRStation				result = new IHRStation();
		
		result.mAdsDartParams = "site=" + get( kSite );
		result.mCallLetters = getLetters() + ":" + guid;
		result.mDisableSongTagging = true;
		result.mName = name;
		result.mDescription = description;
		result.mFileArtist = description;
		result.mFileTitle = name;
		result.mFileURL = url;
		result.mIsTalk = true;
		result.mLogoURL = (String)get( kLogoURL );
		result.mStationID = (String)get( kSite );
		result.mStationURL = (String)get( kStationURL );
		
		return result;
		/**/
	}
	
	public Calendar getCalendar( String inName , long inTime ) {
		Calendar				result = null;
		
		if ( null == sCalendars ) sCalendars = new IHRHashtable();
		else result = (Calendar)sCalendars.get( null == inName ? "" : inName );
		
		if ( null == result ) {
			String				name = inName;
			TimeZone			zone = null;
			
	//		String[]			zones_by_id = TimeZone.getAvailableIDs();
	//		String[]			zones_by_offset = TimeZone.getAvailableIDs( -8*60*60*1000 );
			
			if ( null != name && 0 != name.length() ) {
				String[]		old = { "CST" , "EST" , "HST" , "MST" , "PST" };
	//			String[]		now = { "CST6CDT" , "EST5EDT" , "US/Hawaii" , "MST7MDT" , "PST8PDT" };
				String[]		now = { "US/Central" , "US/Eastern" , "US/Hawaii" , "US/Mountain" , "America/Los_Angeles" };
				
				int				index = Arrays.binarySearch( old , name );
				
				if ( index >= 0 ) name = now[index];
				
				zone = TimeZone.getTimeZone( name );
			}
			
			if ( null == zone || 0 == zone.getRawOffset() ) {
				zone = TimeZone.getTimeZone( "America/Los_Angeles" );
			}
			
			if ( null == zone ) result = Calendar.getInstance();
			else result = Calendar.getInstance( zone );
			
			sCalendars.put( null == inName ? "" : inName , result );
		}
		
		if ( inTime > 0 ) result.setTimeInMillis( inTime );
		else result.setTime( new Date() );
		
		return result;
	}
	
	public int minutesToShow( long inTime , String[] outDescription ) {
		int						result = 0;
		String					string;
		
		int						day_of_week , time_of_day;
		int						index , field , found , start , duration;
		
		Calendar				calendar = getCalendar( getTimezone() , inTime );
		
		day_of_week = calendar.get( Calendar.DAY_OF_WEEK ) - 1;
		switch ( calendar.get( Calendar.DAY_OF_WEEK ) ) {
		case Calendar.MONDAY: day_of_week = 1; break;
		case Calendar.TUESDAY: day_of_week = 2; break;
		case Calendar.WEDNESDAY: day_of_week = 3; break;
		case Calendar.THURSDAY: day_of_week = 4; break;
		case Calendar.FRIDAY: day_of_week = 5; break;
		case Calendar.SATURDAY: day_of_week = 6; break;
		case Calendar.SUNDAY: day_of_week = 7; break;
		}
		
		time_of_day = calendar.get( Calendar.HOUR_OF_DAY ) * 60 + calendar.get( Calendar.MINUTE );
		index = ( day_of_week + 6 ) % kDays;	//	start on previous day for shows that span midnight
		
		//	0 = sunday, 1 = monday, ..., 6 = saturday
		if ( 0 == day_of_week ) day_of_week = 7;
		
		do {
			field = ( index % kDays ) * kDayFields;
			
			start = 0;
			string = get( kDayStart + field );
			
			if ( null != string ) {
				found = string.indexOf( ':' );
				
				if ( found > 0 ) {
					start += Integer.parseInt( string.substring( 0 , found ) ) * 60;
				}
				
				if ( string.length() > found + 1 ) {
					start += Integer.parseInt( string.substring( found + 1 ) );
				}
				
				if ( start < 24 && found < 0 ) start *= 60;
			}
			
			duration = 0;
			string = get( kDayDuration + field );
			
			if ( null != string ) {
				found = string.indexOf( ':' );
				
				if ( found > 0 ) {
					duration += Integer.parseInt( string.substring( 0 , found ) ) * 60;
				}
				
				if ( string.length() > found + 1 ) {
					duration += Integer.parseInt( string.substring( found + 1 ) );
				}
				
				if ( duration < 12 && found < 0 ) duration *= 60;
			}
			
			if ( duration < 1 ) {
				//	no information for this day
			} else if ( ( ( index + 1 ) % kDays ) == day_of_week ) {
				//	show started yesterday and still running
				if ( time_of_day + 1440 < start + duration ) {
					result = ( time_of_day + 1440 ) - ( start + duration );
				}
			} else if ( day_of_week < index ) {
				//	show starts in several days
				result = ( index - day_of_week ) * 24 * 60 - time_of_day + start;
			} else if ( time_of_day < start ) {
				//	show starts today
				result = start - time_of_day;
			} else if ( time_of_day < start + duration ) {
				//	show started today and still running
				result = time_of_day - ( start + duration );	//	negative time until show ends
			}
			
			if ( 0 != result ) {
				if ( null != outDescription && outDescription.length > 0 ) {
					outDescription[0] = get( kDayDescription + field );
				}
				
				break;
			}
		} while ( ++index < day_of_week + kDays );
		
		return result;
	}
	
	public String availableText() {
		String					result = "Available";
		int						minutes = minutesToShow( 0 , null );
		int						absolute = ( minutes < 0 ) ? -minutes : minutes;
		
		if ( minutes < 0 ) result += " for ";
		if ( minutes > 0 ) result += " in ";
		
		if ( absolute < 1 ) result += " now";
		else if ( absolute < 2 ) result += "about a minute";
		else if ( absolute < 70 ) result += "" + absolute + " minutes";
		else if ( absolute < 120 ) result += "an hour and " + ( absolute - 60 ) + " minutes";
		else if ( absolute < 1440 ) result += "" + ( absolute / 60 ) + " hours and " + ( absolute % 60 ) + " minutes";
		else if ( absolute < 2880 ) result += "a day";
		else result += "" + ( absolute / 1440 ) + " days";
		
		return result;
	}
	
	public void applyMapValues( Map inMap ) {
		int						index , count;
		String					key , value;
		
		count = kMap.length;
		for ( index = 0 ; index < count ; ++index ) {
			key = kMap[index];
			value = (String)inMap.get( key );
			
			if ( this.size() < index ) {
				this.set( index , ( null == value ) ? "" : value );
			} else {
				this.add( ( null == value ) ? "" : value );
			}
		}
	}
	
	public void applyKeysWithValues( List inKeys , List inValues ) {
		Map						map = new IHRHashtable();
		int						index , count;
		
		count = inKeys.size();
		index = inValues.size();
		if ( count > index ) count = index;
		for ( index = 0 ; index < count ; ++index ) {
			map.put( inKeys.get( index ) , inValues.get( index ) );
		}
		
		this.applyMapValues( map );
	}
	
	public void applyMapFromXML( Map inRSS ) {
		Map						map = (Map)inRSS.get( "schedule" );
		List					days = (List)map.get( "days" );
		String					value = (String)map.get( "timezone" );
		int						index , count = days.size();
		
		this.applyMapValues( inRSS );
		
		if ( null != value ) this.set( kTimezone , value );
		if ( count > 7 ) count = 7;
		
		for ( index = 0 ; index < count ; ++index ) {
			map = (Map)days.get( index );
			
			value = (String)map.get( "start" );
			if ( null != value ) this.set( kDayStart + index * kDayFields , value );
			
			value = (String)map.get( "duration" );
			if ( null != value ) this.set( kDayDuration + index * kDayFields , value );
			
			value = (String)map.get( "description" );
			if ( null != value ) this.set( kDayDescription + index * kDayFields , value );
		}
	}
	
	public static List parseList( List inItems ) {
		IHRVector				result = new IHRVector();
		int						index , count = ( null == inItems ) ? 0 : inItems.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			result.add( new IHRPremiumChannel( (List)inItems.get( index ) ) );
		}
		
		return result;
	}
	
	public static IHRPremiumChannel parseLine( List inKeys , List inLine ) {
		IHRPremiumChannel		result = new IHRPremiumChannel( inKeys , inLine );
		
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
			IHRPremiumChannel	channel = parseLine( inKeys , line );
			
			if ( null != channel ) result.add( channel );
		}
		
		return result;
	}
	
	public static List parsePremiumList( List inList ) {
		IHRVector				result = new IHRVector();
		IHRPremiumChannel		channel;
		
		int						index , count = inList.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			channel = new IHRPremiumChannel();
			channel.applyMapFromXML( (Map)inList.get( index ) );
			
			if ( channel.isValid() ) result.add( channel );
		}
		
		return result;
	}
	
	public static List parseXML( byte[] inXML ) {
		List					result = null;
		IHRXMLPremiumParser		parser = new IHRXMLPremiumParser();
		
		try {
			parser.parse( inXML );
			result = parsePremiumList( (List)parser._premium.get( "channels" ) );
		} catch ( Exception e ) {
//			Log.d( "channel" , "parseXML: " + e.toString() );
		}
		
		return result;
	}
	
	public static List fromString( String inFlattened ) {
		IHRVector				result = new IHRVector();
		IHRPremiumChannel		channel = new IHRPremiumChannel();
		
		int						count = 0;
		int						found , start = 0;
		String					value;
		
		do {
			found = inFlattened.indexOf( '\n' , start );
			value = ( found < 0 ) ? inFlattened.substring( start ) : inFlattened.substring( start , found );
			
			channel.add( value );
			start = found + 1;
			count += 1;
			
			if ( count == kCapacity ) {
				count = 0;
				result.add( channel );
				channel = new IHRPremiumChannel();
			}
		} while ( start > 0 );
		
		return result;
	}
	
	public static String toString( List inChannels ) {
		String					result = new String();
		int						index , count = ( null == inChannels ) ? 0 : inChannels.size();
		
		for ( index = 0 ; index < count ; ++index ) {
			IHRPremiumChannel	channel = (IHRPremiumChannel)inChannels.get( index );
			
			for ( int i = 0 ; i < kCapacity ; ++i ) {
				String			string = channel.get( i );
				
				result = result.concat( string );
				result = result.concat( "\n" );
			}
		}
		
		return result;
	}
	
}
