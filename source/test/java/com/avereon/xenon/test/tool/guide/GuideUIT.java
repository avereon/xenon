package com.avereon.xenon.test.tool.guide;

import com.avereon.xenon.test.FxProgramUIT;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GuideUIT extends FxProgramUIT {

	private Guide guide;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		this.guide = createGuide();
	}

	@Test
	void testNodeAddRemove() throws Exception {
		Guide guide = new Guide();
		assertThat( guide.getRoot().getChildren().size(), is( 0 ) );

		GuideNode node = new GuideNode( getProgram(), "test", "Test" );
		guide.addNode( node );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getRoot().getChildren().get( 0 ), is( node.getTreeItem() ) );
		assertThat( guide.getRoot().getChildren().size(), is( 1 ) );

		guide.removeNode( node );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getRoot().getChildren().size(), is( 0 ) );
	}

	@Test
	void testNodeAddRemoveChild() throws Exception {
		Guide guide = new Guide();
		assertThat( guide.getRoot().getChildren().size(), is( 0 ) );

		GuideNode parent = new GuideNode( getProgram(), "parent", "Parent" );
		GuideNode child = new GuideNode( getProgram(), "child", "Child" );
		guide.addNode( parent );
		guide.addNode( parent, child );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getRoot().getChildren().get( 0 ), is( parent.getTreeItem() ) );
		assertThat( guide.getRoot().getChildren().size(), is( 1 ) );
		assertThat( parent.getTreeItem().getChildren().get( 0 ), is( child.getTreeItem() ) );
		assertThat( parent.getTreeItem().getChildren().size(), is( 1 ) );

		guide.removeNode( child );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( parent.getTreeItem().getChildren().size(), is( 0 ) );

		guide.removeNode( parent );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getRoot().getChildren().size(), is( 0 ) );
	}

//	@Test
//	void testSetSelectedItems() throws Exception {
//		Fx.run( () -> guide.setSelectedIds( Set.of( "general" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "general" ) );
//
//		Fx.run( () -> guide.setSelectedIds( Set.of( "workspace", "tools" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
//	}
//
//	@Test
//	void testSetExpandedItems() throws Exception {
//		Fx.run( () -> guide.setExpandedIds( Set.of( "general" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "general" ) );
//
//		Fx.run( () -> guide.setExpandedIds( Set.of( "workspace", "tools" ) ) );
//		Fx.waitForWithExceptions( TIMEOUT );
//		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
//	}

	private Guide createGuide() {
		Guide guide = new Guide();

		GuideNode general = new GuideNode( getProgram(), "general", "General" );
		GuideNode workspace = new GuideNode( getProgram(), "workspace", "Workspace" );
		GuideNode network = new GuideNode( getProgram(), "network", "Network" );
		GuideNode tools = new GuideNode( getProgram(), "tools", "Tools" );

		guide.addNode( general );
		guide.addNode( workspace );
		guide.addNode( network );
		guide.addNode( tools );

		guide.addNode( general, new GuideNode( getProgram(), "shutdown", "Shutdown" ) );
		guide.addNode( general, new GuideNode( getProgram(), "security", "Security" ) );
		guide.addNode( general, new GuideNode( getProgram(), "updates", "Updates" ) );

		guide.addNode( workspace, new GuideNode( getProgram(), "theme", "Theme" ) );
		guide.addNode( workspace, new GuideNode( getProgram(), "background", "Background" ) );
		guide.addNode( workspace, new GuideNode( getProgram(), "task-monitor", "Task Monitor" ) );
		guide.addNode( workspace, new GuideNode( getProgram(), "memory-monitor", "Memory Monitor" ) );

		guide.addNode( network, new GuideNode( getProgram(), "proxy", "Proxy" ) );

		return guide;
	}

}
