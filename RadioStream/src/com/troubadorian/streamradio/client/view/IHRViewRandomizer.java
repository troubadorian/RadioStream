package com.troubadorian.streamradio.client.view;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.troubadorian.streamradio.client.model.IHRConfigurationClient;
import com.troubadorian.streamradio.controller.IHRControllerRandomizer;
import com.troubadorian.streamradio.controller.R;
import com.troubadorian.streamradio.model.IHRAnimator;
import com.troubadorian.streamradio.model.IHRStation;

public class IHRViewRandomizer extends RelativeLayout implements Handler.Callback, View.OnClickListener {
	protected static final int			kAnimationRunTimeMs = 3000;
	protected static final int			kAnimationSpinTimeMinMs = 1000;
	protected static final int			kButtonSpacing = 10;
	protected static final int			kGlyphCount = 7;
	protected static final float		kGlyphImageResourceHeight = 88.0f;
	protected static final float		kGlyphImageResourceWidth = 48.0f;
	protected static final int			kGlyphIndexA = 10;
	protected static final int			kGlyphIndexBackground = 38;
	protected static final int			kGlyphIndexDash = 36;
	protected static final int			kGlyphIndexDot = 37;
	protected static final int			kGlyphMarginH = 6;
	protected static final int			kGlyphMarginV = 10;
	protected static final int			kGlyphMaxRandomElement = kGlyphIndexDot + 1;
	protected static final float		kGlyphRatioHtoW = kGlyphImageResourceHeight / kGlyphImageResourceWidth;
	protected static final float		kGlyphRatioWtoH = kGlyphImageResourceWidth / kGlyphImageResourceHeight;
	protected static final int			kGlyphResourceCount = 39;
	protected static final int			kGlyphRows = 1;
	protected static final int			kGlyphSpacing = 4;
	protected static final int			kGlyphsPerRow = kGlyphCount / kGlyphRows;
	protected static final int			kTextMarginH = 6;
	protected static final int			kTextMarginV = 2;
	protected static final int			kTextMarginV2 = kGlyphMarginV + 2;
	
	public static final int[]			kRandomizerImages = {
		R.drawable.randomizer_glyph_0 ,				//	kGlyphIndex0
		R.drawable.randomizer_glyph_1 ,
		R.drawable.randomizer_glyph_2 ,
		R.drawable.randomizer_glyph_3 ,
		R.drawable.randomizer_glyph_4 ,
		R.drawable.randomizer_glyph_5 ,
		R.drawable.randomizer_glyph_6 ,
		R.drawable.randomizer_glyph_7 ,
		R.drawable.randomizer_glyph_8 ,
		R.drawable.randomizer_glyph_9 ,
		R.drawable.randomizer_glyph_a ,				//	kGlyphIndexA
		R.drawable.randomizer_glyph_b ,
		R.drawable.randomizer_glyph_c ,
		R.drawable.randomizer_glyph_d ,
		R.drawable.randomizer_glyph_e ,
		R.drawable.randomizer_glyph_f ,
		R.drawable.randomizer_glyph_g ,
		R.drawable.randomizer_glyph_h ,
		R.drawable.randomizer_glyph_i ,
		R.drawable.randomizer_glyph_j ,
		R.drawable.randomizer_glyph_k ,
		R.drawable.randomizer_glyph_l ,
		R.drawable.randomizer_glyph_m ,
		R.drawable.randomizer_glyph_n ,
		R.drawable.randomizer_glyph_o ,
		R.drawable.randomizer_glyph_p ,
		R.drawable.randomizer_glyph_q ,
		R.drawable.randomizer_glyph_r ,
		R.drawable.randomizer_glyph_s ,
		R.drawable.randomizer_glyph_t ,
		R.drawable.randomizer_glyph_u ,
		R.drawable.randomizer_glyph_v ,
		R.drawable.randomizer_glyph_w ,
		R.drawable.randomizer_glyph_x ,
		R.drawable.randomizer_glyph_y ,
		R.drawable.randomizer_glyph_z ,
		R.drawable.randomizer_glyph_dash ,			//	kGlyphIndexDash
		R.drawable.randomizer_glyph_dot ,			//	kGlyphIndexDot
		R.drawable.randomizer_glyph_background ,	//	kGlyphIndexBackground
		R.drawable.randomizer_play ,
		R.drawable.randomizer_randomize ,
	};

