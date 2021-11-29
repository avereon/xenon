package com.avereon.xenon;

import com.avereon.product.ProductCard;
import lombok.CustomLog;

@CustomLog
public abstract class BaseModUiTestCase extends BaseXenonUiTestCase {

	private Mod mod;

	protected void initMod( ProductCard card ) {
		this.mod = getProgram().getProductManager().getMod( card.getProductKey() );

		getProgram().getProductManager().setModEnabled( card, true );
		//Assertions.assertThat( getProgram().getProductManager().isEnabled( card ) ).withFailMessage( "Module not ready for testing: " + mod ).isTrue();
	}

	protected Mod getMod() {
		return mod;
	}

}
