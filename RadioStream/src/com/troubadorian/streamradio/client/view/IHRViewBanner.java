package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.troubadorian.streamradio.controller.R;

public class IHRViewBanner extends RelativeLayout implements View.OnClickListener {
	protected ImageView			mBackground;
	protected ImageView			mCloseButton;
	protected IHRViewMain		mDelegate;
	
	public static final int		kBannerHeight = 50;
	public static final int 	kBannerWidth = 300;
	
	public IHRViewBanner( Context context, IHRViewMain delegate ) {
		super( context );
	
		mDelegate = delegate;
		
		setWillNotDraw( false );
		
		mCloseButton = new ImageView( context );
		
		addView( mBackground = new ImageView( context ) );
		addView( mCloseButton );
		
		mBackground.setOnClickListener( this );
		mBackground.setScaleType( ImageView.ScaleType.CENTER_INSIDE );
		
		mCloseButton.setImageResource( R.drawable.closebutton_small );
		mCloseButton.setOnClickListener( this );
		mCloseButton.setScaleType( ImageView.ScaleType.CENTER );
	}
	
	public void onClick( View view ) {
		if ( view == mBackground ) mDelegate.bannerClicked();
		else if ( view == mCloseButton ) mDelegate.bannerClosed();
	}
	
	public void setBitmap( Bitmap bitmap ) {
		mBackground.setImageBitmap( bitmap );
		mBackground.setBackgroundColor( 0xFF000000 );
	}
	
	/*
	@Override
	protected void onDraw( Canvas canvas ) {
		canvas.drawARGB( 255, 0, 0, 0 );
		
		super.onDraw( canvas );
	}
	*/
	
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		int						imageW, w, x;
		
		w = r - l;
		
		if ( ( imageW = kBannerWidth ) > w ) imageW = w;
		
		x = ( w - imageW ) / 2;
		
		mBackground.layout( x, 0, x + imageW, kBannerHeight );
			
		x = w - ( w - imageW ) / 2 - 44;
		
		mCloseButton.layout( x, 0, x + 44, b-t );
	}
}
