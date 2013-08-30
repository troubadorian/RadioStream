package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.model.IHRThreadable;
import com.troubadorian.streamradio.controller.IHRControllerPlayer;
import com.troubadorian.streamradio.controller.R;
import com.troubadorian.streamradio.model.IHRBroadcaster;
import com.troubadorian.streamradio.model.IHRCache;
import com.troubadorian.streamradio.model.IHRListener;

public class IHRViewPlayer extends RelativeLayout implements View.OnClickListener, OnSeekBarChangeListener {
	public IHRControllerPlayer			mDelegate;
	
	public IHRLargeAd					mAdLarge;
	public ImageView					mAdSmall;
	public TextView						mArtist;
	public View							mHeader;
//	public IHRViewInfoSeparator			mInfo;
	public TextView						mTrack;
	public TextView						mRight;
	public IHRViewSlider				mVolume;
	
	public Bitmap						mAdSmallBitmap;
	
	protected ImageView					mGlow;
	protected IHRPlayerLogo				mLogo;
	protected ImageView					mToggleTransport;
	protected IHRProgressLine			mProgressLine;
	protected IHRViewButton				mLyricsButton;
//	protected ImageView					mLyrics;
	protected ImageView					mPlay;
//	protected ImageView					mTag;
	
	public IHRViewPlayer( IHRControllerPlayer delegate ) {
		super( delegate.activity() );
		
		Context							context = delegate.activity();
//		Resources						res = getResources();
		
		mDelegate = delegate;
		
		addView( mGlow = new ImageView( context ) );			// glow is behind everything
		
		/**
		mAdSmall = new ImageView( context );
		mAdSmall.setScaleType( ImageView.ScaleType.FIT_END );
		/*/
		mAdSmall = new ImageView( context ) {
			@Override
			protected void onDraw( Canvas canvas ) {
				int				b, bitmapH, bitmapW, l, r, t, viewH, viewW;
				Rect			dst, src;
				Paint			paint;
				
				if ( mAdSmallBitmap == null ) return;

				//	TODO: make ad translucent
				canvas.drawARGB( 255, 0, 0, 0 );

				bitmapH = mAdSmallBitmap.getHeight();
				bitmapW = mAdSmallBitmap.getWidth();
				viewH = getHeight();
				viewW = getWidth();

				b = bitmapH;
				l = bitmapW > viewW ? bitmapW - viewW : 0;
				r = bitmapW;
				t = 0;

				src = new Rect( l, t, r, b );
				
				b = bitmapH < viewH ? bitmapH : viewH;
				l = bitmapW < viewW ? viewW - bitmapW : 0;
				r = viewW;
				
				dst = new Rect( l, t, r, b );
				paint = new Paint();
				paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC_OVER ) );
				canvas.drawBitmap( mAdSmallBitmap, src, dst, paint );				
			}
		};
		/**/
		
		addView( mAdSmall );
		
		addView( mHeader = new View( context ) );
//		addView( mInfo = new IHRViewInfoSeparator( context ) );
		addView( mRight = new TextView( context ) );
		addView( mLogo = new IHRPlayerLogo( context ) );
//		addView( mLyrics = new ImageView( context ) );
		addView( mLyricsButton = new IHRViewButton( context ) );
		addView( mPlay = new ImageView( context ) );
