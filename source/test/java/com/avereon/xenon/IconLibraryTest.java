package com.avereon.xenon;

import com.avereon.rossa.icon.PowerIcon;
import com.avereon.zerra.image.BrokenIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class IconLibraryTest extends ProgramTestCase {

	private IconLibrary library;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		library = new IconLibrary( getProgram() );
	}

	@Test
	void testGetBrokenIcon() {
		assertThat( library.getIcon( (String)null ), is( instanceOf( BrokenIcon.class ) ) );
		assertThat( library.getIcon( "" ), is( instanceOf( BrokenIcon.class ) ) );
		assertThat( library.getIcon( "~a non existent key~" ), is( instanceOf( BrokenIcon.class ) ) );
	}

	@Test
	void testGetIcon() {
		assertThat( library.getIcon( "exit" ), is( instanceOf( PowerIcon.class ) ) );
	}

}
