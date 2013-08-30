package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.troubadorian.streamradio.client.services.IHRServiceConfiguration;
import com.troubadorian.streamradio.controller.IHRControllerSplash;
import com.troubadorian.streamradio.controller.R;

public class IHRViewSplash extends RelativeLayout {
	public SplashImageView				mLogo;
	public TextView						mStatus;
	public TextView						mVersion;
	
	public IHRControllerSplash			mDelegate;
	public boolean						mAnimating;
	
	public IHRViewSplash( IHRControllerSplash inDelegate ) {
		super( inDelegate.getContext() );
		
		this.setBackgroundColor( Color.BLACK );
		
		mDelegate = inDelegate;
		mLogo = new SplashImageView( inDelegate.getContext() );
		mLogo.setFixedSize( null );
		
		addView( mLogo );
		addView( mStatus = new TextView( inDelegate.getContext() ) );
		addView( mVersion = new TextView( inDelegate.getContext() ) );
		
		mStatus.setEllipsize( TextUtils.TruncateAt.END );
		mStatus.setGravity( Gravity.CENTER_HORIZONTAL );
		mStatus.setSingleLine();
		mStatus.setTextColor( Color.WHITE );
		mStatus.setTypeface( Typeface.SANS_SERIF, Typeface.NORMAL );
		
		mVersion.setEllipsize( TextUtils.TruncateAt.END );
		mVersion.setGravity( Gravity.CENTER_HORIZONTAL );
		mVersion.setSingleLine();
		mVersion.setText( "Version " + IHRServiceConfiguration.mApplicationVersion /* + " Beta" */ );
		mVersion.setTextColor( Color.LTGRAY );
		mVersion.setTextSize( 13.0f );
		mVersion.setTypeface( Typeface.SANS_SERIF, Typeface.NORMAL );
	}
	
	public void animateLogo() {
		mAnimating = true;
		mLogo.animateFPS( 24 );
	}
	
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		int						h, imageH, imageW, w, x, y;
		
		h = b - t;
		w = r - l;
		
		x = 10;
		y = h - 10 - 21;
		
		mVersion.layout( x, y, w - 10, y + 21 );

		y -= 21 - 2;
		
		mStatus.layout(  x, y, w - 10, y + 21 );
		
		imageW = mLogo.getSurfaceWidth();
		imageH = mLogo.getSurfaceHeight();
		
		x = ( w - imageW ) / 2;
		y = ( y - imageH ) / 2;			// treating y as new height h
		
		mLogo.layout( x, y, x + imageW, y + imageH );	
	}
	
	class SplashImageView extends AnimatingImageView {
		public static final int	kFrames = 21;
		
		public SplashImageView( Context context ) { super( context ); }
		public void animateFPS( int inFPS ) { animateFPS( inFPS , kFrames ); }
		
		@Override
		public int advanceID( int inPosition ) {
			//	assume id order matches string sort order
			return R.drawable.splash_logo_20000 + inPosition;
		}
		
		@Override
		public void leaveAnimation() {
			mAnimating = false;
			mDelegate.runOnUiThread( mDelegate );
		}
		
	}
	
	class FavoriteImageView extends AnimatingImageView {
		public static final int	kFrames = 9;
		
		public FavoriteImageView( Context context ) { super( context ); }
		public void animateFPS( int inFPS ) { animateFPS( inFPS , kFrames ); }
		
		@Override
		public int advanceID( int inPosition ) {
			int[]					images = { R.drawable.star_1, R.drawable.star_4, R.drawable.star_7, R.drawable.star_10, R.drawable.star_13, R.drawable.star_16, R.drawable.star_19, R.drawable.star_22, /* R.drawable.star_24, */ R.drawable.star_25 };
			
			return images[( inPosition < 0 || inPosition > kFrames ) ? 0 : inPosition];
		}
		
	}
	
	
}
