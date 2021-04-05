package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnException;
import com.avereon.util.Log;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataNodeUndo {

	private static final System.Logger log = Log.get();

	public static EventStream<NodeChange> events( Node node ) {
		EventSource<NodeChange> source = new EventSource<>();
		node.register( NodeEvent.VALUE_CHANGED, e -> {
			Node eventNode = e.getNode();
			String eventKey = e.getKey();
			boolean isModifying = eventNode.isModifyingKey( eventKey );
			boolean isCaptureUndoChanges = node.getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );
			if( isModifying && isCaptureUndoChanges ) source.push( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) );
		} );
		return source;
	}

	public static NodeChange invert( NodeChange change ) {
		List<NodeChange> changes = change.getChanges().stream().map( c -> new NodeChange( c.getNode(), c.getKey(), c.getNewValue(), c.getOldValue() ) ).collect( Collectors.toList() );
		Collections.reverse( changes );
		return new NodeChange( changes );
	}

	public static void apply( NodeChange change ) {
		try( Txn ignore = Txn.create() ) {
			change.getChanges().forEach( c -> c.getNode().setValue( c.getKey(), c.getNewValue() ) );
		} catch( TxnException exception ) {
			log.log( Log.WARN, "Unable to apply node changes" );
		}
	}

	public static Optional<NodeChange> merge( NodeChange a, NodeChange b ) {
		if( a == null || b == null ) return Optional.empty();

		List<NodeChange> changes = new ArrayList<>();
		changes.addAll( a.getChanges() );
		changes.addAll( b.getChanges() );

		return Optional.of( new NodeChange( changes ) );
	}
}
