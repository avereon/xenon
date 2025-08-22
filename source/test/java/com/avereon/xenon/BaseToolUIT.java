package com.avereon.xenon;

import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseToolUIT extends BaseXenonUIT {

	protected static final String MAIN_MENU = "#menu-button-menu";

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// This seems to be a problem on MacOS for some reason
		AssetType assetType = getProgram().getAssetManager().getAssetType( ProgramGuideType.URI.toString() );
		assertThat( assetType ).isNotNull();

		// No tools registered for asset type xenon:/guide
		List<Class<? extends ProgramTool>> tools = getProgram().getToolManager().getRegisteredTools( assetType );
		assertThat( tools ).isNotNull();
		assertThat( tools ).isNotEmpty();
	}

	protected void assertToolCount( Workpane pane, int count ) {
		Collection<Tool> tools = pane.getTools();
		assertThat( tools ).isNotNull();

		try {
			assertThat( tools ).hasSize( count );
		} catch( AssertionError error ) {
			tools.forEach( t -> System.out.println( "Tool: " + t ) );
			throw error;
		}
	}

	protected void openMenuItem( String menuId, String menuItemId ) {
		robot.clickOn( MAIN_MENU );
		robot.moveTo( menuId );
		robot.clickOn( menuItemId );
	}

}