	protected IHRAnimator				mAnimator;
	protected Thread					mAnimatorThread;
	protected ImageView					mButtonPlay;
	protected ImageView					mButtonRandomize;
	protected IHRControllerRandomizer	mDelegate;
	protected TextView					mDescription;
	protected int						mGlyphHeight;		// scaled height calculated based on manager size
	protected int						mGlyphWidth;		// scaled width calculated based on manager size
//	protected Object[]					mGlyphs = new Object[ kGlyphResourceCount ];
	protected Bitmap[]					mGlyphs;
	protected boolean					mHasRun;
	protected LCDGlyph[]				mLCDGlyphs;
	protected TextView					mName;
	protected int						mNavigationAmount;
	protected int						mStationIndex;
	protected String					mStationIdentifier;
	protected long[]					mStopTimes = new long[ kGlyphCount ];
	protected String[]					mText = new String[ 2 ];
	protected Random					mPRNG;
	
	public IHRViewRandomizer( IHRControllerRandomizer delegate ) {
		super( delegate.activity() );
		
		Context							context = delegate.activity();
		int								i, n;
		Resources						res = context.getResources();
		
		mDelegate = delegate;
		mAnimator = new IHRAnimator( this );
		
		mGlyphs = new Bitmap[ kGlyphResourceCount ];
		for ( i = 0 ; i < kGlyphResourceCount ; ++i ) {
			mGlyphs[ i ] = BitmapFactory.decodeResource( res, kRandomizerImages[i] );
		}

		mLCDGlyphs = new LCDGlyph[ n = kGlyphCount * kGlyphRows ];

		for ( i = 0; i < n; ++i ) addView( mLCDGlyphs[ i ] = new LCDGlyph() );
		for ( i = 0; i < kGlyphRows; ++i ) setText( i, "" );

		mPRNG = new Random();

		addView( mDescription = new TextView( context ) );
		addView( mName = new TextView( context ) );

		mName.setEllipsize( TextUtils.TruncateAt.END );
		mName.setGravity( Gravity.CENTER_HORIZONTAL );
		mName.setSingleLine();
		mName.setTextColor( Color.WHITE );
		mName.setTypeface( Typeface.SANS_SERIF, Typeface.BOLD );
		
		mDescription.setEllipsize( TextUtils.TruncateAt.END );
		mDescription.setGravity( Gravity.CENTER_HORIZONTAL );
		mDescription.setSingleLine();
		mDescription.setTextColor( Color.LTGRAY );
		mDescription.setTextSize( 13.0f );
		mDescription.setTypeface( Typeface.SANS_SERIF, Typeface.ITALIC );

		addView( mButtonPlay = new ImageView( context ) );
		addView( mButtonRandomize = new ImageView( context ) );
		
		mButtonPlay.setImageResource( R.drawable.randomizer_play );
		mButtonPlay.setOnClickListener( this );
		mButtonRandomize.setImageResource( R.drawable.randomizer_randomize );
		mButtonRandomize.setOnClickListener( this );

		randomize();
	}
	
	public boolean handleMessage( Message message ) {
		if ( message.obj != mAnimatorThread ) return false;
		
		if ( message.what == IHRAnimator.kMessageAnimationStepped ) {
			animationStepped( message.arg1 );
		} else if ( message.what == IHRAnimator.kMessageAnimationStopped ) {
			animationStopped();
		} else {
			return false;
		}
		
		return true;
	}

	
	public void onClick( View view ) {
		if ( view == mButtonPlay ) mDelegate.onPlay( mStationIdentifier );
		else if ( view == mButtonRandomize ) randomize();
	}

	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		// during first layout pass we calculate the maximum height and width of the lcd glyphs
		
