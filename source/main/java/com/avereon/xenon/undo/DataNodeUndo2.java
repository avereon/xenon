package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnException;
import com.avereon.util.Log;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DataNodeUndo2 {

	private static final String UNDO_CHANGES = DataNodeUndo2.class.getName() + ":undo-changes";

	private static final System.Logger log = Log.get();

	public static EventStream<List<NodeChange>> events( Node node ) {
		EventSource<List<NodeChange>> source = new EventSource<>();

		List<NodeChange> changeList = new LinkedList<>();
		//changeList.add( new NodeChange( node, NodeChange.PUBLISH_UNDO_CHANGES, true, null ) );
		node.setValue( UNDO_CHANGES, changeList);

		node.register( NodeEvent.VALUE_CHANGED, e -> {
			Node eventNode = e.getNode();
			String eventKey = e.getKey();
			boolean isModifying = eventNode.isModifyingKey( eventKey );
			boolean isCaptureUndoChanges = node.getValue( NodeChange.CAPTURE_UNDO_CHANGES, NodeChange.DEFAULT_CAPTURE_UNDO_CHANGES );
			boolean isPublish = NodeChange.PUBLISH_UNDO_CHANGES.equals( eventKey );

			// NOTE Mark, this is getting better, but there is still a challenge
			// knowing when a group of changes should go together and also knowing
			// when that same group of changes has gone in reverse during an undo
			// and probably the same scenario for a redo. Maybe an event can be
			// triggered from apply that will help with the undo/redo logic and then
			// I'm left with when to trigger a group of changes.

			synchronized( node ) {
				List<NodeChange> changes = node.getValue( UNDO_CHANGES );
				if( !isPublish && isModifying && isCaptureUndoChanges ) {
					changes.add( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) );
					System.out.println( eventKey + "=" + e.getOldValue() + " -> " + e.getNewValue() );
				}

				if( isPublish && e.getNewValue() != null ) {
					System.out.println( "publish=" + changes );
					source.push( new ArrayList<>( changes ) );
					changes.clear();

					//if( e.getNewValue() != null ) changes.add( new NodeChange( node, NodeChange.PUBLISH_UNDO_CHANGES, e.getNewValue(), null ) );

					// Reset the publish flag
					node.setValue( NodeChange.PUBLISH_UNDO_CHANGES, null );
				}
			}
		} );

		//		node.register( NodeChange.PUBLISH_UNDO_CHANGES, e -> {
		//			if( e.getNewValue() == null ) return;
		//
		//			synchronized( node ) {
		//				List<NodeChange> changes = node.getValue( UNDO_CHANGES );
		//				source.push( new ArrayList<>( changes ) );
		//				changes.clear();
		//
		//				// Reset the publish flag
		//				node.setValue( NodeChange.PUBLISH_UNDO_CHANGES, null );
		//			}
		//		});

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
