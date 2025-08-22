package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.log.Log;
import com.avereon.util.*;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.event.FxEventWatcher;
import com.avereon.zerra.javafx.Fx;
import javafx.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.AssertionFailedError;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class is a duplicate of com.avereon.xenos.BaseFullXenonTestCase which is
 * intended to be visible for mod testing but is not available to Xenon to
 * avoid a circular dependency. Attempts at making this class publicly available
 * have run in to various challenges. The most recent challenge is with Surefire
 * not putting JUnit 5 on the module path at test time if it is also on the
 * module path at compile time.
 */
@ExtendWith( ApplicationExtension.class )
public abstract class BaseFullXenonTestCase extends BaseXenonTestCase {

	private static final long minInitialMemory = 8 * SizeUnitBase2.MiB.getSize();

	protected FxRobot robot = new FxRobot();

	private EventWatcher programWatcher;

	private FxEventWatcher programFxWatcher;

	private FxEventWatcher workpaneWatcher;

	private long initialMemoryUse;

	private long finalMemoryUse;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// For the parameters to be available using Java 9 or later, the following
		// needs to be added to the test JVM command line parameters because
		// com.sun.javafx.application.ParametersImpl is not exposed, nor is there
		// a "proper" way to access it:
		//
		// --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
		// This is fixed in TestFX 4.0.17+

		Xenon xenon = setProgram( new Xenon() );
		xenon.setProgramParameters( Parameters.parse( ProgramTestConfig.getParameters() ) );
		xenon.register( ProgramEvent.ANY, programWatcher = new EventWatcher( LONG_TIMEOUT ) );
		xenon.getFxEventHub().register( Event.ANY, programFxWatcher = new FxEventWatcher( LONG_TIMEOUT ) );

		// Start the application
		// All application setup needs to be done before this point
		long start = System.currentTimeMillis();
		FxToolkit.setupApplication( () -> xenon );
		programWatcher.waitForEvent( ProgramEvent.STARTED, LONG_TIMEOUT );
		long end = System.currentTimeMillis();
		System.out.println( "Program start duration=" + (end - start) );

		// Get initial memory use after the program is started
		initialMemoryUse = getMemoryUse();
		long initialMemoryUseTimeLimit = System.currentTimeMillis() + TIMEOUT;
		while( initialMemoryUse < minInitialMemory && System.currentTimeMillis() < initialMemoryUseTimeLimit ) {
			initialMemoryUse = getMemoryUse();
		}

		// Check that the program is started and has a workspace
		assertThat( getProgram() ).withFailMessage( "Program is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager() ).withFailMessage( "Workspace manager is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager().getActiveWorkspace() ).withFailMessage( "Active workspace is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() ).withFailMessage( "Active workarea is null" ).isNotNull();

		// Add a workpane event watcher to the active workarea
		Workpane workpane = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher = new FxEventWatcher() );
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@AfterEach
	protected void teardown() throws Exception {
		Xenon program = getProgram();
		if( program != null ) {
			FxToolkit.cleanupAfterTest( robot, program );
			programWatcher.waitForEvent( ProgramEvent.STOPPED );
			program.unregister( ProgramEvent.ANY, programWatcher );
		}

		Log.reset();

		finalMemoryUse = getMemoryUse();
		assertSafeMemoryProfile();

		// Clear the program reference
		setProgram( null );

		super.teardown();
	}

	protected void closeProgram() throws Exception {
		closeProgram( false );
	}

	protected void closeProgram( boolean skipUserChecks ) throws Exception {
		Fx.run( () -> getProgram().requestExit( skipUserChecks ) );
		Fx.waitForWithExceptions( 5, TimeUnit.SECONDS );
	}

	protected EventWatcher getProgramEventWatcher() {
		return programWatcher;
	}

	protected FxEventWatcher getProgramFxEventWatcher() {
		return programFxWatcher;
	}

	protected Workspace getWorkspace() {
		return getProgram().getWorkspaceManager().getActiveWorkspace();
	}

	protected Workpane getWorkarea() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();
	}

	protected FxEventWatcher getWorkpaneEventWatcher() {
		return workpaneWatcher;
	}

	protected double getAllowedMemoryGrowthSize() {
		return 20;
	}

	protected double getAllowedMemoryGrowthPercent() {
		return 0.50;
	}

	private long getMemoryUse() {
		System.gc();

		// Wait for the FX events to finish
		WaitForAsyncUtils.waitForFxEvents();

		// Pause a moment to let the memory use settle down
		ThreadUtil.pause( 50 );

		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	private void assertSafeMemoryProfile() {
		long increaseSize = finalMemoryUse - initialMemoryUse;
		double increaseAbsolute = ((double)increaseSize / (double)SizeUnitBase10.MB.getSize());
		double increasePercent = ((double)finalMemoryUse / (double)initialMemoryUse) - 1.0;

		if( initialMemoryUse > SizeUnitBase2.MiB.getSize() && increaseAbsolute > getAllowedMemoryGrowthSize() ) {
			throw new AssertionFailedError( String.format(
				"Absolute memory growth too large %s -> %s : %s",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				FileUtil.getHumanSizeBase2( increaseSize )
			) );
		}

		if( initialMemoryUse > SizeUnitBase2.MiB.getSize() && increasePercent > getAllowedMemoryGrowthPercent() ) {
			throw new AssertionFailedError( String.format(
				"Relative memory growth too large %s -> %s : %.2f%%",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				increasePercent * 100
			) );
		}
	}

}
