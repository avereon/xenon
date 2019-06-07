package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.ProgramTestCase;
import com.xeomar.xenon.task.Task;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskChainTest extends ProgramTestCase {

	@Test
	public void testInit() {
		TaskChain.init( () -> 0 );
	}

	@Test
	public void testLink() {
		TaskChain.init( () -> 0 ).link( this::inc ).link( this::inc );
	}

	@Test
	public void testRunAfterInit() throws Exception {
		int value = 7;

		Task<Integer> task = TaskChain.init( () -> value ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( value ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

		@Test
	public void testRun() throws Exception {
		Assert.fail("Not working" );
//		Task<Integer> task = TaskChain
//			.init( () -> 0 )
//			.link( this::inc )
//			.link( this::inc )
//			.link( this::inc )
//			.link( this::inc )
//			.link( this::inc )
//			.run( program );
//
//		// This is technically a race condition here
//		assertThat( task.getState(), is( Task.State.WAITING ) );
//		assertThat( task.get(), is( 5 ) );
//		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	private Integer inc( Integer value ) {
		return value + 1;
	}

}
