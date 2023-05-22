package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.product.Profile;
import com.avereon.product.Program;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.util.ThreadUtil;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

@Data
public class CommonProgramTestBase {

	private Program program;

	@BeforeEach
	protected void setup() throws Exception {
		runHeadless();

		// Be sure that the OperatingSystem class is properly set
		OperatingSystem.reset();

		// Turn off logging reduce output during tests
		java.util.logging.Logger.getLogger( "" ).setLevel( Level.OFF );

		// Remove the existing program data folder
		String suffix = "-" + Profile.TEST;
		ProductCard metadata = ProductCard.info( Xenon.class );
		Path programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact() + suffix, metadata.getName() + suffix );
		assertThat( aggressiveDelete( programDataFolder ) ).withFailMessage( "Failed to delete program data folder" ).isTrue();
	}

	private void runHeadless() {
		// Set java.awt.headless to true when running tests in headless mode
		// This is not needed if using Monocle, but just to be safe
		System.setProperty( "java.awt.headless", "true" );

		// Use Monocle to run UI tests
		// <!-- https://wiki.openjdk.java.net/display/OpenJFX/Monocle -->
		System.setProperty( "glass.platform", "Monocle" );

		// When running the desktop build of JavaFX Monocle,
		// then the only Monocle platform option is Headless
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

	private boolean aggressiveDelete( Path path ) throws IOException {
		long limit = System.currentTimeMillis() + TIMEOUT;
		while( Files.exists( path ) && System.currentTimeMillis() < limit ) {
			try {
				FileUtil.delete( path );
			} catch( IOException exception ) {
				ThreadUtil.pause( 100 );
			}
		}
		return FileUtil.delete( path );
	}

}
