package com.avereon.xenon;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.OpenAssetRequest;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.tool.AboutTool;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolManagerUIT extends BaseFullXenonTestCase {

	@Test
	void testOpenDependencies() throws Exception {
		// given
		Resource resource = getProgram().getResourceManager().createAsset( ProgramAboutType.URI );
		ProgramTool tool = new AboutTool( getProgram(), resource );
		OpenAssetRequest request = new OpenAssetRequest();

		// when
		boolean result = getProgram().getToolManager().openDependencies( request, tool );

		// then
		assertThat( result ).isTrue();
	}

	@Test
	void testOpenDependenciesReturnFalseOnException() throws Exception {
		// given
		Resource resource = getProgram().getResourceManager().createAsset( ProgramAboutType.URI );
		ProgramTool tool = new MockProgramTool( getProgram(), resource );
		tool.getAssetDependencies().add( URI.create( "mock:///not-really-an-asset" ) );
		OpenAssetRequest request = new OpenAssetRequest();

		// when
		boolean result = getProgram().getToolManager().openDependencies( request, tool );

		// then
		assertThat( result ).isFalse();
	}

}