		int								buttonHeight, buttonWidth;
		int								descriptionHeight, nameHeight;
		int								fullHeight, fullWidth, height, maxHeight, maxWidth, width;
		int								i, j, n;
		int								offsetX, offsetY;
		
		height = b - t;
		width = r - l;

		buttonHeight = 25;
		buttonWidth = 85;

		descriptionHeight = 21;
		nameHeight = 21;

		// during first layout pass we calculate the maximum height and width of the lcd glyphs

		maxHeight = height;
		maxHeight -= kGlyphMarginV * 3;		// top, bottom, and between bottom glyph row and text
		maxHeight -= nameHeight;
		maxHeight -= kTextMarginV;			// between the name & description
		maxHeight -= descriptionHeight;
		maxHeight -= kTextMarginV2;			// between the description & buttons
		maxHeight -= buttonHeight;
		maxHeight /= kGlyphRows;

		// horizontal margins are left, right, and between glyphs
		maxWidth = ( width - kGlyphMarginH * 2 - kGlyphSpacing * ( kGlyphsPerRow - 1 ) ) / kGlyphsPerRow;

		mGlyphHeight = (int)(maxWidth * kGlyphRatioHtoW);

		if ( mGlyphHeight > maxHeight ) {
			mGlyphHeight = maxHeight;
			mGlyphWidth = (int)(mGlyphHeight * kGlyphRatioWtoH);
		} else {
			mGlyphWidth = maxWidth;
		}

		if ( mGlyphHeight > kGlyphImageResourceHeight || mGlyphWidth > kGlyphImageResourceWidth ) {
			mGlyphHeight = (int) kGlyphImageResourceHeight;
			mGlyphWidth = (int) kGlyphImageResourceWidth;
		}

		fullHeight = mGlyphHeight * kGlyphRows;
		fullHeight += kGlyphMarginV;			// exclude top & bottom margins
		fullHeight += nameHeight;
		fullHeight += kTextMarginV;
		fullHeight += descriptionHeight;
		fullHeight += kTextMarginV2;
		fullHeight += buttonHeight;

		fullWidth = mGlyphWidth * kGlyphsPerRow + kGlyphSpacing * ( kGlyphsPerRow - 1 );

		offsetX = width / 2 - fullWidth / 2;
		offsetY = height / 2 - fullHeight / 2;

		for ( i = 0; i < kGlyphRows; ++i ) {
			for ( j = kGlyphsPerRow * i, n = j + kGlyphsPerRow; j < n; ++j, offsetX += mGlyphWidth + kGlyphSpacing ) {
				mLCDGlyphs[ j ].layout( offsetX, offsetY, offsetX + mGlyphWidth, offsetY + mGlyphHeight );
			}

			offsetX = width / 2 - fullWidth / 2;
			offsetY += mGlyphHeight + kGlyphMarginV;
		}

		fullWidth = width - kTextMarginH * 2;

		mName.layout( kTextMarginH, offsetY, kTextMarginH + fullWidth, offsetY + 21 );				// text fields are 21px tall

		offsetY += 21 - 2;

		mDescription.layout( kTextMarginH, offsetY, kTextMarginH + fullWidth, offsetY + 21 );		// text fields are 21px tall

		offsetY += 21 + kTextMarginV2;

		fullWidth = buttonWidth * 2 + kButtonSpacing;

		offsetX = ( width - fullWidth ) / 2;
		
		mButtonRandomize.layout( offsetX, offsetY, offsetX + buttonWidth, offsetY + buttonHeight );

		offsetX += buttonWidth + kButtonSpacing;
		
