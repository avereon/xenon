package com.avereon.xenon.ui.util;

import com.avereon.product.Rb;
import com.avereon.xenon.ActionLibrary;
import com.avereon.xenon.FxPlatformTestCase;
import com.avereon.xenon.IconLibrary;
import com.avereon.xenon.Program;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class BaseUiFactoryTest extends FxPlatformTestCase {

	private Program program;

	@BeforeEach
	void setup() {
		program = Mockito.mock( Program.class );
		Rb.init( program );

		ActionLibrary actionLibrary = new ActionLibrary( program );
		IconLibrary iconLibrary = new IconLibrary( program );

		when( program.getActionLibrary() ).thenReturn( actionLibrary );
		when( program.getIconLibrary() ).thenReturn( iconLibrary );
		when( program.getParent() ).thenReturn( null );
	}

	protected Program getProgram() {
		return program;
	}

}
