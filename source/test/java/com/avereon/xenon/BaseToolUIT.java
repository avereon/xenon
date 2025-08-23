package com.avereon.xenon;

import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseToolUIT extends BaseXenonUIT {

	protected static final String MAIN_MENU = "#menu-button-menu";

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		//System.out.println( "Using tool manager: " + System.identityHashCode( getProgram().getToolManager() ) );

		// This seems to be a problem on MacOS for some reason
		AssetType assetType = getProgram().getAssetManager().getAssetType( ProgramGuideType.URI.toString() );
		assertThat( assetType ).isNotNull();

		// No tools registered for asset type xenon:/guide
		List<Class<? extends ProgramTool>> tools = getProgram().getToolManager().getRegisteredTools( assetType );
		assertThat( tools ).isNotNull();
		assertThat( tools ).isNotEmpty();

		// FIXME These assertions still do not ensure that the test will pass on MacOS
		// Do we have two asset managers? or two tool managers?

		// Test output

		//		[INFO] Running com.avereon.xenon.tool.product.ProductToolCloseAssetCloseToolUIT
		//		2025-08-22 03:55:01.177 [W] c.a.x.Xenon.configureHomeFolder: Program home folder does not exist: /opt/xenon
		//		Program start duration=924
		//		2025-08-22 03:55:02.832 [W] c.a.x.ToolManager.determineToolClassForAssetType: No tools registered for asset type xenon:/guide
		//		Tool:
		//		SettingsTool { id="tool-settings" title="modules" }
		//		Error:  Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 4.685 s <<< FAILURE! -- in com.avereon.xenon.tool.product.ProductToolCloseAssetCloseToolUIT
		//		Error:  com.avereon.xenon.tool.product.ProductToolCloseAssetCloseToolUIT.execute -- Time elapsed: 4.670 s <<< FAILURE!
		//			java.lang.AssertionError:
		//
		//		Expected size: 2 but was: 1 in: [SettingsTool{ id="tool-settings" title="modules" }]
		//		at com.avereon.xenon@1.9-SNAPSHOT/com.avereon.xenon.BaseToolUIT.assertToolCount(BaseToolUIT.java:37)
		//		at com.avereon.xenon@1.9-SNAPSHOT/com.avereon.xenon.tool.product.ProductToolCloseAssetCloseToolUIT.execute( ProductToolCloseAssetCloseToolUIT.java:28)
		//		at java.base/java.lang.reflect.Method.invoke(Method.java:580)
		//		at java.base/java.util.ArrayList.forEach( ArrayList.java:1597)
		//		at java.base/java.util.ArrayList.forEach(ArrayList.java:1597)

		// NOTE This looks problematic - not enough tools registered

//		2025-08-22 16:52:14.037 [W] c.a.x.ToolManager.determineToolClassForAssetType: No tools registered for asset type xenon:/guide
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Using tool manager: 514793058
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/index-search
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/help
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/themes
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType:   Tool  ThemeTool
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/notice
//		2025-08-22 16:52:14.038 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/asset
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/modules
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType:   Tool  SettingsTool
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  mock
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType:   Tool  MockGuidedTool
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/about
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/task
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/fault
//		2025-08-22 16:52:14.039 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/new
//		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/welcome
//		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/properties
//		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/settings
//		2025-08-22 16:52:14.040 [W] c.a.x.ToolManager.determineToolClassForAssetType: Type  xenon:/guide

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
