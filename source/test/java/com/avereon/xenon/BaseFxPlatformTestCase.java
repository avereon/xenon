package com.avereon.xenon;

import com.avereon.zerra.javafx.Fx;
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
		Fx.startup();
	}

	@AfterEach
	void after() {
		Fx.run( () -> stage.close() );
	}

	// This method is needed in order for skins to be applied to controls
	protected <T extends Control> T resolve( T control ) {
		Scene scene = new Scene( control );

		Fx.run( () -> {
			stage = new Stage( StageStyle.UNDECORATED );
			stage.setScene( scene );
			// Yes, we actually have to show the stage to get the skin applied
			stage.show();
			stage.toBack();
		} );

		Fx.waitFor( TIMEOUT );
		return control;
	}

}
