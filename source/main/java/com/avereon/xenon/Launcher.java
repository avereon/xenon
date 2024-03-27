package com.avereon.xenon;

import com.avereon.util.OperatingSystem;
import com.avereon.util.ThreadUtil;
import com.avereon.weave.ElevatedFlag;
import com.avereon.weave.UpdateFlag;
import com.avereon.weave.Weave;

/**
 * The Launcher class is the entry point for the application. The Launcher is
 * also responsible for determining if the application should be updated or
 * if the application should be launched normally, based on the command line.
 */
public class Launcher {

	public static void main( String[] commands ) {
		ProgramConfig.configureCustomLauncherName();

		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );

		boolean update = parameters.isSet( UpdateFlag.UPDATE );
		boolean callback = parameters.isSet( ElevatedFlag.CALLBACK_SECRET );
		boolean updating = update || callback;

		if( updating ) {
			if( OperatingSystem.isWindows() ) ThreadUtil.pause( 2000 );
			new Weave().start( commands );
		} else {
			Xenon.launch( commands );
		}
	}

}
