package com.xeomar.xenon.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class OperatingSystemTest extends TestCase {

	@Override
	public void setUp() throws Exception {
		System.clearProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY );
	}

	public void testLinux() throws Exception {
		init( "Linux", "x86_64", "2.6.32_45" );
		assertTrue( OperatingSystem.isLinux() );
		assertFalse( OperatingSystem.isMac() );
		assertTrue( OperatingSystem.isUnix() );
		assertFalse( OperatingSystem.isWindows() );
		assertEquals( "2.6.32_45", OperatingSystem.getVersion() );
		assertEquals( "x86_64", OperatingSystem.getSystemArchitecture() );
		assertEquals( OperatingSystem.Family.LINUX, OperatingSystem.getFamily() );
		assertEquals( "java", OperatingSystem.getJavaExecutableName() );
	}

	public void testMac() throws Exception {
		init( "Mac OS X", "ppc", "10" );
		assertFalse( OperatingSystem.isLinux() );
		assertTrue( OperatingSystem.isMac() );
		assertTrue( OperatingSystem.isUnix() );
		assertFalse( OperatingSystem.isWindows() );
		assertEquals( "10", OperatingSystem.getVersion() );
		assertEquals( "ppc", OperatingSystem.getSystemArchitecture() );
		assertEquals( OperatingSystem.Family.MACOSX, OperatingSystem.getFamily() );
		assertEquals( "java", OperatingSystem.getJavaExecutableName() );
	}

	public void testWindows7() throws Exception {
		init( "Windows 7", "x86", "6.1" );
		assertFalse( OperatingSystem.isLinux() );
		assertFalse( OperatingSystem.isMac() );
		assertFalse( OperatingSystem.isUnix() );
		assertTrue( OperatingSystem.isWindows() );
		assertEquals( "6.1", OperatingSystem.getVersion() );
		assertEquals( "x86", OperatingSystem.getSystemArchitecture() );
		assertEquals( OperatingSystem.Family.WINDOWS, OperatingSystem.getFamily() );
		assertEquals( "javaw", OperatingSystem.getJavaExecutableName() );
	}

	public void testWindows8() throws Exception {
		init( "Windows 8", "x86", "6.2" );
		assertFalse( OperatingSystem.isLinux() );
		assertFalse( OperatingSystem.isMac() );
		assertFalse( OperatingSystem.isUnix() );
		assertTrue( OperatingSystem.isWindows() );
		assertEquals( "6.2", OperatingSystem.getVersion() );
		assertEquals( "x86", OperatingSystem.getSystemArchitecture() );
		assertEquals( OperatingSystem.Family.WINDOWS, OperatingSystem.getFamily() );
		assertEquals( "javaw", OperatingSystem.getJavaExecutableName() );
	}

	public void testWindows8_1() throws Exception {
		init( "Windows 8.1", "x86", "6.3" );
		assertFalse( OperatingSystem.isLinux() );
		assertFalse( OperatingSystem.isMac() );
		assertFalse( OperatingSystem.isUnix() );
		assertTrue( OperatingSystem.isWindows() );
		assertEquals( "6.3", OperatingSystem.getVersion() );
		assertEquals( "x86", OperatingSystem.getSystemArchitecture() );
		assertEquals( OperatingSystem.Family.WINDOWS, OperatingSystem.getFamily() );
		assertEquals( "javaw", OperatingSystem.getJavaExecutableName() );
	}

	@Test
	public void testIsProcessElevatedMac() throws Exception {
		OperatingSystemTest.init( "Mac OS X", "ppc", "10" );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertFalse( OperatingSystem.isProcessElevated() );

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertTrue( OperatingSystem.isProcessElevated() );
	}

	@Test
	public void testIsProcessElevatedUnix() throws Exception {
		OperatingSystemTest.init( "Linux", "x86_64", "2.6.32_45" );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertFalse( OperatingSystem.isProcessElevated() );

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertTrue( OperatingSystem.isProcessElevated() );
	}

	@Test
	public void testIsProcessElevatedWindows() throws Exception {
		OperatingSystemTest.init( "Windows 7", "x86", "6.1" );
		OperatingSystem.clearProcessElevatedFlag();
		assertFalse( OperatingSystem.isProcessElevated() );

		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		OperatingSystem.clearProcessElevatedFlag();
		assertTrue( OperatingSystem.isProcessElevated() );
	}

	@Test
	public void testElevateProcessMac() throws Exception {
		OperatingSystemTest.init( "Mac OS X", "ppc", "10" );
		ProcessBuilder builder = new ProcessBuilder( "textmate" );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), "elevate" );

		OperatingSystem.elevateProcessBuilder( "textmate", builder );
		assertEquals( 2, builder.command().size() );
		assertEquals( elevate.getCanonicalPath(), builder.command().get( 0 ) );
		assertEquals( "textmate", builder.command().get( 1 ) );
	}

	@Test
	public void testElevateProcessUnix() throws Exception {
		String program = "vi";
		OperatingSystemTest.init( "Linux", "x86_64", "2.6.32_45" );
		ProcessBuilder builder = new ProcessBuilder( program );
		OperatingSystem.elevateProcessBuilder( program, builder );

		File gksudo = new File( "/usr/bin/gksudo" );
		File kdesudo = new File( "/usr/bin/kdesudo" );
		if( gksudo.exists() ) {
			assertEquals( 5, builder.command().size() );
			assertEquals( gksudo.toString(), builder.command().get( 0 ) );
			assertEquals( program, builder.command().get( 4 ) );
		} else if( kdesudo.exists() ) {
			assertEquals( 3, builder.command().size() );
			assertEquals( kdesudo.toString(), builder.command().get( 0 ) );
			assertEquals( program, builder.command().get( 2 ) );
		} else {
			assertEquals( 6, builder.command().size() );
			assertEquals( "xterm", builder.command().get( 0 ) );
			assertEquals( "-title", builder.command().get( 1 ) );
			assertEquals( program, builder.command().get( 2 ) );
			assertEquals( "-e", builder.command().get( 3 ) );
			assertEquals( "sudo", builder.command().get( 4 ) );
			assertEquals( program, builder.command().get( 5 ) );
		}
	}

	@Test
	public void testElevateProcessWindows() throws Exception {
		OperatingSystemTest.init( "Windows 7", "x86", "6.1" );
		ProcessBuilder builder = new ProcessBuilder( "notepad.exe" );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), "elevate.js" );

		OperatingSystem.elevateProcessBuilder( "Notepad", builder );

		int index = 0;
		assertEquals( 3, builder.command().size() );
		assertEquals( "wscript", builder.command().get( index++ ) );
		assertEquals( elevate.getCanonicalPath(), builder.command().get( index++ ) );
		assertEquals( "notepad.exe", builder.command().get( index++ ) );
	}

	@Test
	public void testReduceProcessMac() throws Exception {
		OperatingSystemTest.init( "Mac OS X", "ppc", "10" );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( "textmate" );

		OperatingSystem.reduceProcessBuilder( builder );

		int index = 0;
		assertEquals( 5, builder.command().size() );
		assertEquals( "su", builder.command().get( index++ ) );
		assertEquals( "-", builder.command().get( index++ ) );
		assertEquals( System.getenv( "SUDO_USER" ), builder.command().get( index++ ) );
		assertEquals( "--", builder.command().get( index++ ) );
		assertEquals( "textmate", builder.command().get( index++ ) );
	}

	@Test
	public void testReduceProcessUnix() throws Exception {
		OperatingSystemTest.init( "Linux", "x86_64", "2.6.32_45" );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( "vi" );

		OperatingSystem.reduceProcessBuilder( builder );

		int index = 0;
		assertEquals( 5, builder.command().size() );
		assertEquals( "su", builder.command().get( index++ ) );
		assertEquals( "-", builder.command().get( index++ ) );
		assertEquals( System.getenv( "SUDO_USER" ), builder.command().get( index++ ) );
		assertEquals( "--", builder.command().get( index++ ) );
		assertEquals( "vi", builder.command().get( index++ ) );
	}

	@Test
	public void testReduceProcessWindows() throws Exception {
		OperatingSystemTest.init( "Windows 7", "x86", "6.1" );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( OperatingSystem.getJavaExecutablePath(), "-jar", "C:\\Program Files\\Escape\\program.jar", "-update", "false" );

		IOException exception = null;
		try {
			OperatingSystem.reduceProcessBuilder( builder );
			fail( "Launching a normal processes from an elevated processes in Windows is impossible." );
		} catch( IOException ioexception ) {
			exception = ioexception;
		}

		assertNotNull( exception );
	}

	public void testGetJavaExecutableName() {
		assertEquals( OperatingSystem.isWindows() ? "javaw" : "java", OperatingSystem.getJavaExecutableName() );
	}

	public void testGetJavaExecutablePath() {
		String java = OperatingSystem.isWindows() ? "javaw" : "java";
		assertEquals( System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + java, OperatingSystem.getJavaExecutablePath() );
	}

	public void testResolveNativeLibPath() throws Exception {
		init( "Windows 8", "x86", "6.2" );
		assertEquals( "win/x86/rxtxSerial.dll", OperatingSystem.resolveNativeLibPath( "rxtxSerial" ) );

		init( "Linux", "x86_64", "2.6.32_45" );
		assertEquals( "linux/x86_64/librxtxSerial.so", OperatingSystem.resolveNativeLibPath( "rxtxSerial" ) );
	}

	private static final void init( String name, String arch, String version ) throws Exception {
		Method initMethod = OperatingSystem.class.getDeclaredMethod( "init", String.class, String.class, String.class );
		initMethod.setAccessible( true );
		initMethod.invoke( null, name, arch, version );
	}

}
