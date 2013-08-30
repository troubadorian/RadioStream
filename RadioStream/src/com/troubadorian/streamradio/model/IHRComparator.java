package com.troubadorian.streamradio.model;

public class IHRComparator extends IHRObject {
	public static IHRComparator stringComparator() {
		return new IHRComparator() {
			public int compare( Object lhs, Object rhs ) {
				return lhs.toString().compareTo( rhs.toString() ); 
			}
		};
	}
	
	public static IHRComparator stringPrefixComparator( int prefixLength ) {
		return new IHRStringPrefixComparator( prefixLength );
	}
	
	// protected classes

	protected static class IHRStringPrefixComparator extends IHRComparator {
		protected int			mPrefixLength;
		
		public IHRStringPrefixComparator( int prefixLength ) { mPrefixLength = prefixLength; }
	
		public int compare( Object lhs, Object rhs ) {
			char			a, b;
			int				i, n, o;
			String			l, r;
			
			l = lhs.toString();
			r = rhs.toString();

			n = l.length();
			o = r.length();
			
			for ( i = 0; i < mPrefixLength; ++i ) {
				// if we ran out of characters on lhs, then either lhs is shorter (lexically
				// less), or the strings are equal.
				if ( i > n ) return i > o ? 0 : -1;
				// if we ran out of characters on rhs, then rhs is shorter (lexically less).
				if ( i > o ) return -1;

				// here we have characters in both lhs and rhs to check
				
				a = l.charAt( i );
				b = r.charAt( i );
				
				if ( a < b ) return -1;
				if ( a > b ) return 1;
			}

			return 0;		// the strings are equal relative to the prefix length
		}
	}
	
	public int compare( Object lhs, Object rhs ) { return 0; }
}
