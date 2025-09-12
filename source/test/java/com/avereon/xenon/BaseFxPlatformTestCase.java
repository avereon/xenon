package com.avereon.xenon;

import com.avereon.zerra.javafx.Fx;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;

public abstract class BaseFxPlatformTestCase extends BaseForAllTests {

	private Stage stage;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// NOTE Do not create an application for tests that only need the FX platform

		// Start the FX platform
		Platform.setImplicitExit( false );
		Fx.startup();
	}

	@AfterEach
	void after() {
		Fx.run( () -> {
			if( stage != null ) stage.close();
		} );
	}

	// This method is needed in order for skins to be applied to controls
	protected <T extends Control> T resolve( T control ) {
		return resolve( control, 1000, 1000 );
	}

	protected <T extends Control> T resolve( T control, double width, double height ) {
		Fx.run( () -> {
			stage = new Stage( StageStyle.UNDECORATED );
			stage.setScene( new Scene( control, width, height ) );
			// Yes, we actually have to show the stage to get the skin applied
			stage.show();
			stage.toBack();
		} );

		Fx.waitFor( TIMEOUT );
		return control;
	}

}
