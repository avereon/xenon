package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class SplashScreenPane extends Pane {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final int WIDTH = 320;

	private static final int HEIGHT = 180;

	private static final int TITLE_PAD = 40;

	private static final int BAR_SIZE = 5;

	private static final int BAR_PAD = 20;

	private String title;

	private int steps;

	private int progress;

	private Rectangle progressTray;

	private Rectangle progressBar;

	public SplashScreenPane( String title ) {
		this.title = title;

		// The background is a workaround to the stage color changing on Windows
		Rectangle background = new Rectangle( 0, 0, WIDTH, HEIGHT );
		background.setFill( new Color( 0.5, 0.5, 0.55, 1.0 ) );

		Text titleText = new Text( title );
		titleText.setFill( new Color( 0.9, 0.9, 0.9, 1.0 ) );
		titleText.setBoundsType( TextBoundsType.VISUAL );
		titleText.setFont( new Font( 60 ) );
		titleText.setX( TITLE_PAD );
		titleText.setY( TITLE_PAD + titleText.getLayoutBounds().getHeight() );

		progressTray = new Rectangle( BAR_PAD, HEIGHT - BAR_PAD - BAR_SIZE, WIDTH - 2 * BAR_PAD, BAR_SIZE );
		progressTray.getStyleClass().addAll( "splashscreen-progress", "splashscreen-progress-tray");

		progressBar = new Rectangle( BAR_PAD, HEIGHT - BAR_PAD - BAR_SIZE, 0, BAR_SIZE );
		progressBar.getStyleClass().addAll( "splashscreen-progress", "splashscreen-progress-incomplete");

		getChildren().add( background );
		getChildren().add( new Circle( -40, 80, 160, new Color( 1, 1, 1, 0.1 ) ) );
		getChildren().add( new Circle( 80, -240, 360, new Color( 1, 1, 1, 0.1 ) ) );
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
		stage.setTitle( title );
		Scene scene = new Scene( this, getWidth(), getHeight(), Color.BLACK );
		scene.getStylesheets().add( Program.STYLESHEET );
		stage.setScene( scene );
		stage.sizeToScene();
		stage.show();
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
		getScene().getWindow().hide();
	}

	public static void main( String[] commands ) {
		JavaFxStarter.startAndWait( 1000 );
		Platform.runLater( () -> {
			SplashScreenPane splash = new SplashScreenPane( "Test" );
			splash.setProgress( 0.8 );
			Scene scene = new Scene( splash, WIDTH, HEIGHT );
			scene.getStylesheets().add( Program.STYLESHEET );
			Stage stage = new Stage();
			stage.initStyle( StageStyle.UTILITY );
			stage.setScene( scene );
			stage.show();
		} );
	}

}
