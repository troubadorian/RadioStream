package com.troubadorian.streamradio.client.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

public class IHRThreadable implements Runnable {
	public static IHRThreadable	gMain;
	
	public final static int		kJumpThread = 0;
	public final static int		kMakeThread = 1;
	public final static int		kStopThread = 2;
	
	public final static int		kDaemon = 0x01;
	public final static int		kOwnsThread = 0x02;
	public final static int		kOwnsHandler = 0x04;
	
	public Thread				mThread;
	public Handler				mHandler;
	
	public Handler.Callback		mCallback;
	public int					mFlags;
	
	public IHRThreadable( Handler.Callback inCallback , Context inContext ) {
		Looper					looper = inContext.getMainLooper();
		
		mFlags = kOwnsHandler;
		mCallback = inCallback;
		mThread = looper.getThread();
		mHandler = new Handler( looper , mCallback );
	}
	
	public IHRThreadable( Handler.Callback inCallback , View inView ) {
		mFlags = 0;
		mCallback = inCallback;
		mThread = inView.getContext().getMainLooper().getThread();
		mHandler = inView.getHandler();
	}
	
	public IHRThreadable( Handler.Callback inCallback , boolean inDaemon ) {
		mCallback = inCallback;
		mFlags = inDaemon ? kDaemon : 0;
		
		create();
	}
	
	public IHRThreadable( Handler.Callback inCallback ) {
		this( inCallback , false );
	}
	
	public void run() {
		Looper.prepare();
		
		mFlags |= ( kOwnsThread | kOwnsHandler );
		mHandler = new Handler( mCallback );
		
		Looper.loop();
	}
	
	public void rethread( int inAction ) {
		if ( 0 == ( kMakeThread & inAction ) ) {
			if ( null != mHandler && 0 != ( mFlags & kOwnsHandler ) ) {
				mHandler.getLooper().quit();
				mHandler = null;
			}
			
			if ( null != mThread && 0 != ( mFlags & kOwnsThread ) ) {
				mThread.interrupt();
				mThread = null;
			}
		}
		
		if ( 0 == ( kStopThread & inAction ) ) {
			/**/
			mThread = new Thread( this , ( null == mCallback ) ? "IHR" : mCallback.getClass().getSimpleName() );
			/*/
			mThread = new HandlerThread( ( null == mCallback ) ? "IHR" : mCallback.getClass().getSimpleName() ) {
				@Override
				public void onLooperPrepared() {
					mFlags |= ( kOwnsThread | kOwnsHandler );
					mHandler = new Handler( ((HandlerThread)mThread).getLooper() , mCallback );
				}
			};
			/**/
			
			if ( 0 != ( mFlags & kDaemon ) ) mThread.setDaemon( true );
			
			mThread.start();
		}
	}
	
	public boolean isAlive() { return mThread.isAlive(); }
	public boolean isDaemon() { return mThread.isDaemon(); }
	public boolean isRunning() { return ( null != mHandler ); }
	public boolean isCurrent() { return null != mThread && mThread.getId() == Thread.currentThread().getId(); }
	
	public void reset() { rethread( kJumpThread ); }
	public void create() { rethread( kMakeThread ); }
	public void destroy() { rethread( kStopThread ); }
	
	public boolean handle( Runnable inRunnable ) { return mHandler.post( inRunnable ); }
	public boolean handle( Runnable inRunnable , long inDelay ) { return ( inDelay < 0 ) ? mHandler.postAtFrontOfQueue( inRunnable ) : mHandler.postDelayed( inRunnable , inDelay ); }
	public boolean handle( Message inMessage ) { return mHandler.sendMessage( inMessage ); }
	public boolean handle( Message inMessage , long inDelay ) { return ( inDelay < 0 ) ? mHandler.sendMessageAtFrontOfQueue( inMessage ) : mHandler.sendMessageDelayed( inMessage , inDelay ); }
	public boolean handle( int inWhat , int inA , int inB , Object inToken ) { return handle( mHandler.obtainMessage( inWhat , inA , inB , inToken ) ); }
	public boolean handle( int inWhat , int inA , int inB , Object inToken , long inDelay ) { return handle( mHandler.obtainMessage( inWhat , inA , inB , inToken ) , inDelay ); }
	public boolean handle( int inWhat ) { return mHandler.sendEmptyMessage( inWhat ); }
	public boolean handle( int inWhat , long inDelay ) { return ( inDelay < 0 ) ? mHandler.sendMessageAtFrontOfQueue( mHandler.obtainMessage( inWhat ) ) : mHandler.sendEmptyMessageDelayed( inWhat , inDelay ); }
	
	public void handleFrames( Runnable inRunnable , long inDuration , int inFrames ) { for ( int i = 1 ; i <= inFrames ; ++i ) { mHandler.postDelayed( inRunnable , i * inDuration / inFrames ); } }
	public void handleRepeat( Runnable inRunnable , long inInterval , int inRepeat ) { for ( int i = 1 ; i <= inRepeat ; ++i ) { mHandler.postDelayed( inRunnable , i * inInterval ); } }
	
	public void remove( int inWhat ) { mHandler.removeMessages( inWhat ); }
	public void remove( int inWhat , Object inToken ) { mHandler.removeMessages( inWhat , inToken ); }
	public void remove( Runnable inRunnable ) { mHandler.removeCallbacks( inRunnable ); }
	public void remove( Runnable inRunnable , Object inToken ) { mHandler.removeCallbacks( inRunnable , inToken ); }
	public void remove( Object inToken ) { mHandler.removeCallbacksAndMessages( inToken ); }
	
	public void handleWaiting( Runnable inRunnable , long inDelay , long inWait ) {
		Thread					source = Thread.currentThread();
		Thread					target = mThread;
		
		if ( null == target || target.getId() == source.getId() ) {
			handle( inRunnable , inDelay );
		} else try {
			Runnable			interruptor = new RunnableInterruptor( inRunnable , source );
			
			synchronized( interruptor ) {
				if ( inDelay == 0 ) handle( interruptor );
				else handle( interruptor , inDelay );
				
				if ( inWait < 0 ) target.join();
				else target.join( inWait );
			}
		} catch ( Exception e ) {}
	}
	
	public class RunnableInterruptor implements Runnable {
		Runnable				mActual;
		Thread					mThread;
		
		public RunnableInterruptor( Runnable inR , Thread inT ) { mActual = inR; mThread = inT; }
		public void run() { try { mActual.run(); } catch ( Exception e ) {} mThread.interrupt(); }
	}
	
}


