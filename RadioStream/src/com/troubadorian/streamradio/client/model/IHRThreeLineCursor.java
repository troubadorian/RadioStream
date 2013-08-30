package com.troubadorian.streamradio.client.model;

import android.content.Context;
import android.widget.SimpleCursorAdapter;

import com.troubadorian.streamradio.controller.R;

public class IHRThreeLineCursor extends IHROneLineCursor {
	final String[]				kColumns = { "line1" , "line2" , "line3" };
	final int[]					kColumnsID = { R.id.StationsListRowTextLine1, R.id.StationsListRowTextLine2, R.id.StationsListRowTextLine3 };
	final int					kResourceID = R.layout.list_row_triple_line;
	
	@Override
	public int getColumnCount() { return kColumns.length; }
	@Override
	public int getColumnIndex( String arg0 ) { return getColumnIndexIn( arg0 , kColumns ); }
	@Override
	public String getColumnName( int arg0 ) { return ( arg0 >= 0 && arg0 < kColumns.length ) ? kColumns[arg0] : null; }
	@Override
	public String[] getColumnNames() { return kColumns; }
	
	@Override
	public SimpleCursorAdapter newAdapter( Context inContext ) { return new SimpleCursorAdapter( inContext , kResourceID , this , kColumns , kColumnsID ); }
}
