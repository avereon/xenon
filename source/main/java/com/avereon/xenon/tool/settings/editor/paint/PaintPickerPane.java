package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.zarra.color.PaintSwatch;
import com.avereon.zarra.color.Paints;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;

public class PaintPickerPane extends BorderPane {

	private final ComboBox<PaintMode> mode;

	private final BorderPane solidColorBox;

	private final TextField paintField;

	private StringProperty paint;

	private String prior;

	public PaintPickerPane() {
		getStyleClass().add( "paint-picker-pane" );
		// How about a combo for the mode: none, solid, linear[] and radial()
		// To the right of the combo a component to define gradient stops
		// Below that, the tabs for palette, RGB, HSB and WEB
		// Opacity can be a slider on the right or the bottom
		// Below that the OK and Cancel buttons

		// The paint text field for manual entry
		paintField = new TextField();

		// The paint palette chooser
		ComboBox<PaintPalette> palette = new ComboBox<>();
		palette.setMaxWidth( Double.MAX_VALUE );
		palette.getItems().addAll( new MaterialPaintPalette(), new StandardPalette(), new BasicPaintPalette() );
		palette.getSelectionModel().selectFirst();

		// The initial color palette
		solidColorBox = new BorderPane( new PaintPaletteBox( palette.getItems().getFirst() ), palette, null, paintField, null );

		// The paint mode chooser
		mode = new ComboBox<>();
		mode.setMaxWidth( Double.MAX_VALUE );
		mode.getItems().addAll( PaintMode.SOLID, PaintMode.NONE );
		mode.getSelectionModel().selectFirst();

		// Add the children
		setCenter( solidColorBox );
		setTop( mode );

		// The mode change handler
		mode.valueProperty().addListener( ( p, o, n ) -> doSetMode( n ) );

		// The palette change handler
		palette.valueProperty().addListener( ( p, o, n ) -> doSetPalette( n ) );

		// The paint text field change handler
		paintField.textProperty().addListener( ( p, o, n ) -> doSetPaint( n ) );

		// Set defaults
		doSetMode( PaintMode.SOLID );
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
	}

	private void doSetPaint( String paint ) {
		paintProperty().set( paint );
		updateModeFromPaintValue( paint );
	}

	private void doSetMode( PaintMode n ) {
		if( n == null || n == PaintMode.NONE ) {
			prior = getPaint();
			doSetPaint( null );
			setCenter( null );
		} else if( n == PaintMode.SOLID ) {
			if( prior != null ) doSetPaint( prior );
			setCenter( solidColorBox );
		} else {
			// TODO If the mode is LINEAR or RADIAL, show a gradient paint box
			setCenter( null );
		}
	}

	private void doSetPalette( PaintPalette n ) {
		if( n == null ) return;
		solidColorBox.setCenter( new PaintPaletteBox( n ) );
	}

	private void updateModeFromPaintValue( String paint ) {
		mode.getSelectionModel().select( PaintMode.getPaintMode( paint ) );
	}

	private class PaintPaletteBox extends GridPane {

		public PaintPaletteBox( PaintPalette palette ) {
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
