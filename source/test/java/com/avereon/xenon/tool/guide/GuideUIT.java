package com.avereon.xenon.tool.guide;

import com.avereon.zerra.javafx.FxUtil;
import com.avereon.xenon.FxProgramUIT;
import javafx.application.Platform;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

public class GuideUIT extends FxProgramUIT {

	private Guide guide;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		this.guide = createGuide();
	}

	@Test
	void testSetSelectedItems() throws Exception {
		Platform.runLater( () -> guide.setSelectedIds( Set.of( "general" ) ) );
		FxUtil.fxWaitWithInterrupt( TIMEOUT );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "general" ) );

		Platform.runLater( () -> guide.setSelectedIds( Set.of( "workspace", "tools" ) ) );
		FxUtil.fxWaitWithInterrupt( TIMEOUT );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
	}

	@Test
	void testSetExpandedItems() throws Exception {
		Platform.runLater( () -> guide.setExpandedIds( Set.of( "general" ) ) );
		FxUtil.fxWaitWithInterrupt( TIMEOUT );
		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "general" ) );

		Platform.runLater( () -> guide.setExpandedIds( Set.of( "workspace", "tools" ) ) );
		FxUtil.fxWaitWithInterrupt( TIMEOUT );
		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
	}

	private Guide createGuide() {
		Guide guide = new Guide();

		GuideNode general = new GuideNode( program, "general", "General" );
		GuideNode workspace = new GuideNode( program, "workspace", "Workspace" );
		GuideNode network = new GuideNode( program, "network", "Network" );
		GuideNode tools = new GuideNode( program, "tools", "Tools" );

		guide.addNode( general );
		guide.addNode( workspace );
		guide.addNode( network );
		guide.addNode( tools );

		guide.addNode( general, new GuideNode( program, "shutdown", "Shutdown" ) );
		guide.addNode( general, new GuideNode( program, "security", "Security" ) );
		guide.addNode( general, new GuideNode( program, "updates", "Updates" ) );

		guide.addNode( workspace, new GuideNode( program, "theme", "Theme" ) );
		guide.addNode( workspace, new GuideNode( program, "background", "Background" ) );
		guide.addNode( workspace, new GuideNode( program, "task-monitor", "Task Monitor" ) );
		guide.addNode( workspace, new GuideNode( program, "memory-monitor", "Memory Monitor" ) );

		guide.addNode( network, new GuideNode( program, "proxy", "Proxy" ) );

		return guide;
	}

}
