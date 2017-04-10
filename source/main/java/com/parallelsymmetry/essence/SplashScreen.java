package com.parallelsymmetry.essence;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashScreen extends Stage {

	private static final Logger log = LoggerFactory.getLogger( SplashScreen.class );

	private int steps;

	private int progress;

	private Rectangle progressBar;

	public SplashScreen( String title ) {
		super( StageStyle.UTILITY );

		setTitle( title );

		progressBar = new Rectangle( 0, 170, 0, 180 );
		progressBar.setFill( new Color( 0.7, 0.7, 0.7, 1.0 ) );

		Text titleText = new Text( 20, 100, title );
		titleText.setFill( new Color( 0.9, 0.9, 0.9, 1.0 ) );
		titleText.setFont( new Font( 40 ) );

		Pane pane = new Pane();
		pane.getChildren().add( new Circle( -40, 80, 160, new Color( 0.5, 0.5, 0.6, 0.5 ) ) );
		pane.getChildren().add( new Circle( 80, -200, 360, new Color( 0.5, 0.6, 0.6, 0.5 ) ) );
		pane.getChildren().add( titleText );
		pane.getChildren().add( progressBar );

		setScene( new Scene( pane, 320, 180, Color.GRAY.darker() ) );
		sizeToScene();
		centerOnScreen();
	}

	public void setSteps( int steps ) {
		this.steps = steps;
	}

	public void update() {
		progress++;
		progressBar.setWidth( getWidth() * ((double)progress / (double)steps) );
	}

	public void done() {
		progress = steps;
		progressBar.setWidth( getWidth() );
		progressBar.setFill( Color.WHITE );
	}

}
