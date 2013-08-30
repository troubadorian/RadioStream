package com.troubadorian.streamradio.client.model;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.troubadorian.streamradio.controller.Streamradio;
import com.troubadorian.streamradio.model.IHRObject;

public class IHRPreferences extends IHRObject {
	private static SharedPreferences	sShared;
	private static SharedPreferences.Editor sEditor;
	
	public static final String kSeparator = "\t";
	public static final String kPrefix = "_";
	public static final String kPreferencesName = "com.clearchannel.iheratradio";
	
	public static SharedPreferences shared() {
		if ( null == sShared ) sShared = Streamradio.g.getSharedPreferences( kPreferencesName , Context.MODE_PRIVATE );
		
		return sShared;
	}
	
	public static Editor getEditor() {
		if ( null == sEditor ) sEditor = shared().edit();
		
		return sEditor;
	}
	
	public static String joinStrings( String[] a ) { int i = 0 , n = a.length; String s = ( n > 0 ) ? a[0] : null; while ( ++i < n ) { s += kSeparator + a[i]; } return s; }
	
	public static Editor remove( String inKey ) { return getEditor().remove( inKey ); }
	public static Editor clear() { return getEditor().clear(); }
	public static Editor put( String inKey ) { return getEditor().remove( inKey ); }
	public static Editor put( String inKey , int inValue ) { return getEditor().putInt( inKey , inValue ); }
	public static Editor put( String inKey , long inValue ) { return getEditor().putLong( inKey , inValue ); }
	public static Editor put( String inKey , float inValue ) { return getEditor().putFloat( inKey , inValue ); }
	public static Editor put( String inKey , boolean inValue ) { return getEditor().putBoolean( inKey , inValue ); }
	public static Editor put( String inKey , byte[] inValue ) { return getEditor().putString( inKey , new String( inValue ) ); }
	public static Editor put( String inKey , byte[] inValue , String inEncoding ) throws UnsupportedEncodingException { return getEditor().putString( inKey , new String( inValue , inEncoding ) ); }
	public static Editor put( String inKey , String inValue ) { return getEditor().putString( inKey , inValue ); }
	public static Editor put( String inKey , String[] inValue ) { return getEditor().putString( inKey , joinStrings( inValue ) ); }
	public static Editor put( String inKey , Collection<String> inValue ) { return put( inKey , inValue.toArray( new String[inValue.size()] ) ); }
	
	public static boolean commit() { Editor e = sEditor; sEditor = null; return ( null == e ) ? false : e.commit(); }
	public static boolean isset( String inKey ) { return shared().contains( inKey ); }
	public static boolean unset( String inKey ) { put( inKey ); return commit(); }
	public static boolean write( String inKey ) { put( inKey ); return commit(); }
	public static boolean write( String inKey , int inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , long inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , float inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , boolean inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , byte[] inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , byte[] inValue , String inEncoding ) throws UnsupportedEncodingException { put( inKey , inValue , inEncoding ); return commit(); }
	public static boolean writeUTF8( String inKey , byte[] inValue ) throws UnsupportedEncodingException { return write( inKey , inValue , "UTF-8" ); }
	public static boolean write( String inKey , String inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , String[] inValue ) { put( inKey , inValue ); return commit(); }
	public static boolean write( String inKey , Collection<String> inValue ) { put( inKey , inValue ); return commit(); }
	
	public static int get( String inKey , int inMissing ) { return shared().getInt( inKey , inMissing ); }
	public static long get( String inKey , long inMissing ) { return shared().getLong( inKey , inMissing ); }
	public static float get( String inKey , float inMissing ) { return shared().getFloat( inKey , inMissing ); }
	public static boolean get( String inKey , boolean inMissing ) { return shared().getBoolean( inKey , inMissing ); }
	public static String get( String inKey , String inMissing ) { return shared().getString( inKey , inMissing ); }
	public static Object getObject( String inKey ) { return shared().contains( inKey ) ? shared().getAll().get( inKey ) : null; }
	
