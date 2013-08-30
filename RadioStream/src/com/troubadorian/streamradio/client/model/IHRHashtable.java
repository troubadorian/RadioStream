package com.troubadorian.streamradio.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

public class IHRHashtable extends HashMap<String, Object> {
	protected static final long 		serialVersionUID = 1;
	public static final String			kBundleKeyClass = "\t class \t";
	
	public IHRHashtable() { super(); }
	public IHRHashtable( HashMap<String, Object> map ) { super( map ); }
	public IHRHashtable( String inKey , Object inValue ) { super(); put( inKey , inValue ); }
	public IHRHashtable( String inKey1 , Object inValue1 , String inKey2 , Object inValue2 ) { super(); put( inKey1 , inValue1 ); put( inKey2 , inValue2 ); }
	public IHRHashtable( String[] inKeys , Object[] inValues ) {
		super();
		
		int						index , count = ( null == inKeys ) ? 0 : inKeys.length;
		
		for ( index = 0 ; index < count ; ++index ) {
			put( inKeys[index] , inValues[index] );
		}
	}
	
	public void putFrom( String inKey , IHRHashtable inSource ) {
		Object					value = ( null == inSource ) ? null : inSource.get( inKey );
		
		if ( null == value ) remove( inKey );
		else put( inKey , value );
	}
	
	public boolean booleanValue( String inKey , boolean inMissing ) {
		boolean					result = inMissing;
		Object					value = get( inKey );
		
		if ( null == value ) result = inMissing;
		else if ( value instanceof Boolean ) result = ((Boolean)value).booleanValue();
		else if ( value instanceof Integer ) result = ((Integer)value).intValue() != 0;
		else if ( value instanceof Long ) result = ((Long)value).longValue() != 0;
		else if ( value instanceof String ) result = ((String)value).length() != 0 && !value.equals( "0" );
		else result = !value.equals( Boolean.FALSE );
		
		return result;
	}
	
	public int integerValue( String inKey , int inMissing ) {
		int						result = inMissing;
		Object					value = get( inKey );
		
		if ( null == value ) result = inMissing;
		else if ( value instanceof Boolean ) result = ((Boolean)value).booleanValue() ? 1 : 0;
		else if ( value instanceof String ) try { result = Integer.parseInt( (String)value ); } catch ( Exception e ) {}
		else if ( value instanceof Integer ) result = ((Integer)value).intValue();
		else if ( value instanceof Long ) result = (int)((Long)value).longValue();
		else if ( value instanceof Float ) result = ((Float)value).intValue();
		else if ( value instanceof Double ) result = ((Double)value).intValue();
		
		return result;
	}
	
	public String stringValue( String inKey , String inMissing ) {
		String					result = inMissing;
		Object					value = get( inKey );
		
		if ( null == value ) result = inMissing;
		else if ( value instanceof Boolean ) result = ((Boolean)value).booleanValue() ? "1" : "0";
		else if ( value instanceof String ) result = (String)value;
		else result = value.toString();
		
		return result;
	}
	
	public double doubleValue( String inKey , double inMissing ) {
		double					result = inMissing;
		Object					value = get( inKey );
		
		if ( null == value ) result = inMissing;
		else if ( value instanceof Boolean ) result = ((Boolean)value).booleanValue() ? 1 : 0;
		else if ( value instanceof String ) try { result = Double.parseDouble( (String)value ); } catch ( Exception e ) {}
		else if ( value instanceof Integer ) result = ((Integer)value).doubleValue();
		else if ( value instanceof Long ) result = ((Long)value).doubleValue();
		else if ( value instanceof Float ) result = ((Float)value).doubleValue();
		else if ( value instanceof Double ) result = ((Double)value).doubleValue();
		
		return result;
	}
	
