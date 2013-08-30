package com.troubadorian.streamradio.model;

public class IHRBinarySearch extends IHRObject {
	public static int search( Object array[], Object value, IHRComparator comparator ) {
		return searchInternal( array, value, comparator, 0, array.length - 1 );
	}
	
	// protected methods
	
	protected static int searchInternal( Object array[], Object value, IHRComparator comparator, int low, int high ) {
		int						compare, middle;
		
		if ( high < low ) return -1;
		
		middle = low + ( high - low ) / 2;
		compare = comparator.compare( array[ middle ], value );
		
		if ( compare > 0 ) return searchInternal( array, value, comparator, low, middle - 1 );
		else if ( compare < 0 ) return searchInternal( array, value, comparator, middle + 1, high );
		else return middle;
	}
}
