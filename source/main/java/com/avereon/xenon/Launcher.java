package com.avereon.xenon;

import com.avereon.zenna.ElevatedFlag;
import com.avereon.zenna.UpdateFlag;

public class Launcher {

	public static void main( String[] commands ) {
		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );
		if( parameters.isSet( ElevatedFlag.CALLBACK_SECRET ) ) {
			// FIXME In order for the elevated launcher to work the name of the launcher must be configured
			new com.avereon.zenna.Program().start( commands );
		} else if( parameters.isSet( UpdateFlag.UPDATE ) ) {
			new com.avereon.zenna.Program().start( commands );
		} else {
			Program.launch( commands );
		}
	}

}
