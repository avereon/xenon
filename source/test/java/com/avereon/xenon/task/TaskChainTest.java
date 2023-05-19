package com.avereon.xenon.task;

import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class TaskChainTest extends ProgramTestCase {

	@Test
	void testInitWithSupplier() throws Exception {
		int value = 7;

		TaskChain<Integer> chain = TaskChain.of( () -> value );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( value );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testInitWithFunction() throws Exception {
		int value = 8;

		TaskChain<Integer> chain = TaskChain.of( ( v ) -> inc( value ) );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( value + 1 );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testInitWithTask() throws Exception {
		TaskChain<Integer> chain = TaskChain.of( new Task<>() {

			@Override
			public Integer call() {
				return 1;
			}

		} );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( 1 );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testLinkWithSupplier() throws Exception {
		TaskChain<Integer> chain = TaskChain.of( () -> 0 ).link( () -> 1 ).link( () -> 2 );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( 2 );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testLinkWithFunction() throws Exception {
		TaskChain<Integer> chain = TaskChain.of( () -> 0 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( 5 );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testLinkWithTask() throws Exception {
		TaskChain<Integer> chain = TaskChain.of( () -> 0 ).link( new Task<>() {

			@Override
			public Integer call() {
				return 3;
			}

		} );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( 3 );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	void testLinkWithDifferentTypes() throws Exception {
		TaskChain<Integer> chain = TaskChain.of( () -> "0" ).link( Integer::parseInt ).link( i -> i + 1 );
		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( 1 );
	}

	@Test
	void testEncapsulatedChain() throws Exception {
		TaskChain<Integer> chain = TaskChain.of( this::count ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 );
		assertThat( chain.build().getState() ).isEqualTo( Task.State.READY );

		Task<Integer> task = chain.run( getProgram() );
		assertThat( task.get() ).isEqualTo( 10 );
		assertThat( task.getState() ).isEqualTo( Task.State.SUCCESS );
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testExceptionCascade() throws Exception {
		RuntimeException expected = new RuntimeException();
		Task<Integer> task = TaskChain.of( () -> 0 ).link( this::inc ).link( this::inc ).link( ( i ) -> {
			if( i != 0 ) throw expected;
			return 0;
		} ).link( this::inc ).link( this::inc ).run( getProgram() );

		try {
			assertThat( task.get() ).isEqualTo( 5 );
			fail( "The get() method should throw an ExecutionException" );
		} catch( ExecutionException exception ) {
			assertThat( exception.getCause() ).isInstanceOf( TaskException.class );
			assertThat( exception.getCause().getCause() ).isEqualTo( expected );
			assertThat( exception.getCause().getCause().getCause() ).isNull();
		}
	}

	private Integer inc( Integer value ) {
		return value + 1;
	}

	private Integer count() throws ExecutionException, InterruptedException {
		return TaskChain.of( () -> 0 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).link( ( i ) -> i + 1 ).run( getProgram() ).get();
	}

}
