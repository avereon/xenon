package com.parallelsymmetry.essence;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ToolManagerTest extends BaseTestCase {

	private ResourceManager resourceManager;

	private ToolManager toolManager;

	@Override
	public void setup() throws Exception {
		super.setup();
		resourceManager = new ResourceManager( program );
		toolManager = new ToolManager( program );
	}

	@Test
	public void testGetToolClassName() {
		toolManager.addToolAlias( "oldName", "newName" );
		assertThat( toolManager.getToolClassName( "oldName" ), is( "newName" ) );
	}

	@Test
	public void testGetToolClassNameWithNull() {
		assertThat( toolManager.getToolClassName( null ), is( nullValue() ) );
	}

	@Test
	public void testOpenToolWithNullResource() {
		try {
			toolManager.openTool( null );
			Assert.fail( "Should throw a NullPointerException" );
		} catch( Exception exception ) {
			assertThat( exception, is( instanceOf( NullPointerException.class ) ) );
		}
	}

}
