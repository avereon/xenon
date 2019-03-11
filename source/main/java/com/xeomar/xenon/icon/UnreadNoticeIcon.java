package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;

import java.util.function.IntSupplier;

public class UnreadNoticeIcon extends ProgramIcon {

	private int count = 8;

	private IntSupplier f;

	public UnreadNoticeIcon() {
		super();
	}

	public UnreadNoticeIcon( IntSupplier f ) {
		this.f = f;
	}

	@Override
	protected void render() {
		render2();
	}

	private void render2() {
		beginPath();
		moveTo( g( 31 ), g( 29 ) );
		lineTo( g( 31 ), g( 16 ) );
		addArc( g( 16 ), g( 16 ), g( 15 ), g( 13 ), 0, 270 );
		closePath();

		fill();
		draw();

		double size = 24;

		int count = 0;
		if( f != null ) count = f.getAsInt();
		if( count > 0 ) {
			setFillPaint( getIconDrawColor() );
			if( count < 10 ) {
				setTextAlign( TextAlignment.CENTER );
				setTextBaseLine( VPos.BASELINE );
				fillText( "1", g( 16 ), g( 24 ), g( size ) );
			} else {
				double r = 2;
				fillCenteredOval( g( 10 ), g( 16 ), g( r ), g( r ) );
				fillCenteredOval( g( 16 ), g( 16 ), g( r ), g( r ) );
				fillCenteredOval( g( 22 ), g( 16 ), g( r ), g( r ) );
			}
		}
	}

	private void render1() {
		Paint defaultDrawPaint = getIconDrawColor();

		Color start = Color.rgb( 220, 192, 0 );
		Color stop = Color.rgb( 220, 110, 0 );
		setFillPaint( linearPaint( 0, 0, 1, 1, new Stop( 0, start ), new Stop( 1, stop ) ) );
		setDrawPaint( stop );
		fillOval( g( 1 ), g( 1 ), g( 30 ), g( 30 ) );
		drawOval( g( 1 ), g( 1 ), g( 30 ), g( 30 ) );
		//setFillPaint( Color.BLACK );
		//setDrawPaint( Color.BLACK );
		//		setFillPaint( linearPaint( 0, 0, 1, 1, new Stop( 0, Color.BLACK ), new Stop( 1, Color.YELLOW ) ) );
		//		setDrawPaint( Color.YELLOW );
		//		super.render();

		// Bar
		beginPath();
		moveTo( g( 14 ), g( 8 ) );
		lineTo( g( 14 ), g( 18 ) );
		addArc( g( 16 ), g( 18 ), g( 2 ), g( 2 ), 180, 180 );
		lineTo( g( 18 ), g( 8 ) );
		addArc( g( 16 ), g( 8 ), g( 2 ), g( 2 ), 0, 180 );
		closePath();

		Color barStart = Color.rgb( 192, 192, 192 );
		Color barStop = Color.BLACK;
		setFillPaint( linearPaint( 0, 0, 1, 1, new Stop( 0, barStart ), new Stop( 1, barStop ) ) );

		//setFill( GradientShade.DARK );
		setDrawPaint( defaultDrawPaint );
		fillAndDraw();

		// Dot
		//setFill( GradientShade.DARK );
		setDrawPaint( defaultDrawPaint );
		fillOval( g( 14 ), g( 22 ), g( 4 ), g( 4 ) );
		drawOval( g( 14 ), g( 22 ), g( 4 ), g( 4 ) );
	}

	public static void main( String[] commands ) {
		proof( new UnreadNoticeIcon() );
	}

}
