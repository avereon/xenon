package com.parallelsymmetry.essence.tool;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class AboutToolTest extends BaseToolTestCase {

	@Test
	@SuppressWarnings( "unchecked" )
	public void testToolInfo() {
		assertThat( AboutTool.getToolInfo().getRequiredToolClasses(), contains( GuideTool.class ) );
	}

}
