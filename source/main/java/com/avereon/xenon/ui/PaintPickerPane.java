package com.avereon.xenon.ui;

import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.UiFactory;
import com.avereon.zerra.color.Colors;
import com.avereon.zerra.color.PaintSwatch;
import com.avereon.zerra.color.Paints;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;
import java.util.stream.Collectors;

public class PaintPickerPane extends VBox {

	private final ComboBox<PaintMode> mode;

	private ObservableList<PaintMode> options;

	private final TextField paintField;

	private StringProperty paint;

	private String prior;

	public PaintPickerPane() {
		getStyleClass().add( "paint-picker-pane" );
		// How about a combo for the mode: none, solid, linear[] and radial()
		// To the right of the combo a component to define gradient stops
		// Below that, the tabs for palette, RGB, HSB an WEB
		// Opacity can be a slider on the right or the bottom
		// Below that the OK and Cancel buttons

		// The paint mode chooser
		mode = new ComboBox<>();
		mode.setMaxWidth( Double.MAX_VALUE );
		mode.getItems().addAll( PaintMode.NONE, PaintMode.SOLID );

		// The paint stop editor
		//RangeSlider paintStopEditor = new RangeSlider();

		// The color palette
		PaintPalette palette = new PaintPalette();

		// The color selection tabs
		// Apparently tab pane does not do well in a popup
		TabPane colorTabs = new TabPane();
		colorTabs.getTabs().add( new Tab( "Palette", new Label( "DONT JITTER" ) ) );

		// The paint text field for manual entry
		paintField = new TextField();

		// Add the children
		getChildren().addAll( mode, palette, paintField );

		getOptions().addListener( (ListChangeListener<PaintMode>)( e ) -> {
			mode.getItems().clear();
			mode.getItems().addAll( options );
		} );

		// The mode change handler
		mode.valueProperty().addListener( this::doModeChanged );

		// The text field change handler
		paintField.textProperty().addListener( ( p, o, n ) -> doSetPaint( n ) );
	}

	@Override
	public void requestFocus() {
		mode.requestFocus();
	}

	public ObservableList<PaintMode> getOptions() {
		if( options == null ) options = FXCollections.observableArrayList();
		return options;
	}

	public String getPaint() {
		return paint == null ? null : paint.get();
	}

	public StringProperty paintProperty() {
		if( paint == null ) paint = new SimpleStringProperty();
		return paint;
	}

	public void setPaint( String paint ) {
		doSetPaint( paint );
		updateMode( paint );
		paintField.setText( paint );
	}

	private void doSetPaint( String paint ) {
		paintProperty().set( paint );
	}

	private void doModeChanged( ObservableValue<? extends PaintMode> p, PaintMode o, PaintMode n ) {
		if( n == PaintMode.NONE ) {
			prior = getPaint();
			doSetPaint( null );
		} else if( prior != null ) {
			doSetPaint( prior );
		}
	}

	private void updateMode( String paint ) {
		mode.getSelectionModel().select( PaintMode.getPaintMode( paint ) );
	}

	private class PaintPalette extends VBox {

		public PaintPalette() {
			setSpacing( UiFactory.PAD );

			// Black, Red, Green Yellow, Blue, Magenta, Cyan, White
			// X, R, G, RG, B, RB, GB, RGB
			List<Color> bases = List.of( Color.GRAY, Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.CYAN );

			for( double factor = 1.0; factor > 0.0; factor -= 0.25 ) {
				final double shadeFactor = factor;
				HBox shades = new HBox( UiFactory.PAD );
				shades.getChildren().addAll( bases.stream().map( base -> getSwatch( Colors.getShade( base, shadeFactor ) ) ).collect( Collectors.toList() ) );
				getChildren().add( shades );
			}

			HBox hue = new HBox( UiFactory.PAD );
			hue.getChildren().addAll( bases.stream().map( this::getSwatch ).collect( Collectors.toList() ) );
			getChildren().add( hue );

			for( double factor = 0.25; factor <= 1.0; factor += 0.25 ) {
				final double tintFactor = factor;
				HBox tints = new HBox( UiFactory.PAD );
				tints.getChildren().addAll( bases.stream().map( base -> getSwatch( Colors.getTint( base, tintFactor ) ) ).collect( Collectors.toList() ) );
				getChildren().add( tints );
			}
		}

		private Node getSwatch( Paint paint ) {
			PaintSwatch swatch = new PaintSwatch( paint );
			swatch.onMouseClickedProperty().set( e -> doSetPaint( Paints.toString( paint ) ) );
			return swatch;
		}

	}

	public static class PaintMode {

		public static final PaintMode NONE;

		public static final PaintMode SOLID;

		public static final PaintMode LINEAR;

		public static final PaintMode RADIAL;

		public static final PaintMode OTHER;

		private final String key;

		private final String label;

		private String value;

		static {
			NONE = new PaintMode( "none", Rb.text( BundleKey.LABEL, "none" ) );
			SOLID = new PaintMode( "solid", Rb.text( BundleKey.LABEL, "solid" ) );
			LINEAR = new PaintMode( "linear", Rb.text( BundleKey.LABEL, "linear" ) );
			RADIAL = new PaintMode( "radial", Rb.text( BundleKey.LABEL, "radial" ) );
			OTHER = new PaintMode( "other", Rb.text( BundleKey.LABEL, "other" ) );
		}

		public PaintMode( String key, String label ) {
			this( key, label, null );
		}

		public PaintMode( String key, String label, String value ) {
			this.key = key;
			this.label = label;
			//this.value = value;

			//		String none = product.rb().textOr( BundleKey.LABEL, "none", "None" );
			//		String solid = product.rb().textOr( BundleKey.LABEL, "solid", "Solid Color" );
			//		String linear = product.rb().textOr( BundleKey.LABEL, "linear-gradient", "Linear Gradient" );
			//		String radial = product.rb().textOr( BundleKey.LABEL, "radial-gradient", "Radial Gradient" );
		}

		public String getKey() {
			return key;
		}

		public String getLabel() {
			return label;
		}

		public static PaintMode getPaintMode( String paint ) {
			if( TextUtil.isEmpty( paint ) ) return PaintMode.NONE;

			return switch( paint.charAt( 0 ) ) {
				case '#' -> PaintMode.SOLID;
				case '[' -> PaintMode.LINEAR;
				case '(' -> PaintMode.RADIAL;
				default -> PaintMode.OTHER;
				//default -> throw new IllegalStateException( "Unexpected value: " + paint );
			};
		}

		//	public String getValue() {
		//		return value;
		//	}
		//
		//	public void setValue( String value ) {
		//		this.value = value;
		//	}

		@Override
		public String toString() {
			return getLabel();
		}

	}
}
