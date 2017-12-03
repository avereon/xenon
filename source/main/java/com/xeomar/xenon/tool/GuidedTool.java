package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GuidedTool extends AbstractTool {

	private static final Logger log = LoggerFactory.getLogger( GuidedTool.class );

	private GuideListener guideListener = new GuideListener();

	public GuidedTool( Product product, Resource resource ) {
		super( product, resource );
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		((Guide)getResource().getResource( Guide.GUIDE_KEY )).setActive( true );
	}

	@Override
	protected void conceal() throws ToolException {
		super.conceal();
		((Guide)getResource().getResource( Guide.GUIDE_KEY )).setActive( false );
	}

	@Override
	protected void deallocate() throws ToolException {
		super.deallocate();
		((Guide)getResource().getResource( Guide.GUIDE_KEY )).selectedItemProperty().removeListener( guideListener );
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		super.resourceReady( parameters );
		((Guide)getResource().getResource( Guide.GUIDE_KEY )).selectedItemProperty().addListener( guideListener );
	}

	protected abstract void guideNodeChanged( GuideNode oldNode, GuideNode newNode );

	private class GuideListener implements ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldItem, TreeItem<GuideNode> newItem ) {
			guideNodeChanged( oldItem == null ? null : oldItem.getValue(), newItem == null ? null : newItem.getValue() );
		}

	}

}
