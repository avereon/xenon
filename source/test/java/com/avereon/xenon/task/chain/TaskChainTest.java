package com.avereon.xenon.task.chain;

import com.avereon.xenon.ProgramTestCase;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

class TaskChainTest extends ProgramTestCase {

	@Test
	void testInitWithSupplier() throws Exception {
		int value = 7;

		TaskChain<Integer> chain = TaskChain.init( () -> value );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( value ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testInitWithFunction() throws Exception {
		int value = 8;

		TaskChain<Integer> chain = TaskChain.init( ( v ) -> inc( value ) );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( value + 1 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testInitWithTask() throws Exception {
		TaskChain<Integer> chain = TaskChain.init( new Task<>() {

			@Override
			public Integer call() {
				return 1;
			}

		} );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( 1 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testLinkWithSupplier() throws Exception {
		TaskChain<Integer> chain = TaskChain.init( () -> 0 ).link( () -> 1 ).link( () -> 2 );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( 2 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testLinkWithFunction() throws Exception {
		TaskChain<Integer> chain = TaskChain
			.init( () -> 0 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( 5 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testLinkWithTask() throws Exception {
		TaskChain<Integer> chain = TaskChain.init( () -> 0 ).link( new Task<>() {

			@Override
			public Integer call() {
				return 3;
			}

		} );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( 3 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testEncapsulatedChain() throws Exception {
		TaskChain<Integer> chain = TaskChain
			.init( this::count )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 );
		assertThat( chain.build().getState(), is( Task.State.READY ) );

		Task<Integer> task = chain.run( program );
		assertThat( task.get(), is( 10 ) );
		assertThat( task.getState(), is( Task.State.SUCCESS ) );
	}

	@Test
	void testExceptionCascade() throws Exception {
		RuntimeException expected = new RuntimeException();
		Task<Integer> task = TaskChain.init( () -> 0 ).link( this::inc ).link( this::inc ).link( ( i ) -> {
			if( i != 0 ) throw expected;
			return 0;
		} ).link( this::inc ).link( this::inc ).run( program );

		try {
			assertThat( task.get(), is( 5 ) );
			fail( "The get() method should throw an ExecutionException" );
		} catch( ExecutionException exception ) {
			assertThat( exception.getCause(), instanceOf( TaskException.class ) );
			assertThat( exception.getCause().getCause(), is( expected ) );
			assertThat( exception.getCause().getCause().getCause(), is( nullValue() ) );
		}
	}

	private Integer inc( Integer value ) {
		return value + 1;
	}

	private Integer count() throws ExecutionException, InterruptedException {
		return TaskChain
			.init( () -> 0 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.link( ( i ) -> i + 1 )
			.run( program )
			.get();
	}

}
