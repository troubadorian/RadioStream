package com.troubadorian.streamradio.model;

public class IHRQuicksort extends IHRObject {
	public static void sort( Object array[], IHRComparator comparator ) {
		sortInternal( array, comparator, 0, array.length - 1 );
	}
	
	// protected methods
		
	protected static int partition( Object array[], IHRComparator comparator, int left, int right ) {
		int				i, j;
		Object			pivot, tmp;
		
		i = left;
		j = right;
		pivot = array[ ( i + j ) / 2 ];

		while ( i <= j ) {
			while ( comparator.compare( array[ i ], pivot ) < 0 ) ++i;
			while ( comparator.compare( array[ j ], pivot ) > 0 ) --j;
			
			if ( i <= j ) {
				tmp = array[ i ];
				array[ i ] = array[ j ];
				array[ j ] = tmp;
				
				++i;
				--j;
			}
		}
		
		return i;
	}
	
	protected static void sortInternal( Object array[], IHRComparator comparator, int left, int right ) {
		int				i, j;
		
		i = partition( array, comparator, left, right );
		
		if ( left < ( j = i - 1 ) ) sortInternal( array, comparator, left, j );
		if ( i < right ) sortInternal( array, comparator, i, right );
	}
}
