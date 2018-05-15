package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class WingDiscLargeIcon extends ProgramIcon {

	private double POINT_RADIUS = g( 3 );

	private double DISC_RADIUS = g( 7 );

	private double zx = g( 16 );

	private double zy = g( 4 );

	private double yx = g( 4 );

	private double yy = g( 28 );

	private double xx = g( 16 );

	private double xy = g( 26 );

	private double wx = g( 28 );

	private double wy = g( 28 );

	private double vx = g( 16 );

	private double vy = g( 24 );

	private double bx;

	private double by;

	private double dx;

	private double dy;

	private double fx;

	private double fy;

	private double nx;

	private double ny;

	private double frontStartAngleDeg;

	private double frontSpanAngleDeg;

	private double leftArcStartAngleDeg;

	private double leftArcSpanAngleDeg;

	private double backArcStartAngleDeg;

	private double backArcSpanAngleDeg;

	private double rightArcStartAngleDeg;

	private double rightArcSpanAngleDeg;

	private double discArcStartAngleDeg;

	private double discArcSpanAngleDeg;

	public WingDiscLargeIcon() {
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
		fillCenteredOval( vx,vy,DISC_RADIUS,DISC_RADIUS );
		drawCenteredOval( vx,vy,DISC_RADIUS,DISC_RADIUS );
//		disc();
//		fill();
//		disc();
//		draw();

		arrow();
		fill();
		arrow();
		draw();
	}

	private void arrow() {
		beginPath();
		addArc( zx, zy, POINT_RADIUS, POINT_RADIUS, frontStartAngleDeg, frontSpanAngleDeg );
		lineTo( bx, by );
		addArc( yx, yy, POINT_RADIUS, POINT_RADIUS, leftArcStartAngleDeg, leftArcSpanAngleDeg );
		lineTo( dx, dy );
		addArc( xx, xy, POINT_RADIUS, POINT_RADIUS, backArcStartAngleDeg, backArcSpanAngleDeg );
		lineTo( fx, fy );
		addArc( wx, wy, POINT_RADIUS, POINT_RADIUS, rightArcStartAngleDeg, rightArcSpanAngleDeg );
		closePath();
	}

	private void disc() {
//		beginPath();
//		addArc( xx, xy, POINT_RADIUS, POINT_RADIUS, backArcStartAngleDeg, backArcSpanAngleDeg );
//		lineTo( nx, ny );
//		addArc( vx, vy, DISC_RADIUS, DISC_RADIUS, discArcStartAngleDeg, discArcSpanAngleDeg );
//		closePath();
	}

	public static void main( String[] commands ) {
		proof( new WingDiscLargeIcon() );
	}

}
