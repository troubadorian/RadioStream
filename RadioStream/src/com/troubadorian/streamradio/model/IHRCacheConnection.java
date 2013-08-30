package com.troubadorian.streamradio.model;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRURLEncoder;
import com.troubadorian.streamradio.client.services.IHRService;

public class IHRCacheConnection extends IHRHTTP {
	public static final int		kFileClose = 0;
	public static final int		kFileOpen = 1;
	public static final int		kFileCloseThenWrite = 2;
	public static final int		kFileCloseThenDelete = 3;
	public static final int		kFileDeleteAfterClose = 4;
	
	public static final int		kResumeFailure = 0;
	public static final int		kResumeAttempt = 1;
	public static final int		kResumeSuccess = 3;
	
	public static final String	kHeaderRanges = "Accept-Ranges";
	public static final String	kHeaderModify = "Last-Modified";
	public static final String	kHeaderLength = "Content-Length";
	public static final String	kResumeOffset = "offset";
	public static final String	kResumeAtomic = "atomic";
	
	public static final String[]	kResumeFlattenKeys = {kResumeAtomic,kResumeOffset,kHeaderLength,kHeaderModify,"url"};
	
	//**
	/*/
	protected static IHRHTTPGate	sCacheGate = new IHRHTTPGate( "IHRCacheGate" );
	/**/
	
	String					_url;
	String					_path;
	String					_site;
	IHRFile					_write;
	OutputStream			_stream;
	IHRHashtable			_resume;
	AtomicBoolean			_atomicFlag;
	AtomicBoolean			_deleteFlag;
	AtomicInteger			_resumeFlag;
	int						_retainFlag;
	IHRCache				_delegate;
	TimerTask				_timer;
	
	public IHRCacheConnection( String inURL , IHRCache inDelegate , String inSite ) {
		//**
		super( null , null );
		/*/
		super();
		/**/
		
		_url = inURL;
		_delegate = inDelegate;
		_atomicFlag = new AtomicBoolean();
		_deleteFlag = new AtomicBoolean();
		_resumeFlag = new AtomicInteger();
		
		//**
		mIgnoreReply = false;
		/*/
		mRead = true;
		/**/
		
		mCredentialsIdentifier = inSite;
	}
	
	public void debugLog( String inMethod , String inMessage ) {
		//**
		Log.d( "## " + inMethod , inMessage + " " + _path.substring(_path.lastIndexOf( '/' ) + 1 ) );
		/*/
		System.out.println( "## " + inMethod + " " + inMessage + " " + _path.substring(_path.lastIndexOf( '/' ) + 1 ) );
		/**/
	}
	
	public void setDestination( String inPath , boolean inAllowOverwrite ) {
		if ( !inAllowOverwrite ) {
			try {
				IHRFile		file = new IHRFile( inPath );
				
				inAllowOverwrite = !file.exists();
				
				file.close();
			} catch ( Exception e ) {}
		}
		
		if ( inAllowOverwrite ) {
			_write = null;
			_path = inPath;
			
			if ( null == mConnection ) recreateHonoringResume( false );
		} else {
			debugLog( "setDestination" , "fail: File Exists" );
			
			_resumeFlag.set( kResumeFailure );
			_delegate.didFail( _url , new Exception( "File Exists" ) );
		}
	}
	
	public void setDestination( String inPath , String inData ) {
		String					string;
		long					size = 0;
		
		_write = null;
		_path = inPath;
		
		if ( null == inData ) {
			_resume = null;
		} else {
			_resume = new IHRHashtable();
			_resume.restore( kResumeFlattenKeys , "\t" , inData , 0 );
			
			string = (String)_resume.get( "url" );
			if ( null != string ) _url = string;
			
			if ( _resume.booleanValue( kResumeAtomic , false ) ) {
				_atomicFlag.set( true );
			}
			
			try {
				_write = new IHRFile( pathWrite() , true );
				
				size = _write.fileSize();
			} catch ( Exception e ) {}
			
			if ( size > 0 && size < Integer.MAX_VALUE ) {
				_resume.put( kResumeOffset , new Integer( (int)size ) );
				_delegate.didWrite( _url , (int)size );
			} else {
				_resume.remove( kResumeOffset );
			}
		}
		
		recreateHonoringResume( true );
	}
	
