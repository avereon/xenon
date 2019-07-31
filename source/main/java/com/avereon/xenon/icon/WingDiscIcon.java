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

	// White
	private Color primaryHighlight = Color.web( "#FFFFFF" );
	// Tint of #206080
	private Color primary = Color.web( "#79A0B3" );

	// Yellow 200
	private Color secondaryHighlight = Color.web( "#FFF59D" );
	// Orange 500
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

		double goA = -0.2;
		double goB = 0.4;
		double leftLeNormal = (zx - yx) / (yy - zy);
		double leftLeX = 0.5 * (yx + zx);
		double leftLeY = 0.5 * (yy + zy);
		double leftGbX = leftLeX + goA;
		double leftGbY = leftLeY + goA * leftLeNormal;
		double leftGeX = leftLeX + goB;
		double leftGeY = leftLeY + goB * leftLeNormal;

		double rightLeNormal = (wx - zx) / (wy - zy);
		double rightLeX = 0.5 * (wx + zx);
		double rightLeY = 0.5 * (wy + zy);
		double rightGbX = rightLeX - goA;
		double rightGbY = rightLeY + goA * rightLeNormal;
		double rightGeX = rightLeX - goB;
		double rightGeY = rightLeY + goB * rightLeNormal;

		Color discColor1 = secondaryHighlight;
		Color discColor2 = secondary;
		setFillPaint( radialPaint( vx, vy - DISC_RADIUS, 2 * DISC_RADIUS, new Stop( 0.5, discColor1 ), new Stop( 1, discColor2 ) ) );
		fillCenteredOval( vx, vy, DISC_RADIUS, DISC_RADIUS );
		drawCenteredOval( vx, vy, DISC_RADIUS, DISC_RADIUS );

		Color wingColor1 = primaryHighlight;
		Color wingColor2 = primary;
		// Left wing
		getGraphicsContext2D().save();
		clip( 0, 0, 0.5, 1 );
		arrow();
		setFillPaint( linearPaint( leftGbX, leftGbY, leftGeX, leftGeY, new Stop( 0, wingColor2 ), new Stop( 1, wingColor1 ) ) );
		//setFillPaint( radialPaint( xx, g( 20 ), g( 20 ), new Stop( 0, wingColor1 ), new Stop( xy, wingColor2 ) ) );
		fill();
		getGraphicsContext2D().restore();

		// Right wing
		getGraphicsContext2D().save();
		clip( 0.5, 0, 1, 1 );
		arrow();
		setFillPaint( linearPaint( rightGbX, rightGbY, rightGeX, rightGeY, new Stop( 0, wingColor2 ), new Stop( 1, wingColor1 ) ) );
		fill();
		getGraphicsContext2D().restore();

		// Outline
		arrow();
		draw();

		// Reference
		//		setDrawPaint( Color.GREEN );
		//		drawDot( leftLeX, leftLeY );
		//		drawDot( rightLeX, rightLeY );
		//		setDrawPaint( Color.YELLOW );
		//		drawDot( leftGeX, leftGeY );
		//		drawDot( rightGeX, rightGeY );
		//drawLine( zx,zy, yx,yy );

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

	private void clip( double x, double y, double w, double h ) {
		startPath();
		addRect( x, y, w, h );
		closePath();
		clip();
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
