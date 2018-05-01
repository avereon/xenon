package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import com.xeomar.xenon.JavaFxStarter;

public class CompassIcon extends ProgramIcon {

	private static final double DEGREES_PER_RADIAN = 180 / Math.PI;

	private static final double POINT_RADIUS = 2;

	private static final double DISC_RADIUS = 5;

	protected static final double zx = 16;

	protected static final double zy = 7;

	protected static final double yx = 7;

	protected static final double yy = 25;

	protected static final double xx = 16;

	protected static final double xy = 22;

	protected static final double wx = 25;

	protected static final double wy = 25;

	private static final double frontNormalAngleDeg, frontPointAngleDeg;

	private static final double leftArcStartAngleDeg, leftArcSpanAngleDeg;

	private static final double backArcStartAngleDeg, backArcSpanDeg;

	private static final double rightArcStartAngleDeg, rightArcSpanAngleDeg;

	private static final double bx, by;

	private static final double dx, dy;

	private static final double fx, fy;

	private static final double nx, ny;

	private static final double discArcStartAngleDeg, discArcSpanAngleDeg;

	static {
		double frontTangent = (yy - zy) / (zx - yx);
		double frontNormal = 1 / frontTangent;
		double frontNormalAngle = Math.atan( frontNormal );
		frontNormalAngleDeg = frontNormalAngle * (DEGREES_PER_RADIAN);
		frontPointAngleDeg = 180 - (2 * frontNormalAngleDeg);

		double hyp = distance( xx, xy, yx, yy );
		double opp = 2 * POINT_RADIUS;
		double adj = Math.sqrt( Math.pow( hyp, 2 ) - Math.pow( opp, 2 ) );

		double alpha = Math.atan2( xy - yy, xx - yx );
		double beta = Math.atan2( adj, opp );

		double backTangentAngle = alpha + beta;
		double backNormalAngle = Math.PI / 2 - backTangentAngle;

		leftArcStartAngleDeg = 180 - frontNormalAngleDeg;
		leftArcSpanAngleDeg = 90 + frontNormalAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;

		backArcStartAngleDeg = (Math.PI - backTangentAngle) * DEGREES_PER_RADIAN;
		backArcSpanDeg = -backNormalAngle * 2 * DEGREES_PER_RADIAN;

		rightArcStartAngleDeg = 180 + backTangentAngle * DEGREES_PER_RADIAN;
		rightArcSpanAngleDeg = 90 + frontNormalAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;

		bx = yx - Math.cos( frontNormalAngle ) * POINT_RADIUS;
		by = yy - Math.sin( frontNormalAngle ) * POINT_RADIUS;

		dx = xx - Math.cos( backTangentAngle ) * POINT_RADIUS;
		dy = xy - Math.sin( backTangentAngle ) * POINT_RADIUS;

		fx = wx - Math.cos( backTangentAngle ) * POINT_RADIUS;
		fy = wy + Math.sin( backTangentAngle ) * POINT_RADIUS;

		double theta = Math.atan2( POINT_RADIUS, Math.sqrt( Math.pow( DISC_RADIUS, 2 ) - Math.pow( POINT_RADIUS, 2 ) ) );
		double kappa = backNormalAngle - theta;
		nx = xx + Math.cos( kappa ) * DISC_RADIUS;
		ny = xy + Math.sin( kappa ) * DISC_RADIUS;

		double discArcStartAngle = 2 * Math.PI - kappa;
		double discArcSpanAngle = Math.PI - 2 * kappa;
		discArcStartAngleDeg = discArcStartAngle * DEGREES_PER_RADIAN;
		discArcSpanAngleDeg = -discArcSpanAngle * DEGREES_PER_RADIAN;
	}

	@Override
	protected void render() {
		arrowPath();
		fillAndDraw();
		discPath();
		fillAndDraw( GradientShade.LIGHT );
	}

	private void arrowPath() {
		beginPath();
		addArc( g( zx ), g( zy ), g( POINT_RADIUS ), g( POINT_RADIUS ), frontNormalAngleDeg, frontPointAngleDeg );
		lineTo( g( bx ), g( by ) );
		addArc( g( yx ), g( yy ), g( POINT_RADIUS ), g( POINT_RADIUS ), leftArcStartAngleDeg, leftArcSpanAngleDeg );
		lineTo( g( dx ), g( dy ) );
		addArc( g( xx ), g( xy ), g( POINT_RADIUS ), g( POINT_RADIUS ), backArcStartAngleDeg, backArcSpanDeg );
		lineTo( g( fx ), g( fy ) );
		addArc( g( wx ), g( wy ), g( POINT_RADIUS ), g( POINT_RADIUS ), rightArcStartAngleDeg, rightArcSpanAngleDeg );
		closePath();
	}

	private void discPath() {
		beginPath();
		addArc( g( xx ), g( xy ), g( POINT_RADIUS ), g( POINT_RADIUS ), backArcStartAngleDeg, backArcSpanDeg );
		lineTo( g( nx ), g( ny ) );
		addArc( g( xx ), g( xy ), g( DISC_RADIUS ), g( DISC_RADIUS ), discArcStartAngleDeg, discArcSpanAngleDeg );
		closePath();
	}

	public static void main( String[] commands ) {
		JavaFxStarter.startAndWait( 1000 );
		//proof( new CompassIcon() );
		save( new CompassIcon(), System.getProperty( "user.home" ) + "/Downloads/xeomar.png" );
	}

}
