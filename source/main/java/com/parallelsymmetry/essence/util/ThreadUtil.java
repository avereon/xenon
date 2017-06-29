package com.parallelsymmetry.essence.util;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

	/**
	 * Pause a thread for a specific amount of time. If an InterruptedException
	 * occurs the method returns immediately.
	 *
	 * @param duration
	 */
	public static final void pause( long duration ) {
		pause( duration, TimeUnit.MILLISECONDS );
	}

	/**
	 * Pause a thread for a specific amount of time with the given unit. If an
	 * InterruptedException occurs the method returns immediately.
	 *
	 * @param duration
	 */
	public static final void pause( long duration, TimeUnit unit ) {
		try {
			unit.sleep( unit.convert( duration, TimeUnit.MILLISECONDS ) );
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception.
		}
	}

	public static final ThreadFactory createDaemonThreadFactory() {
		return new DaemonThreadFactory();
	}

	/**
	 * Check if the calling method was called from a different method. Class names
	 * are compared both by the simple name and by the full name. For example,
	 * both of the following calls will return true:
	 * <p>
	 * <blockquote> <code>ThreadUtil.calledFrom( "Thread", "run" );</code><br/>
	 * <code>ThreadUtil.calledFrom( "java.lang.Thread", "run" );</code>
	 * </blockquote>
	 *
	 * @param className
	 * @param methodName
	 * @return
	 */
	public static final boolean calledFrom( String className, String methodName ) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();

		for( StackTraceElement element : trace ) {
			if( element.getClassName().equals( className ) && element.getMethodName().equals( methodName ) ) return true;
			if( JavaUtil.getClassName( element.getClassName() ).equals( JavaUtil.getClassName( className ) ) && element.getMethodName().equals( methodName ) ) return true;
		}

		return false;
	}

	/**
	 * Append the stack trace of the source throwable to the target throwable.
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	public static final Throwable appendStackTrace( Throwable source, Throwable target ) {
		if( source == null ) return target;
		if( target == null ) return source;
		return appendStackTrace( source.getStackTrace(), target );
	}

	/**
	 * Append stack trace to the target throwable.
	 *
	 * @param target
	 * @param trace
	 * @return
	 */
	public static final Throwable appendStackTrace( StackTraceElement[] trace, Throwable target ) {
		if( target == null ) return null;
		if( trace != null ) {
			StackTraceElement[] originalStack = target.getStackTrace();
			StackTraceElement[] elements = new StackTraceElement[ originalStack.length + trace.length ];
			System.arraycopy( originalStack, 0, elements, 0, originalStack.length );
			System.arraycopy( trace, 0, elements, originalStack.length, trace.length );
			target.setStackTrace( elements );
		}
		return target;
	}

	/**
	 * <p>
	 * Returns the current execution stack as an array of classes. This is useful
	 * to determine the calling class.
	 * <p>
	 * The length of the array is the number of methods on the execution stack
	 * before this method is called. The element at index 0 is the calling class
	 * of this method, the element at index 1 is the calling class of the method
	 * in the previous class, and so on.
	 *
	 * @return A class array of the execution stack before this method was called.
	 */
	public static final Class<?>[] getStackClasses() {
		Class<?>[] frame = new StackClassResolver().getClassContext();
		return Arrays.copyOfRange( frame, 2, frame.length );
	}

	private static final class StackClassResolver extends SecurityManager {

		@Override
		public Class<?>[] getClassContext() {
			return super.getClassContext();
		}

	}

	private static final class DaemonThreadFactory implements ThreadFactory {

		public Thread newThread( Runnable runnable ) {
			Thread thread = Executors.defaultThreadFactory().newThread( runnable );
			thread.setDaemon( true );
			return thread;
		}

	}

}
