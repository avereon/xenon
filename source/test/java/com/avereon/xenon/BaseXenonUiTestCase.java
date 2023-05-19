package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.log.Log;
import com.avereon.util.FileUtil;
import com.avereon.util.SizeUnitBase10;
import com.avereon.util.SizeUnitBase2;
import com.avereon.util.ThreadUtil;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import com.avereon.zarra.event.FxEventWatcher;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.AssertionFailedError;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class is a duplicate of com.avereon.zenna.BaseXenonTestCase which is
 * intended to be visible for mod testing but is not available to Xenon to
 * avoid a circular dependency. Attempts at making this
 * class publicly available have run in to various challenges with the most
 * recent being with Surefire not putting JUnit 5 on the module path.
 */
@ExtendWith( ApplicationExtension.class )
public abstract class BaseXenonUiTestCase extends CommonProgramTestBase {

	private EventWatcher programWatcher;

	private FxEventWatcher workpaneWatcher;

	private long initialMemoryUse;

	private long finalMemoryUse;

	/**
	 * Overrides setup() in ApplicationTest and does not call super.setup().
	 */
	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// For the parameters to be available using Java 9 or later, the following
		// needs to be added to the test JVM command line parameters because
		// com.sun.javafx.application.ParametersImpl is not exposed, nor is there
		// a "proper" way to access it:
		//
		// --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

		// NOTE This starts the application so all setup needs to be done by this point
		setProgram( (Xenon)FxToolkit.setupApplication( Xenon.class, ProgramTestConfig.getParameterValues() ) );

		getProgram().register( ProgramEvent.ANY, programWatcher = new EventWatcher( TIMEOUT ) );
		Fx.waitForWithExceptions( TIMEOUT );
		// NOTE Thread.yield() is helpful but not consistent
		Thread.yield();
		programWatcher.waitForEvent( ProgramEvent.STARTED, TIMEOUT );
		Fx.waitForWithExceptions( TIMEOUT );
		// NOTE Thread.yield() is helpful but not consistent
		Thread.yield();

		// Wait for the active workarea
		// FIXME This should use an event listener to wait for the workarea
		long limit = System.currentTimeMillis() + TIMEOUT;
		while( getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() == null && System.currentTimeMillis() < limit ) {
			ThreadUtil.pause( 100 );
		}

		assertThat( getProgram() ).withFailMessage( "Program is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager() ).withFailMessage( "Workspace manager is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager().getActiveWorkspace() ).withFailMessage( "Active workspace is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() ).withFailMessage( "Active workarea is null" ).isNotNull();
		assertThat( getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane() ).withFailMessage( "Active workpane is null" ).isNotNull();

		Workpane workpane = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher = new FxEventWatcher() );

		initialMemoryUse = getMemoryUse();
	}

	/**
	 * Override cleanup in FxPlatformTestCase and does not call super.cleanup().
	 */
	@AfterEach
	protected void teardown() throws Exception {
		FxToolkit.cleanupApplication( getProgram() );
		FxToolkit.cleanupStages();

		programWatcher.waitForEvent( ProgramEvent.STOPPED );
		getProgram().unregister( ProgramEvent.ANY, programWatcher );
		Log.reset();

		// Pause to let things wind down
		//ThreadUtil.pause( TIMEOUT );

		finalMemoryUse = getMemoryUse();
		assertSafeMemoryProfile();
	}

	protected void closeProgram() throws Exception {
		closeProgram( false );
	}

	protected void closeProgram( boolean force ) throws Exception {
		Fx.run( () -> getProgram().requestExit( force ) );
		Fx.waitForWithExceptions( 5, TimeUnit.SECONDS );
	}

	public Xenon getProgram() {
		return (Xenon)super.getProgram();
	}

	protected EventWatcher getProgramEventWatcher() {
		return programWatcher;
	}

	protected Workpane getWorkpane() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
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
		WaitForAsyncUtils.waitForFxEvents();
		System.gc();
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
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
		if( initialMemoryUse > SizeUnitBase2.MiB.getSize() && increasePercent > getAllowedMemoryGrowthPercent() ) {
			throw new AssertionFailedError( String.format( "Memory growth too large %s -> %s : %.2f%%",
				FileUtil.getHumanSizeBase2( initialMemoryUse ),
				FileUtil.getHumanSizeBase2( finalMemoryUse ),
				increasePercent * 100
			) );
		}
	}

}
