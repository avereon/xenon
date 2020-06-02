package com.avereon.xenon.workpane;

import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.util.IdGenerator;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ecco on 5/29/17.
 */
public class WorkpaneEdge extends Control implements WritableIdentity {

	private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "horizontal" );

	private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "vertical" );

	private StyleableObjectProperty<Orientation> orientation;

	private final Side wall;

	private ObjectProperty<WorkpaneEdge> topEdge;

	private ObjectProperty<WorkpaneEdge> leftEdge;

	private ObjectProperty<WorkpaneEdge> rightEdge;

	private ObjectProperty<WorkpaneEdge> bottomEdge;

	Set<WorkpaneView> topViews;

	Set<WorkpaneView> bottomViews;

	Set<WorkpaneView> leftViews;

	Set<WorkpaneView> rightViews;

	/**
	 * <p>The location where the divider should ideally be positioned, between 0.0
	 * and 1.0 (inclusive) when the position is relative. 0.0 represents the
	 * left-most or top-most point, and 1.0 represents the right-most or
	 * bottom-most point (depending on the orientation property).
	 * <p>As the user drags the edge around this property will be updated to
	 * always represent its current location.
	 */
	private DoubleProperty position;

	private Workpane parent;

	public WorkpaneEdge() {
		this( null );
	}

	WorkpaneEdge( Side wall ) {
		getStyleClass().add( "workpane-edge" );

		setProductId( IdGenerator.getId() );
		setSnapToPixel( true );
		setPosition( 0 );

		this.wall = wall;

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

	final boolean isWall() {
		return wall != null;
	}

	@Override
	public String getProductId() {
		if( isWall() ) return wall.name().toLowerCase();
		return getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setProductId( String id ) {
		getProperties().put( Identity.KEY, id );
	}

	/**
	 * <p>This property controls how the WorkpaneEdge should be displayed to the
	 * user. Orientation.HORIZONTAL will result in a horizontal divider, while
	 * Orientation.VERTICAL will result in a vertical divider.</p>
	 *
	 * @param value the orientation value
	 */
	public WorkpaneEdge setOrientation( Orientation value ) {
		orientationProperty().set( value );
		return this;
	}

	/**
	 * The orientation for the WorkpaneEdge.
	 *
	 * @return The orientation for the WorkpaneEdge.
	 */
	public Orientation getOrientation() {
		return orientation == null ? Orientation.VERTICAL : orientation.get();
	}

	/**
	 * The orientation property for the WorkpaneEdge.
	 *
	 * @return the orientation property for the WorkpaneEdge
	 */
	public StyleableObjectProperty<Orientation> orientationProperty() {
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

	public double getPosition() {
		return position == null ? 0.5F : position.get();
	}

	public void setPosition( double value ) {
		((DoubleProperty)positionProperty()).set( value );
	}

	public ReadOnlyDoubleProperty positionProperty() {
		if( position == null ) position = new SimpleDoubleProperty( this, "position", 0 );
		return position;
	}

	public WorkpaneEdge getTopEdge() {
		return topEdge == null ? null : topEdgeProperty().get();
	}

	public void setTopEdge( WorkpaneEdge topEdge ) {
		((ObjectProperty<WorkpaneEdge>)topEdgeProperty()).set( topEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> topEdgeProperty() {
		if( topEdge == null ) topEdge = new SimpleObjectProperty<>();
		return topEdge;
	}

	public WorkpaneEdge getLeftEdge() {
		return leftEdge == null ? null : leftEdgeProperty().get();
	}

	public void setLeftEdge( WorkpaneEdge leftEdge ) {
		((ObjectProperty<WorkpaneEdge>)leftEdgeProperty()).set( leftEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> leftEdgeProperty() {
		if( leftEdge == null ) leftEdge = new SimpleObjectProperty<>();
		return leftEdge;
	}

	public WorkpaneEdge getRightEdge() {
		return rightEdge == null ? null : rightEdgeProperty().get();
	}

	public void setRightEdge( WorkpaneEdge rightEdge ) {
		((ObjectProperty<WorkpaneEdge>)rightEdgeProperty()).set( rightEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> rightEdgeProperty() {
		if( rightEdge == null ) rightEdge = new SimpleObjectProperty<>();
		return rightEdge;
	}

	public WorkpaneEdge getBottomEdge() {
		return bottomEdge == null ? null : bottomEdgeProperty().get();
	}

	public void setBottomEdge( WorkpaneEdge bottomEdge ) {
		((ObjectProperty<WorkpaneEdge>)bottomEdgeProperty()).set( bottomEdge );
	}

	public ReadOnlyObjectProperty<WorkpaneEdge> bottomEdgeProperty() {
		if( bottomEdge == null ) bottomEdge = new SimpleObjectProperty<>();
		return bottomEdge;
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
				return getTopEdge();
			}
			case BOTTOM: {
				return getBottomEdge();
			}
			case LEFT: {
				return getLeftEdge();
			}
			case RIGHT: {
				return getRightEdge();
			}
		}

		return null;
	}

	public void setEdge( Side direction, WorkpaneEdge edge ) {
		switch( direction ) {
			case TOP: {
				setTopEdge( edge );
				break;
			}
			case LEFT: {
				setLeftEdge( edge );
				break;
			}
			case RIGHT: {
				setRightEdge( edge );
				break;
			}
			case BOTTOM: {
				setBottomEdge( edge );
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

		return Set.of();
	}

	@Override
	@SuppressWarnings( "StringBufferReplaceableByString" )
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( "<" );
		builder.append( getClass().getSimpleName() );
		builder.append( " id=" );
		builder.append( getProductId() );
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

		@SuppressWarnings( "unused" )
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
			STYLEABLES = Collections.unmodifiableList( List.of( ORIENTATION ) );
		}

	}

}
