package com.avereon.xenon.undo;

import com.avereon.data.Node;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeChangeTest {

	private Node node;

	private Node child;

	private UndoManager<NodeChange> undoManager;

	@BeforeEach
	void setup() {
		this.node = new Node();
		this.node.addModifyingKeys( "a" );
		this.child = new Node();
		this.child.addModifyingKeys( "c" );
		this.node.setValue( "child", child );

		// Enable undo change capture
		this.node.setValue( NodeChange.CAPTURE_UNDO_CHANGES, true );
		undoManager = UndoManagerFactory.unlimitedHistorySingleChangeUM( DataNodeUndo.events( node ), DataNodeUndo::invert, DataNodeUndo::apply, DataNodeUndo::merge );
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
	void testMerge() {
		assertThat( node.getValue( "a" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertFalse( undoManager.isRedoAvailable() );

		node.setValue( "a", 0 );
		node.setValue( "a", 1 );
		node.setValue( "a", 2 );
		node.setValue( "a", 3 );
		undoManager.preventMerge();
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

}
