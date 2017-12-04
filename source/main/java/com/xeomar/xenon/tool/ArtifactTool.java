package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Action;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramArtifactType;
import com.xeomar.xenon.util.ActionUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ArtifactTool extends GuidedTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	private static final int ICON_SIZE = 48;

	private Action addSourceAction;

	private Action refreshStateAction;

	private Map<String, ArtifactPage> pages;

	private ArtifactPage currentPage;

	public ArtifactTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-artifact" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "artifact" ) );
		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );

		addSourceAction = new AddSourceAction((Program)product);
		refreshStateAction = new RefreshStateAction((Program)product);

		pages = new HashMap<>();
		pages.put( ProgramArtifactType.INSTALLED, new InstalledPage( (Program)product ) );
		pages.put( ProgramArtifactType.AVAILABLE, new AvailablePage( (Program)product ) );
		pages.put( ProgramArtifactType.UPDATES, new UpdatesPage( (Program)product ) );
		pages.put( ProgramArtifactType.SOURCES, new SourcesPage( (Program)product ) );
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

		currentPage = pages.get( pageId );
		currentPage.updateState();

		ScrollPane scroller = new ScrollPane( currentPage );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

	private class AddSourceAction extends Action {

		protected AddSourceAction( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void handle( Event event ) {
			// TODO Implement AddSourceAction.handle()
		}

	}

	private class RefreshStateAction extends Action {

		protected RefreshStateAction( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void handle( Event event ) {
			if( currentPage != null ) currentPage.updateState();
		}

	}

	private abstract class ArtifactPage extends Pane {

		private Label title;

		private HBox buttons;

		public ArtifactPage( Program program ) {
			setId( "tool-artifact-panel" );

			title = new Label( "" );
			title.setId( "tool-artifact-page-title" );

			buttons = new HBox();

			BorderPane layoutPane = new BorderPane();
			layoutPane.prefWidthProperty().bind( this.widthProperty() );
			layoutPane.setLeft( title );
			layoutPane.setRight( buttons );

			getChildren().addAll( layoutPane );
		}

		protected void setTitle( String title ) {
			this.title.setText( title );
		}

		protected ObservableList<Node> getButtons() {
			return buttons.getChildren();
		}

		protected void updateState() {}

	}

	private abstract class ProductPage extends ArtifactPage {

		private Button refreshButton;

		public ProductPage( Program program ) {
			super( program );

			refreshButton = ActionUtil.createButton( program, program.getActionLibrary().getAction( "refresh" ) );
			refreshButton.setId( "tool-artifact-page-refresh" );

			getButtons().addAll( refreshButton );
		}

	}

	private class InstalledPage extends ProductPage {

		InstalledPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.INSTALLED ) );
		}

	}

	private class AvailablePage extends ProductPage {

		AvailablePage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.AVAILABLE ) );
		}

	}

	private class UpdatesPage extends ProductPage {

		UpdatesPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.UPDATES ) );
		}

	}

	private class SourcesPage extends ArtifactPage {

		private Button addButton;

		SourcesPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.SOURCES ) );

			addButton = ActionUtil.createButton( program, program.getActionLibrary().getAction( "add-market" ) );
			addButton.setId( "tool-artifact-page-add" );

			getButtons().addAll( addButton );

			program.getActionLibrary().getAction( "add-market" ).pushAction( addSourceAction );
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
