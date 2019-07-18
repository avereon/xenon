package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class RefreshIcon extends ProgramIcon {

	@Override
	protected void render() {
		double radius = g( 13 );
		double angle = 0;
		double offset = 10;
		double extent = 180 - (2 * offset);

		// The angle back from the tip of the arrow head
		double alpha = 70;

		// The angle from the center of the arrow head to the edge
		double beta = 70;

		// The angle of the back edge of the arrow head
		double sweep = 20;

		double tailAngle = angle + offset;
		double tipAngle = tailAngle + extent;

		double theta = 180 - (alpha + (90 - beta));
		double arrowAngle = tipAngle - alpha;
		double arrowRadius = radius * (Math.sin( (90 - beta) * RADIANS_PER_DEGREE ) / Math.sin( theta * RADIANS_PER_DEGREE ));
		double arrowX = 0.5 + Math.cos( arrowAngle * RADIANS_PER_DEGREE ) * arrowRadius;
		double arrowY = 0.5 - Math.sin( arrowAngle * RADIANS_PER_DEGREE ) * arrowRadius;

		double shaftRadius = radius + 0.5 * (arrowRadius - radius);
		double shaftX = 0.5 + Math.cos( (arrowAngle + sweep) * RADIANS_PER_DEGREE ) * shaftRadius;
		double shaftY = 0.5 - Math.sin( (arrowAngle + sweep) * RADIANS_PER_DEGREE ) * shaftRadius;

		double tailX = 0.5 + Math.cos( tailAngle * RADIANS_PER_DEGREE ) * radius;
		double tailY = 0.5 - Math.sin( tailAngle * RADIANS_PER_DEGREE ) * radius;

		double midpointX = 0.5 * (tailX + shaftX);
		double midpointY = 0.5 * (tailY + shaftY);

		Point tailVector = new Point( tailX - shaftX, tailY - shaftY );
		double tailMagnitude = Math.sqrt( tailVector.getX() * tailVector.getX() + tailVector.getY() * tailVector.getY() );
		Point centerVector = new Point( -tailVector.getY() / tailMagnitude, tailVector.getX() / tailMagnitude );

		double tailRadius = tailMagnitude / Math.sqrt( 3 );
		Point tailCenter = new Point( midpointX + centerVector.getX() * 0.5 * tailRadius, midpointY + centerVector.getY() * 0.5 * tailRadius );
		double tailStart = Math.atan2( tailCenter.getY() - shaftY, shaftX - tailCenter.getX() ) * DEGREES_PER_RADIAN;

		startPath();
		addArc( 0.5, 0.5, radius, radius, tailAngle, extent );
		lineTo( arrowX, arrowY );
		lineTo( shaftX, shaftY );
		addArc( tailCenter.getX(), tailCenter.getY(), tailRadius, tailRadius, tailStart, -120 );
		closePath();
		fillAndDraw();

		startPath();
		addArc( 0.5, 0.5, radius, radius, tailAngle + 180, extent );
		lineTo( rotate( arrowX ), rotate( arrowY ) );
		lineTo( rotate( shaftX ), rotate( shaftY ) );
		addArc( rotate( tailCenter.getX() ), rotate( tailCenter.getY() ), tailRadius, tailRadius, tailStart + 180, -120 );
		closePath();
		fillAndDraw();
	}

	private double rotate( double value ) {
		return -(value - 0.5) + 0.5;
	}

	public static void main( String[] commands ) {
		proof( new RefreshIcon() );
	}

}
