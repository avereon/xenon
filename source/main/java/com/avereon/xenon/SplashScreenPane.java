package com.avereon.xenon;

import com.avereon.rossa.icon.XRingLargeIcon;
import com.avereon.util.Log;
import com.avereon.venza.color.Colors;
import com.avereon.venza.image.ProgramImage;
import com.avereon.venza.javafx.JavaFxStarter;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

		Color a = Colors.mix( Color.web( "#7986CB" ), Color.BLACK, 0.2 );
		Color b = Colors.mix( Color.web( "#FF9800" ), Color.web( "#7986CB" ), 0.5 );
		double x = 0;
		double y = 0;
		LinearGradient paint = new LinearGradient( x, y, 0, HEIGHT, false, CycleMethod.NO_CYCLE, new Stop( 0, a ), new Stop( 1, b ) );

		// The background is a workaround to the stage color changing on Windows
		Rectangle background = new Rectangle( 0, 0, WIDTH, HEIGHT );
		background.setFill( paint );

		ProgramImage icon = new XRingLargeIcon().setSize( 192 );
		icon.setLayoutX( 0.5 * (WIDTH - icon.getWidth()) );
		icon.setLayoutY( 0.5 * (HEIGHT - icon.getHeight() - BAR_PAD - BAR_SIZE) );

		Rectangle tint = new Rectangle( 0, 0, WIDTH, HEIGHT );
		tint.setFill( new Color( 0.0, 0.0, 0.0, 0.7 ) );

		Text titleText = new Text( title );
		titleText.setFill( new Color( 0.8, 0.8, 0.8, 1.0 ) );
		titleText.setBoundsType( TextBoundsType.VISUAL );
		titleText.setFont( new Font( 100 ) );

		titleText.setX( 0.5 * (WIDTH - titleText.getLayoutBounds().getWidth()) );
		titleText.setY( 0.4 * (HEIGHT - titleText.getLayoutBounds().getHeight() - BAR_PAD - BAR_SIZE) + titleText.getLayoutBounds().getHeight() );

		progressTray = new Rectangle( BAR_PAD, HEIGHT - BAR_PAD - BAR_SIZE, WIDTH - 2 * BAR_PAD, BAR_SIZE );
		progressTray.getStyleClass().addAll( "splashscreen-progress", "splashscreen-progress-tray" );

		progressBar = new Rectangle( BAR_PAD, HEIGHT - BAR_PAD - BAR_SIZE, 0, BAR_SIZE );
		progressBar.getStyleClass().addAll( "splashscreen-progress", "splashscreen-progress-incomplete" );

		getChildren().add( background );
		getChildren().add( new Circle( -40, 80, 160, new Color( 1, 1, 1, 0.1 ) ) );
		getChildren().add( new Circle( 80, -240, 360, new Color( 1, 1, 1, 0.1 ) ) );
		getChildren().add( icon );
		getChildren().add( tint );
		getChildren().addAll( titleText, progressTray, progressBar );

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
		scene.getStylesheets().add( Program.STYLESHEET );

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
			progressTray.setVisible( false );
			progressBar.getStyleClass().remove( "splashscreen-progress-incomplete" );
			progressBar.getStyleClass().add( "splashscreen-progress-complete" );
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

	public static void main( String[] commands ) {
		JavaFxStarter.startAndWait( 1000 );
		Platform.runLater( () -> {
			SplashScreenPane splash = new SplashScreenPane( "Xenon" );
			splash.setProgress( 0.8 );
			Stage stage = new Stage();
			stage.initStyle( StageStyle.UTILITY );
			splash.show( stage );
		} );
	}

}
