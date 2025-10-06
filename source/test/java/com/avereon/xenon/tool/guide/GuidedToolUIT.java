package com.avereon.xenon.tool.guide;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.MockGuidedTool;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.ToolRegistration;
import com.avereon.xenon.asset.MockResourceType;
import com.avereon.xenon.asset.MockCodec;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class GuidedToolUIT extends BaseToolUIT {

	protected MockGuidedTool mockGuidedTool;

	@BeforeEach
	@Override
	protected void setup() throws Exception {
		super.setup();

		assertToolCount( getWorkarea(), 0 );

		MockResourceType assetType = new MockResourceType( getProgram() );
		getProgram().getAssetManager().addAssetType( assetType );

		ToolRegistration registration = new ToolRegistration( getProgram(), MockGuidedTool.class ).setName( "mock" ).setInstanceMode( ToolInstanceMode.SINGLETON );
		getProgram().getToolManager().registerTool( assetType, registration );

		// NOTE Returns immediately
		// NOTE The guide (dependent) tool is opened on yet a separate thread
		//   So there are at least four threads at play
		//   - The test thread
		//   - The the FX thread
		//   - The task thread opening the mock tool
		//   - The task thread opening the guide tool
		// And openAsset only returns one future, the mock tool future
		getProgram().getAssetManager().openAsset( MockCodec.URI );
		// We would expect the first tool event to be the mock tool
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		// We would expect the second tool event to be the guide tool
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );

		System.out.println( "MockGuidedTool FX tool count asserting..." );
		assertThat( getWorkarea().getActiveTool() ).isInstanceOf( MockGuidedTool.class );
		assertToolCount( getWorkarea(), 2 );

		mockGuidedTool = (MockGuidedTool)getWorkarea().getActiveTool();
		mockGuidedTool.reset();
	}

}
