package com.avereon.xenon;

import com.avereon.rossa.icon.XRingLargeIcon;
import com.avereon.util.Log;
import com.avereon.venza.image.ProgramImage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;

import java.lang.System.Logger;

public class SplashScreenPane extends Pane {

	private static final Logger log = Log.get();

	private static final double WIDTH = 480;

	private static final double HEIGHT = 0.5625 * WIDTH;

	private static final double TITLE_PAD = 40;

	private static final double BAR_SIZE = 5;

	private static final double BAR_PAD = 20;

	private String title;

	private int steps;

	private int progress;

	private Rectangle progressTray;

	private Rectangle progressBar;

	public SplashScreenPane( String title ) {
		this.title = title;
		getStyleClass().addAll( "splashscreen" );

		// The background is a workaround to the stage color changing on Windows
		//Rectangle background = new Rectangle( 0, 0, WIDTH, HEIGHT );
		//background.setFill( Color.GRAY );

		ProgramImage icon = new XRingLargeIcon().setSize( 192 );
		// Remove the style class that changes the outline
		icon.getStyleClass().remove( "xe-image" );
		icon.setLayoutX( 0.5 * (WIDTH - icon.getWidth()) );
		icon.setLayoutY( 0.5 * (HEIGHT - icon.getHeight() - BAR_PAD - BAR_SIZE) );

		Rectangle tint = new Rectangle( 0, 0, WIDTH, HEIGHT );
		tint.getStyleClass().addAll( "tint" );

		Text titleText = new Text( title );
		titleText.getStyleClass().addAll( "title" );
		titleText.setBoundsType( TextBoundsType.VISUAL );
		titleText.setFont( new Font( 100 ) );

		titleText.setX( 0.5 * (WIDTH - titleText.getLayoutBounds().getWidth()) );
		titleText.setY( 0.5 * (HEIGHT - titleText.getLayoutBounds().getHeight() - BAR_PAD - BAR_SIZE) + titleText.getLayoutBounds().getHeight() );

		progressTray = new Rectangle( BAR_PAD, HEIGHT - BAR_PAD - BAR_SIZE, WIDTH - 2 * BAR_PAD, BAR_SIZE );
		progressTray.getStyleClass().addAll( "progress", "progress-tray" );

		progressBar = new Rectangle( BAR_PAD, HEIGHT - BAR_PAD - BAR_SIZE, 0, BAR_SIZE );
		progressBar.getStyleClass().addAll( "progress", "progress-incomplete" );

		double radius = 80;
		double centerLine = 0.5 * (HEIGHT - BAR_PAD - BAR_SIZE);

		Circle accentA = new Circle( -40, centerLine - 0.5 * radius, radius );
		Circle accentB = new Circle( -40, centerLine - 0.0 * radius, radius );
		accentA.getStyleClass().addAll( "accent" );
		accentB.getStyleClass().addAll( "accent" );

		getChildren().addAll(  icon, tint );
		getChildren().addAll( accentA, accentB, titleText );
		getChildren().addAll( progressTray, progressBar );

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
		Scene scene = new Scene( this, getWidth(), getHeight(), Color.BLACK );

		// NOTE Application.setUserAgentStylesheet() must be called in application for this to work properly
		scene.getStylesheets().addAll( Program.STYLESHEET );

		stage.setTitle( title );
		stage.setScene( scene );
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
		stage.toFront();

		return this;
	}

	public void update() {
		setProgress( ((double)progress++ / (double)steps) );
	}

	public void setProgress( double progress ) {
		if( progress >= 1.0 ) {
			progress = 1.0;
			progressTray.setVisible( false );
			progressBar.getStyleClass().remove( "progress-incomplete" );
			progressBar.getStyleClass().add( "progress-complete" );
		}
		progressBar.setWidth( (getWidth() - 2 * BAR_PAD) * progress );
	}

	public void done() {
		progress = steps;
		setProgress( 1 );
	}

	public void hide() {
		if( getScene() != null ) getScene().getWindow().hide();
	}

}
