package com.parallelsymmetry.essence.util;

import junit.framework.TestCase;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class JavaUtilTest extends TestCase {

	public void testGetCallingClassName() {
		assertEquals( JavaUtilTest.class.getName(), JavaUtil.getCallingClassName() );
	}

	public void testGetCallingClassNameWithLevel() {
		assertEquals( Thread.class.getName(), JavaUtil.getCallingClassName( 0 ) );
		assertEquals( JavaUtil.class.getName(), JavaUtil.getCallingClassName( 1 ) );
	}

	public void testGetClassNameWithString() {
		assertEquals( "Object", JavaUtil.getClassName( "java.lang.Object" ) );
	}

	public void testGetClassNameWithClass() {
		assertEquals( "Object", JavaUtil.getClassName( Object.class ) );
	}

	public void testGetKeySafeClassNameWithString() {
		assertEquals( "java.awt.geom.Rectangle2D.Double", JavaUtil.getKeySafeClassName( "java.awt.geom.Rectangle2D$Double" ) );
	}

	public void testGetKeySafeClassNameWithClass() {
		assertEquals( "java.awt.geom.Rectangle2D.Double", JavaUtil.getKeySafeClassName( Rectangle2D.Double.class ) );
	}

	public void testGetPackageNameWithString() {
		assertEquals( "java.lang", JavaUtil.getPackageName( "java.lang.Object" ) );
	}

	public void testGetPackageNameWithClass() {
		assertEquals( "java.lang", JavaUtil.getPackageName( Object.class ) );
	}

	public void testGetPackagePathWithString() {
		assertEquals( "/java/lang", JavaUtil.getPackagePath( "java.lang.Object" ) );
	}

	public void testGetPackagePathWithClass() {
		assertEquals( "/java/lang", JavaUtil.getPackagePath( Object.class ) );
	}

	public void testParseClasspath() throws Exception {
		List<URI> entries = JavaUtil.parseClasspath( null );
		assertEquals( 0, entries.size() );

		String separator = ";";
		String classpath = "test1.jar";
		classpath += separator + "test2.jar";
		classpath += separator + URLEncoder.encode( "http://www.parallelsymmetry.com/software/test3.jar", "UTF-8" );
		entries = JavaUtil.parseClasspath( classpath, separator );

		assertEquals( new File( "test1.jar" ).toURI(), entries.get( 0 ) );
		assertEquals( new File( "test2.jar" ).toURI(), entries.get( 1 ) );
		assertEquals( URI.create( "http://www.parallelsymmetry.com/software/test3.jar" ), entries.get( 2 ) );

		separator = ":";
		classpath = "test1.jar";
		classpath += separator + "test2.jar";
		classpath += separator + URLEncoder.encode( "http://www.parallelsymmetry.com/software/test3.jar", "UTF-8" );
		entries = JavaUtil.parseClasspath( classpath, separator );

		assertEquals( new File( "test1.jar" ).toURI(), entries.get( 0 ) );
		assertEquals( new File( "test2.jar" ).toURI(), entries.get( 1 ) );
		assertEquals( URI.create( "http://www.parallelsymmetry.com/software/test3.jar" ), entries.get( 2 ) );
	}

	public void testParseManifestClasspath() throws Exception {
		File home = new File( "." ).getCanonicalFile();
		URI base = home.toURI();
		String classpath = "test1.jar test2.jar test%203.jar";

		List<URL> entries = JavaUtil.parseManifestClasspath( base, null );
		assertEquals( 0, entries.size() );

		entries = JavaUtil.parseManifestClasspath( null, classpath );
		assertEquals( 0, entries.size() );

		entries = JavaUtil.parseManifestClasspath( base, classpath );

		assertEquals( new File( home.getCanonicalFile(), "test1.jar" ).toURI().toURL(), entries.get( 0 ) );
		assertEquals( new File( home.getCanonicalFile(), "test2.jar" ).toURI().toURL(), entries.get( 1 ) );
		assertEquals( new File( home.getCanonicalFile(), "test 3.jar" ).toURI().toURL(), entries.get( 2 ) );
	}

	public void testGetRootCause() {
		assertNull( JavaUtil.getRootCause( null ) );

		Throwable one = new Throwable();
		Throwable two = new Throwable( one );
		Throwable three = new Throwable( two );

		assertEquals( one, JavaUtil.getRootCause( one ) );
		assertEquals( one, JavaUtil.getRootCause( two ) );
		assertEquals( one, JavaUtil.getRootCause( three ) );
	}

}
