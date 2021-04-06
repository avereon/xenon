package com.avereon.xenon.undo;

import com.avereon.data.Node;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataNodeMultiUndoTest {

	private Node node;

	private UndoManager<List<NodeChange>> undoManager;

	@BeforeEach
	void setup() {
		this.node = new Node();
		this.node.addModifyingKeys( "a" );
		this.node.setValue( NodeChange.CAPTURE_UNDO_CHANGES, true );

		this.undoManager = UndoManagerFactory.unlimitedHistoryMultiChangeUM( DataNodeMultiUndo.events( node ), DataNodeMultiUndo::invert, DataNodeMultiUndo::apply, DataNodeMultiUndo::merge );
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
		undoManager.preventMerge();
		assertThat( node.getValue( "a" ), is( 4 ) );

		undoManager.undo();
		assertThat( node.getValue( "a" ), is( 3 ) );

		undoManager.undo();
		undoManager.undo();
		undoManager.undo();
		undoManager.undo();
		assertThat( node.getValue( "a" ), is( nullValue() ) );
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

}
