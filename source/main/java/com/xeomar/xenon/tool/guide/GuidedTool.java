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
import java.util.HashSet;
import java.util.Set;

public abstract class GuidedTool extends ProgramTool {

	private static final String GUIDE_SELECTED_IDS = "guide-selected-ids";

	private static final String GUIDE_EXPANDED_IDS = "guide-expanded-ids";

	private GuideSelectedNodesListener guideSelectedNodesListener = new GuideSelectedNodesListener();

	private Settings settings;

	public GuidedTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
	}

	public Set<URI> getResourceDependencies() {
		Set<URI> resources = new HashSet<>();
		resources.add( ProgramGuideType.URI );
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
		getGuide().selectedItemsProperty().removeListener( guideSelectedNodesListener );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
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

	protected abstract void guideNodesChanged( Set<GuideNode> oldNodes, Set<GuideNode> newNodes );

	private class GuideSelectedNodesListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed( ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue ) {
			Set<GuideNode> oldNodes = new HashSet<>();
			Set<GuideNode> newNodes = new HashSet<>();

			for( TreeItem<GuideNode> node : oldValue ) {
				oldNodes.add( node.getValue() );
			}

			for( TreeItem<GuideNode> node : newValue ) {
				newNodes.add( node.getValue() );
			}

			// If old and new are different, notify
			if( !oldNodes.equals( newNodes ) ) guideNodesChanged( oldNodes, newNodes );
		}

	}

	/**
	 * Get a string of the guide node ids.
	 * <p>
	 * Used for debugging.
	 *
	 * @param nodes The set of nodes
	 * @return A comma delimited string of the node ids
	 */
	@SuppressWarnings( "unused" )
	private String nodesToString( Set<GuideNode> nodes ) {
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
