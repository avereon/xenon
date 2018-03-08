package com.xeomar.xenon;

public interface ProgramFlag {

	String EXECMODE = "execmode";

	String EXECMODE_DEVL = ExecMode.DEV.name().toLowerCase();

	String EXECMODE_TEST = ExecMode.TEST.name().toLowerCase();

	String HOME = "home";

	String LOG_LEVEL = "log-level";

	String NOUPDATE = "noupdate";

	String NOUPDATECHECK = "noupdatecheck";

	/**
	 * Print the help information and exit.
	 */
	String HELP = "help";

	/**
	 * Print the status information and exit.
	 */
	String STATUS = "status";

	/**
	 * Request the program stop.
	 */
	String STOP = "stop";

	/**
	 * Print the version information and exit.
	 */
	String VERSION = "version";

	/**
	 * Watch the host instance
	 */
	String WATCH = "watch";

	/**
	 * Reset the program settings to defaults.
	 */
	String RESET = "reset";

}
