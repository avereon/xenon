package com.avereon.xenon.tool.guide;

import com.avereon.xenon.FxProgramUIT;
import com.avereon.xenon.util.FxUtil;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class GuideUIT extends FxProgramUIT {

	private Guide guide;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		this.guide = createGuide();
	}

	@Test
	public void testSetSelectedItems() throws Exception {
		Platform.runLater( () -> guide.setSelectedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "general" ) );

		Platform.runLater( () -> guide.setSelectedIds( "workspace", "tools" ) );
		FxUtil.fxWait( 1000 );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
	}

	@Test
	public void testSetExpandedItems() throws Exception {
		Platform.runLater( () -> guide.setExpandedIds( "general" ) );
		FxUtil.fxWait( 1000 );
		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "general" ) );

		Platform.runLater( () -> guide.setExpandedIds( "workspace", "tools" ) );
		FxUtil.fxWait( 1000 );
		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
	}

	private Guide createGuide() {
		Guide guide = new Guide();

		TreeItem<GuideNode> general = new TreeItem<>( new GuideNode().init( "general", "General" ) );
		general.getChildren().add( new TreeItem<>( new GuideNode().init( "shutdown", "Shutdown" ) ) );
		general.getChildren().add( new TreeItem<>( new GuideNode().init( "security", "Security" ) ) );
		general.getChildren().add( new TreeItem<>( new GuideNode().init( "updates", "Updates" ) ) );

		TreeItem<GuideNode> workspace = new TreeItem<>( new GuideNode().init( "workspace", "Workspace" ) );
		workspace.getChildren().add( new TreeItem<>( new GuideNode().init( "theme", "Theme" ) ) );
		workspace.getChildren().add( new TreeItem<>( new GuideNode().init( "background", "Background" ) ) );
		workspace.getChildren().add( new TreeItem<>( new GuideNode().init( "task-monitor", "Task Monitor" ) ) );
		workspace.getChildren().add( new TreeItem<>( new GuideNode().init( "memory-monitor", "Memory Monitor" ) ) );

		TreeItem<GuideNode> network = new TreeItem<>( new GuideNode().init( "network", "Network" ) );
		network.getChildren().add( new TreeItem<>( new GuideNode().init( "proxy", "Proxy" ) ) );

		TreeItem<GuideNode> tools = new TreeItem<>( new GuideNode().init( "tools", "Tools" ) );

		guide.getRoot().getChildren().add( general );
		guide.getRoot().getChildren().add( workspace );
		guide.getRoot().getChildren().add( network );
		guide.getRoot().getChildren().add( tools );

		return guide;
	}

}
