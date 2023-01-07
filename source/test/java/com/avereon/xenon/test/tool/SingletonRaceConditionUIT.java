package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.tool.guide.GuideTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

public class SingletonRaceConditionUIT extends BaseToolUIT {

	@Test
	void testOpenToolRaceCondition() throws Exception {
		getProgram().getAssetManager().openAsset( ProgramGuideType.URI, true, false );
		getProgram().getAssetManager().openAsset( ProgramGuideType.URI, true, false );

		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		Workpane pane = getWorkpane();

		assertThat( pane.getTools( GuideTool.class ).size() ).isEqualTo( 1 );
	}

}
