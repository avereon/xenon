package com.xeomar.xenon;

import com.xeomar.util.FileUtil;
import com.xeomar.util.OperatingSystem;
import com.xeomar.product.ProductCard;
import com.xeomar.xenon.event.ProgramStartedEvent;
import com.xeomar.xenon.event.ProgramStoppedEvent;
import com.xeomar.xenon.workarea.WorkpaneWatcher;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;

public abstract class FxProgramTestCase extends ApplicationTest {

	protected Program program;

	protected ProgramWatcher programWatcher;

	protected WorkpaneWatcher workpaneWatcher;

	protected ProductCard metadata;

	/**
	 * Overrides setup() in ApplicationTest and does not call super.setup().
	 */
	@Before
	public void setup() throws Exception {
		// Intentionally do not call super.setup()

		Runtime.getRuntime().gc();

		long max = Runtime.getRuntime().maxMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - Runtime.getRuntime().freeMemory();
		System.out.println( "Memory: " + FileUtil.getHumanBinSize(used) + " / " + FileUtil.getHumanBinSize(total) + " / " + FileUtil.getHumanBinSize( max ) );

		// WORKAROUND The parameters defined below are null during testing due to Java 9 incompatibility
		// NOTE These are also used in ProgramTestCase
		System.setProperty( ProgramParameter.EXECMODE, ProgramParameter.EXECMODE_TEST );
		System.setProperty( ProgramParameter.LOG_LEVEL, "error" );

		// Remove the existing program data folder
		try {
			String prefix = ExecMode.TEST.getPrefix();
			ProductCard metadata = new ProductCard();
			File programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
			if( programDataFolder != null && programDataFolder.exists() ) FileUtils.forceDelete( programDataFolder );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}

		// For the parameters to be available using Java 9, the following needs to be added
		// to the test JVM command line parameters because com.sun.javafx.application.ParametersImpl
		// is not exposed, nor is there a "proper" way to access it:
		//
		// --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

		program = (Program)FxToolkit.setupApplication( Program.class, ProgramTest.getParameterValues() );
		program.addEventListener( programWatcher = new ProgramWatcher() );
		metadata = program.getCard();

		programWatcher.waitForEvent( ProgramStartedEvent.class );
		WaitForAsyncUtils.waitForFxEvents();

		// Now add the workpane watcher
		program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane().addWorkpaneListener( workpaneWatcher = new WorkpaneWatcher() );
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@After
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );
		FxToolkit.cleanupStages();

		programWatcher.waitForEvent( ProgramStoppedEvent.class );
		program.removeEventListener( programWatcher );
	}

	@Override
	public void start( Stage stage ) throws Exception {}

}