	public String trackingURL() {
		String				result = _url;
		
		//**
		IHRURLEncoder		data = IHRService.g.mConfiguration.standardPostData( false );
		/*/
		URLEncodedPostData	data = IHRConfiguration.shared().standardPostData( false );
		/**/
		
		String				parameters = ( null == data ) ? null : data.toString();
		
		if ( null != parameters && 0 != parameters.length() ) {
			result = result + ( result.indexOf( '?' ) < 0 ? "?" : "&" ) + parameters;
		}
		
		return result;
	}
	
	public void recreateHonoringResume( boolean inHonor ) {
		close();
		
		if ( !inHonor ) {
			_resume = null;
		}
		
		//	TODO: tracking url unless archive
		mURL = _url;
//		mURL = trackingURL();
		mBytesRead = 0;
		mSuccess = false;
		mCancelled.set( false );
		mCompressionType = kCompressionTypeNone;
		mContentLength = 0;
		mConnection = null;
		mData = null;
		
		debugLog( "recreateHonoringResume" , "honor " + inHonor + " offset " + ( null == _resume ? 0 : _resume.integerValue( kResumeOffset , 0 ) ) + " length " + ( null == _resume ? 0 : _resume.integerValue( kHeaderLength , 0 ) ) );
		
		//**
		mPostData = null;
		
		Thread					thread = new Thread( this , "Cache " + IHRCache.relativePathForURL( _url ) );
		
		thread.setPriority( Thread.MIN_PRIORITY );
		thread.start();
		/*/
		mException = null;
		mInstance = 0;
		mLastDataReportAmount = 0;
		mLastDataReportTime = 0;
		
		sCacheGate.enqueue( this , false );	//	separate from IHRHTTP sGate
		/**/
	}
	
	public String pathFinal() { return _path; }
	public String pathWrite() { return _atomicFlag.get() ? _path + ".download" : _path; }
	
	private void fileAction( int inAction ) {
		if ( kFileOpen == inAction ) {
			try {
				if ( null == _write ) {
//					debugLog( "fileAction" , "create" );
					
					_write = new IHRFile();
					_write.openCreatingParents( pathWrite() );
				}
				
				if ( null == _stream ) {
					int			offset = ( null == _resume ) ? 0 : _resume.integerValue( kResumeOffset , 0 );
					
//					debugLog( "fileAction" , "stream" );
					
					_stream = _write.openOutputStream( offset );
				}
			} catch ( Exception e ) {}
			/*
			if ( null == _stream ) {
				_stream = _delegate.streamForBufferingPath( _path );
			}
			*/
		} else synchronized( this ) {
			if ( kFileDeleteAfterClose == inAction ) {
				inAction = ( null == _stream ) ? kFileClose : kFileCloseThenDelete;
			}
			
			if ( null != _stream ) {
//				debugLog( "fileAction" , "close" );
				
				try { _stream.close(); } catch ( Exception e ) {}
				_stream = null;
				
				if ( inAction == kFileCloseThenDelete ) {
					debugLog( "fileAction" , "buffer" );
					
					_delegate.deleteForBufferingPath( _path );
				}
			}
			
			if ( null != _write ) {
				if ( kFileCloseThenWrite == inAction ) {
					if ( _atomicFlag.get() ) {
						debugLog( "fileAction" , "rename" );
						
						_write.renameToPathInSameDirectory( _path );
					}
				}
				
				if ( kFileCloseThenDelete == inAction ) {
					if ( _deleteFlag.get() ) {
						debugLog( "fileAction" , "delete" );
						
						try { _write.deleteWithEmptyParent(); } catch ( Exception e ) {}
					}
				}
				
				try { _write.close(); } catch ( Exception e ) {}
				_write = null;
			}
		}
	}
	
	//**
	@Override
	protected boolean processResponse() throws Exception {
		boolean					result = false;
		int						code = getResponseCode();
		
		if ( 200 == code || 206 == code ) {
			processHeaders();
			openInputStream();
			
			result = true;
		}
		
		return result;
	}
	/*/
	protected void setRequestProperty( String inField , String inValue ) throws IOException {
		mConnection.setRequestProperty( inField , inValue );
	}
	
	protected int getResponseCode() throws IOException {
		return mConnection.getResponseCode();
	}
	
	protected String getHeaderField( String inField ) throws IOException {
		return mConnection.getHeaderField( inField );
	}
	/**/
	
//	could add authentication headers here
	@Override
	protected void prepareRequest() throws Exception {
		int					offset = ( null == _resume ) ? 0 : _resume.integerValue( kResumeOffset , 0 );
		
		if ( offset > 0 ) {
			debugLog( "prepareRequest" , "Range: bytes="+offset+"-" );
			
			setRequestProperty( "Range" , "bytes="+offset+"-" );
		} else {
			_resume = null;
		}
	}
	
