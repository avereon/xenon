package com.avereon.xenon;

import com.avereon.weave.ElevatedFlag;
import com.avereon.weave.UpdateFlag;

public class Launcher {

	public static void main( String[] commands ) {
		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );
		ProgramConfig.configureCustomLauncherName();

		if( parameters.isSet( ElevatedFlag.CALLBACK_SECRET ) ) {
			// Launch an elevated updater instance
			new com.avereon.weave.Program().start( commands );
		} else if( parameters.isSet( UpdateFlag.UPDATE ) ) {
			// Launch an updater instance
			// When launching an updater instance, the custom launcher name must be
			// set correctly in the event the updater needs to start an elevated
			// updater to handler elevated tasks.
			new com.avereon.weave.Program().start( commands );
		} else {
			// Launch a program instance
			Program.launch( commands );
		}
	}

}
