package com.avereon.xenon.tool.product;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramProductType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static com.avereon.xenon.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class ProductToolCloseAssetCloseToolUIT extends ProductToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramProductType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitFor( TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( ProductTool.class );
		assertToolCount( pane, 2 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitFor( TIMEOUT );
		assertToolCount( pane, 1 );
	}

}
