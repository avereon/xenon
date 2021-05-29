package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.transaction.Txn;
import org.fxmisc.undo.UndoManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeChangeTest {

	private MockNode node;

	private MockNode child;

	private MockNode setNode1;

	private MockNode setNode2;

	private MockNode setNode3;

	private UndoManager<List<NodeChange>> undoManager;

	@BeforeEach
	void setup() {
		this.node = new MockNode();
		this.node.addModifyingKeys( "a" );
		this.child = new MockNode();
		this.child.addModifyingKeys( "c" );
		this.node.setValue( "child", child );

		this.setNode1 = new MockNode( "set-node-1" );
		this.setNode2 = new MockNode( "set-node-2" );
		this.setNode3 = new MockNode( "set-node-3" );

		// Enable undo change capture
		this.node.setValue( NodeChange.CAPTURE_UNDO_CHANGES, true );
		undoManager = DataNodeUndo.manager( node );
	}

	@Test
	void testSetValue() {
		assertThat( node.getValue( "a" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
		node.setValue( "a", 0 );
		assertThat( node.getValue( "a" ), is( 0 ) );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testUndo() {
		testSetValue();
		undoManager.undo();
		assertThat( node.getValue( "a" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testRedo() {
		testUndo();
		undoManager.redo();
		assertThat( node.getValue( "a" ), is( 0 ) );
		assertFalse( undoManager.isRedoAvailable() );
		assertTrue( undoManager.isUndoAvailable() );
	}

	@Test
	void testMultipleUndo() throws Exception {
		assertThat( node.getValue( "a" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		try( Txn ignore = Txn.create() ) {
			node.setValue( "a", 0 );
			node.setValue( "a", 1 );
			node.setValue( "a", 2 );
			node.setValue( "a", 3 );
		}
		node.setValue( "a", 4 );
		assertThat( node.getValue( "a" ), is( 4 ) );

		undoManager.undo();
		assertThat( node.getValue( "a" ), is( 3 ) );

		undoManager.undo();
		assertThat( node.getValue( "a" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testMultipleUndoWithChildNode() throws Exception {
		assertThat( child.getValue( "c" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		try( Txn ignore = Txn.create() ) {
			child.setValue( "c", 0 );
			child.setValue( "c", 1 );
			child.setValue( "c", 2 );
			child.setValue( "c", 3 );
		}
		child.setValue( "c", 4 );
		assertThat( child.getValue( "c" ), is( 4 ) );

		undoManager.undo();
		assertThat( child.getValue( "c" ), is( 3 ) );

		undoManager.undo();
		assertThat( child.getValue( "c" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testMultipleUndoWithMultipleNodes() throws Exception {
		Node childB = new MockNode();
		Node childC = new MockNode();
		Node childD = new MockNode();

		childB.addModifyingKeys( "x", "y", "z" );
		childC.addModifyingKeys( "x", "y", "z" );
		childD.addModifyingKeys( "x", "y", "z" );

		node.setValue( "childB", childB );
		node.setValue( "childC", childC );
		node.setValue( "childD", childD );

		assertThat( childB.getValue( "x" ), is( nullValue() ) );
		assertThat( childB.getValue( "y" ), is( nullValue() ) );
		assertThat( childB.getValue( "z" ), is( nullValue() ) );
		assertThat( childC.getValue( "x" ), is( nullValue() ) );
		assertThat( childC.getValue( "y" ), is( nullValue() ) );
		assertThat( childC.getValue( "z" ), is( nullValue() ) );
		assertThat( childD.getValue( "x" ), is( nullValue() ) );
		assertThat( childD.getValue( "y" ), is( nullValue() ) );
		assertThat( childD.getValue( "z" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		try( Txn ignore = Txn.create() ) {
			childB.setValue( "x", 1 );
			childB.setValue( "y", 1 );
			childB.setValue( "z", 1 );

			childC.setValue( "x", 2 );
			childC.setValue( "y", 2 );
			childC.setValue( "z", 2 );

			childD.setValue( "x", 3 );
			childD.setValue( "y", 3 );
			childD.setValue( "z", 3 );

			childC.setValue( "x", 4 );
			childC.setValue( "y", 4 );
			childC.setValue( "z", 4 );
		}

		try( Txn ignore = Txn.create() ) {
			childB.setValue( "x", 11 );
			childB.setValue( "y", 11 );
			childB.setValue( "z", 11 );
			childD.setValue( "x", 14 );
			childD.setValue( "y", 14 );
			childD.setValue( "z", 14 );
		}

		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		assertThat( childB.getValue( "x" ), is( 11 ) );
		assertThat( childB.getValue( "y" ), is( 11 ) );
		assertThat( childB.getValue( "z" ), is( 11 ) );
		assertThat( childC.getValue( "x" ), is( 4 ) );
		assertThat( childC.getValue( "y" ), is( 4 ) );
		assertThat( childC.getValue( "z" ), is( 4 ) );
		assertThat( childD.getValue( "x" ), is( 14 ) );
		assertThat( childD.getValue( "y" ), is( 14 ) );
		assertThat( childD.getValue( "z" ), is( 14 ) );

		undoManager.undo();
		assertThat( childB.getValue( "x" ), is( 1 ) );
		assertThat( childB.getValue( "y" ), is( 1 ) );
		assertThat( childB.getValue( "z" ), is( 1 ) );
		assertThat( childC.getValue( "x" ), is( 4 ) );
		assertThat( childC.getValue( "y" ), is( 4 ) );
		assertThat( childC.getValue( "z" ), is( 4 ) );
		assertThat( childD.getValue( "x" ), is( 3 ) );
		assertThat( childD.getValue( "y" ), is( 3 ) );
		assertThat( childD.getValue( "z" ), is( 3 ) );

		undoManager.undo();
		assertThat( childB.getValue( "x" ), is( nullValue() ) );
		assertThat( childB.getValue( "y" ), is( nullValue() ) );
		assertThat( childB.getValue( "z" ), is( nullValue() ) );
		assertThat( childC.getValue( "x" ), is( nullValue() ) );
		assertThat( childC.getValue( "y" ), is( nullValue() ) );
		assertThat( childC.getValue( "z" ), is( nullValue() ) );
		assertThat( childD.getValue( "x" ), is( nullValue() ) );
		assertThat( childD.getValue( "y" ), is( nullValue() ) );
		assertThat( childD.getValue( "z" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testSetChildValue() {
		assertThat( child.getValue( "c" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
		child.setValue( "c", 0 );
		assertThat( child.getValue( "c" ), is( 0 ) );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testUndoChildValue() {
		testSetChildValue();
		undoManager.undo();
		assertThat( child.getValue( "c" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testRedoChildValue() {
		testUndoChildValue();
		undoManager.redo();
		assertThat( child.getValue( "c" ), is( 0 ) );
		assertFalse( undoManager.isRedoAvailable() );
		assertTrue( undoManager.isUndoAvailable() );
	}

	@Test
	void testSetSetValue() {
		assertThat( node.getNodes().size(), is( 0 ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		node.addNode( setNode1 );
		assertTrue( node.isModified() );
		assertThat( node.getNodes().size(), is( 1 ) );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testUndoSetValue() {
		testSetSetValue();
		undoManager.undo();
		assertFalse( node.isModified() );
		assertThat( node.getNodes().size(), is( 0 ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testRedoSetValue() {
		testUndoSetValue();
		undoManager.redo();
		assertTrue( node.isModified() );
		assertThat( node.getNodes().size(), is( 1 ) );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testSetRemoveSetValue() {
		assertThat( node.getNodes().size(), is( 0 ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		node.addNode( setNode1 );
		node.removeNode( setNode1 );
		assertThat( node.getNodes().size(), is( 0 ) );
		assertFalse( node.isModified() );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testUndoSetRemoveSetValue() {
		testSetRemoveSetValue();
		undoManager.undo();
		assertThat( node.getNodes().size(), is( 1 ) );
		assertTrue( node.isModified() );
		assertTrue( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testRedoSetRemoveSetValue() {
		testUndoSetRemoveSetValue();
		undoManager.redo();
		assertThat( node.getNodes().size(), is( 0 ) );
		assertFalse( node.isModified() );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testSetRemoveMultipleSetValues() {
		assertThat( node.getNodes().size(), is( 0 ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		node.addNode( setNode1 );
		node.addNode( setNode2 );
		node.removeNode( setNode1 );
		node.setModified( false );
		assertThat( node.getNodes().size(), is( 1 ) );
		assertFalse( node.isModified() );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

	@Test
	void testUndoSetRemoveMultipleSetValues() {
		testSetRemoveMultipleSetValues();
		undoManager.undo();
		assertThat( node.getNodes().size(), is( 2 ) );
		assertTrue( node.isModified() );
		assertTrue( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testRedoSetRemoveMultipleSetValues() {
		testUndoSetRemoveMultipleSetValues();
		undoManager.redo();
		assertThat( node.getNodes().size(), is( 1 ) );
		assertFalse( node.isModified() );
		assertTrue( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );
	}

}
