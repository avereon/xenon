package com.avereon.xenon;

import com.avereon.weave.ElevatedFlag;
import com.avereon.weave.UpdateFlag;

public class Launcher {

	public static void main( String[] commands ) {
		ProgramConfig.configureCustomLauncherName();

		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );

		boolean update = parameters.isSet( UpdateFlag.UPDATE );
		boolean callback = parameters.isSet( ElevatedFlag.CALLBACK_SECRET );
		boolean updating = update || callback;

		if( updating ) {
			new com.avereon.weave.Program().start( commands );
		} else {
			Xenon.doLaunch( commands );
		}
	}

}
