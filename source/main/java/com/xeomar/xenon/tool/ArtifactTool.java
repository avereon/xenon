package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		log.debug( "Artifact tool resource ready" );
		super.resourceReady( parameters );
		System.out.println( "Artifact tool fragment: " + parameters.getFragment() );
		resourceRefreshed();
	}

	@Override
	public void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
		Program program = (Program)getProduct();
		log.error( "posted update count: " + program.getUpdateManager().getPostedUpdates().size() );
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

	private class ArtifactPanel extends Pane {

		private Label title;

		public ArtifactPanel( Product product, String page ) {
			setId( "tool-artifact-panel" );

			title = new Label( product.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + page ) );
			title.setId( "tool-artifact-page-header" );

			getChildren().addAll( title );
		}

	}

}
