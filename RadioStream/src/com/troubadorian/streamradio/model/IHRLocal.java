package com.troubadorian.streamradio.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.troubadorian.streamradio.client.model.IHRVector;

public class IHRLocal extends ArrayList<String> {
	private static final long	serialVersionUID	= 1L;
	
	public static final int		kName = 0;
	public static final int		kDistance = 1;
	public static final int		kStationList = 2;
	
	public IHRLocal() { super(); }
	public IHRLocal( IHRLocal inLocal ) { super( inLocal ); }
	public IHRLocal( Collection<String> inLocal ) { super( inLocal ); }
	public IHRLocal( String inName , String inDistance , Collection inStations ) { super(); add( inName ); add( inDistance ); addAll( inStations ); }
	
	public String getName() { return get( kName ); }
	public String getDistance() { return get( kDistance ); }
	public String getStation( int index ) { return get( index + kStationList ); }
	public int getStationCount() { return size() - kStationList; }
	public List<String> copyStationList() { return subList( kStationList , size() ); }
	public IHRVector getCallLetters() { return new IHRVector( copyStationList() ); }
	
	public boolean isValid() { return size() > kStationList && getName().length() > 0; }
	public static boolean isValid( ArrayList<String> inLocal ) { return inLocal.size() > kStationList && inLocal.get( kName ).length() > 0; }

}
