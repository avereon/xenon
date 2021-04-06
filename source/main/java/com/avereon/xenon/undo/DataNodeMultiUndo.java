package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnException;
import com.avereon.util.Log;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.List;
import java.util.Optional;

public class DataNodeMultiUndo {

	private static final System.Logger log = Log.get();

	public static EventStream<List<NodeChange>> events( Node node ) {
		EventSource<List<NodeChange>> source = new EventSource<>();
		node.register( NodeEvent.VALUE_CHANGED, e -> {
			Node eventNode = e.getNode();
			String eventKey = e.getKey();
			boolean isModifying = eventNode.isModifyingKey( eventKey );
			boolean isCaptureUndoChanges = node.getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );
			if( isModifying && isCaptureUndoChanges ) source.push( List.of( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) ) );
		} );
		return source;
	}

	public static NodeChange invert( NodeChange change ) {
		return new NodeChange( change.getNode(), change.getKey(), change.getNewValue(), change.getOldValue() );
	}

	public static void apply( List<NodeChange> changes ) {
		try( Txn ignore = Txn.create() ) {
			changes.forEach( c -> {
				System.out.println( "undo=" + c.getKey() + " old=" + c.getOldValue() + " new=" + c.getNewValue() );

				c.getNode().setValue( c.getKey(), c.getNewValue() );
				c.getNode().setValue( NodeChange.CAPTURE_REDO_CHANGES, c.isRedo() ? false : null );
			} );
		} catch( TxnException exception ) {
			log.log( Log.WARN, "Unable to apply node changes" );
		}
	}

	public static Optional<NodeChange> merge( NodeChange a, NodeChange b ) {
		if( a == null || b == null ) return Optional.empty();

		return Optional.empty();
	}

}