	public long longValue( String inKey , long inMissing ) {
		long					result = inMissing;
		Object					value = get( inKey );
		
		if ( null == value ) result = inMissing;
		else if ( value instanceof Boolean ) result = ((Boolean)value).booleanValue() ? 1 : 0;
		else if ( value instanceof String ) try { result = Long.parseLong( (String)value ); } catch ( Exception e ) {}
		else if ( value instanceof Integer ) result = ((Integer)value).intValue();
		else if ( value instanceof Long ) result = ((Long)value).longValue();
		else if ( value instanceof Float ) result = ((Float)value).longValue();
		else if ( value instanceof Double ) result = ((Double)value).longValue();
		
		return result;
	}
	
	public String flatten( String inDelimiter ) {
		String					result = "";
		
		if ( null == inDelimiter || 0 == inDelimiter.length() ) inDelimiter = "\t";
		
		for ( String key : keySet() ) {
			String				value = stringValue( key , "" );
			
			result = result + key + inDelimiter + value + inDelimiter;
		}
		
		return result;
	}
	
	//	restore string into dictionary of strings
	public int restore( String inDelimiter , String inFlattened , int inStart ) {
		int						found = -1 , start = inStart;
		String					value , key;
		
		if ( null == inDelimiter || 0 == inDelimiter.length() ) inDelimiter = "\t";
		
		for ( ;; ) {
			found = inFlattened.indexOf( inDelimiter , start );
			if ( found < start ) break;
			
			key = inFlattened.substring( start , found );
			
			start = found + inDelimiter.length();
			found = inFlattened.indexOf( inDelimiter , start );
			
			value = ( found < start ) ? inFlattened.substring( start ) : inFlattened.substring( start , found );
			put( key , value );
			
			if ( found < start ) break;
			start = found + inDelimiter.length();
		}
		
		return found;
	}
	
	//	flatten dictionary of strings into string
	public String flatten( String[] inKeys , String inDelimiter ) {
		String					result = "";
		int						index , count = inKeys.length;
		
		if ( null == inDelimiter || 0 == inDelimiter.length() ) inDelimiter = "\t";
		
		for ( index = 0 ; index < count ; ++index ) {
			String				string = stringValue( inKeys[index] , null );
			
			result += ( ( null == string ) ? "" : string ) + inDelimiter;
		}
		
		return result;
	}
	
	//	restore string into dictionary of strings
	public int restore( String[] inKeys , String inDelimiter , String inFlattened , int inStart ) {
		int						index , count = inKeys.length;
		int						found = -1 , start = inStart;
		
		if ( null == inDelimiter || 0 == inDelimiter.length() ) inDelimiter = "\t";
		
		for ( index = 0 ; index < count ; ++index ) {
			found = inFlattened.indexOf( inDelimiter , start );
			
			if ( found < 0 ) put( inKeys[index] , inFlattened.substring( start ) );
			if ( found < start ) break;
			
			put( inKeys[index] , inFlattened.substring( start , found ) );
			
			start = found + inDelimiter.length();
		}
		
		return found;
	}
	
	//	flatten dictionary of dictionaries of strings into string
	public String flattenDDS( String[] inKeys , String inDelimiter ) {
		String					result = "";
		
		if ( null == inDelimiter || 0 == inDelimiter.length() ) inDelimiter = "\t";
		
//**
		for ( String key : this.keySet() ) {
/*/
		for ( Enumeration keys = this.keys() ; keys.hasMoreElements() ; ) {
			String				key = (String)keys.nextElement();
/**/
			IHRHashtable		value = (IHRHashtable)get( key );
			
			result += key + inDelimiter + value.flatten( inKeys , inDelimiter );
		}
		
		return result;
	}
	