//		addView( mTag = new ImageView( context ) );
		addView( mArtist = new TextView( context ) );
		addView( mTrack = new TextView( context ) );
		addView( mVolume = new IHRViewSlider( context ) );

		addView( mAdLarge = new IHRLargeAd( context ) );		// large ad is in front of everything

		mAdLarge.setVisibility( View.INVISIBLE );
		mAdSmall.setOnClickListener( this );
		mAdSmall.setVisibility( View.INVISIBLE );
		
		mGlow.setImageResource( R.drawable.glow );
		
		//**
		mLyricsButton.setText( "Lyrics" );
		mLyricsButton.setEnabled( false );
		mLyricsButton.setOnClickListener( this );
		/*/
		mLyrics.setEnabled( false );
		mLyrics.setImageResource( R.drawable.lyrics_button_disabled );
		mLyrics.setOnClickListener( this );
		/**/
		
		/*
		mTag.setEnabled( false );
		mTag.setImageResource( R.drawable.tag_button_disabled );
		mTag.setOnClickListener( this );
		*/
		
		mHeader.setBackgroundResource( R.drawable.player_header_1x34px_bg_grey );
		
		mPlay.setOnClickListener( this );
		mPlay.setScaleType( ImageView.ScaleType.CENTER );
		
		mRight.setEllipsize( TextUtils.TruncateAt.END );
		mRight.setGravity( Gravity.RIGHT );
		mRight.setSingleLine();
		mRight.setTextColor( Color.WHITE );
		mRight.setTextSize( 12.0f );
		mRight.setTypeface( Typeface.SANS_SERIF, Typeface.NORMAL );
		
		mArtist.setEllipsize( TextUtils.TruncateAt.END );
		mArtist.setGravity( Gravity.LEFT );
		mArtist.setSingleLine();
		mArtist.setTextColor( Color.LTGRAY );
		mArtist.setTextSize( 13.0f );
		mArtist.setTypeface( Typeface.SANS_SERIF, Typeface.ITALIC );
		
		mTrack.setEllipsize( TextUtils.TruncateAt.END );
		mTrack.setGravity( Gravity.LEFT );
		mTrack.setSingleLine();
		mTrack.setTextColor( Color.WHITE );
		mTrack.setTypeface( Typeface.SANS_SERIF, Typeface.BOLD );
		
//		mVolume.setThumb( ?? );
		mVolume.setOnSeekBarChangeListener( this );
		mVolume.setMax( 100 );
	}
	
	public void onClick( View view ) {
		if ( view == mAdLarge.mBackground ) mDelegate.adClicked();
		else if ( view == mAdLarge.mClose ) {
			mDelegate.setAdLargeHidden( true, false );
			mGlow.setVisibility(VISIBLE);
			mLogo.setVisibility(VISIBLE);
		}
		else if ( view == mToggleTransport ) mDelegate.toggleTransport();
		else if ( view == mLyricsButton ) {
			mDelegate.showLyrics();
		} else if ( view == mAdSmall ) {
//			IHRAd.shared().report( IHRAd.kAdPurposeClickPlayerSmall );
			mLogo.setVisibility(INVISIBLE);
			mGlow.setVisibility(INVISIBLE);
			mDelegate.setAdLargeHidden( false, true );
		}
		else if ( view == mPlay ) {
			mDelegate.togglePlay();
			mLogo.setBackAvailable( false );
		}
//		else if ( view == mTag ) mDelegate.doTagSong();
	}
	
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		int						c, h, n, w, x, y;
//		int						trackY;
		
		final int				kAdSmallHeight = 20;
		final int				kInfoHeight = 35;
		final int				kTextFieldHeight = 21;
		
		super.onLayout( changed , l , t , r , b );
		
		h = b - t;
		w = r - l;
		y = kInfoHeight;
		
		n = mRight.getMeasuredHeight();
		c = ( kInfoHeight - n ) / 2 - 1;
		mRight.layout( w*2/3 , c-10 , w-4 , c+n );//Code changed by sriram on 08-18-2010
		
		// add header and small add
//		mInfo.layout( 0, 0, w, y );
		mHeader.layout( 0, 0, w, y );
		mAdSmall.layout( 0, y, w, y + kAdSmallHeight );
		
		if ( null != mToggleTransport ) {
			Rect				f = mToggleTransport.getDrawable().getBounds();
			
			mProgressLine.layout( 0, kInfoHeight - 1, w, kInfoHeight );
			mToggleTransport.layout( r - f.width() - 5 , 0 , r, kInfoHeight );
		}
		
		// now build the layout from the bottom up:
		
		x = ( w - 58 - 58 - 18 ) / 2;			// 58 = lyrics,tag button width, 18 = spacing between buttons
		y = h - 12 - 25;						// 25 = lyrics,tag button height, 12 = bottom margin
		
		mPlay.layout( x, y, x + 55, y + 24 );
//		mLyrics.layout( x, y, x + 58, y + 25 );
		
		x += 58 + 18;
		
//		mTag.layout( x, y, x + 58, y + 25 );
		mLyricsButton.layout( x, y, x + 55, y + 24 );

		x = 14;									// margin on left of play button, appears to be 20px because button is 19x19 but hit region is 31x31, so 20 - ( 31 - 19 ) / 2 = 14
		y -= 6 + 31;							// 6 = margin on top of lyrics/tag button, appears to be 12 b/c image is 19x19 but hit region is 31x31, so 12 - (31 - 19 ) / 2 = 6
		
