package com.avereon.xenon;

import com.avereon.util.ThreadUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreenCheck extends Application {

	@Override
	public void init() {
		Application.setUserAgentStylesheet( STYLESHEET_MODENA );
	}

	@Override
	public void start( Stage stage ) {
		SplashScreenPane splash = new SplashScreenPane( "Xenon" );
		stage.initStyle( StageStyle.UTILITY );
		splash.show( stage );

		ThreadUtil.asDaemon( () -> {
			ThreadUtil.pause( 500 );

			double progress = 0;
			while( progress < 1.0 ) {
				double lambdaProgress = progress += 0.009;
				Platform.runLater( () -> splash.setProgress( lambdaProgress ) );
				ThreadUtil.pause( 10 );
			}

			ThreadUtil.pause( 1000 );
			Platform.exit();
		} ).start();
	}

}
