package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class SettingsIcon extends ProgramIcon {

	private static final int SLIDER_LEFT = 3;

	private static final int SLIDER_WIDTH = 26;

	private static final int BUTTON_RADIUS = 2;

	private static final int Y1 = 8;

	private static final int Y2 = 16;

	private static final int Y3 = 24;

	private static final int B1 = 11;

	private static final int B2 = 19;

	private static final int B3 = 9;

	@Override
	protected void render() {
		renderSliders();
	}

	private void renderSliders() {
		renderSlider( B1, Y1 );
		renderSlider( B2, Y2 );
		renderSlider( B3, Y3 );
	}

	private void renderSlider( int b, int y ) {
		renderTray( SLIDER_LEFT, y - BUTTON_RADIUS, SLIDER_WIDTH, 2 * BUTTON_RADIUS );
		//renderButton( b, y, BUTTON_RADIUS );
	}

	private void renderTray( int x, int y, int w, int h ) {

		// Pill box
		startPath();
		int r = h / 2;
		moveTo( g( x + r ), g( y ) );
		addArc( g( x + r ), g( y + r ), g( r ), g( r ), 90, 180 );
		lineTo( g( x + w - r ), g( y + h ) );
		addArc( g( x + w - r ), g( y + r ), g( r ), g( r ), 270, 180 );
		closePath();

		// Square box
		//		beginPath();
		//		moveTo( g( x ), g( y ) );
		//		lineTo( g( x ), g( y + h ) );
		//		lineTo( g( x + w ), g( y + h ) );
		//		lineTo( g( x + w ), g( y ) );
		//		closePath();

		setFillPaint( getIconDrawColor() );
		fill();
	}

	private void renderButton( int x, int y, int r ) {
		//setFill( GradientTone.DARK );
		setFillPaint( getIconDrawColor() );
		fillCenteredOval( g( x ), g( y ), g( r ), g( r ) );
		drawCenteredOval( g( x ), g( y ), g( r ), g( r ) );
	}

	public static void main( String[] commands ) {
		proof( new SettingsIcon() );
	}

}
