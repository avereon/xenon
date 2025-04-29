package com.avereon.xenon;

import com.avereon.zarra.image.RenderedImage;
import com.avereon.zarra.javafx.Fx;
import com.avereon.zenna.icon.XRingLargeIcon;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

@CustomLog
public class SplashScreenPane extends Pane {

	private static final double WIDTH = 480;

	private static final double HEIGHT = 0.5625 * WIDTH;

	private static final double BAR_SIZE = 5;

	private static final double BAR_PAD = 20;

	private final String title;

	@Setter
	@Getter
	private int expectedSteps;

	private int completedSteps;

	private final Rectangle progressTray;

	private final Rectangle progressBar;

	public SplashScreenPane( String title ) {
		this.title = title;
		getStyleClass().addAll( "splashscreen" );

		RenderedImage icon = new XRingLargeIcon().resize( 224 );
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

		getChildren().addAll( icon, tint );
		getChildren().addAll( titleText );
		getChildren().addAll( progressTray, progressBar );

		setWidth( WIDTH );
		setHeight( HEIGHT );
	}

	public SplashScreenPane show( Stage stage ) {
		Scene scene = new Scene( this, getWidth(), getHeight(), Color.BLACK );

		// NOTE Application.setUserAgentStylesheet() must be called in application for this to work properly
		scene.getStylesheets().addAll( Xenon.STYLESHEET );

		stage.setTitle( title );
		stage.setScene( scene );
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
		stage.toFront();

		return this;
	}

	public void update() {
		Fx.run( () -> doSetProgress( ((double)completedSteps++ / (double)expectedSteps) ) );
	}

	public void setCompletedSteps( final double completedSteps ) {
		Fx.run( () -> doSetProgress( completedSteps ) );
	}

	public void done() {
		Fx.run( () -> {
			if( completedSteps != expectedSteps ) log.atWarning().log( "Progress/step mismatch: %d of %d", completedSteps, expectedSteps );
			completedSteps = expectedSteps;
			doSetProgress( 1 );
		} );
	}

	public void hide() {
		if( getScene() != null ) Fx.run( () -> getScene().getWindow().hide() );
	}

	private void doSetProgress( double requestedProgress ) {
		if( requestedProgress >= 1.0 ) {
			requestedProgress = 1.0;
			progressTray.setVisible( false );
			progressBar.getStyleClass().remove( "progress-incomplete" );
			progressBar.getStyleClass().add( "progress-complete" );
		}
		progressBar.setWidth( (getWidth() - 2 * BAR_PAD) * requestedProgress );
	}

}
