package com.avereon.xenon.tool.product;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramGuideType;
import com.avereon.xenon.resource.type.ProgramProductType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class ProductToolGetRequiredResourcesUIT extends ProductToolUIT {

	@Test
	void execute() {
		Resource resource = new Resource( ProgramProductType.URI );
		ProductTool tool = new ProductTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
