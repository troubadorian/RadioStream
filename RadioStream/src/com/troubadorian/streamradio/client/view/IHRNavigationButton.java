package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageButton;

import com.troubadorian.streamradio.controller.R;

public class IHRNavigationButton extends ImageButton {
	public static final int		kImageArrowLeft = 0;
	public static final int		kImageLeft = 1;
	public static final int		kImageFill = 2;
	public static final int		kImageRight = 3;
	
	private int					mCapWidthLeft;
	private final Bitmap		mFill;
	private Bitmap				mLeftCap;
	private final Bitmap		mRightCap;
	private String				mTextLine1;
	private String				mTextLine2;
	private float				mTextLine1Width;
	private float				mTextLine2Width;
	private float				mTextMarginLeft;
	private int					mWidth;
	
	private final int			kButtonHeight = 30;
	private final int			kCapWidthRight = 4;
	private final float			kDoubleLineFontSize = 10.0f;
	private final int			kDoubleLineTextLine1OffsetY = 12;
	private final int			kDoubleLineTextLine2OffsetY = 25;
	private final int			kFillWidth = 1;
	private final int			kMaxTextWidth = 84;		// 100px is arbitrary max width - kTextMarginLeft - kTextMarginRight
	private final float			kSingleLineFontSize = 11.5f;
	private final int			kSingleLineTextOffsetY = 19;
	private final float			kTextMarginRight = 6;
	
	private static Bitmap[]		sBitmaps;
	
	public IHRNavigationButton( Context context, boolean isLeftNavButton ) {
		super( context );
		
		if ( null == sBitmaps ) {
			Resources			r = getResources();
			
			sBitmaps = new Bitmap[4];
			sBitmaps[kImageArrowLeft] = BitmapFactory.decodeResource( r , R.drawable.nav_button_arrow_left_14px );
			sBitmaps[kImageLeft] = BitmapFactory.decodeResource( r , R.drawable.nav_button_left_5px );
			sBitmaps[kImageFill] = BitmapFactory.decodeResource( r , R.drawable.nav_button_fill_1px );
			sBitmaps[kImageRight] = BitmapFactory.decodeResource( r , R.drawable.nav_button_right_4px );
		}
		
		// default to right-button style (no left arrow)
		mFill = sBitmaps[kImageFill];
		mRightCap = sBitmaps[kImageRight];
		mWidth = mCapWidthLeft + kFillWidth + kCapWidthRight;
		setVisibility( View.GONE );
		
		if ( isLeftNavButton ) {
			mCapWidthLeft = 14;
			mLeftCap = sBitmaps[kImageArrowLeft];
			mTextMarginLeft = 12;
		} else {
			mCapWidthLeft = 5;
			mLeftCap = sBitmaps[kImageLeft];
			mTextMarginLeft = 6;
		}
	}
	
	@Override
	public int getSuggestedMinimumWidth() {
		return mWidth;
	}

	@Override
	protected synchronized void onDraw( Canvas canvas ) {		
		Rect				dst, src;
		float				offsetX;
		Paint				paint;
		int					width;
		
		width = getWidth();

		// draw caps
		canvas.drawBitmap( mLeftCap, 0, 0, null );
		canvas.drawBitmap( mRightCap, width - kCapWidthRight, 0, null );
		
		// draw fill
		src = new Rect( 0, 0, kFillWidth, kButtonHeight );
		dst = new Rect( mCapWidthLeft, 0, width - kCapWidthRight, kButtonHeight ); 
		canvas.drawBitmap( mFill, src, dst, null );

		// draw title
		if ( mTextLine2 == null ) {
			canvas.drawText( mTextLine1, mTextMarginLeft, kSingleLineTextOffsetY, getTextPaint( kSingleLineFontSize ) );
		} else {
			paint = getTextPaint( kDoubleLineFontSize );

			offsetX = mTextMarginLeft + ( width - kTextMarginRight - mTextMarginLeft ) / 2.0f - mTextLine1Width / 2.0f;
			canvas.drawText( mTextLine1, offsetX, kDoubleLineTextLine1OffsetY, paint );

			offsetX = mTextMarginLeft + ( width - kTextMarginRight - mTextMarginLeft ) / 2.0f - mTextLine2Width / 2.0f;
			canvas.drawText( mTextLine2, offsetX, kDoubleLineTextLine2OffsetY, paint );
		}
	}

	private Paint getTextPaint( float inFontSize ) {
		Paint				paint;
		
		paint = new Paint();
		paint.setAntiAlias( true );
		paint.setColor( Color.argb( isEnabled() ? 255 : 127, 255, 255, 255 ) );
		paint.setTextSize( inFontSize );
		paint.setTypeface( Typeface.create( Typeface.SANS_SERIF, Typeface.BOLD ) );
		
		return paint;
	}
	
	public void setText( String inTextLine1, String inTextLine2 ) {
		int					n;
		Paint				paint;
		String				prefix, text;

		paint = getTextPaint( inTextLine2 == null ? kSingleLineFontSize : kDoubleLineFontSize );
		
		text = prefix = inTextLine1;
		n = prefix.length();
		
		while ( ( mTextLine1Width = paint.measureText( text ) ) > kMaxTextWidth ) {
			prefix = prefix.substring( 0, --n );
			text = prefix + "...";
		}
		
		mTextLine1 = text;
		mWidth = (int) Math.ceil( mTextMarginLeft + mTextLine1Width + kTextMarginRight );
		
		if ( ( text = inTextLine2 ) != null ) {
			prefix = text;
			n = prefix.length();
			
			while ( ( mTextLine2Width = paint.measureText( text ) ) > kMaxTextWidth ) {
				prefix = prefix.substring( 0, --n );
				text = prefix + "...";
			}
			
			n = (int) Math.ceil( mTextMarginLeft + mTextLine2Width + kTextMarginRight );
			
			if ( n > mWidth ) mWidth = n;
		}

		mTextLine2 = text;

		setVisibility( View.VISIBLE );

		requestLayout();
	}
}
