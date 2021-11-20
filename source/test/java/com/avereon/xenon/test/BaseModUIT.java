package com.avereon.xenon.test;

import com.avereon.product.ProductCard;
import com.avereon.xenon.Mod;
import lombok.CustomLog;

import static org.assertj.core.api.Assertions.assertThat;

@CustomLog
public abstract class BaseModUIT extends FxProgramUIT {

	private Mod mod;

	protected void initMod( ProductCard card ) {
		this.mod = getProgram().getProductManager().getMod( card.getProductKey() );

		getProgram().getProductManager().setModEnabled( card, true );
		assertThat( getProgram().getProductManager().isEnabled( card ) ).withFailMessage( "Module not ready for testing: " + mod ).isTrue();
	}

	protected Mod getMod() {
		return mod;
	}

}
