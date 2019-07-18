package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

/**
 * Use <a href="http://www.pic2icon.com/">Pic2Icon</a> to convert to Windows icon.
 */
public class WingDiscIcon extends ProgramIcon {

	double POINT_RADIUS;

	double DISC_RADIUS;

	double zx;

	double zy;

	double yx;

	double yy;

	double xx;

	double xy;

	double wx;

	double wy;

	double vx;

	double vy;

	private double bx;

	private double by;

	private double dx;

	private double dy;

	private double fx;

	private double fy;

	private double frontStartAngleDeg;

	private double frontSpanAngleDeg;

	private double leftArcStartAngleDeg;

	private double leftArcSpanAngleDeg;

	private double backArcStartAngleDeg;

	private double backArcSpanAngleDeg;

	private double rightArcStartAngleDeg;

	private double rightArcSpanAngleDeg;

//	private Color primaryHighlight = Color.web( "#FFFFFF" );
//
//	private Color primary = Color.web( "#42A5F5" );
//
//	private Color secondaryHighlight = Color.web( "#FFF59D" );
//
//	private Color secondary = Color.web( "#FF9800" );

	private Color primaryHighlight = Color.web( "#FFFFFF" );

	private Color primary = Color.web( "#42A5F5" );

	private Color secondaryHighlight = Color.web( "#FFF59D" );

	private Color secondary = Color.web( "#FF9800" );

	public WingDiscIcon() {
		POINT_RADIUS = g( 2 );
		DISC_RADIUS = g( 5 );
		zx = g( 16 );
		zy = g( 7 );
		yx = g( 7 );
		yy = g( 25 );
		xx = g( 16 );
		xy = g( 22 );
		wx = g( 25 );
		wy = g( 25 );
		vx = xx;
		vy = xy;
	}

	@Override
	protected void render() {
		calculateNumbers();

//		secondaryHighlight = Color.web( "#FFA0A0" );
//		secondary = Color.web( "#FF9800" );

		Color discColor1 = secondaryHighlight;
		Color discColor2 = secondary;
//		Color discColor1 = Color.WHITE;
//		Color discColor2 = getColorTheme().getPrimary();
		//setFillPaint( linearPaint( xx, xy, vx, vy + DISC_RADIUS, new Stop( 0, discColor1 ), new Stop( 1, discColor2 ) ) );
		setFillPaint( radialPaint( vx, vy - DISC_RADIUS, 2 * DISC_RADIUS, new Stop( 0.5, discColor1 ), new Stop( 1, discColor2 ) ) );
		//setFillTone( GradientTone.LIGHT );
		fillCenteredOval( vx, vy, DISC_RADIUS, DISC_RADIUS );
		drawCenteredOval( vx, vy, DISC_RADIUS, DISC_RADIUS );

		Color wingColor1 = primaryHighlight;
		Color wingColor2 = primary;
		//setFillPaint( linearPaint( 0,0,1,1, new Stop( 0, wingColor1 ), new Stop( 1, wingColor2 ) ) );
		setFillTone( GradientTone.MEDIUM );
		arrow();
		fillAndDraw();
	}

	private void arrow() {
		startPath();
		addArc( zx, zy, POINT_RADIUS, POINT_RADIUS, frontStartAngleDeg, frontSpanAngleDeg );
		lineTo( bx, by );
		addArc( yx, yy, POINT_RADIUS, POINT_RADIUS, leftArcStartAngleDeg, leftArcSpanAngleDeg );
		lineTo( dx, dy );
		addArc( xx, xy, POINT_RADIUS, POINT_RADIUS, backArcStartAngleDeg, backArcSpanAngleDeg );
		lineTo( fx, fy );
		addArc( wx, wy, POINT_RADIUS, POINT_RADIUS, rightArcStartAngleDeg, rightArcSpanAngleDeg );
		closePath();
	}

	private void calculateNumbers() {
		double frontTangent = (yy - zy) / (zx - yx);
		double frontNormal = 1 / frontTangent;
		double frontNormalAngle = Math.atan( frontNormal );
		frontStartAngleDeg = frontNormalAngle * (DEGREES_PER_RADIAN);
		frontSpanAngleDeg = 180 - (2 * frontStartAngleDeg);

		double hyp = distance( xx, xy, yx, yy );
		double opp = 2 * POINT_RADIUS;
		double adj = Math.sqrt( Math.pow( hyp, 2 ) - Math.pow( opp, 2 ) );

		double alpha = Math.atan2( xy - yy, xx - yx );
		double beta = Math.atan2( adj, opp );

		double backTangentAngle = alpha + beta;
		double backNormalAngle = Math.PI / 2 - backTangentAngle;

		leftArcStartAngleDeg = 180 - frontStartAngleDeg;
		leftArcSpanAngleDeg = 90 + frontStartAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;

		backArcStartAngleDeg = (Math.PI - backTangentAngle) * DEGREES_PER_RADIAN;
		backArcSpanAngleDeg = -backNormalAngle * 2 * DEGREES_PER_RADIAN;

		rightArcStartAngleDeg = 180 + backTangentAngle * DEGREES_PER_RADIAN;
		rightArcSpanAngleDeg = 90 + frontStartAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;

		bx = yx - Math.cos( frontNormalAngle ) * POINT_RADIUS;
		by = yy - Math.sin( frontNormalAngle ) * POINT_RADIUS;

		dx = xx - Math.cos( backTangentAngle ) * POINT_RADIUS;
		dy = xy - Math.sin( backTangentAngle ) * POINT_RADIUS;

		fx = wx - Math.cos( backTangentAngle ) * POINT_RADIUS;
		fy = wy + Math.sin( backTangentAngle ) * POINT_RADIUS;
	}

	public static void main( String[] commands ) {
		proof( new WingDiscIcon() );
	}

}
