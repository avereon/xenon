package com.avereon.xenon.test;

import com.avereon.product.ProductCard;
import com.avereon.xenon.Mod;
import lombok.CustomLog;

import static org.junit.jupiter.api.Assertions.assertTrue;

@CustomLog
public abstract class BaseModUIT extends FxProgramUIT {

	private Mod mod;

	protected void initMod( ProductCard card ) {
		this.mod = getProgram().getProductManager().getMod( card.getProductKey() );

		getProgram().getProductManager().setModEnabled( card, true );
		assertTrue( getProgram().getProductManager().isEnabled( card ), "Module not ready for testing: " + mod );
	}

	protected Mod getMod() {
		return mod;
	}

}
