package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.ToolException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ArtifactTool extends GuidedTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	private Button refresh;

	private Button apply;

	public ArtifactTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-artifact" );

		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		log.debug( "Artifact tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Artifact tool display" );
		super.display();
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Artifact tool activate" );
		super.activate();
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Artifact tool deactivate" );
		super.deactivate();
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Artifact tool conceal" );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Artifact tool deallocate" );
		super.deallocate();
	}

	@Override
	protected void resourceReady() throws ToolException {
		log.debug( "Artifact tool resource ready" );
		super.resourceReady();
		resourceRefreshed();
	}

	@Override
	public void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
	}

	public void setPostedUpdates( Set<ProductCard> postedUpdates ) {
		log.error( "ArtifactTool.setPostedUpdates() called: " + postedUpdates.size() );
	}

	@Override
	protected void guideNodeChanged( GuideNode oldNode, GuideNode newNode ) {
		if( newNode != null ) selectPage( newNode.getId() );
	}

	private void selectPage( String page ) {
		log.debug( "Artifact page selected: " + page );
		if( page == null ) return;

		ArtifactPanel panel = new ArtifactPanel( getProduct(), page );
		ScrollPane scroller = new ScrollPane( panel );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

	// TODO Register a listener to listen for updates available events

	private class ArtifactPanel extends Pane {

		private Label title;

		public ArtifactPanel( Product product, String page ) {
			setId( "tool-artifact-panel" );

			title = new Label( product.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + page ) );

			getChildren().addAll( title );
		}

	}

}