	//	restore string into dictionary of dictionaries of strings
	public int restoreDDS( String[] inKeys , String inDelimiter , String inFlattened , int inStart ) {
		int						found = -1 , start = inStart;
		IHRHashtable			value;
		String					key;
		
		if ( null == inDelimiter || 0 == inDelimiter.length() ) inDelimiter = "\t";
		
		for ( ;; ) {
			found = inFlattened.indexOf( inDelimiter , start );
			if ( found < 0 ) break;
			
			key = inFlattened.substring( start , found );
			value = new IHRHashtable();
			found = value.restore( inKeys , inDelimiter , inFlattened , found + inDelimiter.length() );
			put( key , value );
			
			if ( found < 0 ) break;
			start = found + inDelimiter.length();
		}
		
		return found;
	}
	
	
	
	
	public static void assignToIntent( Intent ioIntent , String inKey , Object inValue ) {
		if ( null == inValue )						ioIntent.removeExtra( inKey );
		
		else if ( inValue instanceof Boolean )		ioIntent.putExtra( inKey , ((Boolean)inValue).booleanValue() );
		else if ( inValue instanceof Byte )			ioIntent.putExtra( inKey , ((Byte)inValue).byteValue() );
		else if ( inValue instanceof Short )		ioIntent.putExtra( inKey , ((Short)inValue).shortValue() );
		else if ( inValue instanceof Character )	ioIntent.putExtra( inKey , ((Character)inValue).charValue() );
		else if ( inValue instanceof Integer )		ioIntent.putExtra( inKey , ((Integer)inValue).intValue() );
		else if ( inValue instanceof Long )			ioIntent.putExtra( inKey , ((Long)inValue).longValue() );
		else if ( inValue instanceof Float )		ioIntent.putExtra( inKey , ((Float)inValue).floatValue() );
		else if ( inValue instanceof Double )		ioIntent.putExtra( inKey , ((Double)inValue).doubleValue() );
		else if ( inValue instanceof CharSequence )	ioIntent.putExtra( inKey , (CharSequence)inValue );
		else if ( inValue instanceof String )		ioIntent.putExtra( inKey , (String)inValue );
		
		else if ( inValue instanceof boolean[] )	ioIntent.putExtra( inKey , (boolean[])inValue );
		else if ( inValue instanceof byte[] )		ioIntent.putExtra( inKey , (byte[])inValue );
		else if ( inValue instanceof short[] )		ioIntent.putExtra( inKey , (short[])inValue );
		else if ( inValue instanceof char[] )		ioIntent.putExtra( inKey , (char[])inValue );
		else if ( inValue instanceof int[] )		ioIntent.putExtra( inKey , (int[])inValue );
		else if ( inValue instanceof long[] )		ioIntent.putExtra( inKey , (long[])inValue );
		else if ( inValue instanceof float[] )		ioIntent.putExtra( inKey , (float[])inValue );
		else if ( inValue instanceof double[] )		ioIntent.putExtra( inKey , (double[])inValue );
		else if ( inValue instanceof String[] )		ioIntent.putExtra( inKey , (String[])inValue );
		
		else if ( inValue instanceof ArrayList ) {
			boolean				isStrings = true;
			boolean				isIntegers = true;
			boolean				isParcelables = true;
			int					index , count = ((ArrayList)inValue).size();
			
			for ( index = 0 ; index < count ; ++index ) {
				if ( !( ((ArrayList)inValue).get( index ) instanceof String ) ) isStrings = false;
				if ( !( ((ArrayList)inValue).get( index ) instanceof Integer ) ) isIntegers = false;
				if ( !( ((ArrayList)inValue).get( index ) instanceof Parcelable ) ) isParcelables = false;
				if ( !( isStrings || isIntegers || isParcelables ) ) break;
			}
			
			if ( isStrings ) ioIntent.putStringArrayListExtra( inKey , (ArrayList<String>)inValue );
			else if ( isIntegers ) ioIntent.putIntegerArrayListExtra( inKey , (ArrayList<Integer>)inValue );
			else if ( isParcelables ) ioIntent.putParcelableArrayListExtra( inKey , (ArrayList<Parcelable>)inValue );
			else ioIntent.putExtra( inKey , makeBundle( (ArrayList)inValue , null ) );
		}
		
		else if ( inValue instanceof Map )			ioIntent.putExtra( inKey , makeBundle( (Map)inValue , null ) );
		else if ( inValue instanceof List )			ioIntent.putExtra( inKey , makeBundle( (List)inValue , null ) );
		
		else if ( inValue instanceof Bundle )		ioIntent.putExtra( inKey , (Bundle)inValue );
		else if ( inValue instanceof Parcelable )	ioIntent.putExtra( inKey , (Parcelable)inValue );
		else if ( inValue instanceof Parcelable[] )	ioIntent.putExtra( inKey , (Parcelable[])inValue );
		else if ( inValue instanceof Serializable )	ioIntent.putExtra( inKey , (Serializable)inValue );
	}
	
