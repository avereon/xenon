package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GuidedTool extends ProgramTool {

	private static final String GUIDE_SELECTED_IDS = "guide-selected-ids";

	private static final String GUIDE_EXPANDED_IDS = "guide-expanded-ids";

	private GuideSelectedNodeListener guideSelectedNodeListener = new GuideSelectedNodeListener();

	private GuideSelectedNodesListener guideSelectedNodesListener = new GuideSelectedNodesListener();

	private Settings settings;

	public GuidedTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
	}

	public Set<URI> getResourceDependencies() {
		Set<URI> resources = new HashSet<>();
		resources.add( ProgramGuideType.uri );
		return resources;
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		getGuide().setActive( true );
	}

	@Override
	protected void conceal() throws ToolException {
		super.conceal();
		getGuide().setActive( false );
	}

	@Override
	protected void deallocate() throws ToolException {
		super.deallocate();
		getGuide().selectedItemProperty().removeListener( guideSelectedNodeListener );
		getGuide().selectedItemsProperty().removeListener( guideSelectedNodesListener );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getGuide().selectedItemProperty().addListener( guideSelectedNodeListener );
		getGuide().selectedItemsProperty().addListener( guideSelectedNodesListener );
	}

	@Override
	public void setSettings( Settings settings ) {
		super.setSettings( settings );

		if( this.settings != null ) this.settings = settings;

		//Platform.runLater( () -> getGuide().setSelected( settings.get( GUIDE_SELECTED_IDS ) ) );
	}

	protected Guide getGuide() {
		return (Guide)getResource().getResource( Guide.GUIDE_KEY );
	}

	/**
	 * Implemented by
	 *
	 * @param oldNode
	 * @param newNode
	 */
	protected abstract void guideNodeChanged( GuideNode oldNode, GuideNode newNode );

	protected void guideNodesChanged( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	private class GuideSelectedNodeListener implements ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldItem, TreeItem<GuideNode> newItem ) {
			guideNodeChanged( oldItem == null ? null : oldItem.getValue(), newItem == null ? null : newItem.getValue() );
		}

	}

	private class GuideSelectedNodesListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed( ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue ) {

		}

		//		@Override
//		public void onChanged( Change<? extends TreeItem<GuideNode>> change ) {
//			Set<GuideNode> oldNodes = new HashSet<>();
//			Set<GuideNode> newNodes = new HashSet<>();
//
//			//while( change.next() ) {
//			boolean added = change.wasAdded();
//			boolean removed = change.wasRemoved();
//			//				boolean updated = change.wasUpdated();
//			//				boolean permutated = change.wasPermutated();
//			//				boolean replaced = change.wasReplaced();
//			//				System.out.println( "Change u=" + updated + " a=" + added + " r=" + removed + " p=" + permutated + " c=" + replaced );
//
//			//				if( change.wasRemoved() ) {
//			//					List<? extends TreeItem<GuideNode>> oldItems = change.getRemoved();
//			//					for( TreeItem<GuideNode> item : oldItems ) {
//			//						oldNodes.add( item.getValue() );
//			//					}
//			//				}
//			//
//			//				if( change.wasAdded() ) {
//			//					List<? extends TreeItem<GuideNode>> newItems = change.getAddedSubList();
//			//					for( TreeItem<GuideNode> item : newItems ) {
//			//						newNodes.add( item.getValue() );
//			//					}
//			//				}
//
//			//}
//
//			for( TreeItem<? extends GuideNode> item : change.getSet() ) {
//				newNodes.add( item.getValue() );
//			}
//
//			//			oldNodes.add( change.getElementRemoved().getValue() );
//			//			newNodes.add( change.getElementAdded().getValue() );
//			//
//			//			System.out.println( "Old selected nodes: " + nodesToString( oldNodes ) );
//			System.out.println( "a=" + added + "  r=" + removed + "  New selected nodes: " + nodesToString( newNodes ) );
//
//			guideNodesChanged( oldNodes, newNodes );
//
//			if( newNodes.size() == 0 ) {
//				getSettings().set( GUIDE_SELECTED_IDS, null );
//				System.out.println( "Guide nodes cleared" );
//			} else {
//				getSettings().set( GUIDE_SELECTED_IDS, nodesToString( newNodes ) );
//				//guideNodesSelected( newNodes );
//			}
//		}

	}

	private String nodesToString( List<GuideNode> nodes ) {
		if( nodes == null ) return null;
		if( nodes.size() == 0 ) return "";

		StringBuilder builder = new StringBuilder();
		for( GuideNode node : nodes ) {
			builder.append( node.getId() ).append( "," );
		}

		String ids = builder.toString();
		ids = ids.substring( 0, ids.length() - 1 );
		return ids;
	}

	private String nodesToString( Set<GuideNode> nodes ) {
		return nodesToString( new ArrayList<>( nodes ) );
	}

}
