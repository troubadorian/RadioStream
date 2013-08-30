package com.troubadorian.streamradio.client.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.Button;

import com.troubadorian.streamradio.controller.R;

public class IHRViewButton extends Button {
	
	public IHRViewButton( Context context ) {
		super( context );
		this.setBackgroundResource( R.drawable.btn_empty_55 );
	}
	
	@Override
	protected void onDraw( Canvas canvas ) {
		Paint					paint;
		int						h , w;
		
		super.onDraw( canvas );
		
		h = getHeight();
		w = getWidth();
		
		paint = new Paint();
		paint.setAntiAlias( true );
		paint.setTextAlign( Paint.Align.CENTER );
		paint.setColor( this.isEnabled() ? 0xFFFFFFFF : 0xFF999999 );
		canvas.drawText( (String)getText() , w * 0.5F , ( h + paint.getTextSize() ) * 0.5F - paint.descent() , paint );
		
//		Log.d( "font" , "ascent = " + paint.ascent() + " descent = " + paint.descent() + " height = " + paint.getTextSize() + " spacing = " + paint.getFontSpacing() );
	}
}
