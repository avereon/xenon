package com.avereon.xenon;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.tool.AboutTool;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolManagerUIT extends BaseFullXenonTestCase {

	@Test
	void testOpenDependencies() throws Exception {
		// given
		Asset asset = getProgram().getResourceManager().createAsset( ProgramAboutType.URI );
		ProgramTool tool = new AboutTool( getProgram(), asset );
		OpenAssetRequest request = new OpenAssetRequest();

		// when
		boolean result = getProgram().getToolManager().openDependencies( request, tool );

		// then
		assertThat( result ).isTrue();
	}

	@Test
	void testOpenDependenciesReturnFalseOnException() throws Exception {
		// given
		Asset asset = getProgram().getResourceManager().createAsset( ProgramAboutType.URI );
		ProgramTool tool = new MockProgramTool( getProgram(), asset );
		tool.getAssetDependencies().add( URI.create( "mock:///not-really-an-asset" ) );
		OpenAssetRequest request = new OpenAssetRequest();

		// when
		boolean result = getProgram().getToolManager().openDependencies( request, tool );

		// then
		assertThat( result ).isFalse();
	}

}
