package com.xeomar.xenon.util;

import junit.framework.TestCase;

public class ThreadUtilTest extends TestCase {

	public void testPause() {
		long length = 100;
		long start = System.nanoTime();
		ThreadUtil.pause( length );
		long stop = System.nanoTime();
		long delta = stop - start;
		assertTrue( "Delta: " + delta, delta >= length * 1000 );
	}

	public void testCalledFrom() {
		assertFalse( ThreadUtil.calledFrom( "ThreadUtilTest", "notHere" ) );

		assertTrue( ThreadUtil.calledFrom( "ThreadUtilTest", "testCalledFrom" ) );
		assertTrue( ThreadUtil.calledFrom( "com.parallelsymmetry.utility.ThreadUtilTest", "testCalledFrom" ) );
	}

	public void testAppendStackTraceWithNullSource() throws Exception {
		Throwable target = new Throwable();
		StackTraceElement[] trace = target.getStackTrace();
		assertEquals( trace, ThreadUtil.appendStackTrace( (Throwable)null, target ).getStackTrace() );
	}

	public void testAppendStackTraceWithNullTarget() throws Exception {
		Throwable source = new Throwable();
		StackTraceElement[] trace = source.getStackTrace();
		assertEquals( trace, ThreadUtil.appendStackTrace( source, null ).getStackTrace() );
	}

	public void testAppendStackTrace() throws Exception {
		Throwable source = new Throwable();
		Throwable target = new Throwable();

		StackTraceElement[] sourceTrace = source.getStackTrace();
		StackTraceElement[] targetTrace = target.getStackTrace();

		StackTraceElement[] elements = new StackTraceElement[targetTrace.length + sourceTrace.length];
		System.arraycopy( targetTrace, 0, elements, 0, targetTrace.length );
		System.arraycopy( sourceTrace, 0, elements, targetTrace.length, sourceTrace.length );

		assertEquals( elements, ThreadUtil.appendStackTrace( source, target ).getStackTrace() );
	}

	public void testGetStackClasses() throws Exception {
		Class<?>[] frame = ThreadUtil.getStackClasses();
		assertEquals( ThreadUtilTest.class, frame[0] );
	}

	private void assertEquals( Object[] array1, Object[] array2 ) {
		for( int index = 0; index < array1.length; index++ ) {
			assertEquals( array1[index], array2[index] );
		}
	}

}
