package com.avereon.xenon;

import com.avereon.product.ProgramFlag;

import java.util.Set;

public interface XenonFlag extends ProgramFlag {

	/**
	 * Start the program without showing the workspaces. Combine this with the
	 * NOSPLASH flag to start the program "in the background".
	 */
	String DAEMON = "--daemon";

	/**
	 * Start the program normally even if the daemon flag is specified. This is
	 * most commonly used when restarting after an update.
	 */
	String NO_DAEMON = "--no-daemon";

	/**
	 * Disable a specific mod. May be specified more than once.
	 */
	String DISABLE_MOD = "--disable-mod";

	/**
	 * Enable a specific mod. May be specified more than once. Overrides {@link #DISABLE_MOD}.
	 */
	String ENABLE_MOD = "--enable-mod";

	/**
	 * Send greeting to already running instance.
	 */
	String HELLO = "--hello";

	/**
	 * Specify a different program home than the default.
	 */
	String HOME = "--home";

	/**
	 * Don't show the splash screen at startup. Useful in combination with the
	 * DAEMON flag.
	 */
	String NO_SPLASH = "--no-splash";

	/**
	 * Explicitly turn off automated updates.
	 */
	String NO_UPDATES = "--no-updates";

	/**
	 * Specify an execution profile. Execution profiles are used to change the
	 * program folder so that the program can execute with different modules and
	 * configuration.
	 */
	String PROFILE = "--profile";

	/**
	 * Specify an execution mode. Execution modes are limited, and used to change
	 * specific program functionality. Only supported modes are allowed. Only one
	 * execution mode may be specified. Specifying an execution mode will also
	 * modify the program profile, if specified, by appending the execution mode
	 * to the profile name. If a profile name is not specified, the execution mode
	 * will be used as the profile name.
	 *
	 * <h2>Supported Execution Modes</h2>
	 * <table>
	 *   <tr><th>Mode</th><th>Description</th></tr>
	 *   <tr><td>dev</td><td>Developers can use the <code>dev</code> mode to enable development features.</td></tr>
	 *   <tr><td>test</td><td>Test mode is used for automated testing to change certain behavior during tests.</td></tr>
	 *   <tr><td>screenshot</td><td>Screen shot mode is used when taking automated screen shots of the program.</td></tr>
	 * </table>
	 */
	String MODE = "--mode";

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
	 * Flags that a host will respond to without showing the program.
	 */
	Set<String> QUIET_ACTIONS = Set.of( HELLO, STATUS, STOP, WATCH );

	/**
	 * All flags that a host will respond to.
	 */
	Set<String> ACTIONS = QUIET_ACTIONS;

}
