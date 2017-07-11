package com.parallelsymmetry.essence.tool;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class ProductInfoToolTest {

	@Test
	@SuppressWarnings( "unchecked" )
	public void testToolInfo() {
		assertThat( ProductInfoTool.getToolInfo().getRequiredToolClasses(), contains( GuideTool.class ) );
	}

}
