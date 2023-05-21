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
		// Run in headless mode
		//System.setProperty( "java.awt.headless", "true" );
		System.setProperty( "glass.platform", "Monocle" );
		System.setProperty( "monocle.platform", "Headless" );
		//System.setProperty( "prism.order", "sw" );
		System.setProperty( "testfx.headless", "true" );
		System.setProperty( "testfx.robot", "glass" );

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
