package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.event.ProgramStoppedEvent;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.util.OperatingSystem;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class FxProgramTestCase extends ApplicationTest {

	private ProgramWatcher watcher;

	protected Program program;

	protected ProductMetadata metadata;

	/**
	 * Override setup() in FxPlatformTestCase and does not call super.setup().
	 */
	@Before
	public void setup() throws Exception {
		// WORKAROUND The parameters defined below are null during testing due to Java 9 incompatibility
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		// Remove the existing program data folder
		try {
			String prefix = ExecMode.TEST.getPrefix();
			ProductMetadata metadata = new ProductMetadata();
			File programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
			if( programDataFolder != null && programDataFolder.exists() ) FileUtils.forceDelete( programDataFolder );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}

		program = (Program)FxToolkit.setupApplication( Program.class, ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );

		metadata = program.getMetadata();
		program.addEventListener( watcher = new ProgramWatcher() );

		waitForEvent( ProgramStartedEvent.class );
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@After
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );

		for( Window window : Stage.getWindows() ) {
			Platform.runLater( window::hide );
		}

		waitForEvent( ProgramStoppedEvent.class );
		program.removeEventListener( watcher );
	}

	@Override
	public void start( Stage stage ) throws Exception {}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException, TimeoutException {
		watcher.waitForEvent( clazz );
	}

	protected void waitForEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
		watcher.waitForEvent( clazz, timeout );
	}

	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException, TimeoutException {
		watcher.waitForNextEvent( clazz );
	}

	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
		watcher.waitForNextEvent( clazz, timeout );
	}

}
