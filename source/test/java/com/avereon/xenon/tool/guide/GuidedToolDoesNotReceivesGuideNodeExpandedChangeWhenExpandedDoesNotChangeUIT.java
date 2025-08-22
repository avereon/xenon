package com.avereon.xenon.tool.guide;

import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

public class GuidedToolDoesNotReceivesGuideNodeExpandedChangeWhenExpandedDoesNotChangeUIT extends GuidedToolUIT {

	@Test
	void execute() throws Exception {
		// NOTE When testing expanded nodes the node to expand cannot be a leaf
		// Assert initial state
		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount() ).isEqualTo( 1 );

		Fx.run( () -> mockGuidedTool.getGuideContext().setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( mockGuidedTool.getGuideNodesExpandedEventCount() ).isEqualTo( 1 );
	}

}
