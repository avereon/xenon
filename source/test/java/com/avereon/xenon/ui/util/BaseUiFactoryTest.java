package com.avereon.xenon.ui.util;

import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.xenon.ActionLibrary;
import com.avereon.xenon.FxPlatformTestCase;
import com.avereon.xenon.IconLibrary;
import com.avereon.xenon.Xenon;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class BaseUiFactoryTest extends FxPlatformTestCase {

	private Xenon program;

	@BeforeEach
	void setup() {
		program = Mockito.mock( Xenon.class );
		when( program.getCard() ).thenReturn( new ProductCard().setArtifact( "mock" ).setName( "Mock" ) );
		Rb.init( program );

		ActionLibrary actionLibrary = new ActionLibrary( program );
		IconLibrary iconLibrary = new IconLibrary( program );

		when( program.getActionLibrary() ).thenReturn( actionLibrary );
		when( program.getIconLibrary() ).thenReturn( iconLibrary );
		when( program.getParent() ).thenReturn( null );
	}

	protected Xenon getProgram() {
		return program;
	}

}
