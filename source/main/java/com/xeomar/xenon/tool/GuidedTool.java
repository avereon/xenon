package com.xeomar.xenon.tool;

import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public abstract class GuidedTool extends ProgramTool {

	private static final Logger log = LoggerFactory.getLogger( GuidedTool.class );

	private GuideListener guideListener = new GuideListener();

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
		getGuide().selectedItemProperty().removeListener( guideListener );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		getGuide().selectedItemProperty().addListener( guideListener );
	}

	protected Guide getGuide() {
		return (Guide)getResource().getResource( Guide.GUIDE_KEY );
	}

	protected abstract void guideNodeChanged( GuideNode oldNode, GuideNode newNode );

	private class GuideListener implements ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldItem, TreeItem<GuideNode> newItem ) {
			guideNodeChanged( oldItem == null ? null : oldItem.getValue(), newItem == null ? null : newItem.getValue() );
		}

	}

}
