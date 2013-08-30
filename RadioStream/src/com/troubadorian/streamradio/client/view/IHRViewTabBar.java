package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.troubadorian.streamradio.controller.R;

public class IHRViewTabBar extends RelativeLayout implements View.OnClickListener {
	public static final int		kTabBarHeight = 50;
	
	public static final int		kTabBarButtonStateUp = 0;
	public static final int		kTabBarButtonStateDown = 1;
	public static final int		kTabBarButtonStateOver = 2;
	public static final int		kTabBarButtonStateOffline = 3;
	public static final int		kTabBarButtonStateCount = 4;
	
	public static final int		kTabBarButtonCategories = 0;
	public static final int		kTabBarButtonFavorites = 1;
	public static final int		kTabBarButtonRandomizer = 2;
	public static final int		kTabBarButtonSettings = 3;
	public static final int		kTabBarButtonPlayer = 4;	//	not real button
	public static final int		kTabBarButtonCount = 4;
	
	public static final int		kPositionLeft = -1;
	public static final int		kPositionCenter = 0;
	public static final int		kPositionRight = 1;
	
	public static final String[]kTabBarButtonNames = {"Home","Favorites","Randomizer","Settings","Player"};
	public static final int[]	kTabBarButtonColors = { 0xFFCCCCCC,0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF };
	
	protected IHRTabBarButton[]	mButtons = new IHRTabBarButton[ kTabBarButtonCount ];
	protected IHRViewMain		mDelegate;
	
	protected Bitmap[]			mBitmaps;
	public static final int		kIndexIcons = 0;
	public static final int		kIndexBacks = kIndexIcons + kTabBarButtonCount * kTabBarButtonStateCount;
	public static final int[]	kResources = {
		R.drawable.tabbar_icon_categories_up , R.drawable.tabbar_icon_categories_down , R.drawable.tabbar_icon_categories_over , R.drawable.tabbar_icon_categories_over ,
		R.drawable.tabbar_icon_favorites_up , R.drawable.tabbar_icon_favorites_down , R.drawable.tabbar_icon_favorites_over , R.drawable.tabbar_icon_favorites_over ,
		R.drawable.tabbar_icon_random_up , R.drawable.tabbar_icon_random_down , R.drawable.tabbar_icon_random_over , R.drawable.tabbar_icon_random_over ,
		R.drawable.tabbar_icon_settings_up , R.drawable.tabbar_icon_settings_down , R.drawable.tabbar_icon_settings_over , R.drawable.tabbar_icon_settings_over ,
		
		R.drawable.tabbar_up_left_cap , R.drawable.tabbar_down_left_cap , R.drawable.tabbar_over_left_cap , R.drawable.tabbar_over_left_cap ,
		R.drawable.tabbar_up_fill , R.drawable.tabbar_down_fill , R.drawable.tabbar_over_fill , R.drawable.tabbar_over_fill ,
		R.drawable.tabbar_up_right_cap , R.drawable.tabbar_down_right_cap , R.drawable.tabbar_over_right_cap , R.drawable.tabbar_over_right_cap ,
	};
	
	public IHRViewTabBar( Context context, IHRViewMain delegate ) {
		super( context );

		int						i;
		
		mDelegate = delegate;

		for ( i = 0; i < kTabBarButtonCount; ++i ) {
			mButtons[ i ] = new IHRTabBarButton( context, i );
			mButtons[ i ].setOnClickListener( this );
			
			addView( mButtons[ i ] );
		}

		mButtons[ kTabBarButtonCategories ].setIsDown( true );
	}
	
	public void onClick( View view ) {
		if ( this.isEnabled() ) {
			int						type = ((IHRTabBarButton) view).mType; 
			
			setSelectedTab( type );
			
			mDelegate.setSelectedTab( type );
		}
	}
	