	protected void processData( byte[] inData , int inOffset , int inLength ) throws IOException {
		if ( !_delegate.available() ) {
			debugLog( "processData" , "buffer " + inData.length );
			
			_delegate.didReceive( _url , inData , inOffset , inLength );
		} else {
			if ( null == _stream ) {
				fileAction( kFileOpen );
			}
			
			_retainFlag = 0;
			_stream.write( inData , inOffset , inLength );
			_retainFlag = 1;
			
			if ( null != _resume ) {
				int			wrote = _resume.integerValue( kResumeOffset , 0 );
				
				_resume.put( kResumeOffset , new Integer( wrote + inLength ) );
			}
			
			_delegate.didWrite( _url , inLength );
		}
	}
	
	//	PRNCache connection:didReceiveData:
	@Override
	protected void processData( byte[] inData ) {
		try {
			processData( inData , 0 , inData.length );
		} catch ( Exception e ) {
			cancel();
			
			debugLog( "processData" , "fail: " + e.getMessage() );
			
			_resumeFlag.set( kResumeFailure );
			_delegate.didFail( _url , e );
		}
	}
	
	//	PRNCache connection:didReceiveResponse:
	@Override
	protected void processHeaders() throws Exception {
		String					header;
		
		super.processHeaders();
		
//		mConnection.getHeaderField( "Content-Encoding" );
//		mConnection.getHeaderFieldInt( "content-length", 0 );
//		mConnection.getHeaderField( "content-type" );
		
		int						code = getResponseCode();
		
		if ( code == 301 || code == 302 || code == 303 || code == 304 || code == 307 ) {
			//	do nothing on redirect
		} else if ( code >= 400 && code != 401 && code != 407 ) {
			_resumeFlag.set( kResumeFailure );
			cancel();
			_delegate.didFail( _url , new Exception( "HTTP " + code ) );
		} else if ( null != _resume ) {
			header = getHeaderField( kHeaderModify );
			
			if ( null == header || !header.equalsIgnoreCase( (String)_resume.get( kHeaderModify ) ) ) {
				debugLog( "processHeaders" , "modify " + header + " != " + _resume.get( kHeaderModify ) );
				
				_resumeFlag.set( kResumeAttempt );
				_resume = null;
				cancel();
				resumeAfter( 10 );
			} else {
				mBytesRead = _resume.integerValue( kResumeOffset , 0 );
				mContentLength = _resume.integerValue( kHeaderLength , 0 );
				
				debugLog( "processHeaders" , "resume " + mBytesRead + "+" + getHeaderField( kHeaderLength ) + " = " + mContentLength + "modify " + header );
			}
		} else {
			header = getHeaderField( kHeaderRanges );
			
			if ( null != header && header.equalsIgnoreCase( "bytes" ) ) {
				_resume = new IHRHashtable();
				_resumeFlag.set( kResumeSuccess );
				
				_resume.put( "url" , _url );
				_resume.put( kHeaderModify , getHeaderField( kHeaderModify ) );
				_resume.put( kHeaderLength , getHeaderField( kHeaderLength ) );
				
				debugLog( "processHeaders" , "ranges " + _resume.get( kHeaderLength ) );
			}
		}
	}
	
	@Override
	protected void read() throws IOException {
		byte[]					buffer = new byte[( mContentLength > 0 && mContentLength < 0x00010000 ) ? mContentLength : 0x00010000];
		int						n;
		
		for ( ;; ) {
			if ( mCancelled.get() ) throw new CancellationException( "cancelled" );
			
			if ( mContentLength > 0 && !( mBytesRead < mContentLength ) ) {
				mSuccess = true;
				
				break;
			}
			
			n = mInputStream.read( buffer );
			
//			debugLog( "read" , "bytes " + n + " + " + mBytesRead + " of " + mContentLength );
			
			if ( mCancelled.get() ) throw new CancellationException( "cancelled" );
			
			if ( n < 0 ) {
				if ( mBytesRead < mContentLength ) {
					throw new EOFException( "unexpected end of file" );
				}
				
				mSuccess = true;
				
				break;
			} else {
				mBytesRead += n;
				
				processData( buffer , 0 , n );
			}
			
			try { Thread.sleep( 500 ); } catch ( Exception e ) {}
		}
		
		buffer = null;
	}
	
