package com.parallelsymmetry.essence;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashScreen extends Canvas {

	private static final Logger log = LoggerFactory.getLogger( SplashScreen.class );

	private String title;

	private Stage stage;

	private int steps;

	private int progress;

	public SplashScreen( String title ) {
		super( 320, 180 );
		this.title = title;

		paint();

		stage = new Stage( StageStyle.UTILITY );
		stage.setTitle( title );
		stage.setResizable( false );
		stage.setAlwaysOnTop( true );
		stage.setScene( new Scene( new BorderPane( this ) ) );

		FxUtil.centerStage( stage, getWidth(), getHeight() );
	}

	public void setSteps( int steps ) {
		this.steps = steps;
	}

	public SplashScreen show() {
		stage.show();
		return this;
	}

	public SplashScreen hide() {
		stage.hide();
		return this;
	}

	public void update() {
		progress++;
		paint();
	}

	public void done() {
		progress = steps;
		paint();
	}

	private void paint() {
		double w = getWidth();
		double h = getHeight();
		double p = w * ((double)progress / (double)steps);

		GraphicsContext gc = getGraphicsContext2D();

		// Background
		gc.setFill( Color.GRAY.darker() );
		gc.fillRect( 0, 0, w, h );

		// Scenery
		gc.setFill( new Color( 0.5, 0.5, 0.6, 0.5 ) );
		gc.fillArc( -160, -160, 320, 320, 0, 360, ArcType.CHORD );
		gc.setFill( new Color( 0.5, 0.6, 0.6, 0.5 ) );
		gc.fillArc( -60, -240, 320, 320, 0, 360, ArcType.CHORD );

		// Title
		gc.setFont( new Font( 40 ) );
		gc.setFill( new Color( 0.9, 0.9, 0.9, 1.0 ) );
		gc.fillText( title, 20, 100 );

		// Progress bar
		gc.setFill( progress == steps ? Color.WHITE : Color.GRAY );
		gc.fillRect( 0, h - 10, p, h );
	}

}
