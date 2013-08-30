package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.troubadorian.streamradio.controller.R;

public class IHRViewSearch extends RelativeLayout {
	public static Bitmap			sFill;
	protected TextView				mFind;
	protected EditText				mSearch;
	protected View					mList;
	
	public IHRViewSearch( Context context ) {
		super( context );
		
		setWillNotDraw( false );
		
		addView( mFind = new TextView( context ) );
		addView( mSearch = new EditText( context ) );
		
		if ( null == sFill ) {
			sFill = BitmapFactory.decodeResource( getResources(), R.drawable.nav_border_1px );
		}
		
		mFind.setText( "Find:" );
	}
	
	@Override
	protected void onDraw( Canvas canvas ) {
		Rect						rect;
		
		rect = new Rect( 0, mSearch.getHeight(), getWidth(), getHeight() );
		
		canvas.drawBitmap( sFill, null, rect, null );
		
		super.onDraw( canvas );
	}
	
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
/*
		int							h, w;
		
		h = b - t;
		w = r - l;
*/
		
		
	}
	
/*
	protected SearchElement			mElements[];
	protected IHRLabelField			mFind;
	protected int					mFindTextHeight;
	protected int					mFocusedRow;
	protected IHRSubviewList		mList;
	protected IHRSearchListRow		mRows[];
	protected IHREditField			mSearch;
	protected int					mSearchTextHeight;
	protected int					mSeparatorOffset;
	
	protected static final int		kTextMarginLeft = 6;
	protected static final int		kTextMarginTop = 6;
		
	public IHRSubviewSearch() {
		super();
		
//#ifdef JDE_MIN_470
		mFindTextHeight = 22;
		mSearchTextHeight = 20;
//#else
		mFindTextHeight = 18;
		mSearchTextHeight = 16;		
//#endif

		add( mFind = new IHRLabelField( "Find:", Field.USE_ALL_WIDTH, Font.BOLD, mFindTextHeight, Color.WHITE ) );
		add( mSearch = new IHREditField( IHRResources.getApplicationFont( Font.ITALIC, mSearchTextHeight ), this ) );
		add( mList = new IHRSubviewList( this ) {
			// IHRSubviewSearch will manage on[Und|D]isplay()
			protected void onDisplay() { }
			protected void onUndisplay() { }
		} );

		mFocusedField = mSearch;
		
//#ifndef CONSERVE_MEMORY
		initElements();
//#endif
	}

	// search only deals with vertical movement
	public void handleNavigationMovement( IHRMutableInteger dx, IHRMutableInteger dy, int status, int time ) {
		dy.set( moveFocus( dy.intValue(), status, time ) );
	}
	
	public void restoreFocus() {
		if ( mFocusedField == mSearch ) super.restoreFocus();
		else mList.focusOnRow( mFocusedRow );
	}
	
	public void saveFocus() {
		if ( isFocus() ) {
			super.saveFocus();
			mFocusedRow = mList.getFocusedRow();
		}
	}
	
	public void textChanged( IHREditField field, String text ) {
		if ( text == null || text.length() == 0 ) {
			removeRows();
			mList.addRows( mRows );
		} else {
			text = text.toLowerCase();
			
			if ( text.equals( "debugon" ) ) setDebug( true );
			else if ( text.equals( "debugoff" ) ) setDebug( false );
			else updateList( text );
		}
	}
	
	// protected methods

	protected boolean navigationClick( int status, int time ) {
		if ( mList.isFocus() ) { mList.navigationClick( status, time ); }
		
		return true;
	}

	protected void initElements() {
		int						i, n;
		
		if ( mElements == null ) return;
		
		n = mElements.length;
		mRows = new IHRSearchListRow[ n ];
		
		for ( i = 0; i < n; ++i ) {
			mRows[ i ] = new IHRSearchListRow( mElements[ i ].mContext );
			mRows[ i ].setText( mElements[ i ].string() );
		}
	}
	
	protected void onDisplay() {
		restoreFocus();
		mList.restoreScroll();
	}
	
	protected void onPop( boolean thisIsDisplayed ) {
//#ifdef CONSERVE_MEMORY
		if ( ! thisIsDisplayed ) {
			mElements = null;
			removeRows();
			mRows = null;
		}
//#endif
	}
	
	protected void onPush( boolean thisIsDisplayed ) {
//#ifdef CONSERVE_MEMORY
		if ( ! thisIsDisplayed ) {
			initElements();
		} else {
			textChanged( mSearch, mSearch.getText() );
			onDisplay();
		}
//#endif		
	}
	
	protected void onUndisplay() {
		saveFocus();
		mList.saveScroll();
	}
	
	protected void removeRows() {
		focusField( mSearch );		// move focus away from any row that is about to be deleted
		mList.removeAllRows();
	}
	
	protected void setDebug( boolean on ) {
		if ( on ) {
			if ( ! IHRPreferences.getBoolean( IHRConfiguration.kKeyDebugModeEnabled, false ) ) {
				if ( IHRUtilities.debugModePermitted() ) {
					IHRPreferences.setBoolean( IHRConfiguration.kKeyDebugModeEnabled, true );
					IHRUtilities.alert( "Debug mode enabled. Relaunch Streamradio." );
				}
			}		
		} else {
			if ( IHRPreferences.getBoolean( IHRConfiguration.kKeyDebugModeEnabled, false ) ) {
				IHRPreferences.setBoolean( IHRConfiguration.kKeyDebugModeEnabled, false );
				IHRUtilities.alert( "Debug mode disabled. Relaunch Streamradio." );
			}
		}
	}

	protected void updateList( String prefix ) {
		IHRComparator					comparator;
		int								i, m, n, o;
		
		comparator = IHRComparator.stringPrefixComparator( prefix.length() );
		
		removeRows();
		
		if ( ( i = IHRBinarySearch.search( mElements, prefix, comparator ) ) < 0 ) return;
		
		for ( n = i; n > 0; --n ) {
			if ( comparator.compare( mElements[ n - 1 ], prefix ) != 0 ) break;
		}
		for ( o = i + 1, m = mElements.length; o < m; ++o ) {
			if ( comparator.compare( mElements[ o ], prefix ) != 0 ) break; 
		}
		
		mList.addRows( mRows, n, o );
	}

	// protected methods

	protected int moveFocus( int amount, int status, int time ) {
		IHRMutableInteger		dy;
		
		if ( amount == 0 ) return 0;

		if ( mSearch.isFocus() ) {
			if ( amount < 0 ) return 0;
			if ( mList.getRowCount() == 0 ) return amount;
			
			mList.focusOnRow( 0 );

			if ( --amount == 0 ) return 0;
			
			dy = new IHRMutableInteger( amount );
			mList.handleNavigationMovement( null, dy, status, time );
		} else {
			dy = new IHRMutableInteger( amount );
			mList.handleNavigationMovement( null, dy, status, time );

			if ( dy.intValue() < 0 ) {
				focusField( mSearch );
				return 0;
			}
		}
		
		return dy.intValue();
	}

	protected void paint( Graphics graphics ) {
		graphics.setColor( Color.GRAY );
		graphics.fillRect( 0, mSeparatorOffset, getWidth(), 1 );

		super.paint( graphics );
	}

	protected void sublayout( int width, int height ) {
		int							 fieldWidth, findHeight, offsetX, searchHeight;

		setExtent( width, height );

		findHeight = mFind.getFont().getHeight();
		searchHeight = mSearch.getPreferredHeight();

		mSeparatorOffset = findHeight > searchHeight ? findHeight : searchHeight;
		mSeparatorOffset += kTextMarginTop * 2 + 1;		// +1 because I'm going to offset the search field down 1

		fieldWidth = mFind.getFont().getAdvance( mFind.getText() );

		setPositionChild( mFind, kTextMarginLeft, mSeparatorOffset / 2 - findHeight / 2 );
		layoutChild( mFind, fieldWidth, findHeight );

		offsetX = kTextMarginLeft * 2 + fieldWidth;

		fieldWidth = width - kTextMarginLeft - offsetX;

		setPositionChild( mSearch, offsetX, mSeparatorOffset / 2 - searchHeight / 2 + 1 );
		layoutChild( mSearch, fieldWidth, searchHeight );

		setPositionChild( mList, 0, mSeparatorOffset + 1 );
		layoutChild( mList, width, height - ( mSeparatorOffset + 1 ) );
	}
	
	// protected classes
	
	protected static class SearchElement {
		protected Object			mContext;
		protected String			mString;
		protected String			mStringLowercase;

		public SearchElement( String string, Object context ) {
			mContext = context;
			mString = string;
			mStringLowercase = string.toLowerCase();
		}
		
		public Object context() { return mContext; }
		public String string() { return mString; }
		public String toString() { return mStringLowercase; }	// for comparator
	}
*/	
}

