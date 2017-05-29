package com.parallelsymmetry.essence.workarea;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ecco on 5/29/17.
 */
public class WorkpaneEdge extends Control {

	Orientation orientation;

	WorkpaneEdge northEdge;

	WorkpaneEdge southEdge;

	WorkpaneEdge westEdge;

	WorkpaneEdge eastEdge;

	Set<WorkpaneView> northViews;

	Set<WorkpaneView> southViews;

	Set<WorkpaneView> westViews;

	Set<WorkpaneView> eastViews;

	/**
	 * <p>Represents the location where the divider should ideally be
	 * positioned, between 0.0 and 1.0 (inclusive) when the position is
	 * relative. 0.0 represents the left- or top-most point, and 1.0 represents
	 * the right- or bottom-most point (depending on the orientation property).
	 * <p>
	 * <p>As the user drags the edge around this property will be updated to
	 * always represent its current location.
	 */
	private DoubleProperty position;

	private boolean absolute;

	private boolean wall;

	private Set<WorkpaneView> viewsA;

	private Set<WorkpaneView> viewsB;

	private Workpane parent;

	public WorkpaneEdge( Orientation orientation ) {
		this( orientation, false );
	}

	public WorkpaneEdge( Orientation orientation, boolean wall ) {
		this.position = new SimpleDoubleProperty( this, "position", 0 );
		this.orientation = orientation;
		this.wall = wall;

		// Create the view lists.
		viewsA = new CopyOnWriteArraySet<WorkpaneView>();
		viewsB = new CopyOnWriteArraySet<WorkpaneView>();
		northViews = viewsA;
		southViews = viewsB;
		westViews = viewsA;
		eastViews = viewsB;

		// Set the control cursor
		setCursor( orientation == Orientation.VERTICAL ? Cursor.H_RESIZE : Cursor.V_RESIZE );

		// Set the default background
		// TODO The colors should be set by the style sheet
		setBackground( new Background( new BackgroundFill( Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY ) ) );

		// Register the mouse handlers
		onMouseDraggedProperty().set( this::mouseDragged );
	}

	@Override
	protected Skin<WorkpaneEdge> createDefaultSkin() {
		return new EdgeSkin( this );
	}

	private void mouseDragged( MouseEvent event ) {
		Point2D point = localToParent( event.getX(), event.getY() );
		switch( orientation ) {
			case VERTICAL: {
				parent.moveEdge( this, point.getX() - (getPosition() * parent.getWidth()) );
				break;
			}
			case HORIZONTAL: {
				parent.moveEdge( this, point.getY() - (getPosition() * parent.getHeight()) );
				break;
			}
		}
	}

	public final boolean isWall() {
		return wall;
	}

	public final Orientation getOrientation() {
		return orientation;
	}

	public final double getPosition() {
		return position == null ? 0.5F : position.get();
	}

	public final void setPosition( double value ) {
		positionProperty().set( value );
	}

	public final DoubleProperty positionProperty() {
		return position;
	}

	Workpane getWorkpane() {
		return parent;
	}

	void setWorkpane( Workpane parent ) {
		this.parent = parent;
	}

	public WorkpaneEdge getEdge( Side direction ) {

		switch( direction ) {
			case TOP: {
				return northEdge;
			}
			case BOTTOM: {
				return southEdge;
			}
			case LEFT: {
				return westEdge;
			}
			case RIGHT: {
				return eastEdge;
			}
		}

		return null;
	}

	public void setEdge( Side direction, WorkpaneEdge edge ) {
		switch( direction ) {
			case TOP: {
				northEdge = edge;
				break;
			}
			case BOTTOM: {
				southEdge = edge;
				break;
			}
			case LEFT: {
				westEdge = edge;
				break;
			}
			case RIGHT: {
				eastEdge = edge;
				break;
			}
		}
	}

	public Set<WorkpaneView> getViews( Side direction ) {
		switch( direction ) {
			case TOP: {
				return northViews;
			}
			case BOTTOM: {
				return southViews;
			}
			case LEFT: {
				return westViews;
			}
			case RIGHT: {
				return eastViews;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( getClass().getSimpleName() );

		builder.append( " orientation=" );
		builder.append( getOrientation() );
		builder.append( " position=" );
		builder.append( getPosition() );
		builder.append( " wall=" );
		builder.append( isWall() );

		return builder.toString();
	}

}
