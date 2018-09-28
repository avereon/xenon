package com.xeomar.xenon.tool.guide;

import com.xeomar.xenon.BaseTestCase;
import com.xeomar.xenon.resource.Resource;
import javafx.scene.control.TreeItem;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class GuideTest extends BaseTestCase {

	private Guide guide;

	private Resource resource;

	@Before
	public void setup() throws Exception {
		super.setup();
		this.guide = createGuide();
	}

	@Test
	public void testSetSelectedItems() {
		guide.setSelectedIds( "general" );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "general" ) );

		guide.setSelectedIds( "workspace", "tools" );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
	}

	@Test
	public void testSetExpandedItems() {
		guide.setExpandedIds( "general" );
		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "general" ) );

		guide.setExpandedIds( "workspace", "tools" );
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
