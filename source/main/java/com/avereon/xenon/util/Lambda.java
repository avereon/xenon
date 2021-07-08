package com.avereon.xenon.util;

import lombok.CustomLog;

import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

@CustomLog
public class Lambda {

	public static TimerTask timerTask( Runnable runnable ) {
		return new TimerTask() {

			@Override
			public void run() {
				runnable.run();
			}

		};
	}

	public static <T> Consumer<T> wrapper( Consumer<T> consumer ) {

		return parameter -> {
			try {
				consumer.accept( parameter );
			} catch( Throwable throwable ) {
				log.atError().withCause(throwable).log( "Exception occurred in lambda" );
			}
		};

	}

	public static <T, R> Function<T, R> functionWrapper( Function<T, R> function ) {

		return parameter -> {
			try {
				return function.apply( parameter );
			} catch( Throwable throwable ) {
				log.atError().withCause( throwable ).log( "Exception occurred in lambda" );
			}
			return null;
		};

	}

	public static <T, R, E extends Exception> Consumer<T> handlingFunctionWrapper(
			ThrowingFunction<T, R, E> throwingFunction, Class<E> exceptionClass
	) {

		return i -> {
			try {
				throwingFunction.apply( i );
			} catch( Exception ex ) {
				try {
					E exCast = exceptionClass.cast( ex );
					System.err.println( "Exception occured : " + exCast.getMessage() );
				} catch( ClassCastException ccEx ) {
					throw new RuntimeException( ex );
				}
			}
		};
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R, E extends Exception> {

		R apply( T t ) throws E;

	}

}
