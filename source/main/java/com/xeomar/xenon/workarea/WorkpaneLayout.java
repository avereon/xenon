package com.xeomar.xenon.workarea;

import javafx.geometry.*;
import javafx.scene.Node;

public class WorkpaneLayout {

	private Workpane workpane;

	WorkpaneLayout( Workpane workpane ) {
		this.workpane = workpane;
	}

	public void layout() {
		Bounds bounds = workpane.getLayoutBounds();
		if( bounds.getWidth() == 0 | bounds.getHeight() == 0 ) return;

		//System.out.println( "Layout pane: w=" + bounds.getWidth() + " h=" + bounds.getHeight() );

		WorkpaneView maximizedView = workpane.getMaximizedView();
		if( maximizedView == null ) {
			layoutNormal( bounds );
		} else {
			layoutMaximized( bounds, maximizedView );
		}
	}

	private void layoutNormal(Bounds bounds) {
		for( Node node : workpane.getChildren() ) {
			if( node instanceof WorkpaneView ) {
				layoutView( bounds, (WorkpaneView)node );
			} else if( node instanceof WorkpaneEdge ) {
				layoutEdge( bounds, (WorkpaneEdge)node );
			}
		}
	}

	private void layoutMaximized(Bounds bounds, WorkpaneView maximizedView) {
		for( Node node : workpane.getChildren() ) {
			if( node == maximizedView ) {
				layoutMaximizedView( bounds, (WorkpaneView)node );
			} else {
				node.setVisible( false );
			}
		}
	}

	private Bounds updateBounds( Bounds bounds, Insets insets ) {
		return new BoundingBox( 0, 0, bounds.getWidth() - insets.getLeft() - insets.getRight(), bounds.getHeight() - insets.getTop() - insets.getBottom() );
	}

	private void layoutView( Bounds bounds, WorkpaneView view ) {
		Insets insets = workpane.getInsets();
		bounds = updateBounds( bounds, insets );

		double edgeSize = workpane.getEdgeSize();
		double edgeHalf = 0.5 * edgeSize;
		double edgeRest = edgeSize - edgeHalf;

		double x1 = view.getEdge( Side.LEFT ).getPosition();
		double y1 = view.getEdge( Side.TOP ).getPosition();
		double x2 = view.getEdge( Side.RIGHT ).getPosition();
		double y2 = view.getEdge( Side.BOTTOM ).getPosition();

		double x = x1 * bounds.getWidth();
		double y = y1 * bounds.getHeight();
		double w = x2 * bounds.getWidth() - x;
		double h = y2 * bounds.getHeight() - y;

		// Leave space for the edges.
		double north = 0;
		double south = 0;
		double east = 0;
		double west = 0;

		if( !view.getEdge( Side.TOP ).isWall() ) north = edgeRest;
		if( !view.getEdge( Side.BOTTOM ).isWall() ) south = edgeHalf;
		if( !view.getEdge( Side.LEFT ).isWall() ) west = edgeRest;
		if( !view.getEdge( Side.RIGHT ).isWall() ) east = edgeHalf;

		x += west + insets.getLeft();
		y += north + insets.getTop();
		w -= (west + east);
		h -= (north + south);

		//System.out.println( "Layout view: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		// In the end we are still dealing with pixels so cast the bounds to int
		workpane.layoutInArea( view, (int)x, (int)y, (int)w, (int)h, 0, HPos.LEFT, VPos.TOP );
		view.setVisible( true );
	}

	private void layoutEdge( Bounds bounds, WorkpaneEdge edge ) {
		Insets insets = workpane.getInsets();
		bounds = updateBounds( bounds, insets );

		double edgeSize = edge.isWall() ? 0 : workpane.getEdgeSize();
		double edgeHalf = 0.5 * edgeSize;
		double edgeRest = edgeSize - edgeHalf;
		double position = edge.getPosition();

		double x;
		double y;
		double w;
		double h;

		if( edge.getOrientation() == Orientation.VERTICAL ) {
			x = position * bounds.getWidth() - edgeHalf;
			y = edge.getEdge( Side.TOP ) == null ? 0 : edge.getEdge( Side.TOP ).getPosition() * bounds.getHeight();
			w = edgeSize;
			h = edge.getEdge( Side.BOTTOM ) == null ? 1 : edge.getEdge( Side.BOTTOM ).getPosition() * bounds.getHeight() - y;

			double north = edge.getEdge( Side.TOP ) == null ? 0 : edge.getEdge( Side.TOP ).isWall() ? 0 : edgeRest;
			double south = edge.getEdge( Side.BOTTOM ) == null ? 0 : edge.getEdge( Side.BOTTOM ).isWall() ? 0 : edgeHalf;

			y += north;
			h -= (north + south);
		} else {
			x = edge.getEdge( Side.LEFT ) == null ? 0 : edge.getEdge( Side.LEFT ).getPosition() * bounds.getWidth();
			y = position * bounds.getHeight() - edgeHalf;
			w = edge.getEdge( Side.RIGHT ) == null ? 1 : edge.getEdge( Side.RIGHT ).getPosition() * bounds.getWidth() - x;
			h = edgeSize;

			double west = edge.getEdge( Side.LEFT ) == null ? 0 : edge.getEdge( Side.LEFT ).isWall() ? 0 : edgeRest;
			double east = edge.getEdge( Side.RIGHT ) == null ? 0 : edge.getEdge( Side.RIGHT ).isWall() ? 0 : edgeHalf;

			x += west;
			w -= (west + east);
		}

		x += insets.getLeft();
		y += insets.getTop();

		//System.out.println( "Layout edge: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		// In the end we are still dealing with pixels so cast the bounds to int
		workpane.layoutInArea( edge, (int)x, (int)y, (int)w, (int)h, 0, HPos.CENTER, VPos.CENTER );
		edge.setVisible( true );
	}

	private void layoutMaximizedView( Bounds bounds, WorkpaneView view ) {
		Insets insets = workpane.getInsets();

		double x = insets.getLeft();
		double y = insets.getTop();
		double w = bounds.getWidth() - insets.getLeft() - insets.getRight();
		double h = bounds.getHeight() - insets.getTop() - insets.getBottom();

		//System.out.println( "Layout view max: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		workpane.layoutInArea( view, x, y, w, h, 0, HPos.CENTER, VPos.CENTER );
		view.setVisible( true );
	}

}