	public void setSelectedTab( int which ) {
		int						i;
		
		if ( !isEnabled() ) {
			which = -1;
		}
		
		for ( i = 0; i < kTabBarButtonCount; ++i ) {
			if ( which < 0 ) mButtons[ i ].setState( i == kTabBarButtonCategories ? kTabBarButtonStateDown : kTabBarButtonStateOffline );
			else mButtons[ i ].setIsDown( i == which );
		}
	}
	
	// protected methods
	
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		int								h, i, n, w, x;
		
		h = b - t;
		w = r - l;
		n = w / kTabBarButtonCount;
		x = 0;
		
		for ( i = 0; i < kTabBarButtonCount; ++i, x += n ) {
			mButtons[ i ].layout( x, 0, x + n, h );
		}
	}
	
	private Bitmap imageForResource( int inIndex ) {
		Bitmap					result = null;
		
		if ( null == mBitmaps ) {
			mBitmaps = new Bitmap[32];
		} else {
			result = mBitmaps[inIndex];
		}
		
		if ( null == result ) {
			result = BitmapFactory.decodeResource( getResources() , kResources[inIndex] );
			
			mBitmaps[inIndex] = result;
		}
		
		return result;
	}
	
	public Bitmap imageForBack( int inState , int inPosition ) {
		return imageForResource( ( inPosition + 1 ) * 4 + ( inState & 3 ) + kIndexBacks );
	}
	
	public Bitmap imageForIcon( int inState , int inType ) {
		return imageForResource( ( inType ) * 4 + ( inState & 3 ) + kIndexIcons );
	}

	public class IHRTabBarButton extends Button {
		
		protected int			mType;
		protected int			mState;
		
		public IHRTabBarButton( Context context, int type ) {
			super( context );
	
			mType = type;
		}
		
		public void setState( int inState ) {
			if ( mState != inState ) {
				mState = inState;
				
				invalidate();
			}
		}
		
		public boolean isDown() { return mState == kTabBarButtonStateDown; }
		public void setIsDown( boolean down ) { setState( down ? kTabBarButtonStateDown : kTabBarButtonStateUp ); }
		
		// protected methods
	
		@Override
		protected void onDraw( Canvas canvas ) {
			Bitmap							bitmap;
			int								h, iconW, maxW, w;
			Paint							paint = null;
			Rect							rect;
			int								state = mState;
			
			if ( state != kTabBarButtonStateOffline && isPressed() ) {
				state = kTabBarButtonStateUp;
			}
			
			rect = new Rect();
			h = getHeight();
			w = getWidth();
			
			rect.right = w;
			rect.bottom = h;
			
			bitmap = imageForBack( state , kPositionCenter );
			canvas.drawBitmap( bitmap, null, rect, paint );
			
			// draw left-edge separator
			if ( mType > 0 ) {
				bitmap = imageForBack( state, kPositionLeft );
				canvas.drawBitmap( bitmap, 0, 0, paint );
			}
			
			// draw right-edge separator
			if ( mType < 3 ) {
				bitmap = imageForBack( state, kPositionRight );
				canvas.drawBitmap( bitmap, w - 1, 0, paint );
			}
			
			if ( kTabBarButtonStateOffline == mState ) {
				paint = new Paint();
				paint.setAlpha( 128 );
			}
			
			if ( kTabBarButtonStateOver == state && kTabBarButtonStateDown == mState ) {
				state = kTabBarButtonStateDown;
			}
			
			// draw icon
			bitmap = imageForIcon( state , mType );
			maxW = w - 4;		// max icon width is width - 4 (2px margin)
			
			if ( ( iconW = bitmap.getWidth() ) > maxW ) iconW = maxW;
			
			rect.left = ( w - iconW ) / 2;
			rect.right = rect.left + iconW;
			
			canvas.drawBitmap( bitmap, null, rect, paint );
			
			paint = new Paint();
			paint.setAntiAlias( true );
			paint.setTextAlign( Paint.Align.CENTER );
			paint.setColor( kTabBarButtonColors[state] );
			canvas.drawText( kTabBarButtonNames[mType] , w * 0.5f , h - paint.getTextSize() * 0.5f + 1.0f , paint );
		}
	}
}
