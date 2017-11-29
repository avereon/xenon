package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.ToolException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ArtifactTool extends AbstractTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	private Button refresh;

	private Button apply;

	private GuideListener guideListener = new GuideListener();

	public ArtifactTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-artifact" );

		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );
	}

	@Override
	protected void allocate() throws ToolException {
		log.debug( "Artifact tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Artifact tool display" );
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Artifact tool activate" );
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.setActive( true );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Artifact tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Artifact tool conceal" );
		((Guide)getResource().getResource( Guide.GUIDE_KEY )).setActive( false );
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Artifact tool deallocate" );
	}

	@Override
	protected void resourceReady() throws ToolException {
		log.debug( "Artifact tool resource ready" );

		// Register the guide selection listener
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.selectedItemProperty().removeListener( guideListener );
		guide.selectedItemProperty().addListener( guideListener );

		resourceRefreshed();
	}

	@Override
	public void resourceRefreshed() {
	}

	public void setPostedUpdates( Set<ProductCard> postedUpdates ) {
		log.error( "ArtifactTool.setPostedUpdates() called: " + postedUpdates.size() );
	}

	private void selectItem( TreeItem<GuideNode> item ) {
		if( item == null ) return;
		selectPage( item.getValue().getId() );
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

	private class GuideListener implements ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldSelection, TreeItem<GuideNode> newSelection ) {
			selectItem( newSelection );
		}

	}

}
