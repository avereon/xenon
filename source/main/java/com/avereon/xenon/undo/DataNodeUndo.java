package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnEvent;
import com.avereon.transaction.TxnException;
import com.avereon.util.Log;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DataNodeUndo {

	private static final String UNDO_CHANGES = DataNodeUndo.class.getName() + ":undo-changes";

	private static final System.Logger log = Log.get();

	public static UndoManager<List<NodeChange>> manager( Node node ) {
		return UndoManagerFactory.unlimitedHistoryMultiChangeUM( events( node ),
			DataNodeUndo::invert,
			DataNodeUndo::apply,
			DataNodeUndo::merge
		);
	}

	public static EventStream<List<NodeChange>> events( Node node ) {
		EventSource<List<NodeChange>> source = new EventSource<>();

		node.setValue( UNDO_CHANGES, new LinkedList<>() );

		node.register( NodeEvent.VALUE_CHANGED, e -> {
			Node eventNode = e.getNode();
			String eventKey = e.getKey();
			boolean isModifying = eventNode.isModifyingKey( eventKey );
			boolean isCaptureUndoChanges = node.getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );

			synchronized( node ) {
				List<NodeChange> changes = node.getValue( UNDO_CHANGES );
				if( isModifying && isCaptureUndoChanges ) changes.add( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) );
			}
		} );

		node.register( TxnEvent.COMMIT_END, e -> {
			synchronized( node ) {
				List<NodeChange> changes = node.getValue( UNDO_CHANGES );
				source.push( new ArrayList<>( changes ) );
				changes.clear();
			}
		} );

		return source;
	}

	public static NodeChange invert( NodeChange change ) {
		return new NodeChange( change.getNode(), change.getKey(), change.getNewValue(), change.getOldValue() );
	}

	public static void apply( List<NodeChange> changes ) {
		try( Txn ignore = Txn.create() ) {
			changes.forEach( c -> c.getNode().setValue( c.getKey(), c.getNewValue(), false ) );
		} catch( TxnException exception ) {
			log.log( Log.WARN, "Unable to apply node changes" );
		}
	}

	public static Optional<NodeChange> merge( NodeChange a, NodeChange b ) {
		return Optional.empty();
	}

}
