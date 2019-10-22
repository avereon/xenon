package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.util.SizeUnitBase10;
import com.avereon.xenon.event.ProgramStartedEvent;
import com.avereon.xenon.event.ProgramStoppedEvent;
import com.avereon.xenon.workarea.WorkpaneWatcher;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.AssertionFailedError;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FxProgramUIT extends ApplicationTest {

	private final long max = Runtime.getRuntime().maxMemory();

	protected Program program;

	protected WorkpaneWatcher workpaneWatcher;

	protected ProductCard metadata;

	private ProgramWatcher programWatcher;

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
	@BeforeEach
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
	@AfterEach
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
		System.out.println( String.format(
			"Memory use: %s - %s = %s",
			FileUtil.getHumanSizeBase2( finalMemoryUse ),
			FileUtil.getHumanSizeBase2( initialMemoryUse ),
			FileUtil.getHumanSizeBase2( increaseSize )
		) );

		if( ((double)increaseSize / (double)SizeUnitBase10.MB.getSize()) > getAllowedMemoryGrowthSize() ) {
			throw new AssertionFailedError( String.format(
				"Memory growth too large %s -> %s : %s",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				FileUtil.getHumanSizeBase2( increaseSize )
			) );
		}
		double increasePercent = ((double)finalMemoryUse / (double)initialMemoryUse) - 1.0;
		if( increasePercent > getAllowedMemoryGrowthPercent() ) {
			throw new AssertionFailedError( String.format(
				"Memory growth too large %s -> %s : %.2f%%",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				increasePercent * 100
			) );
		}
	}

}
