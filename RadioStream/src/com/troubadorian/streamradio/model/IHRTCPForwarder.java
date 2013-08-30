package com.troubadorian.streamradio.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;

public class IHRTCPForwarder extends IHRByteStreamModifier implements Runnable {
	protected boolean					mClosed;
	protected boolean					mInitialized;
	protected IHRTCPConnectionHalf		mLocalReader;
	protected Socket					mLocalSocket;
	protected String					mRemoteHost;
	protected int						mRemotePort;
	protected IHRTCPConnectionHalf		mRemoteReader;
	protected Socket					mRemoteSocket;
	protected ServerSocket				mServerSocket;
	protected int						mUnique;
	protected URI						mURI;
	
	public IHRTCPForwarder( URI uri ) throws Exception {
		mUnique = (int)( System.currentTimeMillis() % 1000000L );
		
		mRemoteHost = InetAddress.getByName( uri.getHost() ).getHostAddress();
		mRemotePort = uri.getPort();
		mServerSocket = new ServerSocket( 0, 8, InetAddress.getByName( "127.0.0.1" ) );

		if ( mRemotePort < 0 ) mRemotePort = defaultPort();

		log( "IHRTCPForwarder", "creating " + getClass().getName() + " for traffic to " + mRemoteHost + ":" + mRemotePort );				
		
		mURI = new URI( uri.getScheme(), uri.getUserInfo(), mRemoteHost, mRemotePort, uri.getPath(), uri.getQuery(), uri.getFragment() );
	}
	
	public void close() {
		synchronized( this ) {
			if ( mClosed ) return;
			
			mClosed = true;
		}
		
		if ( mLocalReader != null ) mLocalReader.close();
		if ( mRemoteReader != null ) mRemoteReader.close();
		
		if ( mLocalSocket != null ) try { mLocalSocket.close(); } catch ( Exception e ) { } 
		if ( mRemoteSocket != null ) try { mRemoteSocket.close(); } catch ( Exception e ) { } 
		if ( mServerSocket != null ) try { mServerSocket.close(); } catch ( Exception e ) { }
		
		mLocalReader = null;
		mLocalSocket = null;
		mRemoteReader = null;
		mRemoteSocket = null;
		mServerSocket = null;
	}

	public int getLocalPort() { return mServerSocket.getLocalPort(); }
	
	public boolean isClosed() { return mClosed; }
	
	public void open() throws Exception { new Thread( this , "IHRTCPForwarder " + mUnique ).start(); Thread.sleep( 100 ); }

	public synchronized void run() {
		try {
			mLocalSocket = mServerSocket.accept();
			mRemoteSocket = new Socket( mRemoteHost, mRemotePort );
			
			mLocalReader = new IHRTCPConnectionHalf( kDataSourceLocal, mLocalSocket.getInputStream(), mRemoteSocket.getOutputStream() );
			mRemoteReader = new IHRTCPConnectionHalf( kDataSourceRemote, mRemoteSocket.getInputStream(), mLocalSocket.getOutputStream() );

			try { mServerSocket.close(); } catch ( Exception e ) { }

			mServerSocket = null;
		} catch ( Exception e ) {
			log( "run", "error: " + e.getMessage() );
			close();
		}
		
		mInitialized = true;
		
		notifyAll();
	}
		
	// protected methods
	
	// if no port specified in original URI
	protected int defaultPort() { return 80; }
	
	// protected classes

	protected class IHRTCPConnectionHalf implements Runnable {
		protected int					mDataSource;
		protected InputStream			mIStream;
		protected OutputStream			mOStream;
		
		public IHRTCPConnectionHalf( int dataSource, InputStream inputStream, OutputStream outputStream ) {
			mDataSource = dataSource;
			mIStream = inputStream;
			mOStream = outputStream;
			
			new Thread( this , "IHRTCPConnectionHalf " + mUnique ).start();
		}
		
		public synchronized void close() {
			if ( mIStream != null ) try { mIStream.close(); } catch ( Exception e ) { }
			if ( mOStream != null ) try { mOStream.close(); } catch ( Exception e ) { }
			
			mIStream = null;
			mOStream = null;
		}
		
		public void run() {
			byte[]						buffer = new byte[ 16384 ];
			ByteBuffer					byteBuffer;
			int							n;

			synchronized( IHRTCPForwarder.this ) {
				while ( ! mInitialized ) try { IHRTCPForwarder.this.wait(); } catch ( Exception e ) { }
			}
			
			try {
				while ( ! mClosed ) {
					if ( ( n = IHRUtilities.readInputStream( mIStream, buffer, 0, buffer.length ) ) < 0 ) break; 
					
					log( "IHRTCPConnectionHalf", "read " + n + " bytes from " + ( mDataSource == kDataSourceLocal ? "local" : "remote" ) + " data source" );
					
					if ( ( byteBuffer = modifyByteStream( mDataSource, buffer, n, false ) ) != null ) {
						String			target;
						
						if ( mDataSource == kDataSourceLocal ) {
							target = "remote " + mRemoteHost + ":" + mRemotePort;
						} else {
							target = "local  " + "127.0.0.1:" + mLocalSocket.getLocalPort();
						}
						
						dump( "IHRTCPConnectionHalf writing to " + target, byteBuffer.array(), 0, byteBuffer.limit() );
						
						mOStream.write( byteBuffer.array(), 0, byteBuffer.limit() );
						mOStream.flush();
					}
				}
			} catch ( Exception e ) {
				log( "run", "error: " + e.getMessage() );
			}
			
			IHRTCPForwarder.this.close();
			
//			try { Thread.sleep( 10000 ); } catch ( Exception e ) {}
		}
	}	
}
