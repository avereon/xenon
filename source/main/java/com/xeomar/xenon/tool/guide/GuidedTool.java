package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public abstract class GuidedTool extends ProgramTool {

	private static final String GUIDE_NODE_ID = "guide-node-id";

	private GuideListener guideListener = new GuideListener();

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
		// FIXME Should watch the selection model
		getGuide().selectedItemProperty().removeListener( guideListener );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getGuide().selectedItemProperty().addListener( guideListener );
	}

	@Override
	public void setSettings( Settings settings ) {
		super.setSettings( settings );

		if( this.settings != null ) this.settings = settings;

		Platform.runLater( () -> getGuide().setSelected( settings.get( GUIDE_NODE_ID ) ) );
	}

	protected Guide getGuide() {
		return (Guide)getResource().getResource( Guide.GUIDE_KEY );
	}

	/**
	 * Implemented by
	 * @param oldNode
	 * @param newNode
	 */
	protected abstract void guideNodeChanged( GuideNode oldNode, GuideNode newNode );

	private class GuideListener implements ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldItem, TreeItem<GuideNode> newItem ) {
			GuideNode oldNode = oldItem == null ? null : oldItem.getValue();
			GuideNode newNode = newItem == null ? null : newItem.getValue();
			guideNodeChanged( oldNode, newNode );
			getSettings().set( GUIDE_NODE_ID, newNode == null ? null : newNode.getId() );
		}

	}

}
