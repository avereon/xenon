package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class SplashScreenPane extends Pane {

	private static final int WIDTH = 320;

	private static final int HEIGHT = 180;

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private String title;

	private int steps;

	private int progress;

	private Rectangle progressBar;

	public SplashScreenPane( String title ) {
		this.title = title;

		// The background is a workaround to the stage color changing on Windows
		Rectangle background = new Rectangle( 0, 0, WIDTH, HEIGHT );
		background.setFill( Color.GRAY.darker() );

		progressBar = new Rectangle( 0, 170, 0, 180 );
		progressBar.setFill( new Color( 0.7, 0.7, 0.7, 1.0 ) );

		Text titleText = new Text( 20, 100, title );
		titleText.setFill( new Color( 0.9, 0.9, 0.9, 1.0 ) );
		titleText.setFont( new Font( 40 ) );

		getChildren().add( background );
		getChildren().add( new Circle( -40, 80, 160, new Color( 0.5, 0.5, 0.6, 0.5 ) ) );
		getChildren().add( new Circle( 80, -200, 360, new Color( 0.5, 0.6, 0.6, 0.5 ) ) );
		getChildren().add( titleText );
		getChildren().add( progressBar );

		setWidth( WIDTH );
		setHeight( HEIGHT );
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps( int steps ) {
		this.steps = steps;
	}

	public int getCompletedSteps() {
		return progress;
	}

	public SplashScreenPane show( Stage stage ) {
		stage.setTitle( title );
		stage.setScene( new Scene( this, getWidth(), getHeight(), Color.BLACK ) );
		stage.sizeToScene();
		stage.show();
		return this;
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

	public void hide() {
		getScene().getWindow().hide();
	}

}
