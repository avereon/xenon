package com.avereon.xenon.tool.guide;

import com.avereon.xenon.OpenToolRequestParameters;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramGuideType;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workarea.ToolException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GuidedTool extends ProgramTool {

	protected static final String GUIDE_SELECTED_IDS = "guide-selected-ids";

	protected static final String GUIDE_EXPANDED_IDS = "guide-expanded-ids";

	private static final Guide EMPTY_GUIDE = new Guide();

	private GuideExpandedNodesListener guideExpandedNodesListener = new GuideExpandedNodesListener();

	private GuideSelectedNodesListener guideSelectedNodesListener = new GuideSelectedNodesListener();

	public GuidedTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
	}

	@Override
	public Set<URI> getResourceDependencies() {
		Set<URI> resources = new HashSet<>();
		resources.add( ProgramGuideType.URI );
		return resources;
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		// Set the expanded ids before setting the selected ids
		Platform.runLater( () -> {
			getGuide().setExpandedIds( getSettings().get( GUIDE_EXPANDED_IDS, "" ).split( "," ) );
			getGuide().setSelectedIds( getSettings().get( GUIDE_SELECTED_IDS, "" ).split( "," ) );
		} );
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		getGuide().setActive( true );
	}

	@Override
	protected void conceal() throws ToolException {
		getGuide().setActive( false );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		getGuide().expandedItemsProperty().removeListener( guideExpandedNodesListener );
		getGuide().selectedItemsProperty().removeListener( guideSelectedNodesListener );
		super.deallocate();
	}

	@Override
	protected void resourceReady( OpenToolRequestParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getGuide().expandedItemsProperty().addListener( guideExpandedNodesListener );
		getGuide().selectedItemsProperty().addListener( guideSelectedNodesListener );
	}

	protected Guide getGuide() {
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		return guide == null ? EMPTY_GUIDE : guide;
	}

	protected void guideNodesExpanded( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	private class GuideExpandedNodesListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed(
			ObservableValue<? extends Set<TreeItem<GuideNode>>> observable,
			Set<TreeItem<GuideNode>> oldValue,
			Set<TreeItem<GuideNode>> newValue
		) {
			Set<GuideNode> oldNodes = oldValue.stream().map( TreeItem::getValue ).collect( Collectors.toSet() );
			Set<GuideNode> newNodes = newValue.stream().map( TreeItem::getValue ).collect( Collectors.toSet() );

			// If old and new are different, notify
			if( !oldNodes.equals( newNodes ) ) {
				guideNodesExpanded( oldNodes, newNodes );
				getSettings().set( GUIDE_EXPANDED_IDS, Guide.nodesToString( newNodes ) );
			}
		}

	}

	private class GuideSelectedNodesListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed(
			ObservableValue<? extends Set<TreeItem<GuideNode>>> observable,
			Set<TreeItem<GuideNode>> oldValue,
			Set<TreeItem<GuideNode>> newValue
		) {
			Set<GuideNode> oldNodes = oldValue.stream().map( TreeItem::getValue ).collect( Collectors.toSet() );
			Set<GuideNode> newNodes = newValue.stream().map( TreeItem::getValue ).collect( Collectors.toSet() );

			// If old and new are different, notify
			if( !oldNodes.equals( newNodes ) ) {
				guideNodesSelected( oldNodes, newNodes );
				getSettings().set( GUIDE_SELECTED_IDS, Guide.nodesToString( newNodes ) );
			}
		}

	}

}
