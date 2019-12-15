package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;
import javafx.scene.shape.ArcType;

public class PowerIcon extends ProgramIcon {

	private double angle = 35;

	private double width = g( 4 );

	private double radius = g( 12 );

	private Point center = new Point( g(16), g(18) );

	@Override
	protected void render() {
		renderWithOutline();
		//renderWithStroke();
	}

	private void renderWithOutline() {
		setFillPaint( getIconDrawColor() );

		double halfWidth = 0.5 * width;
		double outerRadius = radius + halfWidth;
		double innerRadius = radius - halfWidth;

		double alpha = RADIANS_PER_DEGREE * (90 - angle);

		Point o = new Point( outerRadius * Math.cos( alpha ), outerRadius * Math.sin( alpha ) );
		Point c = new Point( radius * Math.cos( alpha ), radius * Math.sin( alpha ) );

		double extent = 360 - (2 * angle);
		startPath();
		moveTo( center.x - o.x, center.y - o.y );
		addArc( center.x, center.y, outerRadius, outerRadius, 90 + angle, extent );
		addArc( center.x + c.x, center.y - c.y, halfWidth, halfWidth, 90 - angle, 180 );
		addArc( center.x, center.y, innerRadius, innerRadius, 90 - angle, -extent );
		addArc( center.x - c.x, center.y - c.y, halfWidth, halfWidth, 270 + angle, 180 );
		closePath();
		fill();
		//draw();

		startPath();
		moveTo( center.x + halfWidth, g( 4 ) );
		addArc( center.x, g(4 ), halfWidth, halfWidth, 0, 180 );
		lineTo( center.x - halfWidth, g(14) );
		addArc( center.x, g(14), halfWidth, halfWidth, 180, 180 );
		closePath();
		fill();
		//draw();
	}

	private void renderWithStroke() {
		setDrawWidth( width );
		drawCenteredArc( center.x, center.y, radius, radius, 90 + angle, 360 - (2 * angle), ArcType.OPEN );
		drawLine( g( 16 ), g( 4 ), g( 16 ), g( 14 ) );
	}

	public static void main( String[] commands ) {
		proof( new PowerIcon() );
	}

}
