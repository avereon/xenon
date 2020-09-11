package com.avereon.xenon.tool.guide;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.zerra.javafx.Fx;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import java.lang.System.Logger;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GuidedTool extends ProgramTool {

	private static final Logger log = Log.get();

	protected static final String GUIDE_SELECTED_IDS = "guide-selected-ids";

	protected static final String GUIDE_EXPANDED_IDS = "guide-expanded-ids";

	private final GuideExpandedNodesListener guideExpandedNodesListener;

	private final GuideSelectedNodesListener guideSelectedNodesListener;

	public GuidedTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		guideExpandedNodesListener = new GuideExpandedNodesListener();
		guideSelectedNodesListener = new GuideSelectedNodesListener();
	}

	@Override
	public Set<URI> getAssetDependencies() {
		return Set.of( ProgramGuideType.URI );
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();

		getGuide().expandedItemsProperty().addListener( guideExpandedNodesListener );
		getGuide().selectedItemsProperty().addListener( guideSelectedNodesListener );

		// Set the expanded ids before setting the selected ids
		Fx.run( () -> {
			getGuide().setExpandedIds( Arrays.stream( getSettings().get( GUIDE_EXPANDED_IDS, "" ).split( "," ) ).collect( Collectors.toSet() ) );
			getGuide().setSelectedIds( Arrays.stream( getSettings().get( GUIDE_SELECTED_IDS, "" ).split( "," ) ).collect( Collectors.toSet() ) );
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

	/**
	 * This method should be overridden by tool implementations to provide the
	 * guide that is appropriate for the tool.
	 *
	 * @return The tool guide
	 */
	protected Guide getGuide() {
		return Guide.EMPTY;
	}

	protected void guideNodesExpanded( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	private class GuideExpandedNodesListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed(
			ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue
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
			ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue
		) {
			Set<GuideNode> oldNodes = oldValue.stream().map( TreeItem::getValue ).collect( Collectors.toSet() );
			Set<GuideNode> newNodes = newValue.stream().map( TreeItem::getValue ).collect( Collectors.toSet() );

			// If old and new are different, notify
			if( !oldNodes.equals( newNodes ) ) {
				getSettings().set( GUIDE_SELECTED_IDS, Guide.nodesToString( newNodes ) );
				guideNodesSelected( oldNodes, newNodes );

				// Run this later to set the tool to be the active tool again
				Fx.run( () -> getWorkpane().setActiveTool( GuidedTool.this ) );
			}
		}

	}

}
