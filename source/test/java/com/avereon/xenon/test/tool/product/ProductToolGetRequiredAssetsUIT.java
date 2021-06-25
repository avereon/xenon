package com.avereon.xenon.test.tool.product;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.asset.type.ProgramProductType;
import com.avereon.xenon.tool.product.ProductTool;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class ProductToolGetRequiredAssetsUIT extends ProductToolUIT {

	@Test
	void execute() {
		Asset asset = new Asset( ProgramProductType.URI );
		ProductTool tool = new ProductTool( getProgram(), asset );
		Set<URI> assets = tool.getAssetDependencies();
		assertThat( assets, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
