package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.product.ProgramMode;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.util.ThreadUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;

/**
 * The super class for {@link BasePartXenonTestCase} and {@link BaseFullXenonTestCase}
 * classes. This class should not be subclassed directly by tests, but should
 * use one of the previous classes.
 */
public abstract class BaseXenonTestCase extends BaseForAllTests {

	private Xenon program;

	static {
		runHeadless();
	}

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		if( OperatingSystem.isWindows() ) {
			System.setProperty( "jpackage.app-path", "C:\\Program Files\\Xenon\\Xenon.exe" );
		} else {
			System.setProperty( "jpackage.app-path", "/opt/xenon/bin/Xenon" );
		}

		// Remove the existing program data folder
		String suffix = "-" + ProgramMode.TEST;
		ProductCard metadata = ProductCard.info( Xenon.class );
		Path programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact() + suffix, metadata.getName() + suffix );

		// Try to clean up the program data folder, but don't fail if we can't
		try {
			FileUtil.delete( programDataFolder );
		} catch( IOException exception ) {
			// Ignore
		}
	}

	@AfterEach
	protected void teardown() throws Exception {
		// Clean up the settings
		// This fixes the problem where unexpected workspaces were being restored
		// and there was not an active workarea.
		if( program != null ) program.getSettingsManager().getSettings( ProgramSettings.BASE ).delete();
	}

	protected Xenon getProgram() {
		return program;
	}

	protected Xenon setProgram( Xenon program ) {
		this.program = program;
		return program;
	}

	private boolean aggressiveDelete( Path path ) throws IOException {
		// NOTE It has been determined that the StoredSettings can cause problems.
		//  The StoredSettings class can put these files back due to the delayed
		//  persist nature of StoredSettings. Be sure to also delete settings in
		//  teardown methods to reduce test cross-contamination.

		long limit = System.currentTimeMillis() + LONG_TIMEOUT;
		IOException exception = null;
		while( Files.exists( path ) && System.currentTimeMillis() < limit ) {
			try {
				FileUtil.delete( path );
			} catch( IOException deleteException ) {
				exception = deleteException;
				ThreadUtil.pause( 10 );
			}
		}

		// Check for a timeout
		if( System.currentTimeMillis() >= limit && exception != null ) throw exception;

		return !Files.exists( path );
	}

	private static void runHeadless() {
		// Set java.awt.headless to true when running tests in headless mode
		// This is not needed if using Monocle, but just to be safe
		//System.setProperty( "java.awt.headless", "true" );

		// Use Monocle to run UI tests
		// <!-- https://wiki.openjdk.java.net/display/OpenJFX/Monocle -->
		System.setProperty( "glass.platform", "Monocle" );

		// When running the desktop build of JavaFX Monocle,
		// then the only Monocle platform option is Headless,
		// but it does have to be set.
		System.setProperty( "monocle.platform", "Headless" );

		// Set prism.order to sw when running tests in headless mode
		//System.setProperty( "prism.order", "sw" );

		// Not sure what this setting does, but it's in all the examples found
		//System.setProperty( "prism.text", "t2k" );

		// Set testfx.setup.timeout to a reasonable time
		// 5000 - GitHub Actions, Mintbox Mini
		//  | Slower computers
		//  |
		//  | Faster computers
		// 1000 - AMD Threadripper, Intel i9</pre>
		System.setProperty( "testfx.setup.timeout", "5000" );

		// When using Monocle, TestFX should also run in headless mode
		System.setProperty( "testfx.headless", "true" );

		// When using Monocle, use the Glass robot
		System.setProperty( "testfx.robot", "glass" );
	}

}
