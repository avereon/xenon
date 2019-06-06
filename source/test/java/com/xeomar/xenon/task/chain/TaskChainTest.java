package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.ProgramTestCase;
import com.xeomar.xenon.task.Task;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskChainTest extends ProgramTestCase {

	@Test
	public void testInit() {
		TaskChain.init( this::zero );
	}

	@Test
	public void testLink() {
		TaskChain.init( this::zero ).link( this::inc ).link( this::inc );
	}

	@Test
	public void testRunAfterInit() throws Exception {
		Task<Integer> task = TaskChain.init( () -> 7 ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( 7 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

		@Test
	public void testRun() throws Exception {
		Task<Integer> task = TaskChain
			.init( this::zero )
			.link( this::inc )
			.link( this::inc )
			.link( this::inc )
			.link( this::inc )
			.run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( 4 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	private int zero() {
		return 0;
	}

	private Integer inc( Integer value ) {
		return ++value;
	}

}
