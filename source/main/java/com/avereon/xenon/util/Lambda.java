package com.avereon.xenon.util;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

public class Lambda {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

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
				log.error( "Exception occurred in lambda", throwable );
			}
		};

	}

	public static <T, R> Function<T, R> functionWrapper( Function<T, R> function ) {

		return parameter -> {
			try {
				return function.apply( parameter );
			} catch( Throwable throwable ) {
				log.error( "Exception occurred in lambda", throwable );
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