//		mPlay.layout( x, y, x + 31, y + 31 );
		
//		x += 31 + 12;							// apparent 18px distance between play button and volume slider
		x = 20;
		y += ( 31 - 24 ) / 2;					// height of volume slider is 18px
		
		mVolume.layout( x, y, w - x, y + mVolume.getMeasuredHeight() );
		
		x = 4;
//		y -= 6 + kTextFieldHeight - 5;			// reset y to top of play button field (+ 6), subtract 21 px for height of artist field (let some of the margin come from the text field itself)
		y = kTextFieldHeight - 5;
		
		mArtist.layout( x, y, x + w*2/3+55, y + kTextFieldHeight );
		
		y = 0;				// The track field is 21px tall but we let them overlap a bit to move the text closer together vertically
		
		mTrack.layout( x, y, x + w*2/3+55, y + kTextFieldHeight );
		
		y = h - 12 - 25;						// 25 = lyrics,tag button height, 12 = bottom margin
		y -= 6 + 31;							// 6 = margin on top of lyrics/tag button, appears to be 12 b/c image is 19x19 but hit region is 31x31, so 12 - (31 - 19 ) / 2 = 6
		y += ( 31 - 18 ) / 2;					// height of volume slider is 18px
		y -= 6 + kTextFieldHeight - 5;			// reset y to top of play button field (+ 6), subtract 21 px for height of artist field (let some of the margin come from the text field itself)
		y -= kTextFieldHeight - 2;				// The track field is 21px tall but we let them overlap a bit to move the text closer together vertically
		
		n = kAdSmallHeight + kInfoHeight;
		h = y - n;								// h is the available vertical space remaining between the top of the track field and the bottom of the small ad
		c = n + h / 2;							// c is the vertical center of the remaining available space
		
		n = h - 12 + 12;						// leave a 12px margin at top and bottom;
		if ( n > 200 ) n = 200;					// the logo is 200x200 px from dart.  limit if necessary
		
		x = ( w - n ) / 2;
		y = c - n / 2;
		
		mLogo.layout( x, y, x + n, y + n );

		if ( ( n = 320 ) > w ) n = w;			// the large ad is 320x210 from dart.  constrain if necessary.
		
		y = kInfoHeight;						// large ad overlaps small ad
		
		mAdLarge.layout( 0, y, w, y+210 );

		x = ( w - 320 ) / 2;					// glow is 320/263
		y = c - 263 / 2;
		
		mGlow.layout( x, y, x + 320, y + 263 );
	}
	
	public void setSmallAd( Bitmap bitmap ) {
		mAdSmallBitmap = bitmap;
//		mAdSmall.setImageBitmap( bitmap );
	}
	
	public void setLogoBack( Bitmap bitmap ) { mLogo.setBack( bitmap ); }
	public void setLogoFront( Bitmap bitmap ) { mLogo.setFront( bitmap ); }
	public void setLogoFrontResource( int inResourceID ) { mLogo.setFrontResource( inResourceID ); }
	
	public void setInfoTextLeft( String text ) {
//		mInfo.mLeft.setText( text );
		mArtist.setText( text );
	}
	
	public void setInfoTextRight( String text ) {
//		if ( null == mToggleTransport ) mInfo.mRight.setText( text );
		//Code changed by sriram on 08-19-2010
		//mLogo.mHandler.sendEmptyMessageDelayed(2,2000);

		if ( null == mToggleTransport ) mRight.setText( text );
	}
	
	public void updatePlaying( int inPlaying ) {
//		mPlay.setImageResource( ( inPlaying > 0 ) ? R.drawable.control_stop : R.drawable.control_play );
		if(mToggleTransport == null){
			mPlay.setImageResource( ( inPlaying > 0 ) ? R.drawable.btn_stop : R.drawable.btn_play );
		}else{
			mPlay.setImageResource( ( inPlaying > 0 ) ? R.drawable.btn_pause : R.drawable.btn_play );
		}
	}
	
	public void setLyricsEnabled( boolean enabled ) {
		//**
		mLyricsButton.setEnabled( enabled );
		/*/
		mLyrics.setEnabled( enabled );
		mLyrics.setImageResource( enabled ? R.drawable.btn_lyrics : R.drawable.lyrics_button_disabled );
		/**/
	}
	
	public void setArtistAndTrack( String inArtist , String inTrack ) {
		//code added by sriram
		if((inArtist==null||inTrack==null) ||(inArtist.trim().length()<=0||inTrack.trim().length()<=0))
			return;
//code ends here
		String					label = ( null == inArtist ) ? "" : inArtist;
		
		if ( null != inTrack && 0 != inTrack.length() ) {
			if ( label.length() > 0 ) label += " - ";
			label += inTrack;
		}
		
		mArtist.setText( label );
	}
	
	public void setNameAndDescription( String inName , String inDescription ) {
		mTrack.setText( inName );
		mArtist.setText( inDescription );
	}
	
	/*
	public void setTagging( int inTagging ) {
		mTag.setEnabled( inTagging > 0 );
		mTag.setImageResource( ( inTagging > 0 ) ? ( inTagging > 1 ) ? R.drawable.tag_button_down : R.drawable.tag_button_up : R.drawable.tag_button_disabled );
	}
	*/
	
	public void setVolume( int volume ) {
		if ( volume < 0 ) volume = 0;
		if ( volume > 100 ) volume = 100;
		
		mVolume.setProgress( volume );
	}
	
	public void updateThroughput( String message ) {
		setInfoTextLeft( message );
	}
	
	public void updateVolume( float inVolume ) {
		setVolume( (int)( inVolume * 100 ) );
	}
	
	public void updateSeekable( int inSeekable , String inURL ) {
		if ( inSeekable > 0 ) {
			if ( null == mToggleTransport ) {
				setInfoTextRight( "" );
				addView( mToggleTransport = new ImageView( getContext() ) );
				
				mToggleTransport.setOnClickListener( this );
				mToggleTransport.setScaleType( ImageView.ScaleType.CENTER );
//				mToggleTransport.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT ) );
				mToggleTransport.setImageResource( R.drawable.mini_transport );
			}
			
			if ( null == mProgressLine ) {
				addView( mProgressLine = new IHRProgressLine( getContext() ) );
				
				mProgressLine.setBackgroundColor( Color.WHITE );
				mProgressLine.updateWithURL( inURL );
			}
			
			mToggleTransport.setAlpha( inSeekable > 1 ? 255 : 191 );
		} else if ( null != mToggleTransport ) {
			this.removeView( mToggleTransport );
			mToggleTransport = null;
			
			this.removeView( mProgressLine );
			mProgressLine.updateWithURL( null );
			mProgressLine = null;
		}
	}
	
	public void onProgressChanged( SeekBar inBar , int inValue , boolean inFromUser ) {
		if ( inFromUser ) {
			mDelegate.assignVolume( (float)( inBar.getProgress() * 0.01 ) );
		}
	}

	public void onStartTrackingTouch( SeekBar inBar ) {
		
	}

	public void onStopTrackingTouch( SeekBar inBar ) {
		
	}
	
	public class IHRProgressLine extends View implements IHRListener {
		public int						mCurrent;
		public int						mMaximum;
		public String					mURL;
		
		public IHRProgressLine( Context context ) {
			super( context );
			
//			setWillNotDraw( false );
		}
		
		@Override
		protected void onDraw( Canvas canvas ) {
//			super.onDraw( canvas );
			
			if ( mCurrent > 0 && mCurrent < mMaximum ) {
				Rect				frame;
				Paint				paint = new Paint();
				
				paint.setStyle( Style.FILL );
				
				frame = new Rect( 0, 0, getWidth(), getHeight() );
				paint.setARGB( 255 , 255 , 255 , 255 );
				canvas.drawRect( frame , paint );
				
				frame.right = (int)((long) getWidth() * mCurrent / mMaximum);
				paint.setARGB( 255 , 255 , 0 , 0 );
				canvas.drawRect( frame , paint );
			}
		}
		
		public void setProgress( int inCurrent , int inMaximum ) {
			mCurrent = ( inCurrent > 0 ) ? inCurrent : 0;
			mMaximum = ( inMaximum > 0 ) ? inMaximum : 1;
			
			setWillNotDraw( !( mCurrent > 0 && mCurrent < mMaximum ) );
			invalidate();
		}
		
		public void setProgress( IHRHashtable inProgress ) {
			int					offset = ( null == inProgress ) ? 0 : inProgress.integerValue( "offset" , 0 );
			int					length = ( null == inProgress ) ? 0 : inProgress.integerValue( "length" , 0 );
			
			setVisibility( offset < length ? VISIBLE : GONE );
			setProgress( offset , length );
		}
		
		public void listen( String inName , IHRHashtable inDetails ) {
			if ( null != mURL && inName.equals( IHRCache.kNotifyNameData ) ) {
				String					url = ( null == inDetails ) ? null : inDetails.stringValue( "url" , null );
				
				if ( null != url && url.equals( mURL ) ) {
					setProgress( inDetails );
				}
			}
		}
		
		public void updateWithURL( String inURL ) {
			if ( inURL == null ) {
				IHRBroadcaster.common().removeFor( IHRCache.kNotifyNameData , this );
			} else {
				IHRBroadcaster.common().listenFor( IHRCache.kNotifyNameData , this );
				
				setProgress( IHRCache.shared().progressForURL( inURL ) );
			}
			
			mURL = inURL;
		}
	}
	
	public class IHRLargeAd extends RelativeLayout {
		protected ImageView				mBackground;
		protected boolean				mBackgroundSet;
		
		public ImageView				mClose;
		
		public IHRLargeAd( Context context ) {
			super( context );
			
			setWillNotDraw( false );
			
			addView( mBackground = new ImageView( context ) );
			addView( mClose = new ImageView( context ) );

			mBackground.setOnClickListener( IHRViewPlayer.this );

			mClose.setImageResource( R.drawable.closebutton );
			mClose.setOnClickListener( IHRViewPlayer.this );
			mClose.setVisibility( View.INVISIBLE );
		}
		
		public void setBackground( Bitmap bitmap ) {
			if ( bitmap != null ) {
				mBackground.setImageBitmap( bitmap );
				mBackgroundSet = true;
				
				requestLayout();
			}
		}
		
		@Override
		protected void onDraw( Canvas canvas ) {
			if ( ! mBackgroundSet ) return;

			canvas.drawARGB( 255, 0, 0, 0 );

			super.onDraw( canvas );
		}
		
		@Override
		protected void onLayout( boolean change, int l, int t, int r, int b ) {
			int					bitmapH, bitmapW, h, w;
			float				scale, scaleH, scaleW;
			
			if ( ! mBackgroundSet ) return;
			
			h = b - t;
			w = r - l;
			
			bitmapH = mBackground.getDrawable().getIntrinsicHeight();
			bitmapW = mBackground.getDrawable().getIntrinsicWidth();
			
			scaleH = bitmapH > h ? (float) h / (float) bitmapH : 1.0f;
			scaleW = bitmapW > w ? (float) w / (float) bitmapW : 1.0f;
			
			if ( ( scale = scaleH < scaleW ? scaleH : scaleW ) < 1.0f ) {
				bitmapH = (int)(bitmapH * scale);
				bitmapW = (int)(bitmapW * scale);
			}
			
			l = ( w - bitmapW ) / 2;
			r = l + bitmapW;
//			t = ( w - bitmapH ) / 2;
			t = ( h - bitmapH ) / 2;
//			t = 0;	//	align to top not center
			b = t + bitmapH;
			
			mBackground.layout( l, t, r, b );
			
			bitmapH = mClose.getDrawable().getIntrinsicHeight();
			bitmapW = mClose.getDrawable().getIntrinsicWidth();
			
			l = r - 8 - bitmapW;
			r = l + bitmapW;
			t += 8;
			b = t + bitmapH;
			
			mClose.layout( l, t, r, b ); 
		}
	}
}


