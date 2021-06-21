package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.product.ProductCard;
import com.avereon.util.*;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import com.avereon.zerra.event.FxEventWatcher;
import com.avereon.zerra.javafx.Fx;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.AssertionFailedError;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class FxProgramUIT extends ApplicationTest {

	protected static final int TIMEOUT = 5000;

	protected Program program;

	private EventWatcher programWatcher;

	protected Workpane workpane;

	protected FxEventWatcher workpaneWatcher;

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
		String suffix = "-" + Profile.TEST;
		ProductCard metadata = ProductCard.info( Program.class );
		Path programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact() + suffix, metadata.getName() + suffix );
		if( Files.exists( programDataFolder ) ) assertTrue( FileUtil.delete( programDataFolder ), "Failed to delete program data folder" );
		if( Files.exists( programDataFolder ) ) Assertions.fail( "Program data folder still exists" );

		// For the parameters to be available using Java 9, the following needs to be added
		// to the test JVM command line parameters because com.sun.javafx.application.ParametersImpl
		// is not exposed, nor is there a "proper" way to access it:
		//
		// --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

		program = (Program)FxToolkit.setupApplication( Program.class, ProgramTestConfig.getParameterValues() );
		program.register( ProgramEvent.ANY, programWatcher = new EventWatcher() );
		programWatcher.waitForEvent( ProgramEvent.STARTED );
		Fx.waitForWithExceptions( TIMEOUT );

		// Wait for the active workarea to not be null
		long limit = System.currentTimeMillis() + TIMEOUT;
		while( program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() == null && System.currentTimeMillis() < limit ) {
			ThreadUtil.pause( 100 );
		}

		assertNotNull( program, "Program is null" );
		assertNotNull( program.getWorkspaceManager(), "Workspace manager is null" );
		assertNotNull( program.getWorkspaceManager().getActiveWorkspace(), "Active workspace is null" );
		assertNotNull( program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea(), "Active workarea is null" );
		assertNotNull( program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane(), "Active workpane is null" );

		workpane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher = new FxEventWatcher() );

		initialMemoryUse = getMemoryUse();
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@AfterEach
	public void teardown() throws Exception {
		FxToolkit.cleanupApplication( program );
		FxToolkit.cleanupStages();

		programWatcher.waitForEvent( ProgramEvent.STOPPED );
		program.unregister( ProgramEvent.ANY, programWatcher );
		Log.reset();

		// Pause to let things wind down
		ThreadUtil.pause( TIMEOUT );

		finalMemoryUse = getMemoryUse();
		assertSafeMemoryProfile();
	}

	@Override
	public void start( Stage stage ) {}

	protected void closeProgram() throws Exception {
		closeProgram( false );
	}

	protected void closeProgram( boolean force ) throws Exception {
		Fx.run( () -> program.requestExit( force ) );
		Fx.waitForWithExceptions( 5, TimeUnit.SECONDS );
	}

	protected double getAllowedMemoryGrowthSize() {
		return 20;
	}

	protected double getAllowedMemoryGrowthPercent() {
		return 0.50;
	}

	private void assertSafeMemoryProfile() {
		long increaseSize = finalMemoryUse - initialMemoryUse;
		System.out.printf( "Memory use: %s - %s = %s%n", FileUtil.getHumanSizeBase2( finalMemoryUse ), FileUtil.getHumanSizeBase2( initialMemoryUse ), FileUtil.getHumanSizeBase2( increaseSize ) );

		if( ((double)increaseSize / (double)SizeUnitBase10.MB.getSize()) > getAllowedMemoryGrowthSize() ) {
			throw new AssertionFailedError( String.format( "Memory growth too large %s -> %s : %s",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				FileUtil.getHumanSizeBase2( increaseSize )
			) );
		}
		double increasePercent = ((double)finalMemoryUse / (double)initialMemoryUse) - 1.0;
		if( increasePercent > getAllowedMemoryGrowthPercent() ) {
			throw new AssertionFailedError( String.format( "Memory growth too large %s -> %s : %.2f%%",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				increasePercent * 100
			) );
		}
	}

}
