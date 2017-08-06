package com.xeomar.xenon.util;

import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ReleaseTest extends TestCase {

	private String versionString = "1.2.3-u-04";

	private String timestampString = "1970-01-01 00:00:00";

	private DateFormat dateFormat = new SimpleDateFormat( Release.DATE_FORMAT );

	@Override
	public void setUp() {
		dateFormat.setTimeZone( DateUtil.DEFAULT_TIME_ZONE );
	}

	public void testConstructorWithStringAndDate() throws Exception {
		Release release = new Release( versionString, dateFormat.parse( timestampString ) );
		assertEquals( versionString, release.getVersion().toString() );
		assertEquals( new Date( 0 ), release.getDate() );
	}

	public void testConstructorWithStringStringAndNullDate() throws Exception {
		Release release = new Release( versionString, null );
		assertEquals( versionString, release.getVersion().toString() );
		assertEquals( null, release.getDate() );
	}

	public void testGetVersion() {
		Release release = new Release( versionString );
		assertEquals( 0, new Version( "1.2.3-u-04" ).compareTo( release.getVersion() ) );
	}

	public void testGetDate() {
		assertNull( new Release( versionString ).getDate() );
		assertEquals( new Date( 0 ), new Release( new Version( versionString ), new Date( 0 ) ).getDate() );
	}

	public void testGetDateString() {
		assertEquals( "", new Release( versionString ).getDateString() );
		assertEquals( "1970-01-01 00:00:00", new Release( versionString, new Date( 0 ) ).getDateString() );
	}

	public void testGetDateStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "America/Phoenix" );
		assertEquals( "", new Release( versionString ).getDateString( zone ) );
		assertEquals( "1969-12-31 17:00:00 MST", new Release( versionString, new Date( 0 ) ).getDateString( zone ) );
	}

	public void testToString() {
		assertEquals( "1.2.3-u-04", new Release( versionString ).toString() );
		assertEquals( "1.2.3-u-04  1970-01-01 00:00:00", new Release( new Version( versionString ), new Date( 0 ) ).toString() );
	}

	public void testToHumanString() {
		assertEquals( "1.2.3 Update 04", new Release( versionString ).toHumanString() );
		assertEquals( "1.2.3 Update 04  1970-01-01 00:00:00", new Release( new Version( versionString ), new Date( 0 ) ).toHumanString() );
	}

	public void testToHumanStringWithTimeZone() {
		TimeZone zone = TimeZone.getTimeZone( "America/Phoenix" );
		assertEquals( "1.2.3 Update 04", new Release( versionString ).toHumanString( zone ) );
		assertEquals( "1.2.3 Update 04  1969-12-31 17:00:00 MST", new Release( new Version( versionString ), new Date( 0 ) ).toHumanString( zone ) );
	}

	public void testEncode() {
		assertEquals( "", Release.encode( new Release( "" ) ) );
		assertEquals( "1.2.3-u-04", Release.encode( new Release( versionString ) ) );
		assertEquals( "1.2.3-u-04  0", Release.encode( new Release( versionString, new Date( 0 ) ) ) );
	}

	public void testDecode() {
		assertNull( Release.decode( null ) );
		assertEquals( new Release( "" ), Release.decode( "" ) );
		assertEquals( new Release( versionString ), Release.decode( "1.2.3-u-04" ) );
		assertEquals( new Release( versionString, new Date( 0 ) ), Release.decode( "1.2.3-u-04  0" ) );
	}

	public void testEquals() {
		assertTrue( new Release( versionString ).equals( new Release( versionString ) ) );
		assertFalse( new Release( "1.2.3 Update 04" ).equals( new Release( versionString ) ) );

		assertTrue( new Release( new Version( "1" ) ).equals( new Release( "1" ) ) );
		assertFalse( new Release( new Version( "1" ) ).equals( new Release( "2" ) ) );
		assertTrue( new Release( new Version( "1" ), new Date( 0 ) ).equals( new Release( "1", new Date( 0 ) ) ) );
		assertFalse( new Release( new Version( "1" ), new Date( 0 ) ).equals( new Release( "1", new Date( 1 ) ) ) );
	}

	public void testHashCode() {
		assertTrue( new Release( versionString ).hashCode() == new Release( versionString ).hashCode() );
		assertTrue( new Release( versionString, null ).hashCode() == new Release( versionString, null ).hashCode() );
		assertTrue( new Release( versionString, new Date( 0 ) ).hashCode() == new Release( versionString, new Date( 0 ) ).hashCode() );
	}

	public void testCompareTo() {
		assertTrue( new Release( "1" ).compareTo( new Release( "1" ) ) == 0 );
		assertTrue( new Release( "1" ).compareTo( new Release( "2" ) ) < 0 );
		assertTrue( new Release( "2" ).compareTo( new Release( "1" ) ) > 0 );

		assertTrue( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) == 0 );
		assertTrue( new Release( new Version( "1" ), new Date( -1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) < 0 );
		assertTrue( new Release( new Version( "1" ), new Date( 1 ) ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) > 0 );

		assertTrue( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), null ) ) == 0 );
		assertTrue( new Release( new Version( "1" ), null ).compareTo( new Release( new Version( "1" ), new Date( 0 ) ) ) == 0 );
		assertTrue( new Release( new Version( "1" ), new Date( 0 ) ).compareTo( new Release( new Version( "1" ), null ) ) == 0 );
	}

}
