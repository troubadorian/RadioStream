package com.troubadorian.streamradio.controller;

import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.troubadorian.streamradio.client.model.IHRVector;
import com.troubadorian.streamradio.client.view.IHRListView;

/*
 * IHRControlerActivity
 * 
 * maintain a stack of IHRController objects
 * broadcast each Activity onEvent to each IHRController
 * display the content() of the top IHRController
 * allow push and pop of IHRController objects
 * inform IHRController objects of view usage
 * 
 * */

public class IHRControllerActivity extends Activity {
	public final static String	kKeyStack = "controller_activity_intent_stack";
	public final static String	kKeyState = "controller_saved_instance_state";
	public final static String	kKeyOrder = "controller_stack_insert_order";
	public final static String	kKeyModal = "controller_report_result";
	public final static String	kKeyClass = "class";
	
	protected int				mState;
	protected IHRController		mController;
	protected IHRVector			mControllers;
	
	protected void prepareContent() {
		
	}
	
	protected void onShowController( IHRController inPushing ) {
		
	}
	
	protected void onPushController( IHRController inPushing ) {
		
	}
	
	protected void onPopController( IHRController inPopping ) {
		
	}
	
	protected void onNoController( boolean inCreating ) {
		if ( !inCreating ) finish();
	}
	
	protected void onControllerResult( int inUnique , int inResult , Intent inParameters ) {
		
	}
	
	protected void swapController( IHRController inNew , IHRController inOld ) {
		setContentView( null == inNew ? null : inNew.content() );
	}
	
	protected void showController( IHRController inController ) {
		if ( mController != inController ) {
			IHRController		old = mController;
			IHRController		now = inController;
			
			mController = inController;
			
			if ( null != old ) old.beingShown( false );
			if ( null != now ) now.beingShown( true );
			
			swapController( now , old );
			
			if ( null != old ) old.afterShown( false );
			if ( null != now ) now.afterShown( true );
			
			onShowController( inController );
		}
	}
	
	public IHRController[] controllers() {
		return ( null == mControllers ) ? null : mControllers.toArray( new IHRController[mControllers.size()] );
	}
	
	public IHRController visibleController() {
		return mController;
	}
	
	public IHRController topController() {
		return ( null == mControllers || 0 == mControllers.size() ) ? null : (IHRController)mControllers.lastElement();
	}
	
	public IHRController ancestor( IHRController inController , int inDepth ) {
		IHRController			result = null;
		
		if ( null != inController ) {
			int					count = ( null == mControllers ) ? 0 : mControllers.size();
			int					index = ( count > 0 ) ? mControllers.indexOf( inController ) : -1;
			
			if ( index >= 0 ) {
				index -= inDepth;
				
				if ( index > 0 && index < count ) {
					result = (IHRController)mControllers.get( index );
				}
			}
		}
		
		return result;
	}
	