final class IHRPlayerLogo extends RelativeLayout implements Animation.AnimationListener, Runnable, View.OnClickListener {
	protected ImageView					mBack;
	protected boolean					mBackAvailable;
	protected boolean					mDisplayingBack;
	protected ImageView					mFront;
	
	public IHRPlayerLogo( Context context ) {
		super( context );
		
		addView( mBack = new ImageView( context ) );
		addView( mFront = new ImageView( context ) );

		setOnClickListener( this );
		
		mBack.setScaleType( ImageView.ScaleType.CENTER_INSIDE );
		mBack.setVisibility( View.INVISIBLE );
		mFront.setScaleType( ImageView.ScaleType.CENTER_INSIDE );
		
		setFrontResource( R.drawable.no_station_logo );
		
		IHRThreadable.gMain.remove( this ); // remove any existing messages from queue to prevent stacking of requests
		IHRThreadable.gMain.handle( this , 10000 ); // add new message to kick off image rotation
		//Code inserted by sriram for handling automatic switch of album art 08-19-2010
		//mHandler.sendEmptyMessageDelayed(2, 10000);

	}

//Code for displaying the Album art logo //Sriram on 08-19-2010
/*    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        		case 2:display( mDisplayingBack ); break;
        		default: break;
        	}
        }
      };*/
//Code ends here

