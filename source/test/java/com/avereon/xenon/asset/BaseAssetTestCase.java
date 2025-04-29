package com.avereon.xenon.asset;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.mod.MockMod;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseAssetTestCase {

	@Getter
	private XenonProgramProduct product;

	@BeforeEach
	public void setup() throws Exception {
		product = new MockMod();
	}

}
