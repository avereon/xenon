package com.avereon.xenon;

import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseToolUIT extends BaseXenonUIT {

	protected static final String MAIN_MENU = "#menu-button-menu";

	// NOTE For MacOS debugging
	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		setLogLevel( Level.INFO );

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

		//		// Test output
		//		//		2025-08-22 03:55:02.832 [W] c.a.x.ToolManager.determineToolClassForAssetType: No tools registered for asset type xenon:/guide
		//		//		Error:  Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 4.685 s <<< FAILURE! -- in com.avereon.xenon.tool.product.ProductToolCloseAssetCloseToolUIT
		//
		//		// Not enough tools registered
		//    // Not only is xenon:/guide not registered, but so many other asset types do not have registrations either
		/// /		2025-08-22 16:52:14.037 [W] c.a.x.ToolManager.determineToolClassForAssetType: No tools registered for asset type xenon:/guide
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Using tool manager: 514793058
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/index-search
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/help
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/themes
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType:   Tool  ThemeTool
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/notice
		/// /		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/asset
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/modules
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType:   Tool  SettingsTool
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  mock
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType:   Tool  MockGuidedTool
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/about
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/task
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/fault
		/// /		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/new
		/// /		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/welcome
		/// /		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/properties
		/// /		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/settings
		/// /		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/guide

	}

	protected void assertToolCount( Workpane pane, int count ) {
		assertThat( pane.getTools() ).hasSize( count );
	}

	protected void openMenuItem( String menuId, String menuItemId ) {
		robot.clickOn( MAIN_MENU );
		robot.moveTo( menuId );
		robot.clickOn( menuItemId );
	}

}
