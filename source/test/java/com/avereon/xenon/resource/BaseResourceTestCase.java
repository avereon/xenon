package com.avereon.xenon.resource;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.mod.MockMod;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseResourceTestCase {

	@Getter
	private XenonProgramProduct product;

	@BeforeEach
	public void setup() throws Exception {
		product = new MockMod();
	}

}