	/* 
	 * inPop  -1 pop to existing else push new
	 *         0 move existing else push new
	 *         1 pop to existing else pop to index
	 *         2 pop to index then push
	 *         3 pop existing
	 * */
	private void modifyControllerStack( IHRController inController , int inPop , int inIndex ) {
		int						count = ( null == mControllers ) ? 0 : mControllers.size();
		int						index = -1;
		int						found = ( count > 0 && null != inController ) ? mControllers.indexOf( inController ) : -1;
		
		Intent					intent;
		IHRVector				removed = new IHRVector();
		IHRController			controller = null;
		IHRController			pop;
		
		if ( inPop < 0 && found >= 0 ) {
			inPop = 1;	//	on push existing do pop to existing
		}
		
		if ( inPop > 0 ) {
			if ( inPop > 2 ) {
				if ( found < 0 ) { inController.mCreationIntent.putExtra( "controller_finished" , true ); return; }	//	pop existing failed
				else index = found;
				
				if ( index > 0 ) controller = (IHRController)mControllers.elementAt( index - 1 );
				inController = null;
			} else if ( found < 0 || inPop > 1 ) {
				//	pop to index
				if ( inIndex < 0 ) index = 0;
				else if ( inIndex < count ) { index = inIndex + 1; controller = (IHRController)mControllers.elementAt( inIndex ); }
				else index = count;
			} else {
				//	pop to existing
				index = found + 1;
				controller = inController;
				inController = null;
			}
		} else if ( null == inController ) {
			//	move from index
			index = ( inIndex < 0 ) ? count + inIndex : inIndex;
			
			if ( index >= 0 && index < count ) {
				inController = (IHRController)mControllers.remove( found = index );
			}
		}
		
		if ( null != inController ) {
			if ( found < 0 ) {
				inController.mControllerActivity = this;
				mControllers.add( inController );	//	at count
				
				if ( mState > 1 ) inController.onStart();
				if ( mState > 1 ) inController.onPostCreate( null );
				if ( mState > 2 ) inController.onResume();
				
				onPushController( inController );
			} else if ( found < count - 1 ) {
				mControllers.remove( found );
				mControllers.add( inController );	//	at count-1
				if ( mState > 0 ) inController.onRestart();
			}
			
			controller = inController;
		}
		
		if ( inPop > 0 ) {
			if ( index < count ) {
				showController( controller );
				
				do {
					pop = (IHRController)mControllers.remove( --count );
					
					if ( mState > 2 ) pop.onPause();
					if ( mState > 1 ) pop.onStop();
					if ( mState > 0 ) pop.onDestroy();
					
					onPopController( pop );
					removed.add( pop );
				} while ( index < count );
				
				count = removed.size();
				
				for ( index = 0 ; index < count ; ++index ) {
					pop = (IHRController)removed.elementAt( index );
					intent = pop.getIntent();
					found = intent.getIntExtra( kKeyModal , 0 );
					
					if ( found > 0 ) {
						onControllerResult( found , pop.getResult() , intent );
					}
					
					pop.mControllerActivity = null;
				}
			} else if ( null == mController ) {
				showController( controller );
			}
			
			if ( 0 == mControllers.size() ) {
				onNoController( false );
			}
		} else if ( null != inController ) {
			showController( inController );
		}
	}
	
	public IHRController existingController( Intent intent ) {
		IHRController			result = null;
		IHRController			controller;
		
		Bundle					other;
		int						match , trial;
		int						index = ( null == mControllers ) ? 0 : mControllers.size();
		Bundle					extras = ( null == intent ) ? null : intent.getExtras();
		Set<String>				set = ( null == extras ) ? null : extras.keySet();
		int						count = ( null == set ) ? 0 : set.size();
		String[]				keys = ( 0 == count ) ? null : set.toArray( new String[count] );
		String					key;
		
		while ( index-- > 0 ) {
			controller = (IHRController)mControllers.elementAt( index );
			other = ( null == controller || null == controller.mCreationIntent ) ? null : controller.mCreationIntent.getExtras();
			match = 0;
			
			for ( trial = 0 ; trial < count ; ++trial ) {
				key = keys[trial];
				
				if ( other.containsKey( key ) ) {
					if ( /*extras.getBoolean( key ) == true ||*/ extras.get( key ).equals( other.get( key ) ) ) {
						match += 1;
					}
				} else if ( key.equals( "identifier" ) ) {
					if ( extras.get( key ).equals( controller.mIdentifier ) ) {
						match += 1;
					}
				} else {
					if ( extras.get( key ) instanceof Boolean && extras.getBoolean( key ) == false ) {
						match += 1;
					}
				}
			}
			
			if ( match == count ) {
				result = controller;
				break;
			}
		}
		
		return result;
	}
	
	public IHRController makeController( Intent intent ) {
		IHRController			result = null;
		String					identifier = intent.getStringExtra( "identifier" );
		
		try {
			result = (IHRController)Class.forName( intent.getStringExtra( kKeyClass ) ).newInstance();
			
			if ( null != identifier ) result.mIdentifier = identifier;
			result.mControllerActivity = this;
			result.mCreationIntent = intent;
			result.onCreate( intent.getBundleExtra( kKeyState ) );
			
			if ( result.mCreationIntent.getBooleanExtra( "controller_finished" , false ) ) {
				result.onDestroy();
				result = null;
			} else {
				result.onNewIntent( intent );
			}
		} catch ( Exception e ) {}
		
		return result;
	}
	
