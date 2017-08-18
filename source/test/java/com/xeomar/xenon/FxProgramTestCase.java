package com.xeomar.xenon;

import com.sun.javafx.stage.StageHelper;
import com.xeomar.xenon.event.ProgramStartedEvent;
import com.xeomar.xenon.event.ProgramStoppedEvent;
import com.xeomar.xenon.product.ProductMetadata;
import com.xeomar.xenon.util.OperatingSystem;
import com.xeomar.xenon.workarea.WorkpaneWatcher;
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

public abstract class FxProgramTestCase extends ApplicationTest {

	protected Program program;

	protected ProgramWatcher programWatcher;

	protected WorkpaneWatcher workpaneWatcher;

	protected ProductMetadata metadata;

	/**
	 * Overrides setup() in FxPlatformTestCase and does not call super.setup().
	 */
	@Before
	public void setup() throws Exception {
		// WORKAROUND The parameters defined below are null during testing due to Java 9 incompatibility
		// NOTE These are also used in ProgramTestCase
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );
		System.setProperty( ProgramParameter.LOG_LEVEL, "error" );

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
		program.addEventListener( programWatcher = new ProgramWatcher() );
		metadata = program.getMetadata();

		programWatcher.waitForEvent( ProgramStartedEvent.class );
		program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane().addWorkpaneListener( workpaneWatcher = new WorkpaneWatcher() );
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@After
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );

		programWatcher.waitForEvent( ProgramStoppedEvent.class );
		program.removeEventListener( programWatcher );

		// For some reason FxToolkit.cleanupApplication does not get the windows closed
		for( Window window : StageHelper.getStages() ) {
			Platform.runLater( window::hide );
		}
	}

	@Override
	public void start( Stage stage ) throws Exception {}

	//	protected void waitForEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException, TimeoutException {
	//		programWatcher.waitForEvent( clazz );
	//	}
	//
	//	protected void waitForEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
	//		programWatcher.waitForEvent( clazz, timeout );
	//	}
	//
	//	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz ) throws InterruptedException, TimeoutException {
	//		programWatcher.waitForNextEvent( clazz );
	//	}
	//
	//	protected void waitForNextEvent( Class<? extends ProgramEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
	//		programWatcher.waitForNextEvent( clazz, timeout );
	//	}

}
