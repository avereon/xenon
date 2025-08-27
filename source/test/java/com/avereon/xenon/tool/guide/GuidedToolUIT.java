package com.avereon.xenon.tool.guide;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.MockGuidedTool;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.ToolRegistration;
import com.avereon.xenon.asset.MockAssetType;
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

		MockAssetType assetType = new MockAssetType( getProgram() );
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
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		// We would expect the second tool event to be the guide tool
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );

		// For whatever reason, the theory is that the test thread makes it to this
		// point, discovers there is only one tool in the workarea and fails the
		// test. It's possible that this is happening before the task thread even
		// has a chance to finish opening the guide tool. By the time the task
		// thread can get to that point, the program is shutting down. At least this
		// theory matches the current output. If that is the case, a little more
		// waiting here on the test thread should resolve the problem for this set
		// of tests.

		// FIXME Looks like this is our problem. The mock tool was either added twice, or
		// the event posted twice, or something like that. The event was received twice:
		//		Received event=Workarea > ToolEvent : ADDED: GuidedToolUIT$MockGuidedTool
		//		Received event=Workarea > ToolEvent : ADDED: GuidedToolUIT$MockGuidedTool

		System.out.println( "MockGuidedTool FX tool count asserting..." );
		assertThat( getWorkarea().getActiveTool() ).isInstanceOf( MockGuidedTool.class );
		assertToolCount( getWorkarea(), 2 );

		mockGuidedTool = (MockGuidedTool)getWorkarea().getActiveTool();
		mockGuidedTool.reset();
	}

}
