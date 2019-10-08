package com.avereon.xenon;

import com.avereon.util.LogFlag;

import java.util.Set;

public interface ProgramFlag extends LogFlag {

	/**
	 * Specify the exec mode. The values are 'dev' and 'test'. This flag is not
	 * intended to be used for normal operation. Developers can use the 'dev'
	 * value to run the program as a different instance than their production
	 * instance. The 'test' value is used during unit and integration tests.
	 */
	String EXECMODE = "--execmode";

	/**
	 * Send greeting to already running instance.
	 */
	String HELLO = "--hello";

	/**
	 * Print the help information and exit.
	 */
	String HELP = "--help";

	/**
	 * Specify a different program home than the default.
	 */
	String HOME = "--home";

	/**
	 * Explicitly turn off automated updates.
	 */
	String NOUPDATE = "--noupdate";

	/**
	 * Print the status information and exit.
	 */
	String STATUS = "--status";

	/**
	 * Request the program stop.
	 */
	String STOP = "--stop";

	/**
	 * Print the version information and exit.
	 */
	String VERSION = "--version";

	/**
	 * Watch the host instance
	 */
	String WATCH = "--watch";

	/**
	 * Reset the program settings to defaults.
	 */
	String RESET = "--reset";

	/**
	 * Flags that a host will respond to without showing a hidden workspace.
	 */
	Set<String> QUIET_ACTIONS = Set.of( HELLO, STATUS, STOP, WATCH );

	/**
	 * All flags that a host will respond to.
	 */
	Set<String> ACTIONS = QUIET_ACTIONS;

}