	public void pushController( IHRController inController ) {
		modifyControllerStack( inController , -1/* pop to exising else push */ , -1 );
	}
	
	public Intent pushing( String inClass , int inFlags ) {
		return new Intent().putExtra( kKeyClass , inClass ).addFlags( inFlags );
	}
	
	public Intent pushing( Class inClass , int inFlags ) {
		return pushing( inClass.getName() , inFlags );
	}
	
	public IHRController pushControllerIntent( Intent intent ) {
		IHRController			result = null;
		int						flags = intent.getFlags();
		
		if ( 0 != ( flags & ( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP ) ) ) {
			result = existingController( intent );
		}
		
		if ( null == result ) {
			try {
				int				order = intent.getIntExtra( kKeyOrder , -1 );
				
				result = makeController( intent );
				
				if ( null == result ) {
					//	failed or finished
				} else if ( !( order < 0 ) && order < mControllers.size() ) {
					mControllers.add( order , result );
					
					if( mState > 1 ) {
						result.onStart();
						result.onPostCreate( null );
					}
				} else if ( 0 != ( flags & Intent.FLAG_ACTIVITY_NEW_TASK ) ) {
					modifyControllerStack( result , 2/* pop to index then push */ , -1 );
				} else {
					modifyControllerStack( result , 0/* push new */ , 0 );
				}
			} catch ( Exception e ) {}
		} else {
			if ( 0 != ( flags & Intent.FLAG_ACTIVITY_SINGLE_TOP ) && result.equals( mControllers.lastElement() ) ) {
				//	do nothing
			} else if ( 0 != ( flags & ( Intent.FLAG_ACTIVITY_CLEAR_TOP ) ) ) {
				modifyControllerStack( result , 1/* pop to existing */ , 0 );
			} else if ( 0 != ( flags & Intent.FLAG_ACTIVITY_REORDER_TO_FRONT ) ) {
				modifyControllerStack( result , 0/* move existing */ , 0 );
			}
		}
		
		return result;
	}
	
	public IHRController pushControllerForResult( Intent intent , int inUnique ) {
		IHRController			result = pushControllerIntent( intent.putExtra( kKeyModal , inUnique ) );
		
		result.mResult = Activity.RESULT_CANCELED;
		
		return result;
	}
	
	public IHRController pushControllerClass( String inClass , Bundle inState ) {
		Intent					intent = new Intent();
		
		intent.putExtra( kKeyClass , inClass );
		if ( null != inState ) intent.putExtra( kKeyState , inState );
		
		return pushControllerIntent( intent );
	}
	
	public void popToRoot() {
		modifyControllerStack( null , 1/* pop to index */ , 0 );
	}
	
	public void popController( IHRController inController ) {
		modifyControllerStack( inController , 3/* pop existing */ , 0 );
	}
	
	public void popToController( IHRController inController ) {
		modifyControllerStack( inController , 1/* pop to existing */ , -1 );
	}
	
	public IHRController popTopController() {
		IHRController			result = null;
		int						count = ( null == mControllers ) ? 0 : mControllers.size();
		
		if ( count > 0 ) {
			modifyControllerStack( null , 1 , count - 2 );
		}
		
		return result;
	}
	
	@Override
	public boolean onKeyDown( int keyCode , KeyEvent event ) {
		boolean					result = false;
		
		if ( null != mController ) {
			result = mController.onKeyDown( keyCode , event );
		}
		
		if ( false == result ) {
			if ( KeyEvent.KEYCODE_BACK == keyCode ) {
				if ( null == mControllers || mControllers.size() < 1 ) {
					onNoController( false );
				} else {
					popTopController();
				}
				
				result = true;
			} else {
				result = super.onKeyDown( keyCode , event );
			}
		}
		
		return result;
	}
	
