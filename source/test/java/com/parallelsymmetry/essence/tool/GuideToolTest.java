package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.FxPlatformTestCase;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GuideToolTest extends FxPlatformTestCase {

	@Test
	public void testToolInfo() {
		assertThat( GuideTool.getToolInfo().getRequiredToolClasses().size(), is( 0 ) );
	}

}