	public void display( boolean front ) {
		Animation				animation;
		View					hide, show;
		if (this.isShown() ) { // don't worry about the display if the screen is not visible
			try {
				IHRThreadable.gMain.remove( this ); //get rid of any messages in the queue
				IHRThreadable.gMain.handle( this , 10000 ); //add new request to display again in 10 seconds
			} catch (Exception e) {
				// thread is not available
			}
			//System.out.println("We are calling the image swap with " + (mBackAvailable ? "a " : "no ") + "background image.");
			if ( front && ! mDisplayingBack ) return;
			if ( ! front && mDisplayingBack ) return;
			if ( ! front && ! mBackAvailable ) return;
			
			if ( ( mDisplayingBack = ! front ) ) {
				hide = mFront;
				show = mBack;
			} else {
				hide = mBack;
				show = mFront;
			}
			//Code inserted by sriram for handling automatic switch of album art 08-19-2010
			//onClick( show );
			//code ends here
			
			setClickable( false );
	
			show.setVisibility( View.VISIBLE );
			
			animation = AnimationUtils.loadAnimation( getContext(), android.R.anim.fade_out );
			animation.setAnimationListener( this );
			animation.setDuration( 333 );
	
			hide.startAnimation( animation );
	
			animation = AnimationUtils.loadAnimation( getContext(), android.R.anim.fade_in );
			animation.setAnimationListener( this );
			animation.setDuration( 333 );
	
			show.startAnimation( animation );
		}
	}
	
