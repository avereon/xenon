package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.ProgramStartedEvent;
import com.parallelsymmetry.essence.testutil.FxTestCase;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramWorkspaceTest extends FxTestCase {

//	@Test
//	public void testA() {
//		Assert.assertEquals( "Essence", program.getMetadata().getName() );
//	}
//
//	@Test
//	public void testB() {
//		Assert.assertEquals( "Essence", program.getMetadata().getName() );
//	}

	@Test
	public void testWorkspaceWindowTitle() throws Exception {
		waitForEvent( ProgramStartedEvent.class );
		String workareaName = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();
		assertThat( program.getWorkspaceManager().getActiveWorkspace().getStage().getTitle(), is( workareaName + " - " + metadata.getName() ) );
		System.out.println( "All good in the window title test" );
	}


}
