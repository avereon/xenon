package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GuidedTool extends ProgramTool {

	private static final String GUIDE_NODE_ID = "guide-node-id";

	private static final String GUIDE_SELECTED_IDS = "guide-selected-ids";

	private static final String GUIDE_EXPANDED_IDS = "guide-expanded-ids";

	//private GuideListener guideListener = new GuideListener();

	private GuideSelectionListener guideSelectionListener = new GuideSelectionListener();

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
		getGuide().selectedItemsProperty().removeListener( guideSelectionListener );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getGuide().selectedItemsProperty().addListener( guideSelectionListener );
	}

	@Override
	public void setSettings( Settings settings ) {
		super.setSettings( settings );

		if( this.settings != null ) this.settings = settings;

		//Platform.runLater( () -> getGuide().setSelected( settings.get( GUIDE_NODE_ID ) ) );
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

	protected void guideNodesChanged( List<GuideNode> oldNodes, List<GuideNode> newNodes ) {}

	private class GuideSelectionListener implements ListChangeListener<TreeItem<GuideNode>> {

		@Override
		public void onChanged( Change<? extends TreeItem<GuideNode>> change ) {
			List<GuideNode> oldNodes = new ArrayList<>();
			List<GuideNode> newNodes = new ArrayList<>();

			while( change.next() ) {
				boolean added = change.wasAdded();
				boolean removed = change.wasRemoved();
				boolean updated = change.wasUpdated();
				boolean permutated = change.wasPermutated();
				boolean replaced = change.wasReplaced();
				System.out.println( "Change u=" + updated + " a=" + added + " r=" + removed + " p=" + permutated + " c=" + replaced );

				if( change.wasRemoved() ) {
					List<? extends TreeItem<GuideNode>> oldItems = change.getRemoved();
					for( TreeItem<GuideNode> item : oldItems ) {
						oldNodes.add( item.getValue() );
					}
				}

				if( change.wasAdded() ) {
					List<? extends TreeItem<GuideNode>> newItems = change.getAddedSubList();
					for( TreeItem<GuideNode> item : newItems ) {
						newNodes.add( item.getValue() );
					}
				}

			}

			System.out.println( "Old selected nodes: " + nodesToString( oldNodes ) );
			System.out.println( "New selected nodes: " + nodesToString( newNodes ) );

			guideNodesChanged( oldNodes, newNodes );

			//			if( newNodes.size() == 0 ) {
			//				getSettings().set( GUIDE_NODE_ID, null );
			//				System.out.println( "Guide nodes cleared" );
			//			} else {
			//				getSettings().set( GUIDE_NODE_ID, nodesToString( newNodes ) );
			//				guideNodesSelected( newNodes );
			//			}
		}

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

}
