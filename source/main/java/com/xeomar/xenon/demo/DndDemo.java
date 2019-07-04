package com.xeomar.xenon.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DndDemo extends Application {

	@Override
	public void start( Stage stage ) {
		Group root = new Group();
		Scene scene = new Scene( root, 400, 200 );

		final Label source = new Label( "DRAG ME" );
		source.setCursor( Cursor.HAND );
		source.relocate( 50, 100 );
		source.setScaleX( 2.0 );
		source.setScaleY( 2.0 );
		source.setBackground( new Background( new BackgroundFill( Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY ) ) );

		final Label target = new Label( "DROP HERE" );
		target.relocate( 250, 100 );
		target.setScaleX( 2.0 );
		target.setScaleY( 2.0 );
		target.setBackground( new Background( new BackgroundFill( Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY ) ) );

		source.setOnDragDetected( ( event ) -> {
			System.out.println( "onDragDetected" );

			Dragboard db = source.startDragAndDrop( TransferMode.MOVE );
			ClipboardContent content = new ClipboardContent();
			content.putString( "DRAGGED CONTENT" );
			db.setContent( content );

			// FIXME Using setDragView() breaks DnD on OpenJFK 13
			Image image = source.snapshot( null, null );
			db.setDragView( image, 0.5 * image.getWidth(), 0.5 * image.getHeight() );

			event.consume();
		} );

		source.setOnDragDone( ( event ) -> {
			System.out.println( "onDragDone" );

			if( event.getTransferMode() == TransferMode.MOVE ) source.setText( "" );

			event.consume();
		} );

		target.setOnDragOver( ( event ) -> {
			System.out.println( "onDragOver" );

			if( event.getGestureSource() != target && event.getDragboard().hasString() ) {
				event.acceptTransferModes( TransferMode.MOVE );
			}

			event.consume();
		} );

		target.setOnDragEntered( ( event ) -> {
			System.out.println( "onDragEntered" );

			if( event.getGestureSource() != target && event.getDragboard().hasString() ) {
				target.setTextFill( Color.WHITE );
			}

			event.consume();
		} );

		target.setOnDragExited( ( event ) -> {
			target.setTextFill( Color.BLACK );

			event.consume();
		} );

		target.setOnDragDropped( ( event ) -> {
			System.out.println( "onDragDropped" );

			Dragboard db = event.getDragboard();
			boolean hasString = db.hasString();
			if( hasString ) target.setText( db.getString() );

			event.setDropCompleted( hasString );

			event.consume();
		} );

		root.getChildren().add( source );
		root.getChildren().add( target );
		stage.setScene( scene );
		stage.show();
	}

	public static void main( String[] args ) {
		Application.launch( args );
	}

}
