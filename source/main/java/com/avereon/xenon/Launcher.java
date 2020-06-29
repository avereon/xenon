package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.zenna.ElevatedFlag;
import com.avereon.zenna.UpdateFlag;

public class Launcher {

	public static void main( String[] commands ) {
		ProductCard card = ProgramConfig.loadProductCard();
		ProgramConfig.configureCustomLauncherName( card );
		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );

		if( parameters.isSet( ElevatedFlag.CALLBACK_SECRET ) ) {
			new com.avereon.zenna.Program().start( commands );
		} else if( parameters.isSet( UpdateFlag.UPDATE ) ) {
			new com.avereon.zenna.Program().start( commands );
		} else {
			Program.launch( commands );
		}
	}

}
