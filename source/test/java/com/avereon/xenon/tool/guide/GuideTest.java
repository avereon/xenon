package com.avereon.xenon.tool.guide;

import com.avereon.xenon.BaseFullXenonTestCase;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

public class GuideTest extends BaseFullXenonTestCase {

	@Test
	void testNodeAddRemove() throws Exception {
		Guide guide = new Guide();
		assertThat( guide.getRoot().getChildren().size() ).isEqualTo( 0 );

		GuideNode node = new GuideNode( getProgram(), "test", "Test" );
		guide.addNode( node );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( guide.getRoot().getChildren().getFirst() ).isEqualTo( node.getTreeItem() );
		assertThat( guide.getRoot().getChildren().size() ).isEqualTo( 1 );

		guide.removeNode( node );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( guide.getRoot().getChildren().size() ).isEqualTo( 0 );
	}

	@Test
	void testNodeAddRemoveChild() throws Exception {
		Guide guide = new Guide();
		assertThat( guide.getRoot().getChildren().size() ).isEqualTo( 0 );

		GuideNode parent = new GuideNode( getProgram(), "parent", "Parent" );
		GuideNode child = new GuideNode( getProgram(), "child", "Child" );
		guide.addNode( parent );
		guide.addNode( parent, child );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( guide.getRoot().getChildren().getFirst() ).isEqualTo( parent.getTreeItem() );
		assertThat( guide.getRoot().getChildren().size() ).isEqualTo( 1 );
		assertThat( parent.getTreeItem().getChildren().getFirst() ).isEqualTo( child.getTreeItem() );
		assertThat( parent.getTreeItem().getChildren().size() ).isEqualTo( 1 );

		guide.removeNode( child );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( parent.getTreeItem().getChildren().size() ).isEqualTo( 0 );

		guide.removeNode( parent );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( guide.getRoot().getChildren().size() ).isEqualTo( 0 );
	}

	//	@Test
	//	void testSetSelectedItems() throws Exception {
	//		Fx.run( () -> guide.setSelectedIds( Set.of( "general" ) ) );
	//		Fx.waitForWithExceptions( TIMEOUT );
	//		assertThat( guide.getSelectedIds()).contains( "general" ) );
	//
	//		Fx.run( () -> guide.setSelectedIds( Set.of( "workspace", "tools" ) ) );
	//		Fx.waitForWithExceptions( TIMEOUT );
	//		assertThat( guide.getSelectedIds()).contains( "workspace", "tools" ) );
	//	}
	//
	//	@Test
	//	void testSetExpandedItems() throws Exception {
	//		Fx.run( () -> guide.setExpandedIds( Set.of( "general" ) ) );
	//		Fx.waitForWithExceptions( TIMEOUT );
	//		assertThat( guide.getExpandedIds()).contains( "general" ) );
	//
	//		Fx.run( () -> guide.setExpandedIds( Set.of( "workspace", "tools" ) ) );
	//		Fx.waitForWithExceptions( TIMEOUT );
	//		assertThat( guide.getExpandedIds()).contains( "workspace", "tools" ) );
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