	@Override
	public void openThenClose() {
		boolean					opened = false;
		
		/** TO DO: disable debug code
		Debug.startMethodTracing( _path.substring( _path.lastIndexOf( "/" ) + 1 ).replace( '.' , '_' ) , 1<<24 );
		/**/
		
		try {
			debugLog( "openThenClose" , "open" );
			open();
			opened = true;
			
			//	downloads can be very slow to begin so offer feedback
			if ( _delegate.available() ) _delegate.didWrite( _url , 0 );
			
			debugLog( "openThenClose" , "read" );
			read();
			
			//	PRNCache connectionDidFinishLoading:
			debugLog( "openThenClose" , "done" );
			fileAction( kFileCloseThenWrite );
			
			if ( !mCancelled.get() ) _delegate.didFinish( _url );
		} catch ( Exception e ) {
			mData = null;
			
			debugLog( "openThenClose" , "fail: " + e.getClass().getName().substring( e.getClass().getName().lastIndexOf( '.' ) + 1 ) + " " + e.getMessage() );
			
			if ( !opened ) _resumeFlag.set( kResumeFailure );
			fileAction( 0 == _retainFlag ? kFileDeleteAfterClose : kFileCloseThenWrite );
			
			//	PRNCache connection:didFailWithError:
			if ( !mCancelled.get() ) _delegate.didFail( _url , e );
		}
		
		close();
		
		/** TO DO: disable debug code
		Debug.stopMethodTracing();
		/**/
	}
	
	public void go() { openThenClose(); }
	
	@Override
	public void cancel() {
		resumeAbort();
		super.cancel();
		fileAction( kFileCloseThenDelete );
	}
	
	public boolean isErrorRecoverable() {
		return ( kResumeFailure != _resumeFlag.get() );
	}
	
	public boolean deletesFileUponFailure() {
		return _deleteFlag.get();
	}
	
	public void setDeletesFileUponFailure( boolean inDelete ) {
		_deleteFlag.set( inDelete );
	}
	
	public void resumePosition( IHRHashtable ioResume ) {
//		ioResume.put( "path" , _path );
		
		if ( _atomicFlag.get() ) {
			ioResume.put( "write" , pathWrite() );
		}
		
		if ( null != _resume ) synchronized( _resume ) {
			ioResume.putFrom( "offset" , _resume );
			ioResume.put( "length" , new Integer( _resume.integerValue( kHeaderLength , 0 ) ) );
		}
	}
	
	public String resumeData() {
		String					result = null;
		
		if ( null != _resume ) synchronized( _resume ) {
			_resume.put( kResumeAtomic , new Boolean( _atomicFlag.get() ) );
			
			result = _resume.flatten( kResumeFlattenKeys , "\t" );
		}
		
		return result;
	}
	
	public class IHRCacheTimerTask extends TimerTask {
		@Override
		public void run() {
			resumeAbort();
			recreateHonoringResume( true );
		}
	}
	
	public void resumeAbort() {
		if ( null != _timer ) {
			_timer.cancel();
			_timer = null;
		}
	}
	
	public void resumeAfter( int inSeconds ) {
		if ( null != _timer ) _timer.cancel();
		
		debugLog( "resumeAfter" , "" + inSeconds + " seconds" );
		
		_timer = new IHRCacheTimerTask();
		
		IHRCache.shared().timer().schedule( _timer , inSeconds * 1000 );
	}
	
	public double progress() {
		double					result;
		
		if ( null == _resume ) {
			result = (double)mBytesRead / (double)mContentLength;
		} else synchronized( _resume ) {
			result = _resume.doubleValue( "offset" , 0 ) / _resume.doubleValue( kHeaderLength , 1 );
		}
		
		return result;
	}
	
	public boolean hasContent() {
		boolean					result;
		
		if ( null == _resume ) {
			result = ( 0 < mContentLength ) && !( mBytesRead < mContentLength );
		} else synchronized( _resume ) {
			long				length = _resume.longValue( kHeaderLength , 0 );
			
			result = ( 0 < length ) && !( _resume.longValue( "offset" , 0 ) < length );
		}
		
		return result;
	}
	
	
}
