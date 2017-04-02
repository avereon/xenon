package com.parallelsymmetry.essence;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Program extends Application {

	private static final Logger log = LoggerFactory.getLogger( Program.class );

	private static String title;

	//	public static void main( String commands ) {
	//		System.out.println( "Main method before launch" );
	//		//launch(commands);
	//	}

	@Override
	public void init() throws Exception {
		super.init();
		log.info( "Initialize the program" );

		title = "Essence";
	}

	@Override
	public void start( Stage primaryStage ) throws Exception {
		log.info( "Start the program" );

		Stage splashStage = createSplashStage();
		splashStage.show();

		primaryStage.setTitle(title);
	}

	@Override
	public void stop() throws Exception {
		log.info( "Stop the program" );
		super.stop();
	}

	private Stage createSplashStage() {
		Rectangle2D bounds = Screen.getPrimary().getBounds();

		SplashScreen splashScreen = new SplashScreen();

		Stage stage = new Stage(StageStyle.UTILITY);
		stage.setTitle( title );
		stage.setResizable( false );
		stage.setAlwaysOnTop( true );
		stage.setScene( new Scene( new VBox( splashScreen ) ) );
		stage.setX( bounds.getMinX() + ((bounds.getWidth() - splashScreen.getWidth()) / 2) );
		stage.setY( bounds.getMinY() + ((bounds.getHeight() - splashScreen.getHeight()) / 2) );

		return stage;
	}

}
