package com.avereon.xenon.test.tool;

import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.test.BaseToolUIT;
import com.avereon.xenon.tool.guide.GuideTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class SingletonRaceConditionUIT extends BaseToolUIT {

	@Test
	void testOpenToolRaceCondition() throws Exception {
		getProgram().getAssetManager().openAsset( ProgramGuideType.URI, true, false );
		getProgram().getAssetManager().openAsset( ProgramGuideType.URI, true, false );

		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		Workpane pane = getWorkpane();

		assertThat( pane.getTools( GuideTool.class ).size(), Matchers.is( 1 ) );
	}

}