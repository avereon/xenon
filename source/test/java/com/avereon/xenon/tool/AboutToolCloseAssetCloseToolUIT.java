package com.avereon.xenon.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class AboutToolCloseAssetCloseToolUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		// given
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		//System.out.println( "AboutToolCloseAssetCloseToolUIT openAsset" );
		// NOTE Returns immediately
		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramAboutType.URI );
		//System.out.println( "AboutToolCloseAssetCloseToolUIT waiting for tool added one" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		//System.out.println( "AboutToolCloseAssetCloseToolUIT waiting for tool added two" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		//System.out.println( "AboutToolCloseAssetCloseToolUIT FX is settled" );
		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		System.out.println( "AboutToolCloseAssetCloseToolUIT FX tool count asserting..." );
		assertToolCount( pane, 2 );
		//System.out.println( "AboutToolCloseAssetCloseToolUIT FX tool count asserted" );

		// when
		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );

		// then
		assertToolCount( pane, 1 );
	}

	// NOTE Research on MacOS Aug 2025
	// After researching the test failure flakiness on MacOS this is what we
	// have discovered so far. It appears the tests occasionally fail on MacOS
	// because there is not a tool registered for the xenon:/guide asset type at
	// the time the dependent tool is being opened. This does not appear to be a
	// consistent failure, even during the same test run and most times, only
	// one or two tests will fail due to this problem.
	//
	// We have not, however, determined what is causing the tool registrations
	// not to be complete. We have investigated incomplete setup as well as
	// premature teardown, and neither has resulted in evidence of a problem.
	//
	// We have researched the possibility of multiple asset or tool managers and
	// believe we have ruled out this situation.
	//
	// The test that fails most often appears to be, possibly because it is the
	// first of this type of test to be run, but others fail on occasion as well:
	// - AboutToolCloseAssetCloseToolUIT
	//
	// The JvmSureStop timeout is 10 seconds. I have adjusted this time to 15
	// seconds on 24 Aug 2025 to see if that helps. If it does, then I might
	// consider increasing the timeout to 20 seconds, just to give more
	// breathing room for the tests. If not, at least we have a lead about what
	// is going on. Result: this does not appear to have been the cause.
	//
	// This might also be a mix of the Maven FailSafe plugin not using a
	// separate JVM for each UI test as expected, new tests being run in an old
	// JVM that is shutting down, and the JvmSureStop hook terminating the JVM
	// as expected. Xenon tries to allow the JVM to terminate nicely, but we
	// implemented JvmSureStop to ensure that the JVM does terminate. There may
	// be something going on with the tests (not really a surprise) that is
	// causing the JVM to not terminate.
	//
	// 24 Aug 2025 - Finally some progress
	// We finally have some evidence of premature shutdown. See the following
	// output. Also note the time different between the workspace visible event
	// and the program shutdown event, about 10 seconds. Is there a test timeout
	// getting in the way?:
	//		Program start duration=1007
	//		time=2389 marker=program-started thread=TaskPool-3-worker-1
	//->	time=2692 marker=workspace-visible thread=JavaFX Application Thread
	//		registerTool: assetType=mock -> tool=com.avereon.xenon.tool.guide.GuidedToolUIT$MockGuidedTool
	//->	time=3640 marker=program-shutdown thread=JavaFX Application Thread
	//		unregisterTool: assetType=xenon:/properties -> tool=com.avereon.xenon.tool.PropertiesTool
	//		time=3641 marker=do-shutdown-tasks thread=TaskPool-1-worker-1
	//		2025-08-25 01:48:01.804 [W] c.a.x.ToolManager.determineToolClassForAssetType: No tools registered for asset type xenon:/guide
	//		unregisterTool: assetType=xenon:/help -> tool=com.avereon.xenon.tool.HelpTool
	//		unregisterTool: assetType=xenon:/asset -> tool=com.avereon.xenon.tool.AssetTool
	//		unregisterTool: assetType=xenon:/new -> tool=com.avereon.xenon.tool.NewAssetTool
	//		unregisterTool: assetType=xenon:/fault -> tool=com.avereon.xenon.tool.FaultTool
	//		unregisterTool: assetType=xenon:/task -> tool=com.avereon.xenon.tool.TaskTool
	//		unregisterTool: assetType=xenon:/modules -> tool=com.avereon.xenon.tool.product.ProductTool
	//		unregisterTool: assetType=xenon:/welcome -> tool=com.avereon.xenon.tool.WelcomeTool
	//		unregisterTool: assetType=xenon:/index-search -> tool=com.avereon.xenon.tool.SearchTool
	//		unregisterTool: assetType=xenon:/notice -> tool=com.avereon.xenon.tool.NoticeTool
	//		unregisterTool: assetType=xenon:/settings -> tool=com.avereon.xenon.tool.settings.SettingsTool
	//		unregisterTool: assetType=xenon:/about -> tool=com.avereon.xenon.tool.AboutTool
	//		unregisterTool: assetType=xenon:/guide -> tool=com.avereon.xenon.tool.guide.GuideTool
	//		ToolManager
	//		Type  xenon:/fault
	//		Type  xenon:/themes
	//		Tool  ThemeTool
	//		Type  xenon:/modules
	//		Tool  SettingsTool
	//		Type  xenon:/help
	//		Type  xenon:/properties
	//		Type  xenon:/about
	//		Type  xenon:/welcome
	//		Type  xenon:/index-search
	//		Type  xenon:/new
	//			Type  mock
	//		Tool  MockGuidedTool
	//		Type  xenon:/asset
	//		Type  xenon:/notice
	//		Type  xenon:/settings
	//		Type  xenon:/task
	//		Type  xenon:/guide

	// 27 Aug 2025 - Looks like this is our problem. The mock tool was either added twice, or
	// the event posted twice, or something like that. The event was received twice:
	//		Received event=Workarea > ToolEvent : ADDED: GuidedToolUIT$MockGuidedTool
	//		Received event=Workarea > ToolEvent : ADDED: GuidedToolUIT$MockGuidedTool

	// 04 Sep 2025 - Further research has yielded that we receive two different
	// tool added events for the same tool (need to double check this). Because
	// of the double event, the test is allowed to proceed before it should.
	//		Received event=[1497538476] Workarea > ToolEvent : ADDED: AboutTool
	//		Received event=[118504112] Workarea > ToolEvent : ADDED: AboutTool
	// Next insight:
	//		Received event=[1497538476] Workarea > ToolEvent : ADDED: SettingsTool from tool=[1688940590]
	//		Received event=[1485624601] Workarea > ToolEvent : ADDED: SettingsTool from tool=[1688940590]
	// The double events appear to be for the same tool. So, we'll need to look
	// for why two events are being posted.

	// 08 Sep 2025 - We believe we have narrowed down the problem to the handling
	// of the delayed event propagation logic in Workpane. Because of the nature
	// of how FX fires events and the delayed nature of the event propagation,
	// we were unintentionally causing a race condition where a copy of the tool
	// was being fired under certain conditions.
	//
	// New logic has been added to Workpane.dispatchEvents() to prevent the race
	// condition and avoid the duplicate event.
}