		mButtonPlay.layout( offsetX, offsetY, offsetX + buttonWidth, offsetY + buttonHeight );
	}
	
	public void animationStepped( int totalRunTimeMs ) {
		LCDGlyph						glyph;
		int								i, n, o, p;

		for ( i = 0, n = mLCDGlyphs.length; i < n; ++i ) {
			glyph = mLCDGlyphs[ i ];

			if ( totalRunTimeMs < mStopTimes[ i ] ) {
				o = glyph.getGlyphIndex();

				do { p = ( mPRNG.nextInt() & 0x7fffffff ) % kGlyphMaxRandomElement; } while ( o == p );

				glyph.setGlyphIndex( p );
			} else {
				glyph.setChar( mText[ i / kGlyphsPerRow ].charAt( i % kGlyphsPerRow ) );
			}
		}
	}

	public void animationStopped() {
		IHRStation						station;

		station = IHRConfigurationClient.shared().stationForIndex( mStationIndex );

		mDescription.setText( station.getDescription() );
		mName.setText( station.getName() );
	}

	// randomizer only deals with (and consumes all) horizontal movement

	public void randomize() {
		IHRConfigurationClient			client = IHRConfigurationClient.shared();
		IHRStation						station;
		int								count = client.stationCount();
		
		do {
			mStationIndex = ( new Random().nextInt() & 0x7fffffff ) % count;
			station = client.stationForIndex( mStationIndex );
		} while ( station.getCallLetters().charAt( 0 ) == '!' );

		mDescription.setText( "" );
		mName.setText( "" );
		mStationIdentifier = station.getCallLetters();

		randomize( mStationIdentifier );
	}

	public boolean wantsBanner() { return false; }

	// protected methods

	protected void randomize( String text ) {
		int								i, n;

		setText( 0, text );

		n = kAnimationRunTimeMs - kAnimationSpinTimeMinMs;

		for ( i = 0; i < kGlyphCount; ++i ) {
			mStopTimes[ i ] = ( mPRNG.nextInt() & 0x7fffffff ) % n + kAnimationSpinTimeMinMs;
		}

		mAnimatorThread = mAnimator.start( kAnimationRunTimeMs );
	}

	protected void setText( int row, String text ) {
		int								n;

		if ( text == null ) text = "";

		if ( ( n = text.length() ) >= kGlyphsPerRow ) {
			mText[ row ] = text.substring( 0, kGlyphsPerRow );
		} else {
			mText[ row ] = " ";
			for ( n = kGlyphsPerRow - n - 1; n > 0; --n ) mText[ row ] += " ";
			mText[ row ] += text;
		}
	}

	// protected classes

	protected class LCDGlyph extends View {
		protected int					mGlyphIndex = -1;

		public LCDGlyph() {
			super( IHRViewRandomizer.this.getContext() );
			
			setBackgroundColor( 0xFF000000 );
			setWillNotDraw( false );
		}
		
		public int getGlyphIndex() { return mGlyphIndex; }

		public void setChar( char c ) {
			int							i;

			if ( c >= '0' && c <= '9' ) i = c - '0';
			else if ( c >= 'a' && c <= 'z' ) i = c - 'a' + kGlyphIndexA;
			else if ( c >= 'A' && c <= 'Z' ) i = c - 'A' + kGlyphIndexA;
			else if ( c == '-' ) i = kGlyphIndexDash;
			else if ( c == '.' ) i = kGlyphIndexDot;
			else i = -1;

			if ( i != mGlyphIndex ) {
				mGlyphIndex = i;
				invalidate();
			}
		}

		public void setGlyphIndex( int index ) { mGlyphIndex = index; invalidate(); }

		// protected methods

		@Override
		protected void onDraw( Canvas canvas ) {
			Rect				rect;
			
			rect = new Rect( 0, 0, getWidth(), getHeight() );
			
			canvas.drawBitmap( mGlyphs[ kGlyphIndexBackground ], null, rect, null );
			if ( mGlyphIndex >= 0 ) canvas.drawBitmap( mGlyphs[ mGlyphIndex ], null, rect, null );
		}
	}
}
