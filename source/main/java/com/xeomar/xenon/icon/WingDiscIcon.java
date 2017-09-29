package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

/*
Conversion chart

A ----- 0
    J - 2
  F --- 4
    K - 6
D ----- 8
    L - 10
  G --- 12
    M - 14
C ----- 16
    N - 18
  H --- 20
    O - 22
E ----- 24
    P - 26
  I --- 28
    Q - 30
B ----- 32

ZA - 1
ZB - 3
ZC - 5
ZD - 7
ZE - 9
ZF - 11
ZG - 13
ZH - 15
ZI - 17
ZJ - 19
ZK - 21
ZL - 23
ZM - 25
ZN - 27
ZO - 29
ZP - 31
*/
public class WingDiscIcon extends ProgramIcon {

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

	@Override
	protected void render() {
		double frontTangent = (yy - zy) / (zx - yx);
		double frontNormal = 1 / frontTangent;
		double frontNormalAngle = Math.atan( frontNormal );
		double frontNormalAngleDeg = frontNormalAngle * (DEGREES_PER_RADIAN);
		double frontPointAngleDeg = 180 - (2 * frontNormalAngleDeg);

		double hyp = distance( xx, xy, yx, yy );
		double opp = 2 * POINT_RADIUS;
		double adj = Math.sqrt( Math.pow( hyp, 2 ) - Math.pow( opp, 2 ) );

		double alpha = Math.atan2( xy - yy, xx - yx );
		double beta = Math.atan2( adj, opp );

		double backTangentAngle = alpha + beta;
		double backNormalAngle = Math.PI / 2 - backTangentAngle;

		double leftArcStartAngleDeg = 180 - frontNormalAngleDeg;
		double leftArcSpanAngleDeg = 90 + frontNormalAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;

		double backArcStartAngleDeg = (Math.PI - backTangentAngle) * DEGREES_PER_RADIAN;
		double backArcSpanDeg = -backNormalAngle * 2 * DEGREES_PER_RADIAN;

		double rightArcStartAngleDeg = 180 + backTangentAngle * DEGREES_PER_RADIAN;
		double rightArcSpanAngleDeg = 90 + frontNormalAngleDeg + backNormalAngle * DEGREES_PER_RADIAN;

		double bx = yx - Math.cos( frontNormalAngle ) * POINT_RADIUS;
		double by = yy - Math.sin( frontNormalAngle ) * POINT_RADIUS;

		double dx = xx - Math.cos( backTangentAngle ) * POINT_RADIUS;
		double dy = xy - Math.sin( backTangentAngle ) * POINT_RADIUS;

		double fx = wx - Math.cos( backTangentAngle ) * POINT_RADIUS;
		double fy = wy + Math.sin( backTangentAngle ) * POINT_RADIUS;


	}

	public static void main( String[] commands ) {
		proof( new WingDiscIcon() );
	}

}