	public void run() {
		display( mDisplayingBack );
	}
	
	public boolean handleMessage( Message message ) {
		display( mDisplayingBack );
		return true;
	}

	public void onAnimationEnd( Animation animation ) {
		if ( mDisplayingBack ) {
			mBack.setVisibility( View.VISIBLE );
			mFront.setVisibility( View.INVISIBLE );
		} else {
			mBack.setVisibility( View.INVISIBLE );
			mFront.setVisibility( View.VISIBLE );
		}
		
		setClickable( true );
	}

	public void onAnimationRepeat( Animation animation ) { }
	public void onAnimationStart( Animation animation ) { }
	
	public void setBack( Bitmap bitmap ) {
		if ( mBackAvailable = ( bitmap != null ) ) {
			mBack.setImageBitmap( bitmap );
		} else {
			setBackResource( R.drawable.no_album_art );
			display( true );
		}
	}
	
	public void setBackResource( int inResourceID ) {
		if ( 0 != inResourceID ) mBack.setImageResource( inResourceID );
	}

	public void setBackAvailable( boolean available ) {
		if ( ! ( mBackAvailable = available ) ) display( true );
	}
	
	public void setFront( Bitmap bitmap ) {
		if ( bitmap != null ) mFront.setImageBitmap( bitmap );
	}

	public void setFrontResource( int inResourceID ) {
		if ( 0 != inResourceID ) mFront.setImageResource( inResourceID );
	}

	public void onClick( View view ) {
		//IHRThreadable.gMain.remove( this );
		display( mDisplayingBack );
		//IHRThreadable.gMain.handle( this , 6000 );
	}
	
	@Override
	public void onLayout( boolean changed, int l, int t, int r, int b ) {
		int						h, w;
		
		h = b - t;
		w = r - l;
		
		mBack.layout( 0, 0, w, h );
		mFront.layout( 0, 0, w, h );
	}
	
	@Override
	protected void  onDetachedFromWindow  () {
		if (null != IHRThreadable.gMain) {
			IHRThreadable.gMain.remove( this ); //make sure we are not trying to run the display when this view is not on screen.
		}
	}
}
