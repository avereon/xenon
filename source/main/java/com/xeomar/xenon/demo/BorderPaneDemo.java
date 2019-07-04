package com.xeomar.xenon.demo;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class BorderPaneDemo extends Application {

	@Override
	public void start( Stage stage ) {

		Scene scene = new Scene( getTest1(), 400, 250 );
		stage.setScene( scene );
		stage.show();
	}

	private Parent getTest1() {
		BorderPane root = new BorderPane();

		// TODO Put work here
		root.setTop( new Label( "Mark" ) );

		// NOTE Adding this directly to the border pane keeps the pane from squishing
		// NOTE Putting it into a stack pane did not help
		Rectangle a = new Rectangle( 200, 200 );
		a.setFill( Color.GREEN );
		Rectangle b = new Rectangle( 200, 200 );
		b.setFill( Color.BLUE );
		b.relocate( 100, 100 );
		Rectangle c = new Rectangle( 200, 200 );
		c.setFill( Color.RED);
		c.relocate( -100, -100 );

		Rectangle clip = new Rectangle(  );

		Pane p = new Pane( b, a, c );
		clip.widthProperty().bind( p.widthProperty() );
		clip.heightProperty().bind( p.heightProperty() );

		p.setClip( clip );
		root.setCenter( p );

		root.setBottom( new Label( "Soderquist" ) );
		return root;
	}

}
