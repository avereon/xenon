package com.avereon.xenon.test.tool.guide;

import com.avereon.xenon.tool.guide.GuideContext;
import org.junit.jupiter.api.BeforeEach;

public class GuideContextTest {

	private GuideContext context;

	@BeforeEach
	void setup() {
		context = new GuideContext( null );
	}

//	@Test
//	void testSetSelectedItems() throws Exception {
//		Fx.run( () -> context.setSelectedIds( Set.of( "general" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( context.getSelectedIds(), CoreMatchers.hasItems( "general" ) );
//
//		Fx.run( () -> context.setSelectedIds( Set.of( "workspace", "tools" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( context.getSelectedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
//	}
//
//	@Test
//	void testSetExpandedItems() throws Exception {
//		Fx.run( () -> context.setExpandedIds( Set.of( "general" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( context.getExpandedIds(), CoreMatchers.hasItems( "general" ) );
//
//		Fx.run( () -> context.setExpandedIds( Set.of( "workspace", "tools" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( context.getExpandedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
//	}


}