package com.avereon.xenon.test;

import com.avereon.xenon.IconLibrary;
import com.avereon.zarra.image.BrokenIcon;
import com.avereon.zenna.icon.PowerIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IconLibraryTest extends ProgramTestCase {

	private IconLibrary library;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		library = new IconLibrary( getProgram() );
	}

	@Test
	void testGetBrokenIcon() {
		assertThat( library.getIcon( (String)null ) ).isInstanceOf( BrokenIcon.class );
		assertThat( library.getIcon( "" ) ).isInstanceOf( BrokenIcon.class );
		assertThat( library.getIcon( "~a non existent key~" ) ).isInstanceOf( BrokenIcon.class );
	}

	@Test
	void testGetIcon() {
		assertThat( library.getIcon( "exit" ) ).isInstanceOf( PowerIcon.class );
	}

}
