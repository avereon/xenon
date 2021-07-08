package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnEvent;
import com.avereon.transaction.TxnException;
import lombok.CustomLog;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@CustomLog
public class DataNodeUndo {

	private static final String UNDO_CHANGES = DataNodeUndo.class.getName() + ":undo-changes";

	public static UndoManager<List<NodeChange>> manager( Node node ) {
		return UndoManagerFactory.unlimitedHistoryMultiChangeUM( events( node ), DataNodeUndo::invert, DataNodeUndo::apply );
	}

	@SuppressWarnings( "SynchronizationOnLocalVariableOrMethodParameter" )
	public static EventStream<List<NodeChange>> events( Node node ) {
		EventSource<List<NodeChange>> events = new EventSource<>();

		node.setValue( UNDO_CHANGES, new LinkedList<>() );

		node.register( NodeEvent.VALUE_CHANGED, e -> {
			Node eventNode = e.getNode();
			String eventKey = e.getKey();
			boolean isModifying = eventNode.isModifyingKey( eventKey );
			boolean isCaptureUndoChanges = node.getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );

			if( isModifying && isCaptureUndoChanges ) {
				LinkedList<NodeChange> changes = node.getValue( UNDO_CHANGES );
				synchronized( changes ) {
					changes.add( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) );
				}
			}
		} );

		node.register( TxnEvent.COMMIT_END, e -> {
			LinkedList<NodeChange> changes = node.getValue( UNDO_CHANGES );
			synchronized( changes ) {
				events.push( new ArrayList<>( changes ) );
				changes.clear();
			}
		} );

		return events;
	}

	public static NodeChange invert( NodeChange change ) {
		return new NodeChange( change.getNode(), change.getKey(), change.getNewValue(), change.getOldValue() );
	}

	public static void apply( List<NodeChange> changes ) {
		try( Txn ignore = Txn.create() ) {
			//changes.forEach( c -> System.out.println( "apply change=" + c ));
			changes.forEach( c -> c.getNode().setValue( c.getKey(), c.getNewValue() ) );
		} catch( TxnException exception ) {
			log.atWarning().withCause( exception ).log( "Unable to apply node changes" );
		}
	}

}
