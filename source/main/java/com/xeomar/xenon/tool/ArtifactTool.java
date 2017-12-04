package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Actions;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramArtifactType;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ArtifactTool extends GuidedTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	private static final int ICON_SIZE = 48;

	private Map<String, ArtifactPanel> panes;

	public ArtifactTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-artifact" );
		// FIXME Tool tab does not show icon
		setGraphic( ((Program)product).getIconLibrary().getIcon( "artifact" ) );
		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );

		panes = new HashMap<>();
		panes.put( ProgramArtifactType.INSTALLED, new InstalledPane( (Program)product ) );
		panes.put( ProgramArtifactType.AVAILABLE, new AvailablePane( (Program)product ) );
		panes.put( ProgramArtifactType.UPDATES, new UpdatesPane( (Program)product ) );
		panes.put( ProgramArtifactType.SOURCES, new SourcesPane( (Program)product ) );
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
		resourceRefreshed();
		getGuide().setSelected( parameters.getFragment() );
	}

	@Override
	public void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
		Program program = (Program)getProduct();
	}

	@Override
	protected void guideNodeChanged( GuideNode oldNode, GuideNode newNode ) {
		if( newNode != null ) selectPage( newNode.getId() );
	}

	private void selectPage( String pageId ) {
		log.debug( "Artifact page selected: " + pageId );
		if( pageId == null ) return;

		ScrollPane scroller = new ScrollPane( panes.get( pageId ) );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

	private abstract class ArtifactPanel extends Pane {

		private Label title;

		private Button refreshButton;

		public ArtifactPanel( Program program ) {
			setId( "tool-artifact-panel" );

			title = new Label( "" );
			title.setId( "tool-artifact-page-title" );

			refreshButton = Actions.createButton( program, program.getActionLibrary().getAction( "refresh" ) );
			refreshButton.setId( "tool-artifact-page-refresh" );

			HBox titleBox = new HBox( title, refreshButton );

			getChildren().addAll( titleBox );
		}

		protected void setTitle( String title ) {
			this.title.setText( title );
		}

	}

	private class InstalledPane extends ArtifactPanel {

		InstalledPane( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.INSTALLED ) );
		}

	}

	private class AvailablePane extends ArtifactPanel {

		AvailablePane( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.AVAILABLE ) );
		}

	}

	private class UpdatesPane extends ArtifactPanel {

		UpdatesPane( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.UPDATES ) );
		}

	}

	private class SourcesPane extends ArtifactPanel {

		SourcesPane( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.SOURCES ) );
		}

	}

	private class ProductPane extends Pane {

		private ProductSource source;

		private ProductCard update;

		private Label iconLabel;

		private Label nameLabel;

		public ProductPane( ProductSource source, ProductCard update ) {
			this.source = source;
			this.update = update;

			Program program = (Program)getProduct();

			Node productIcon = program.getIconLibrary().getIcon( source.getCard().getIconUri(), ICON_SIZE );
			if( productIcon == null ) productIcon = program.getIconLibrary().getIcon( "product", ICON_SIZE );

			iconLabel = new Label( null, productIcon );
			nameLabel.setId( "tool-artifact-product-icon" );
			nameLabel = new Label( source.getCard().getName() );
			nameLabel.setId( "tool-artifact-product-name" );

			getChildren().addAll( iconLabel, nameLabel );
		}

	}

}
