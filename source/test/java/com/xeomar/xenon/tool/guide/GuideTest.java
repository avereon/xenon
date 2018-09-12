package com.xeomar.xenon.tool.guide;

import javafx.scene.control.TreeItem;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class GuideTest {

	@Test
	public void testSetSelectedItems() {
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

		guide.setSelectedItems( "general" );
		assertThat( guide.getSelectedIndicies(), CoreMatchers.hasItems( 0 ) );

		guide.setSelectedItems( "workspace" );
		assertThat( guide.getSelectedIndicies(), CoreMatchers.hasItems( 1 ) );
	}

}
