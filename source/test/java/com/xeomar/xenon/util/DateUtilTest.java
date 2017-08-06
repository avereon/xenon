package com.xeomar.xenon.util;

import junit.framework.TestCase;

import java.util.Date;
import java.util.TimeZone;

public class DateUtilTest extends TestCase {

	public void testParse() {
		try {
			DateUtil.parse( "", null );
			fail( "Null format should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		assertNull( DateUtil.parse( null, null ) );
		assertNull( DateUtil.parse( null, "" ) );
		assertNull( DateUtil.parse( "", "" ) );

		assertEquals( new Date( 0 ), DateUtil.parse( "1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss" ) );
	}

	public void testParseWithTimeZone() {
		try {
			DateUtil.parse( "", null, (TimeZone)null );
			fail( "Null format should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		try {
			DateUtil.parse( "", "", (TimeZone)null );
			fail( "Null time zone should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		try {
			DateUtil.parse( "", null, (String)null );
			fail( "Null format should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		try {
			DateUtil.parse( "", "", (String)null );
			fail( "Null time zone should cause a NullPointerException." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		assertNull( DateUtil.parse( null, null, (TimeZone)null ) );
		assertNull( DateUtil.parse( null, null, (String)null ) );
		assertNull( DateUtil.parse( null, "", "" ) );
		assertNull( DateUtil.parse( "", "", "" ) );

		assertEquals( new Date( 0 ), DateUtil.parse( "1970-01-01 05:00:00", "yyyy-MM-dd HH:mm:ss", "GMT+05" ) );
	}

	public void testFormatDuration() {
		assertEquals( "", DateUtil.formatDuration( 0L ) );
		assertEquals( "345ms", DateUtil.formatDuration( 345L ) );
		assertEquals( "1s", DateUtil.formatDuration( 1000L ) );
		assertEquals( "1m", DateUtil.formatDuration( 60000L ) );
		assertEquals( "1h", DateUtil.formatDuration( 3600000L ) );
		assertEquals( "1h 5s", DateUtil.formatDuration( 3605000L ) );
		assertEquals( "1d", DateUtil.formatDuration( 24 * 3600000L ) );
		assertEquals( "12h 7m 53s 654ms", DateUtil.formatDuration( 43673654L ) );
		assertEquals( "74d 13h 54m 33s 321ms", DateUtil.formatDuration( 6443673321L ) );
	}

}