	public static void assignToBundle( Bundle ioBundle , String inKey , Object inValue ) {
		if ( null == inValue )						ioBundle.remove( inKey );
		
		else if ( inValue instanceof Boolean )		ioBundle.putBoolean( inKey , ((Boolean)inValue).booleanValue() );
		else if ( inValue instanceof Byte )			ioBundle.putByte( inKey , ((Byte)inValue).byteValue() );
		else if ( inValue instanceof Short )		ioBundle.putShort( inKey , ((Short)inValue).shortValue() );
		else if ( inValue instanceof Character )	ioBundle.putChar( inKey , ((Character)inValue).charValue() );
		else if ( inValue instanceof Integer )		ioBundle.putInt( inKey , ((Integer)inValue).intValue() );
		else if ( inValue instanceof Long )			ioBundle.putLong( inKey , ((Long)inValue).longValue() );
		else if ( inValue instanceof Float )		ioBundle.putFloat( inKey , ((Float)inValue).floatValue() );
		else if ( inValue instanceof Double )		ioBundle.putDouble( inKey , ((Double)inValue).doubleValue() );
		else if ( inValue instanceof CharSequence )	ioBundle.putCharSequence( inKey , (CharSequence)inValue );
		else if ( inValue instanceof String )		ioBundle.putString( inKey , (String)inValue );
		
		else if ( inValue instanceof boolean[] )	ioBundle.putBooleanArray( inKey , (boolean[])inValue );
		else if ( inValue instanceof byte[] )		ioBundle.putByteArray( inKey , (byte[])inValue );
		else if ( inValue instanceof short[] )		ioBundle.putShortArray( inKey , (short[])inValue );
		else if ( inValue instanceof char[] )		ioBundle.putCharArray( inKey , (char[])inValue );
		else if ( inValue instanceof int[] )		ioBundle.putIntArray( inKey , (int[])inValue );
		else if ( inValue instanceof long[] )		ioBundle.putLongArray( inKey , (long[])inValue );
		else if ( inValue instanceof float[] )		ioBundle.putFloatArray( inKey , (float[])inValue );
		else if ( inValue instanceof double[] )		ioBundle.putDoubleArray( inKey , (double[])inValue );
		else if ( inValue instanceof String[] )		ioBundle.putStringArray( inKey , (String[])inValue );
		
		else if ( inValue instanceof ArrayList ) {
			boolean				isStrings = true;
			boolean				isIntegers = true;
			boolean				isParcelables = true;
			int					index , count = ((ArrayList)inValue).size();
			
			for ( index = 0 ; index < count ; ++index ) {
				if ( !( ((ArrayList)inValue).get( index ) instanceof String ) ) isStrings = false;
				if ( !( ((ArrayList)inValue).get( index ) instanceof Integer ) ) isIntegers = false;
				if ( !( ((ArrayList)inValue).get( index ) instanceof Parcelable ) ) isParcelables = false;
				if ( !( isStrings || isIntegers || isParcelables ) ) break;
			}
			
			if ( isStrings ) ioBundle.putStringArrayList( inKey , (ArrayList<String>)inValue );
			else if ( isIntegers ) ioBundle.putIntegerArrayList( inKey , (ArrayList<Integer>)inValue );
			else if ( isParcelables ) ioBundle.putParcelableArrayList( inKey , (ArrayList<Parcelable>)inValue );
			else ioBundle.putBundle( inKey , makeBundle( (ArrayList)inValue , null ) );
		}
		
		else if ( inValue instanceof Map )			ioBundle.putBundle( inKey , makeBundle( (Map)inValue , null ) );
		else if ( inValue instanceof List )			ioBundle.putBundle( inKey , makeBundle( (List)inValue , null ) );
		
		else if ( inValue instanceof Bundle )		ioBundle.putBundle( inKey , (Bundle)inValue );
		else if ( inValue instanceof Parcelable )	ioBundle.putParcelable( inKey , (Parcelable)inValue );
		else if ( inValue instanceof Parcelable[] )	ioBundle.putParcelableArray( inKey , (Parcelable[])inValue );
		else if ( inValue instanceof Serializable )	ioBundle.putSerializable( inKey , (Serializable)inValue );
	}
	
