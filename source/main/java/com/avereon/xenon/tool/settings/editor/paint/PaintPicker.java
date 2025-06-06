package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.zerra.color.PaintSwatch;
import com.avereon.zerra.color.Paints;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Paint;
import javafx.stage.Popup;
import lombok.Setter;

public class PaintPicker extends Button {

	private final PaintSwatch swatch;

	private final PaintPickerPane pickerPane;

	private final Popup popup;

	private boolean priorNotSet = true;

	@Setter
	private String prior;

	public PaintPicker() {
		getStyleClass().add( "cartesia-paint-picker" );

		setGraphic( swatch = new PaintSwatch() );

		DialogPane pane = new DialogPane() {

			protected Node createButton( ButtonType buttonType ) {
				return doCreateButton( buttonType );
			}
		};
		pane.setContent( pickerPane = new PaintPickerPane() );
		pane.getButtonTypes().addAll( ButtonType.OK, ButtonType.CANCEL );
		pane.minWidthProperty().bind( this.widthProperty() );

		popup = new Popup();
		popup.setAutoHide( true );
		popup.setHideOnEscape( true );
		popup.getContent().add( pane );

		setOnAction( e -> doTogglePaintDialog() );

		doUpdateText( null );
		pickerPane.paintProperty().addListener( ( p, o, n ) -> {
			swatch.setPaint( Paints.parseWithNullOnException( n ) );
			doUpdateText( n );
		} );
	}

	//	/**
	//	 * Convenience method to get the paint.
	//	 * @return the paint
	//	 */
	//	public Paint getPaint() {
	//		return calcPaint();
	//	}
	//
	//	/**
	//	 * Convenience method to set the paint.
	//	 * @param paint the paint
	//	 */
	//	public void setPaint( Paint paint ) {
	//		setPaintAsString( Paints.toString( paint ) );
	//	}

	public String getPaintAsString() {
		return pickerPane.getPaint();
	}

	public StringProperty paintAsStringProperty() {
		return pickerPane.paintProperty();
	}

	public void setPaintAsString( String paint ) {
		pickerPane.setPaint( paint );
	}

	public ObservableList<PaintMode> getOptions() {
		return pickerPane.getOptions();
	}

	private Paint calcPaint() {
		// TODO Use the paint modes to convert from string to paint
		String paint = pickerPane.getPaint();
		return paint == null ? null : Paints.parseWithNullOnException( paint );
	}

	private Node doCreateButton( ButtonType buttonType ) {
		final Button button = new Button( buttonType.getText() );
		final ButtonBar.ButtonData buttonData = buttonType.getButtonData();
		ButtonBar.setButtonData( button, buttonData );
		button.setDefaultButton( buttonData.isDefaultButton() );
		button.setCancelButton( buttonData.isCancelButton() );
		button.addEventHandler( ActionEvent.ACTION, e -> {
			if( e.isConsumed() ) return;
			setResultAndClose( buttonType );
		} );

		return button;
	}

	private void doUpdateText( String paint ) {
		String text = paint == null ? PaintMode.NONE.getKey() : paint.trim();

		if( PaintMode.LAYER.getKey().equals( text ) ) text = Rb.text( RbKey.LABEL, "layer" ).toLowerCase();
		if( PaintMode.NONE.getKey().equals( text ) ) text = Rb.text( RbKey.LABEL, "none" ).toLowerCase();

		setText( text );
	}

	private void doTogglePaintDialog() {
		if( !popup.isShowing() ) {
			setPrior( getPaintAsString() );
			Point2D anchor = localToScreen( new Point2D( 0, getHeight() ) );
			popup.show( this, anchor.getX(), anchor.getY() );
			pickerPane.requestFocus();
		} else {
			popup.hide();
		}
	}

	private void setResultAndClose( ButtonType type ) {
		//if( type == ButtonType.APPLY ) setPaintAsString( prior );
		if( type == ButtonType.CANCEL ) setPaintAsString( prior );
		popup.hide();
	}

}
