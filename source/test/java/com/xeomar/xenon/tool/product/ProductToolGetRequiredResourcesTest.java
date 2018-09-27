package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.resource.type.ProgramProductType;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ProductToolGetRequiredResourcesTest extends ProductToolTest {

	@Test
	public void execute() {
		Resource resource = new Resource( ProgramProductType.URI );
		ProductTool tool = new ProductTool( program, resource );
		Set<URI> resources = tool.getResourceDependencies();
		assertThat( resources, containsInAnyOrder( ProgramGuideType.URI ) );
	}

}
