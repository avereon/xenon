package com.avereon.xenon.workarea;

import com.avereon.settings.Settings;
import com.avereon.util.Configurable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.*;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
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
public class WorkpaneEdge extends Control implements Configurable {

	private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "horizontal" );

	private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "vertical" );

	private StyleableObjectProperty<Orientation> orientation;

	private WorkpaneEdge topEdge;

	private WorkpaneEdge leftEdge;

	private WorkpaneEdge rightEdge;

	private WorkpaneEdge bottomEdge;

	Set<WorkpaneView> topViews;

	Set<WorkpaneView> bottomViews;

	Set<WorkpaneView> leftViews;

	Set<WorkpaneView> rightViews;

	/**
	 * <p>Represents the location where the divider should ideally be positioned, between 0.0 and 1.0 (inclusive) when the position is relative. 0.0 represents the left- or top-most point, and 1.0 represents the right- or bottom-most point
	 * (depending on the orientation property).
	 * <p>
	 * <p>As the user drags the edge around this property will be updated to always represent its current location.
	 */
	private DoubleProperty position;

	private boolean absolute;

	private Side side;

	private Workpane parent;

	private Settings settings;

	public WorkpaneEdge( Orientation orientation ) {
		this( orientation, null );
	}

	public WorkpaneEdge( Orientation orientation, Side side ) {
		getStyleClass().add( "workpane-edge" );
		setOrientation( orientation );
		setSnapToPixel( true );
		setPosition( 0 );
		this.side = side;

		// Create the view lists.
		Set<WorkpaneView> viewsA = new CopyOnWriteArraySet<>();
		Set<WorkpaneView> viewsB = new CopyOnWriteArraySet<>();
		topViews = viewsA;
		bottomViews = viewsB;
		leftViews = viewsA;
		rightViews = viewsB;

		// Register the mouse handlers
		onMouseDraggedProperty().set( this::mouseDragged );
	}

	@Override
	protected Skin<WorkpaneEdge> createDefaultSkin() {
		return new EdgeSkin( this );
	}

	public final boolean isWall() {
		return side != null;
	}

	public String getEdgeId() {
		if( settings != null ) return settings.getName();
		if( side != null ) return side.name().toLowerCase();
		return null;
	}

	/**
	 * <p>This property controls how the WorkpaneEdge should be displayed to the
	 * user. Orientation.HORIZONTAL will result in a horizontal divider, while
	 * Orientation.VERTICAL will result in a vertical divider.</p>
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
	public final StyleableObjectProperty<Orientation> orientationProperty() {
		if( orientation == null ) {
			orientation = new StyleableObjectProperty<>( null ) {

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
		if( settings != null ) settings.set( "position", value );
	}

	public final DoubleProperty positionProperty() {
		if( position == null ) position = new SimpleDoubleProperty( this, "position", 0 );
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
				return topEdge;
			}
			case BOTTOM: {
				return bottomEdge;
			}
			case LEFT: {
				return leftEdge;
			}
			case RIGHT: {
				return rightEdge;
			}
		}

		return null;
	}

	public void setEdge( Side direction, WorkpaneEdge edge ) {
		switch( direction ) {
			case TOP: {
				topEdge = edge;
				if( settings != null ) settings.set( "t", edge == null ? null : edge.getEdgeId() );
				break;
			}
			case LEFT: {
				leftEdge = edge;
				if( settings != null ) settings.set( "l", edge == null ? null : edge.getEdgeId() );
				break;
			}
			case RIGHT: {
				rightEdge = edge;
				if( settings != null ) settings.set( "r", edge == null ? null : edge.getEdgeId() );
				break;
			}
			case BOTTOM: {
				bottomEdge = edge;
				if( settings != null ) settings.set( "b", edge == null ? null : edge.getEdgeId() );
				break;
			}
		}
	}

	Set<WorkpaneView> getViews( Side direction ) {
		switch( direction ) {
			case TOP: {
				return topViews;
			}
			case LEFT: {
				return leftViews;
			}
			case RIGHT: {
				return rightViews;
			}
			case BOTTOM: {
				return bottomViews;
			}
		}

		return null;
	}

	@Override
	public void setSettings( Settings settings ) {
		if( settings == null ) {
			this.settings = null;
			return;
		} else if( this.settings != null ) {
			return;
		}

		this.settings = settings;

		// Restore state from settings
		if( settings.get( "position" ) != null ) setPosition( settings.get( "position", Double.class ) );

		// Persist state to settings
		settings.set( "orientation", getOrientation().name().toLowerCase() );
		settings.set( "position", getPosition() );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( "<" );
		builder.append( getClass().getSimpleName() );
		builder.append( " id=" );
		builder.append( getEdgeId() );
		builder.append( " orientation=" );
		builder.append( getOrientation() );
		builder.append( " position=" );
		builder.append( getPosition() );
		builder.append( " wall=" );
		builder.append( isWall() );
		builder.append( ">" );

		return builder.toString();
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

	private static class StyleableProperties {

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

		private static final StyleConverter<String, Orientation> converter = StyleConverter.getEnumConverter( Orientation.class );

		private static final CssMetaData<WorkpaneEdge, Orientation> ORIENTATION = new CssMetaData<>( "-fx-orientation", converter, Orientation.HORIZONTAL ) {

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
				return edge.orientationProperty();
			}
		};

		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>( Control.getClassCssMetaData() );
			styleables.add( ORIENTATION );
			STYLEABLES = Collections.unmodifiableList( styleables );
		}

	}

}
