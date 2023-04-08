package com.avereon.xenon.test;

import com.avereon.event.EventWatcher;
import com.avereon.product.ProductCard;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.Profile;
import com.avereon.xenon.Program;
import org.testfx.framework.junit5.ApplicationTest;

import java.nio.file.Path;

public class NewBaseProgramTestCase extends ApplicationTest {


	private Program program;

	private EventWatcher programWatcher;

	//@BeforeEach
	protected void setup() throws Exception {
		// Remove the existing program data folder
		String suffix = "-" + Profile.TEST;
		ProductCard metadata = ProductCard.info( Program.class );
		Path programDataFolder = OperatingSystem.getUserProgramDataFolder( metadata.getArtifact() + suffix, metadata.getName() + suffix );
		//assertThat( aggressiveDelete( programDataFolder ) ).withFailMessage( "Failed to delete program data folder" ).isTrue();
	}

	private boolean aggressiveDelete( Object object ) {
		return true;
	}

}
