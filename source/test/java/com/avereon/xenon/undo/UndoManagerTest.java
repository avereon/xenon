package com.avereon.xenon.undo;

import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactfx.EventSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UndoManagerTest {

	private EventSource<MockChange> changes;

	private UndoManager<MockChange> undoManager;

	@BeforeEach
	void setup() {
		changes = new EventSource<>();
		undoManager = UndoManagerFactory.unlimitedHistorySingleChangeUM( changes, MockChange::invert, MockChange::apply );
	}

	@Test
	void testPush() {
		assertFalse( undoManager.isRedoAvailable() );
		assertFalse( undoManager.isUndoAvailable() );
		changes.push( new MockChange() );
		assertFalse( undoManager.isRedoAvailable() );
		assertTrue( undoManager.isUndoAvailable() );
	}

	@Test
	void testUndo() {
		testPush();
		undoManager.undo();
		assertFalse( undoManager.isUndoAvailable() );
		assertTrue( undoManager.isRedoAvailable() );
	}

	@Test
	void testRedo() {
		testUndo();
		undoManager.redo();
		assertFalse( undoManager.isRedoAvailable() );
		assertTrue( undoManager.isUndoAvailable() );
	}

	public class MockChange {

		public MockChange invert() {
			return new MockChange();
		}

		public void apply() {
			// For testing purposes, push the change into the change stream when it
			// happens. Normally this would occur naturally because the apply method
			// would cause a change that would end up in the undo manger, but here we
			// have to fake it.
			changes.push( this );
		}

	}

}