	public static Collection copyStringsInto( String inKey , List inCollection ) { String[] strings = copyStrings( inKey ); if ( null == strings ) inCollection = null; else inCollection.addAll( Arrays.asList( strings ) ); return inCollection; }
	public static List<String> copyStringList( String inKey ) { return Arrays.asList( copyStrings( inKey ) ); }
	public static String[] copyStrings( String inKey ) { String string = shared().getString( inKey , null ); return ( null == string ) ? null : string.split( kSeparator ); }
	public static byte[] copyBytes( String inKey ) { String string = shared().getString( inKey , null ); return ( null == string ) ? null : string.getBytes(); }
	public static byte[] copyBytes( String inKey , String inEncoding ) throws UnsupportedEncodingException { String string = shared().getString( inKey , null ); return ( null == string ) ? null : string.getBytes( inEncoding ); }
	public static byte[] copyBytesUTF8( String inKey ) throws UnsupportedEncodingException { String string = shared().getString( inKey , null ); return ( null == string ) ? null : string.getBytes( "UTF-8" ); }
	
	/*
	 * long keys support shared code from blackberry
	 * */
	
	public static Editor remove( long inKey ) { return remove( kPrefix + inKey ); }
	public static Editor put( long inKey ) { return put( kPrefix + inKey ); }
	public static Editor put( long inKey , int inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , long inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , float inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , boolean inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , byte[] inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , byte[] inValue , String inEncoding ) throws UnsupportedEncodingException { return put( kPrefix + inKey , inValue , inEncoding ); }
	public static Editor put( long inKey , String inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , String[] inValue ) { return put( kPrefix + inKey , inValue ); }
	public static Editor put( long inKey , Collection<String> inValue ) { return put( kPrefix + inKey , inValue ); }
	
	public static boolean isset( long inKey ) { return isset( kPrefix + inKey ); }
	public static boolean unset( long inKey ) { return unset( kPrefix + inKey ); }
	public static boolean write( long inKey ) { return write( kPrefix + inKey ); }
	public static boolean write( long inKey , int inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , long inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , float inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , boolean inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , byte[] inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , byte[] inValue , String inEncoding ) throws UnsupportedEncodingException { return write( kPrefix + inKey , inValue , inEncoding ); }
	public static boolean writeUTF8( long inKey , byte[] inValue ) throws UnsupportedEncodingException { return writeUTF8( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , String inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , String[] inValue ) { return write( kPrefix + inKey , inValue ); }
	public static boolean write( long inKey , Collection<String> inValue ) { return write( kPrefix + inKey , inValue ); }
	
	public static int get( long inKey , int inMissing ) { return get( kPrefix + inKey , inMissing ); }
	public static long get( long inKey , long inMissing ) { return get( kPrefix + inKey , inMissing ); }
	public static float get( long inKey , float inMissing ) { return get( kPrefix + inKey , inMissing ); }
	public static boolean get( long inKey , boolean inMissing ) { return get( kPrefix + inKey , inMissing ); }
	public static String get( long inKey , String inMissing ) { return get( kPrefix + inKey , inMissing ); }
	public static Object getObject( long inKey ) { return getObject( kPrefix + inKey ); }
	
	public static List<String> copyStringList( long inKey ) { return copyStringList( kPrefix + inKey ); }
	public static String[] copyStrings( long inKey ) { return copyStrings( kPrefix + inKey ); }
	public static byte[] copyBytes( long inKey ) { return copyBytes( kPrefix + inKey ); }
	public static byte[] copyBytes( long inKey , String inEncoding ) throws UnsupportedEncodingException { return copyBytes( kPrefix + inKey ); }
	public static byte[] copyBytesUTF8( long inKey ) throws UnsupportedEncodingException { return copyBytesUTF8( kPrefix + inKey ); }
	
	public static void setBytes( long inKey , byte[] inValue ) { write( inKey , inValue ); }
	public static void setStrings( long inKey , String[] inValue ) { write( inKey , inValue ); }
	public static void setInteger( long inKey , int inValue ) { write( inKey , inValue ); }
	public static void setBoolean( long inKey , boolean inValue ) { write( inKey , inValue ); }
	public static void setString( long inKey , String inValue ) { write( inKey , inValue ); }
	public static void setFloat( long inKey , float inValue ) { write( inKey , inValue ); }
	public static void setLong( long inKey , long inValue ) { write( inKey , inValue ); }
	
	public static byte[] getBytes( long inKey ) { return copyBytes( inKey ); }
	public static String[] getStrings( long inKey ) { return copyStrings( inKey ); }
	public static int getInteger( long inKey ) { return get( inKey , 0 ); }
	public static boolean getBoolean( long inKey ) { return get( inKey , false ); }
	public static String getString( long inKey ) { return get( inKey , "" ); }
	public static float getFloat( long inKey ) { return get( inKey , (float)0 ); }
	public static long getLong( long inKey ) { return get( inKey , 0L ); }
	
}
