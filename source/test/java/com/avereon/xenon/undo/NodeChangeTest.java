package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.transaction.Txn;
import org.fxmisc.undo.UndoManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeChangeTest {

	private MockNode node;

	private MockNode child;

	private MockNode setNode1;

	private MockNode setNode2;

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

		// Enable undo change capture
		this.node.setValue( NodeChange.CAPTURE_UNDO_CHANGES, true );
		undoManager = DataNodeUndo.manager( node );
	}

	@Test
	void testSetValue() {
		assertThat( node.<Object> getValue( "a" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		node.setValue( "a", 0 );
		assertThat( node.<Integer> getValue( "a" ) ).isEqualTo( 0 );
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testUndo() {
		testSetValue();
		undoManager.undo();
		assertThat( node.<Object> getValue( "a" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testRedo() {
		testUndo();
		undoManager.redo();
		assertThat( node.<Integer> getValue( "a" ) ).isEqualTo( 0 );
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
	}

	@Test
	void testMultipleUndo() throws Exception {
		assertThat( node.<Object> getValue( "a" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

		try( Txn ignore = Txn.create() ) {
			node.setValue( "a", 0 );
			node.setValue( "a", 1 );
			node.setValue( "a", 2 );
			node.setValue( "a", 3 );
		}
		node.setValue( "a", 4 );
		assertThat( node.<Integer> getValue( "a" ) ).isEqualTo( 4 );

		undoManager.undo();
		assertThat( node.<Integer> getValue( "a" ) ).isEqualTo( 3 );

		undoManager.undo();
		assertThat( node.<Object> getValue( "a" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testMultipleUndoWithChildNode() throws Exception {
		assertThat( child.<Object> getValue( "c" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

		try( Txn ignore = Txn.create() ) {
			child.setValue( "c", 0 );
			child.setValue( "c", 1 );
			child.setValue( "c", 2 );
			child.setValue( "c", 3 );
		}
		child.setValue( "c", 4 );
		assertThat( child.<Integer> getValue( "c" ) ).isEqualTo( 4 );

		undoManager.undo();
		assertThat( child.<Integer> getValue( "c" ) ).isEqualTo( 3 );

		undoManager.undo();
		assertThat( child.<Object> getValue( "c" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
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

		assertThat( childB.<Object> getValue( "x" ) ).isNull();
		assertThat( childB.<Object> getValue( "y" ) ).isNull();
		assertThat( childB.<Object> getValue( "z" ) ).isNull();
		assertThat( childC.<Object> getValue( "x" ) ).isNull();
		assertThat( childC.<Object> getValue( "y" ) ).isNull();
		assertThat( childC.<Object> getValue( "z" ) ).isNull();
		assertThat( childD.<Object> getValue( "x" ) ).isNull();
		assertThat( childD.<Object> getValue( "y" ) ).isNull();
		assertThat( childD.<Object> getValue( "z" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

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

		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

		assertThat( childB.<Integer> getValue( "x" ) ).isEqualTo( 11 );
		assertThat( childB.<Integer> getValue( "y" ) ).isEqualTo( 11 );
		assertThat( childB.<Integer> getValue( "z" ) ).isEqualTo( 11 );
		assertThat( childC.<Integer> getValue( "x" ) ).isEqualTo( 4 );
		assertThat( childC.<Integer> getValue( "y" ) ).isEqualTo( 4 );
		assertThat( childC.<Integer> getValue( "z" ) ).isEqualTo( 4 );
		assertThat( childD.<Integer> getValue( "x" ) ).isEqualTo( 14 );
		assertThat( childD.<Integer> getValue( "y" ) ).isEqualTo( 14 );
		assertThat( childD.<Integer> getValue( "z" ) ).isEqualTo( 14 );

		undoManager.undo();
		assertThat( childB.<Integer> getValue( "x" ) ).isEqualTo( 1 );
		assertThat( childB.<Integer> getValue( "y" ) ).isEqualTo( 1 );
		assertThat( childB.<Integer> getValue( "z" ) ).isEqualTo( 1 );
		assertThat( childC.<Integer> getValue( "x" ) ).isEqualTo( 4 );
		assertThat( childC.<Integer> getValue( "y" ) ).isEqualTo( 4 );
		assertThat( childC.<Integer> getValue( "z" ) ).isEqualTo( 4 );
		assertThat( childD.<Integer> getValue( "x" ) ).isEqualTo( 3 );
		assertThat( childD.<Integer> getValue( "y" ) ).isEqualTo( 3 );
		assertThat( childD.<Integer> getValue( "z" ) ).isEqualTo( 3 );

		undoManager.undo();
		assertThat( childB.<Object> getValue( "x" ) ).isNull();
		assertThat( childB.<Object> getValue( "y" ) ).isNull();
		assertThat( childB.<Object> getValue( "z" ) ).isNull();
		assertThat( childC.<Object> getValue( "x" ) ).isNull();
		assertThat( childC.<Object> getValue( "y" ) ).isNull();
		assertThat( childC.<Object> getValue( "z" ) ).isNull();
		assertThat( childD.<Object> getValue( "x" ) ).isNull();
		assertThat( childD.<Object> getValue( "y" ) ).isNull();
		assertThat( childD.<Object> getValue( "z" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testSetChildValue() {
		assertThat( child.<Object> getValue( "c" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		child.setValue( "c", 0 );
		assertThat( child.<Integer> getValue( "c" ) ).isEqualTo( 0 );
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testUndoChildValue() {
		testSetChildValue();
		undoManager.undo();
		assertThat( child.<Object> getValue( "c" ) ).isNull();
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testRedoChildValue() {
		testUndoChildValue();
		undoManager.redo();
		assertThat( child.<Integer> getValue( "c" ) ).isEqualTo( 0 );
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
	}

	@Test
	void testSetSetValue() {
		assertThat( node.getNodes().size() ).isEqualTo( 0 );
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

		node.addNode( setNode1 );
		assertThat( node.isModified() ).isTrue();
		assertThat( node.getNodes().size() ).isEqualTo( 1 );
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testUndoSetValue() {
		testSetSetValue();
		undoManager.undo();
		assertThat( node.isModified() ).isFalse();
		assertThat( node.getNodes().size() ).isEqualTo( 0 );
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testRedoSetValue() {
		testUndoSetValue();
		undoManager.redo();
		assertThat( node.isModified() ).isTrue();
		assertThat( node.getNodes().size() ).isEqualTo( 1 );
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testSetRemoveSetValue() {
		assertThat( node.getNodes().size() ).isEqualTo( 0 );
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

		node.addNode( setNode1 );
		node.removeNode( setNode1 );
		assertThat( node.getNodes().size() ).isEqualTo( 0 );
		assertThat( node.isModified() ).isFalse();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testUndoSetRemoveSetValue() {
		testSetRemoveSetValue();
		undoManager.undo();
		assertThat( node.getNodes().size() ).isEqualTo( 1 );
		assertThat( node.isModified() ).isTrue();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testRedoSetRemoveSetValue() {
		testUndoSetRemoveSetValue();
		undoManager.redo();
		assertThat( node.getNodes().size() ).isEqualTo( 0 );
		assertThat( node.isModified() ).isFalse();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testSetRemoveMultipleSetValues() {
		assertThat( node.getNodes().size() ).isEqualTo( 0 );
		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();

		node.addNode( setNode1 );
		node.addNode( setNode2 );
		node.removeNode( setNode1 );
		node.setModified( false );
		assertThat( node.getNodes().size() ).isEqualTo( 1 );
		assertThat( node.isModified() ).isFalse();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

	@Test
	void testUndoSetRemoveMultipleSetValues() {
		testSetRemoveMultipleSetValues();
		undoManager.undo();
		assertThat( node.getNodes().size() ).isEqualTo( 2 );
		assertThat( node.isModified() ).isTrue();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
	}

	@Test
	void testRedoSetRemoveMultipleSetValues() {
		testUndoSetRemoveMultipleSetValues();
		undoManager.redo();
		assertThat( node.getNodes().size() ).isEqualTo( 1 );
		assertThat( node.isModified() ).isFalse();
		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
	}

}