	@Override
	protected void onCreate( Bundle inState ) {
		super.onCreate( inState );
		
		mControllers = new IHRVector();
		
		//	prepare to handle swapController
		prepareContent();
		
		if ( null != inState ) {
			Parcelable[]		intents = inState.getParcelableArray( kKeyStack );
			int					i , n = ( null == intents ) ? 0 : intents.length;
			
			for ( i = 0 ; i < n ; ++i ) {
				try {
					Intent			intent = (Intent)intents[i];
					IHRController	controller = makeController( intent );
					
					if ( null != controller ) mControllers.add( controller );
				} catch ( Exception e ) {}
			}
		}
		
		if ( mControllers.size() > 0 ) {
			showController( (IHRController)mControllers.lastElement() );
		} else {
			onNoController( true );
		}
		
		mState = 1;
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onRestart();
		}
		
		mState = 1;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onStart();
		}
		
		mState = 2;
	}
	
	@Override
	protected void onRestoreInstanceState( Bundle inState ) {
		super.onRestoreInstanceState( inState );
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
//			((IHRController)iterator.next()).onRestoreInstanceState( inState );
			IHRController		controller = (IHRController)iterator.next();
			
			controller.onRestoreInstanceState( controller.mCreationIntent.getBundleExtra( kKeyState ) );
		}
	}
	
	@Override
	protected void onPostCreate( Bundle inState ) {
		super.onPostCreate( inState );
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
//			((IHRController)iterator.next()).onPostCreate( inState );
			IHRController		controller = (IHRController)iterator.next();
			
			controller.onPostCreate( controller.mCreationIntent.getBundleExtra( kKeyState ) );
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onResume();
		}
		
		mState = 3;
	}
	
	@Override
	protected void onPostResume() {
		super.onPostResume();
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onPostResume();
		}
	}
	
	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onUserLeaveHint();
		}
	}
	
	@Override
	protected void onPause() {
		mState = 2;
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onPause();
		}
		
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		mState = 1;
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onStop();
		}
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		mState = 0;
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onDestroy();
		}
		
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState( Bundle ioState ) {
		super.onSaveInstanceState( ioState );
		
		int						index = 0;
		int						count = ( null == mControllers ) ? 0 : mControllers.size();
		Parcelable[]			intents = new Parcelable[count];
		
		for ( index = 0 ; index < count ; ++index ) {
			IHRController		controller = (IHRController)mControllers.elementAt( index );
			Intent				intent = controller.mCreationIntent;
			Bundle				state = new Bundle();
			
			if ( null == intent ) {
				intent = new Intent().putExtra( kKeyClass , controller.getClass().getName() );
			}
			
			controller.onSaveInstanceState( state );
			intent.putExtra( kKeyState , state );
			
			intents[index] = intent;
		}
		
		ioState.putParcelableArray( kKeyStack , intents );
		/**/
	}
	
	@Override
	public void onConfigurationChanged( Configuration c ) {
		super.onConfigurationChanged( c );
		
		for ( Iterator<Object> iterator = mControllers.iterator() ; iterator.hasNext() ; ) {
			((IHRController)iterator.next()).onConfigurationChanged( c );
		}
	}
	
	@Override
	public void onCreateContextMenu( ContextMenu ioMenu, View inView, ContextMenu.ContextMenuInfo inDetails ) {
		super.onCreateContextMenu( ioMenu , inView , inDetails );
		
		if ( inView instanceof IHRListView ) {
			((IHRListView)inView).addContextMenuItems( ioMenu , inDetails );
		}
	}
	
	@Override
	public boolean onContextItemSelected( MenuItem inItem ) {
		return false;
	}
	
	public void displayAlert( CharSequence inText , int inXPos,  int inYPos) {
		Toast toast = Toast.makeText( this , inText , Toast.LENGTH_SHORT );
		toast.setGravity(Gravity.TOP|Gravity.LEFT, inXPos, inYPos);
		toast.show();
	}
	
	public void displayAlert( CharSequence inText , int inDuration ) {
		Toast.makeText( this , inText , inDuration ).show();
	}
	
	public void displayAlert( CharSequence inText ) {
		displayAlert( inText , Toast.LENGTH_SHORT );
	}
	
}
