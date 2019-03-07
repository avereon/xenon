package com.xeomar.xenon.workarea;

import javafx.geometry.*;

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

	private void layoutNormal( Bounds bounds ) {
		// Edges must be laid out first
		workpane.getChildren().filtered( ( c ) -> c instanceof WorkpaneEdge ).forEach( ( c ) -> layoutEdge( bounds, (WorkpaneEdge)c ) );

		// Views depend on the edge bounds
		workpane.getChildren().filtered( ( c ) -> c instanceof WorkpaneView ).forEach( ( c ) -> layoutView( (WorkpaneView)c ) );
	}

	private void layoutMaximized( Bounds bounds, WorkpaneView maximizedView ) {
		workpane.getChildren().forEach( ( node ) -> {
			if( node == maximizedView ) {
				layoutMaximizedView( bounds, maximizedView );
			} else {
				node.setVisible( false );
			}
		} );
	}

	private void layoutEdge( Bounds bounds, WorkpaneEdge edge ) {
		Insets insets = workpane.getInsets();
		bounds = new BoundingBox( 0, 0, bounds.getWidth() - insets.getLeft() - insets.getRight(), bounds.getHeight() - insets.getTop() - insets.getBottom() );

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

		workpane.layoutInArea( edge, x, y, w, h, 0, HPos.CENTER, VPos.CENTER );
		edge.setVisible( true );
	}

	private void layoutView( WorkpaneView view ) {
		double x = view.getEdge( Side.LEFT ).getLayoutX();
		double y = view.getEdge( Side.TOP ).getLayoutY();
		double w = view.getEdge( Side.RIGHT ).getLayoutX() - x;
		double h = view.getEdge( Side.BOTTOM ).getLayoutY() - y;

		// Leave space for the edges.
		double edgeSize = workpane.getEdgeSize();
		double north = view.getEdge( Side.TOP ).isWall() ? 0 : edgeSize;
		double west = view.getEdge( Side.LEFT ).isWall() ? 0 : edgeSize;

		Insets insets = workpane.getInsets();
		x += west + insets.getLeft();
		y += north + insets.getTop();
		w -= west;
		h -= north;

		//System.out.println( "Layout view: x=" + x + " y=" + y + " w=" + w + " h=" + h );

		workpane.layoutInArea( view, x, y, w, h, 0, HPos.CENTER, VPos.CENTER );
		view.setVisible( true );
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
