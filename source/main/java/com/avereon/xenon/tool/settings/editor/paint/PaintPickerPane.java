package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.zarra.color.PaintSwatch;
import com.avereon.zarra.color.Paints;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import java.util.Map;

public class PaintPickerPane extends VBox {

	private final ComboBox<PaintMode> mode;

	private final TextField paintField;

	private final Map<PaintMode, PaintPaletteBox2> paletteBoxes;

	private StringProperty paint;

	private String prior;

	private PaintPaletteBox2 paletteBox;

	public PaintPickerPane() {
		getStyleClass().add( "paint-picker-pane" );
		// How about a combo for the mode: none, solid, linear[] and radial()
		// To the right of the combo a component to define gradient stops
		// Below that, the tabs for palette, RGB, HSB and WEB
		// Opacity can be a slider on the right or the bottom
		// Below that the OK and Cancel buttons

		this.paletteBoxes = Map.of( PaintMode.PALETTE_BASIC, new PaintPaletteBox2( new BasicPaintPalette() ), PaintMode.PALETTE_MATERIAL, new PaintPaletteBox2( new MaterialPaintPalette() ), PaintMode.NONE, new PaintPaletteBox2( new EmptyPaintPalette() ) );

		// The paint mode chooser
		mode = new ComboBox<>();
		mode.setMaxWidth( Double.MAX_VALUE );
		mode.getItems().addAll( PaintMode.PALETTE_MATERIAL, PaintMode.PALETTE_BASIC, PaintMode.NONE );

		// The paint stop editor
		//RangeSlider paintStopEditor = new RangeSlider();

		// The color palette
		paletteBox = paletteBoxes.get( PaintMode.PALETTE_BASIC );

		// The color selection tabs
		// Apparently tab pane does not do well in a popup
		TabPane colorTabs = new TabPane();
		colorTabs.getTabs().add( new Tab( "Palette", new Label( "DONT JITTER" ) ) );

		// The paint text field for manual entry
		paintField = new TextField();

		// Add the children
		getChildren().addAll( mode, paletteBox, paintField );

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
		return mode.getItems();
	}

	public String getPaint() {
		return paint == null ? null : paint.get();
	}

	public StringProperty paintProperty() {
		if( paint == null ) paint = new SimpleStringProperty();
		return paint;
	}

	public void setPaint( String paint ) {
		// Do not call doSetPaint() here, changing the paintField will do that if needed
		paintField.setText( paint );
		updateMode( paint );
	}

	private void doSetPaint( String paint ) {
		paintProperty().set( paint );
	}

	private void doModeChanged( ObservableValue<? extends PaintMode> p, PaintMode o, PaintMode n ) {
		if( n == null || n == PaintMode.NONE ) {
			if(paletteBox != null) paletteBox.setVisible( false );
			prior = getPaint();
			doSetPaint( null );
		} else {
			// If n is a palette mode, set the paint to the first color in the palette
			if( n.isPalette() ) {
				// Change the palette box to the new palette
				getChildren().set( 1, paletteBox = paletteBoxes.get( n ) );
				paletteBox.setVisible( true );
			}
			if( prior != null ) doSetPaint( prior );
		}
	}

	private void updateMode( String paint ) {
		mode.getSelectionModel().select( PaintMode.getPaintMode( paint ) );
	}

	private class PaintPaletteBox2 extends GridPane {

		public PaintPaletteBox2( PaintPalette palette ) {
			getStyleClass().addAll( "paint-palette-box" );
			for( int row = 0; row < palette.rowCount(); row++ ) {
				for( int column = 0; column < palette.columnCount(); column++ ) {
					add( getSwatch( palette.getPaint( row, column ) ), column, row );
				}
			}
		}
	}

	private PaintSwatch getSwatch( Paint paint ) {
		PaintSwatch swatch = new PaintSwatch( paint );
		swatch.onMouseClickedProperty().set( e -> doSetPaint( Paints.toString( paint ) ) );
		return swatch;
	}

}
