package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.util.SizeUnitBase10;
import com.avereon.xenon.workpane.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

	protected ProductCard metadata;

	private ProgramWatcher programWatcher;

	protected Workpane workpane;

	protected WorkpaneWatcher workpaneWatcher;

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
			String suffix = "-" + Profile.TEST;
			ProductCard metadata = new ProductCard().init( Program.class );
			Path programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact() + suffix, metadata.getName() + suffix );
			if( Files.exists( programDataFolder ) ) FileUtil.delete( programDataFolder );
			if( Files.exists( programDataFolder ) ) Assertions.fail( "Program data folder still exists" );
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}

		// For the parameters to be available using Java 9, the following needs to be added
		// to the test JVM command line parameters because com.sun.javafx.application.ParametersImpl
		// is not exposed, nor is there a "proper" way to access it:
		//
		// --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

		program = (Program)FxToolkit.setupApplication( Program.class, ProgramTestConfig.getParameterValues() );
		program.register( ProgramEvent.ANY, programWatcher = new ProgramWatcher() );
		programWatcher.waitForEvent( ProgramEvent.STARTED );
		metadata = program.getCard();

		workpane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher = new WorkpaneWatcher() );

		initialMemoryUse = getMemoryUse();
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@AfterEach
	public void cleanup() throws Exception {
		FxToolkit.cleanupApplication( program );
		FxToolkit.cleanupStages();

		programWatcher.waitForEvent( ProgramEvent.STOPPED );
		program.unregister( ProgramEvent.ANY, programWatcher );

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
