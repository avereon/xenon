package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.xenon.ui.PropertyListCell;
import com.avereon.zarra.color.PaintSwatch;
import com.avereon.zarra.color.Paints;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import java.util.Map;

public class PaintPickerPane extends VBox {

	private final ComboBox<PaintMode> mode;

	private final ComboBox<PaintPalette> palette;

	private final TextField paintField;

	private StringProperty paint;

	private String prior;

	@Deprecated
	private final Map<PaintMode, PaintPaletteBox> paletteBoxes;

	@Deprecated
	private PaintPaletteBox paletteBox;

	public PaintPickerPane() {
		getStyleClass().add( "paint-picker-pane" );
		// How about a combo for the mode: none, solid, linear[] and radial()
		// To the right of the combo a component to define gradient stops
		// Below that, the tabs for palette, RGB, HSB and WEB
		// Opacity can be a slider on the right or the bottom
		// Below that the OK and Cancel buttons

		this.paletteBoxes = Map.of(
			PaintMode.PALETTE_BASIC,
			new PaintPaletteBox( new BasicPaintPalette() ),
			PaintMode.PALETTE_MATERIAL,
			new PaintPaletteBox( new MaterialPaintPalette() ),
			PaintMode.PALETTE_STANDARD,
			new PaintPaletteBox( new StandardPalette() ),
			PaintMode.NONE,
			new PaintPaletteBox( new EmptyPaintPalette() )
		);

		// The paint mode chooser
		mode = new ComboBox<>();
		mode.setMaxWidth( Double.MAX_VALUE );
		// FIXME This is a hack to get the palettes to work.
		mode.getItems().addAll( PaintMode.PALETTE_MATERIAL, PaintMode.PALETTE_STANDARD, PaintMode.PALETTE_BASIC, PaintMode.NONE );

		// The paint palette chooser
		palette = new ComboBox<>();
		palette.setMaxWidth( Double.MAX_VALUE );
		palette.getItems().addAll( new MaterialPaintPalette(), new StandardPalette(), new BasicPaintPalette() );

		// TODO palette.setCellFactory( new PropertyValueFactory<PaintPalette, String>( "name" ) );

		palette.setCellFactory( new PropertyListCell<>( PaintPalette.class, "name" ) );

		// The initial color palette
		paletteBox = paletteBoxes.get( PaintMode.PALETTE_MATERIAL );

		// The paint text field for manual entry
		paintField = new TextField();

		// Add the children
		getChildren().addAll( mode, palette, paletteBox, paintField );

		// The mode change handler
		mode.valueProperty().addListener( ( p, o, n ) -> doSetMode( n ) );

		// The palette change handler
		palette.valueProperty().addListener( ( p, o, n ) -> doSetPalette( n ) );

		// The paint text field change handler
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

	private void doSetMode( PaintMode n ) {
		if( n == null || n == PaintMode.NONE ) {
			if( paletteBox != null ) paletteBox.setVisible( false );
			prior = getPaint();
			doSetPaint( null );
		} else {
			// FIXME This is a hack to get the palettes to work.
			// If n is a palette mode, change the palette box
			if( n.isPalette() ) {
				getChildren().set( 2, paletteBox = paletteBoxes.get( n ) );
				paletteBox.setVisible( true );
			}
			if( prior != null ) doSetPaint( prior );
		}
	}

	private void doSetPalette( PaintPalette n ) {
		//		if( n != null ) {
		//			getChildren().set( 1, paletteBox = new PaintPaletteBox( n ) );
		//		}
	}

	private void updateMode( String paint ) {
		mode.getSelectionModel().select( PaintMode.getPaintMode( paint ) );
	}

	private class PaintPaletteBox extends GridPane {

		public PaintPaletteBox( PaintPalette palette ) {
			getStyleClass().addAll( "paint-palette-box" );

			managedProperty().bind( visibleProperty() );

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
