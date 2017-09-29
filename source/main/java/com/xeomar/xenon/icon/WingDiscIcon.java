package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class WingDiscIcon extends ProgramIcon {

	private static final double DEGREES_PER_RADIAN = 180 / Math.PI;

//	private static final double POINT_RADIUS = J;
//
//	private static final double DISC_RADIUS = ZC;
//
//	protected static final double zx = C;
//
//	protected static final double zy = ZD;
//
//	protected static final double yx = ZD;
//
//	protected static final double yy = ZM;
//
//	protected static final double xx = C;
//
//	protected static final double xy = O;
//
//	protected static final double wx = ZM;
//
//	protected static final double wy = ZM;

	@Override
	protected void render() {
//		double frontTangent = ( yy - zy ) / ( zx - yx );
//		double frontNormal = 1 / frontTangent;
//		double frontNormalAngle = Math.atan( frontNormal );
//		double frontNormalAngleDeg = frontNormalAngle * ( DEGREES_PER_RADIAN );
//		double frontPointAngleDeg = 180 - ( 2 * frontNormalAngleDeg );
//
//		double hyp = distance( xx, xy, yx, yy );
//		double opp = 2 * POINT_RADIUS;
//		double adj = Math.sqrt( Math.pow( hyp, 2 ) - Math.pow( opp, 2 ) );
//
//		double alpha = Math.atan2( xy - yy, xx - yx );
//		double beta = Math.atan2( adj, opp );
//
//		double backTangentAngle = alpha + beta;
//		double backNormalAngle = Math.PI / 2 - backTangentAngle;
//
//		double leftArcStartAngleDeg = 180 - frontNormalAngleDeg;
//		double leftArcSpanAngleDeg = 90 + frontNormalAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;
//
//		double backArcStartAngleDeg = ( Math.PI - backTangentAngle ) * DEGREES_PER_RADIAN;
//		double backArcSpanDeg = -backNormalAngle * 2 * DEGREES_PER_RADIAN;
//
//		double rightArcStartAngleDeg = 180 + backTangentAngle * DEGREES_PER_RADIAN;
//		double rightArcSpanAngleDeg = 90 + frontNormalAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;
//
//		double bx = yx - Math.cos( frontNormalAngle ) * POINT_RADIUS;
//		double by = yy - Math.sin( frontNormalAngle ) * POINT_RADIUS;
//
//		double dx = xx - Math.cos( backTangentAngle ) * POINT_RADIUS;
//		double dy = xy - Math.sin( backTangentAngle ) * POINT_RADIUS;
//
//		double fx = wx - Math.cos( backTangentAngle ) * POINT_RADIUS;
//		double fy = wy + Math.sin( backTangentAngle ) * POINT_RADIUS;

	}

	public static void main( String[] commands ) {
		proof( new WingDiscIcon() );
	}

}
