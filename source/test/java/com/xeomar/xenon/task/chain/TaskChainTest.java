package com.xeomar.xenon.task.chain;

import com.xeomar.xenon.ProgramTestCase;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskChainTest extends ProgramTestCase {

	@Test
	public void testInitWithSupplier() throws Exception {
		int value = 7;

		Task<Integer> task = TaskChain.init( () -> value ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( value ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testInitWithFunction() throws Exception {
		int value = 8;

		Task<Integer> task = TaskChain.init( ( v ) -> inc( value ) ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( value + 1 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testInitWithTask() throws Exception {
		Task<Integer> task = TaskChain.init( new Task<Integer>() {

			@Override
			public Integer call() {
				return 1;
			}

		} ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( 1 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testLinkWithSupplier() throws Exception {
		Task<Integer> task = TaskChain.init( () -> 0 ).link( () -> 1 ).link( () -> 2 ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( 2 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testLinkWithFunction() throws Exception {
		Task<Integer> task = TaskChain
			.init( () -> 0 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( 5 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testLinkWithTask() throws Exception {
		Task<Integer> task = TaskChain.init( () -> 0 ).link( new Task<Integer>() {

			@Override
			public Integer call() {
				return 3;
			}

		} ).run( program );

		// This is technically a race condition here
		assertThat( task.getState(), is( Task.State.WAITING ) );
		assertThat( task.get(), is( 3 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	public void testExceptionCascade() throws Exception {
		RuntimeException expected = new RuntimeException();
		Task<Integer> task = TaskChain.init( () -> 0 ).link( this::inc ).link( this::inc ).link( ( i ) -> {
			if( i != 0 ) throw expected;
			return 0;
		} ).link( this::inc ).link( this::inc ).run( program );

		try {
			assertThat( task.get(), is( 5 ) );
			Assert.fail( "The get() method should throw an ExecutionException" );
		} catch( ExecutionException exception ) {
			Throwable cause1 = exception.getCause();
			assertThat( cause1, instanceOf( TaskException.class ) );
			Throwable cause2 = cause1.getCause();
			assertThat( cause2, instanceOf( RuntimeException.class ) );
			assertThat( cause2, is( expected ) );
		}
	}

	private Integer inc( Integer value ) {
		return value + 1;
	}

}
