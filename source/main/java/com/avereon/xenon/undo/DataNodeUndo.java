package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnException;
import com.avereon.util.Log;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.*;
import java.util.stream.Collectors;

public class DataNodeUndo {

	private static final System.Logger log = Log.get();

	public static EventStream<NodeChange> events( Node node ) {
		EventSource<NodeChange> source = new EventSource<>();
		node.register( NodeEvent.VALUE_CHANGED, e -> {
			if( !e.isUndoable() ) return;

			Node eventNode = e.getNode();
			String eventKey = e.getKey();

			synchronized( eventNode ) {
				boolean isModifying = eventNode.isModifyingKey( eventKey );
				boolean isCaptureUndoChanges = node.getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );
				//boolean isCaptureRedoChanges = node.getValue( NodeChange.CAPTURE_REDO_CHANGES, false );
				if( isModifying && isCaptureUndoChanges ) {
					source.push( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) );
				}
				//System.out.println( "change=" + eventKey + " old=" + e.getOldValue() + " new=" + e.getNewValue() );
			}
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
			change.getChanges().forEach( c -> {
				//System.out.println( "undo=" + c.getKey() + " old=" + c.getOldValue() + " new=" + c.getNewValue() );

				c.getNode().setValue( c.getKey(), c.getNewValue(), true );
				//c.getNode().setValue( NodeChange.CAPTURE_REDO_CHANGES, c.isRedo() ? false : null );
			} );
		} catch( TxnException exception ) {
			log.log( Log.WARN, "Unable to apply node changes" );
		}
	}

	public static Optional<NodeChange> merge( NodeChange a, NodeChange b ) {
		if( a == null || b == null ) return Optional.empty();

		List<NodeChange> changes = new ArrayList<>( a.getChanges() );
		b.getChanges().forEach( c -> {
			Node n = c.getNode();
			String k = c.getKey();

			int index = 0;
			boolean found = false;
			for( NodeChange v : changes ) {
				if( Objects.equals( v.getNode(), n ) && Objects.equals( v.getKey(), k ) ) {
					changes.remove( index );
					changes.add( index, new NodeChange( n, k, v.getOldValue(), c.getNewValue() ) );
					found = true;
					index++;
				}
			}
			if( !found ) changes.add( c );
		} );

		System.out.println();
		changes.forEach( c -> System.out.println( "merged " + c.getKey() + " = " + c.getOldValue() + "->" + c.getNewValue() ) );

		return Optional.of( new NodeChange( changes ) );
	}
}
