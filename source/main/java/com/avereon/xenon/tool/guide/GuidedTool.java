package com.avereon.xenon.tool.guide;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.zerra.javafx.Fx;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import lombok.CustomLog;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public abstract class GuidedTool extends ProgramTool {

	protected static final String GUIDE_SELECTED_IDS = "guide-selected-ids";

	protected static final String GUIDE_EXPANDED_IDS = "guide-expanded-ids";

	private final GuideContext guideContext;

	private final GuideExpandedNodesListener guideExpandedNodesListener;

	private final GuideSelectedNodesListener guideSelectedNodesListener;

	public GuidedTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		guideContext = new GuideContext( this );
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

		guideContext.expandedItemsProperty().addListener( guideExpandedNodesListener );
		guideContext.selectedItemsProperty().addListener( guideSelectedNodesListener );
		guideContext.focusedProperty().addListener( ( p, o, n ) -> doGuideFocused( n ) );

		Fx.run( () -> {
			// Set the expanded ids before setting the selected ids
			guideContext.setExpandedIds( Arrays.stream( getSettings().get( GUIDE_EXPANDED_IDS, "" ).split( "," ) ).collect( Collectors.toSet() ) );
			guideContext.setSelectedIds( Arrays.stream( getSettings().get( GUIDE_SELECTED_IDS, "" ).split( "," ) ).collect( Collectors.toSet() ) );
		} );
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		getCurrentGuide().setActive( true );
	}

	@Override
	protected void conceal() throws ToolException {
		getCurrentGuide().setActive( false );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		guideContext.expandedItemsProperty().removeListener( guideExpandedNodesListener );
		guideContext.selectedItemsProperty().removeListener( guideSelectedNodesListener );

		super.deallocate();
	}

	/**
	 * Get the guide context for the tool. The guide context is used to manage the
	 * tool guides, pass events between this tool and the {@link GuideTool} and
	 * pass events between the {@link GuideTool} and this tool.
	 *
	 * @return The guide context
	 */
	protected GuideContext getGuideContext() {
		return guideContext;
	}

	/**
	 * Convenience method for quick access to the current guide.
	 *
	 * @return The current guide
	 */
	public Guide getCurrentGuide() {
		return guideContext.getCurrentGuide();
	}

	protected void guideNodesExpanded( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {}

	protected void guideFocusChanged( boolean focused, Set<GuideNode> nodes ) {}

	private void doGuideFocused( boolean focused ) {
		guideFocusChanged( focused, guideContext.selectedItemsProperty().get().stream().map( TreeItem::getValue ).collect( Collectors.toSet() ) );
	}

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
				//Fx.run( () -> getWorkpane().setActiveTool( GuidedTool.this ) );
			}
		}

	}

}
