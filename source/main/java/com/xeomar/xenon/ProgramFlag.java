package com.xeomar.xenon;

import com.xeomar.util.LogFlag;

public interface ProgramFlag extends LogFlag {

	String EXECMODE = "--execmode";

	String EXECMODE_DEVL = ExecMode.DEV.name().toLowerCase();

	String EXECMODE_TEST = ExecMode.TEST.name().toLowerCase();

	String HOME = "--home";

	String NOUPDATE = "--noupdate";

	String NOUPDATECHECK = "--noupdatecheck";

	// FIXME Should this be used in place of NOUPDATECHECK
	String UPDATE_IN_PROGRESS = "--updateinprogress";

	/**
	 * Print the help information and exit.
	 */
	String HELP = "--help";

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

}