	public static Bundle makeBundle( List inList , Bundle ioBundle ) {
		Bundle					result = ( null == ioBundle ) ? new Bundle() : ioBundle;
		int						index , count = inList.size();
		
		result.putString( kBundleKeyClass , inList.getClass().getName() );
		
		for ( index = 0 ; index < count ; ++index ) {
			assignToBundle( result , "" + index , inList.get( index ) );
		}
		
		return result;
	}
	
	public static Bundle makeBundle( Map inMap , Bundle ioBundle ) {
		Bundle					result = ( null == ioBundle ) ? new Bundle() : ioBundle;
		
		result.putString( kBundleKeyClass , inMap.getClass().getName() );
		
		for ( Object key : inMap.keySet() ) {
			assignToBundle( result , "" + key , inMap.get( key ) );
		}
		
		return result;
	}
	
	public static Object scanBundle( Bundle inBundle ) {
		Object					result = null;
		Object					value = ( null == inBundle ) ? null : inBundle.get( kBundleKeyClass );
		
		if ( !( value instanceof String ) ) {
			result = inBundle;
		} else {
			try {
				result = Class.forName( (String)value ).newInstance();
			} catch ( Exception e ) {}
			
			if ( result instanceof List ) {
				List			array = (List)result;
				int				index = 0;
				
				do {
					value = inBundle.get( "" + index );
					
					if ( null == value ) break;
					else if ( value instanceof Bundle ) array.add( scanBundle( (Bundle)value ) );
					else array.add( value );
				} while ( ++index > 0 );
			}
			
			if ( result instanceof Map ) {
				Map				map = (Map)result;
				
				for ( String key : inBundle.keySet() ) {
					value = inBundle.get( key );
					
					if ( null == value ) map.remove( key );
					else if ( value instanceof Bundle ) map.put( key , scanBundle( (Bundle)value ) );
					else map.put( key , value );
				}
			}
		}
		
		return result;
	}
	
	public void assign( Bundle in ) {
		if ( null != in ) {
			for ( String key : in.keySet() ) {
				Object			value = in.get( key );
				
				if ( null == value ) remove( key );
				else if ( value instanceof Bundle ) put( key , scanBundle( (Bundle)value ) );
//				else if ( value instanceof ArrayList ) put( key , new IHRVector( (ArrayList)value ) );
				else put( key , value );
			}
		}
	}
	
	public Bundle bundle( Bundle in ) {
		return makeBundle( this , in );
	}
	
	public Intent intent( Intent in ) {
		Intent					result = ( null == in ) ? new Intent() : in;
		
		for ( String key : this.keySet() ) {
			assignToIntent( result , key , get( key ) );
		}
		
		return result;
	}
	
}
