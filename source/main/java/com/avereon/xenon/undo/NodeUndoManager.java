package com.avereon.xenon.undo;

import com.avereon.data.Node;
import javafx.beans.value.ObservableBooleanValue;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.reactfx.value.Val;

import java.util.List;

public class NodeUndoManager implements UndoManager<List<NodeChange>> {

	private final Node node;

	private final UndoManager<List<NodeChange>> manager;

	public NodeUndoManager( Node node ) {
		this.node = node;
		this.manager = UndoManagerFactory.unlimitedHistoryMultiChangeUM( DataNodeUndo2.events( node ), DataNodeUndo2::invert, DataNodeUndo2::apply );
	}

	@Override
	public boolean undo() {return manager.undo();}

	@Override
	public boolean redo() {return manager.redo();}

	@Override
	public Val<Boolean> undoAvailableProperty() {return manager.undoAvailableProperty();}

	@Override
	public boolean isUndoAvailable() {return manager.isUndoAvailable();}

	@Override
	public Val<List<NodeChange>> nextUndoProperty() {return manager.nextUndoProperty();}

	@Override
	public List<NodeChange> getNextUndo() {return manager.getNextUndo();}

	@Override
	public Val<List<NodeChange>> nextRedoProperty() {return manager.nextRedoProperty();}

	@Override
	public List<NodeChange> getNextRedo() {return manager.getNextRedo();}

	@Override
	public Val<Boolean> redoAvailableProperty() {return manager.redoAvailableProperty();}

	@Override
	public boolean isRedoAvailable() {return manager.isRedoAvailable();}

	@Override
	public ObservableBooleanValue performingActionProperty() {return manager.performingActionProperty();}

	@Override
	public boolean isPerformingAction() {return manager.isPerformingAction();}

	@Override
	public void preventMerge() {manager.preventMerge();}

	@Override
	public void forgetHistory() {manager.forgetHistory();}

	@Override
	public UndoPosition getCurrentPosition() {return manager.getCurrentPosition();}

	@Override
	public void mark() {manager.mark();}

	@Override
	public ObservableBooleanValue atMarkedPositionProperty() {return manager.atMarkedPositionProperty();}

	@Override
	public boolean isAtMarkedPosition() {return manager.isAtMarkedPosition();}

	@Override
	public void close() {manager.close();}
}
