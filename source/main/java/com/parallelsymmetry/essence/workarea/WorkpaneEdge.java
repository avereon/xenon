package com.parallelsymmetry.essence.workarea;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.*;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ecco on 5/29/17.
 */
public class WorkpaneEdge extends Control {

	private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "horizontal" );

	private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "vertical" );

	private ObjectProperty<Orientation> orientation;

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
		this.wall = wall;

		setOrientation( orientation );

		// Set the style class
		getStyleClass().add( "workpane-divider" );

		// Create the view lists.
		viewsA = new CopyOnWriteArraySet<WorkpaneView>();
		viewsB = new CopyOnWriteArraySet<WorkpaneView>();
		northViews = viewsA;
		southViews = viewsB;
		westViews = viewsA;
		eastViews = viewsB;

		// Set the control cursor
		setCursor( orientation == Orientation.VERTICAL ? Cursor.H_RESIZE : Cursor.V_RESIZE );

		// Register the mouse handlers
		onMouseDraggedProperty().set( this::mouseDragged );
	}

	@Override
	protected Skin<WorkpaneEdge> createDefaultSkin() {
		return new EdgeSkin( this );
	}

	private void mouseDragged( MouseEvent event ) {
		Point2D point = localToParent( event.getX(), event.getY() );
		switch( getOrientation() ) {
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

	/**
	 * <p>This property controls how the WorkpaneEdge should be displayed to the
	 * user. {@link javafx.geometry.Orientation#HORIZONTAL} will result in
	 * a horizontal divider, while {@link javafx.geometry.Orientation#VERTICAL}
	 * will result in a vertical divider.</p>
	 *
	 * @param value the orientation value
	 */
	public final void setOrientation( Orientation value ) {
		orientationProperty().set( value );
	}

	/**
	 * The orientation for the WorkpaneEdge.
	 *
	 * @return The orientation for the WorkpaneEdge.
	 */
	public final Orientation getOrientation() {
		return orientation == null ? Orientation.VERTICAL : orientation.get();
	}

	/**
	 * The orientation property for the WorkpaneEdge.
	 *
	 * @return the orientation property for the WorkpaneEdge
	 */
	public final ObjectProperty<Orientation> orientationProperty() {
		if( orientation == null ) {
			orientation = new StyleableObjectProperty<Orientation>( null ) {

				@Override
				public void invalidated() {
					final boolean isHorizontal = (get() == Orientation.HORIZONTAL);
					pseudoClassStateChanged( HORIZONTAL_PSEUDOCLASS_STATE, isHorizontal );
					pseudoClassStateChanged( VERTICAL_PSEUDOCLASS_STATE, !isHorizontal );
				}

				@Override
				public CssMetaData<WorkpaneEdge, Orientation> getCssMetaData() {
					return WorkpaneEdge.StyleableProperties.ORIENTATION;
				}

				@Override
				public Object getBean() {
					return WorkpaneEdge.this;
				}

				@Override
				public String getName() {
					return "orientation";
				}
			};
		}

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
		StringBuilder builder = new StringBuilder();

		builder.append( "<" );
		builder.append( getClass().getSimpleName() );
		builder.append( " orientation=" );
		builder.append( getOrientation() );
		builder.append( " position=" );
		builder.append( getPosition() );
		builder.append( " wall=" );
		builder.append( isWall() );
		builder.append( ">" );

		return builder.toString();
	}

	private static class StyleableProperties {

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

		private static final CssMetaData<WorkpaneEdge, Orientation> ORIENTATION = new CssMetaData<WorkpaneEdge, Orientation>( "-fx-orientation", new EnumConverter<Orientation>( Orientation.class ), Orientation.HORIZONTAL ) {

			@Override
			public Orientation getInitialValue( WorkpaneEdge edge ) {
				return edge.getOrientation();
			}

			@Override
			public boolean isSettable( WorkpaneEdge edge ) {
				return edge.orientation == null || !edge.orientation.isBound();
			}

			@Override
			public StyleableProperty<Orientation> getStyleableProperty( WorkpaneEdge edge ) {
				return (StyleableProperty<Orientation>)edge.orientationProperty();
			}
		};

		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>( Control.getClassCssMetaData() );
			styleables.add( ORIENTATION );
			STYLEABLES = Collections.unmodifiableList( styleables );
		}

	}

}
