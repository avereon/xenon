package com.avereon.xenon;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Set;

public class DnDCheck extends Application {

	@Override
	public void start( Stage primaryStage ) throws Exception {
		Label source = new Label( "Drag Me" );
		source.setOnDragDetected( e -> {
			Dragboard board = ((Node)e.getSource()).startDragAndDrop( TransferMode.ANY );
			ClipboardContent content = new ClipboardContent();
			content.putUrl( "test:test" );
			content.putString( "test:test" );
			board.setContent( content );
		} );
		source.setStyle( "-fx-background-color: #C0D0FF;" );
		source.setMaxWidth( Double.MAX_VALUE );
		source.setAlignment( Pos.CENTER );

		Pane target = new Pane();
		target.setBackground( new Background( new BackgroundFill( Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY ) ) );

		target.setOnDragEntered( DnDCheck::acceptTransferMode );
		target.setOnDragOver( target.getOnDragEntered() );

		BorderPane root = new BorderPane( target, source, null, null, null );
		root.setBackground( Background.EMPTY );

		Scene scene = new Scene( root, 500, 500, Color.RED );

		primaryStage.setScene( scene );
		primaryStage.centerOnScreen();
		primaryStage.setX( 2000 );
		primaryStage.setY( 200 );
		primaryStage.show();
	}

	private static final TransferMode[] defaultTransferMode = new TransferMode[]{ TransferMode.MOVE };

	private static void acceptTransferMode( DragEvent e ) {
		Set<TransferMode> modes = e.getDragboard().getTransferModes();
		System.out.println( "etype="+ e.getEventType()+ " dtms=" + modes + " tm=" + e.getTransferMode() );
		if( modes.isEmpty() ) return;
		TransferMode[] mode = modes.size() == 1 ? TransferMode.ANY : defaultTransferMode;
		e.acceptTransferModes( mode );
	}

}
