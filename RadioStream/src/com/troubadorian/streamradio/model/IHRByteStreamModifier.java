package com.troubadorian.streamradio.model;

import java.nio.ByteBuffer;

abstract public class IHRByteStreamModifier extends IHRObject {
	protected final int					kDataSourceLocal = 0;
	protected final int					kDataSourceRemote = 1;

	protected ByteBuffer modifyByteStream( int dataSource, byte[] bytes, int length, boolean returnCopy ) throws Exception {
		return returnCopy ? copy( bytes, length ) : ByteBuffer.wrap( bytes, 0, length );
	}

	protected byte[] concatenateBuffers( byte[] prefix, int prefixOffset, int prefixLength, byte[] suffix, int suffixOffset, int suffixLength ) {
		byte[]					copy = new byte[ prefixLength + suffixLength ];
		
		System.arraycopy( prefix, prefixOffset, copy, 0, prefixLength );
		System.arraycopy( suffix, suffixOffset, copy, prefixLength, suffixLength );
		
		return copy;
	}
	
	protected ByteBuffer copy( byte[] bytes, int length ) {
		ByteBuffer				buffer;
		
		buffer = ByteBuffer.allocate( length );
		buffer.put( bytes, 0, length );
		
		return buffer;
	}
}
