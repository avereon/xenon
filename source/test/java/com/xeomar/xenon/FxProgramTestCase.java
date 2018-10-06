package com.xeomar.xenon;

import com.xeomar.product.ProductCard;
import com.xeomar.util.FileUtil;
import com.xeomar.util.OperatingSystem;
import com.xeomar.util.SizeUnit;
import com.xeomar.xenon.event.ProgramStartedEvent;
import com.xeomar.xenon.event.ProgramStoppedEvent;
import com.xeomar.xenon.workarea.WorkpaneWatcher;
import javafx.application.Platform;
import javafx.stage.Stage;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FxProgramTestCase extends ApplicationTest {

	private final long max = Runtime.getRuntime().maxMemory();

	protected Program program;

	protected ProgramWatcher programWatcher;

	protected WorkpaneWatcher workpaneWatcher;

	protected ProductCard metadata;

	private long initialMemoryUse;

	private long finalMemoryUse;

	private long getMemoryUse() {
		WaitForAsyncUtils.waitForFxEvents();
		System.gc();
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	/**
	 * Overrides setup() in ApplicationTest and does not call super.setup().
	 */
	@Before
	public void setup() throws Exception {
		// Intentionally do not call super.setup()

		// Remove the existing program data folder
		try {
			String prefix = ExecMode.TEST.getPrefix();
			ProductCard metadata = new ProductCard().init( Program.class );
			Path programDataFolder = OperatingSystem.getUserProgramDataFolder( prefix + metadata.getArtifact(), prefix + metadata.getName() );
			if( Files.exists( programDataFolder ) ) FileUtil.delete( programDataFolder );
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
		programWatcher.waitForEvent( ProgramStartedEvent.class );
		metadata = program.getCard();

		program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane().addWorkpaneListener( workpaneWatcher = new WorkpaneWatcher() );

		initialMemoryUse = getMemoryUse();
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

		finalMemoryUse = getMemoryUse();

		assertSafeMemoryProfile();
	}

	@Override
	public void start( Stage stage ) {}

	protected void closeProgram() {
		closeProgram( false );
	}

	protected void closeProgram( boolean force ) {
		Platform.runLater( () -> program.requestExit( force ) );
		WaitForAsyncUtils.waitForFxEvents();
	}

	protected double getAllowedMemoryGrowthSize() {
		return 20;
	}

	protected double getAllowedMemoryGrowthPercent() {
		return 0.50;
	}

	private void assertSafeMemoryProfile() {
		long increaseSize = finalMemoryUse - initialMemoryUse;
		System.out.println( String.format( "Memory use: %s - %s = %s", FileUtil.getHumanBinSize( finalMemoryUse ), FileUtil.getHumanBinSize( initialMemoryUse ), FileUtil.getHumanBinSize( increaseSize ) ) );

		if( ((double)increaseSize / (double)SizeUnit.MB.getSize()) > getAllowedMemoryGrowthSize() ) {
			throw new AssertionFailedError( String.format( "Memory growth too large %s -> %s : %s", FileUtil.getHumanBinSize( initialMemoryUse ), FileUtil.getHumanBinSize( finalMemoryUse ), FileUtil.getHumanBinSize( increaseSize ) ) );
		}
		double increasePercent = ((double)finalMemoryUse / (double)initialMemoryUse) - 1.0;
		if( increasePercent > getAllowedMemoryGrowthPercent() ) {
			throw new AssertionFailedError( String.format( "Memory growth too large %s -> %s : %.2f%%", FileUtil.getHumanBinSize( initialMemoryUse ), FileUtil.getHumanBinSize( finalMemoryUse ), increasePercent * 100 ) );
		}
	}

}
