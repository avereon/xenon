package com.avereon.xenon.tool.guide;

import com.avereon.xenon.FxProgramUIT;
import com.avereon.zerra.javafx.Fx;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GuideUIT extends FxProgramUIT {

	private Guide guide;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		this.guide = createGuide();
	}

	@Test
	void testNodeAddRemove() throws Exception {
		Guide guide = new Guide();
		assertThat( guide.getRoot().getChildren().size(), is( 0 ) );

		GuideNode node = new GuideNode( program, "test", "Test" );
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

		GuideNode parent = new GuideNode( program, "parent", "Parent" );
		GuideNode child = new GuideNode( program, "child", "Child" );
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

	@Test
	void testSetSelectedItems() throws Exception {
		Fx.run( () -> guide.setSelectedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "general" ) );

		Fx.run( () -> guide.setSelectedIds( Set.of( "workspace", "tools" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getSelectedIds(), CoreMatchers.hasItems( "workspace", "tools" ) );
	}

	@Test
	void testSetExpandedItems() throws Exception {
		Fx.run( () -> guide.setExpandedIds( Set.of( "general" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( guide.getExpandedIds(), CoreMatchers.hasItems( "general" ) );

		Fx.run( () -> guide.setExpandedIds( Set.of( "workspace", "tools" ) ) );
		Fx.waitForWithExceptions( TIMEOUT );
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
