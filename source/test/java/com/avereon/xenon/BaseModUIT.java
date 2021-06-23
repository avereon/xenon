package com.avereon.xenon;

import com.avereon.product.ProductCard;
import com.avereon.product.Rb;

public abstract class BaseModUIT extends FxProgramUIT {

	private Mod mod;

	protected void initMod( Mod mod ) {
		mod.init( getProgram(), ProductCard.card( mod ) );
		Rb.init( mod );
		this.mod = mod;
	}

	protected Mod getMod() {
		return mod;
	}

}
