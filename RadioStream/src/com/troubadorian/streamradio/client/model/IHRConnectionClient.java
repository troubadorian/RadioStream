package com.troubadorian.streamradio.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.troubadorian.streamradio.client.services.IHRService;
import com.troubadorian.streamradio.controller.Streamradio;

public class IHRConnectionClient extends BroadcastReceiver implements ServiceConnection {
	private IBinder				mConnection;
	
	public void debugLog( String inMethod , String inMessage ) {
		Log.d( "==> " + inMethod , inMessage );
	}
	
	public boolean isConnected() {
		return ( null != mConnection );
	}
	
	public Parcel emptyParcel() {
		return Parcel.obtain();
	}
	
	public Parcel parcel( String inString ) {
		Parcel result = Parcel.obtain(); result.writeString( inString ); return result;
	}
	
	public Parcel parcel( int inValue ) {
		Parcel result = Parcel.obtain(); result.writeInt( inValue ); return result;
	}
	
	public Parcel parcel( long inValue ) {
		Parcel result = Parcel.obtain(); result.writeLong( inValue ); return result;
	}
	
	public Parcel parcel( float inValue ) {
		Parcel result = Parcel.obtain(); result.writeFloat( inValue ); return result;
	}
	
	public Parcel parcel( boolean inValue ) {
		Parcel result = Parcel.obtain(); result.writeInt( inValue ? 1 : 0 ); return result;
	}
	
	public Parcel parcel( String[] inValue ) {
		Parcel result = Parcel.obtain(); result.writeStringArray( inValue ); return result;
	}
	
	public Parcel parcel( List<String> inValue ) {
		Parcel result = Parcel.obtain(); result.writeStringList( inValue ); return result;
	}
	
	public Parcel serviceFetch( int inCode , Parcel inSend ) throws RemoteException {
		Parcel						result = null;
		
		if ( null != mConnection ) {
//			debugLog( "serviceFetch" , "enter " + inCode );
			
			result = Parcel.obtain();
			
			mConnection.transact( inCode , inSend , result , 0 );
			
//			debugLog( "serviceFetch" , "leave " + inCode );
		}
		
		return result;
	}
	
	public void serviceTell( int inCode , Parcel inSend ) {
		if ( null != mConnection ) try {
//			debugLog( "serviceTell" , "enter " + inCode );
			mConnection.transact( inCode , /*null == inSend ? Parcel.obtain() :*/ inSend , null , IBinder.FLAG_ONEWAY );
			
//			debugLog( "serviceTell" , "leave " + inCode );
		} catch ( Exception e ) {}
	}
	
	public void serviceTell( int inCode ) { serviceTell( inCode , emptyParcel() ); }
	public void serviceTell( int inCode , int inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	public void serviceTell( int inCode , float inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	public void serviceTell( int inCode , long inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	public void serviceTell( int inCode , boolean inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	public void serviceTell( int inCode , String inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	public void serviceTell( int inCode , String[] inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	public void serviceTell( int inCode , List<String> inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	
//	public void serviceTell( int inCode , String inKey , String[] inParameter ) { serviceTell( inCode , parcel( inParameter ) ); }
	
	
	
	
	
	
	public String serviceFetchString( int inCode , Parcel inSend ) throws RemoteException {
		return serviceFetch( inCode , inSend ).readString();
	}
	
	public Object serviceFetchValue( int inCode , Parcel inSend ) throws RemoteException {
		return serviceFetch( inCode , inSend ).readValue( null );
	}
	
	public byte[] serviceFetchBytes( int inCode , Parcel inSend ) {
		byte[]					result = null;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).createByteArray(); } catch ( Exception e ) {}
		
		return result;
	}
	
	public float serviceFetchFloat( int inCode , Parcel inSend ) {
		float					result = 0;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).readFloat(); } catch ( Exception e ) {}
		
		return result;
	}

	public int serviceFetchInteger( int inCode , Parcel inSend ) {
		int						result = 0;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).readInt(); } catch ( Exception e ) {}
		
		return result;
	}
	
	public int[] serviceFetchIntegers( int inCode , Parcel inSend ) {
		int[]					result = null;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).createIntArray(); } catch ( Exception e ) {}
		
		return result;
	}
	
	public String serviceFetchString( int inCode , Parcel inSend , String inMissing ) {
		String					result = null;
		
		if ( null != mConnection ) try { result = serviceFetchString( inCode , inSend ); } catch ( Exception e ) {}
		
		return result;
	}
	
	public Map serviceFetchMap( int inCode , Parcel inSend ) {
		Map						result = null;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).readHashMap( null ); } catch ( Exception e ) {}
		
		return result;
	}
	
	public List serviceFetchList( int inCode , Parcel inSend ) {
		List					result = null;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).readArrayList( null ); } catch ( Exception e ) {}
		
		return result;
	}
	
	public ArrayList<String> serviceFetchStrings( int inCode , Parcel inSend ) {
		ArrayList<String>		result = null;
		
		if ( null != mConnection ) try { result = serviceFetch( inCode , inSend ).createStringArrayList(); } catch ( Exception e ) {}
		
		return result;
	}
	
	public Collection serviceFetchStringsInto( int inCode , Parcel inSend , Collection inType ) {
		List					list = serviceFetchStrings( inCode , inSend );
		
		if ( null == list || list.isEmpty() ) inType = null;
		else inType.addAll( list );
		
		return inType;
	}
	
	public Collection serviceFetchListInto( int inCode , Parcel inSend , Collection inType ) {
		List					list = serviceFetchList( inCode , inSend );
		
		if ( null == list || list.isEmpty() ) inType = null;
		else inType.addAll( list );
		
		return inType;
	}
	
	public Map serviceFetchMapInto( int inCode , Parcel inSend , Map inType ) {
		Map						map = serviceFetchMap( inCode , inSend );
		
		if ( null == map || map.isEmpty() ) inType = null;
		else inType.putAll(  map );
		
		return inType;
	}
	
	public IHRVector serviceFetchVector( int inCode , Parcel inParcel ) {
		return (IHRVector)serviceFetchListInto( inCode , inParcel , new IHRVector() );
	}
	
	public IHRHashtable serviceFetchHashtable( int inCode , Parcel inParcel ) {
		return (IHRHashtable)serviceFetchMapInto( inCode , inParcel , new IHRHashtable() );
	}
	
	public IHRVector serviceFetchPreference( String inKey ) {
		return (IHRVector)serviceFetchStringsInto( IHRService.kFetchStrings , parcel( inKey ) , new IHRVector() );
	}
	
	public void serviceWritePreference( String inKey , IHRVector inValue ) {
		Parcel					send = parcel( inKey );
		
//		Collections.checkedCollection( (List)inValue , String.class );
		send.writeStringList( (List)inValue );
		
		serviceTell( IHRService.kWriteStrings , send );
	}
	
	
	
	public void onServiceConnected( ComponentName inName , IBinder inBinder ) {
		mConnection = inBinder;
	}
	
	public void onServiceDisconnected( ComponentName inName ) {
		mConnection = null;
		
		try { Streamradio.g.unregisterReceiver( this ); } catch ( Exception e ) {}
	}

	public void onDestroy() {
		onServiceDisconnected( null );
	}

	@Override
	public void onReceive( Context context , Intent intent ) {
		
	}
}
