package com.xeomar.xenon.tool;

import com.xeomar.product.ProductCard;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.DateUtil;
import com.xeomar.xenon.*;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramArtifactType;
import com.xeomar.xenon.update.UpdateManager;
import com.xeomar.xenon.util.ActionUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ArtifactTool extends GuidedTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	private static final int ICON_SIZE = 48;

	private Action addSourceAction;

	private Action refreshStateAction;

	private BorderPane layoutPane;

	private Map<String, ArtifactPage> pages;

	private ArtifactPage currentPage;

	private UpdateCheckInfo checkInfo;

	public ArtifactTool( ProgramProduct product, Resource resource ) {
		super( product, resource );

		Program program = getProgram();

		setId( "tool-artifact" );
		setGraphic( program.getIconLibrary().getIcon( "artifact" ) );
		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );

		addSourceAction = new AddSourceAction( program );
		refreshStateAction = new RefreshStateAction( program );

		pages = new HashMap<>();
		pages.put( ProgramArtifactType.INSTALLED, new InstalledPage( program ) );
		pages.put( ProgramArtifactType.AVAILABLE, new AvailablePage( program ) );
		pages.put( ProgramArtifactType.UPDATES, new UpdatesPage( program ) );
		pages.put( ProgramArtifactType.SOURCES, new SourcesPage( program ) );

		checkInfo = new UpdateCheckInfo( program );

		layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiManager.PAD ) );
		layoutPane.setBottom( checkInfo );
		getChildren().add( layoutPane );
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
		checkInfo.updateInfo();
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Artifact tool activate" );
		super.activate();

		((Program)getProduct()).getActionLibrary().getAction( "add-market" ).pushAction( addSourceAction );
		((Program)getProduct()).getActionLibrary().getAction( "refresh" ).pushAction( refreshStateAction );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Artifact tool deactivate" );
		super.deactivate();

		((Program)getProduct()).getActionLibrary().getAction( "refresh" ).pullAction( refreshStateAction );
		((Program)getProduct()).getActionLibrary().getAction( "add-market" ).pullAction( addSourceAction );
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

		layoutPane.setCenter( currentPage );
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

	private abstract class ArtifactPage extends VBox {

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

		protected void updateState() {
			System.out.println( "Update state for " + title.getText() );

			//
		}

	}

	private abstract class ProductPage extends ArtifactPage {

		private Button refreshButton;

		public ProductPage( Program program ) {
			super( program );

			refreshButton = ActionUtil.createButton( program, program.getActionLibrary().getAction( "refresh" ) );
			refreshButton.setId( "tool-artifact-page-refresh" );

			getButtons().addAll( refreshButton );
		}

		Button getRefreshButton() {
			return refreshButton;
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

			//Set<UpdateSource> updateSources = program.getUpdateManager().getUpdateSources();
		}

		Button getAddButton() {
			return addButton;
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

	private class UpdateCheckInfo extends HBox implements SettingsListener {

		private Program program;

		private Label lastUpdateCheckField;

		private Label nextUpdateCheckField;

		public UpdateCheckInfo( Program program ) {
			this.program = program;
			Label lastUpdateCheckLabel = new Label( program.getResourceBundle().getString( BundleKey.UPDATE, "product-update-check-last" ) );
			Label nextUpdateCheckLabel = new Label( program.getResourceBundle().getString( BundleKey.UPDATE, "product-update-check-next" ) );
			lastUpdateCheckField = new Label();
			nextUpdateCheckField = new Label();

			Pane spring = new Pane();
			HBox.setHgrow( spring, Priority.ALWAYS );
			getChildren().addAll( lastUpdateCheckLabel, lastUpdateCheckField, spring, nextUpdateCheckLabel, nextUpdateCheckField );

			program.getUpdateManager().getSettings().addSettingsListener( this );
		}

		public void updateInfo() {
			long lastUpdateCheck = program.getUpdateManager().getLastUpdateCheck();
			long nextUpdateCheck = program.getUpdateManager().getNextUpdateCheck();

			String unknown = program.getResourceBundle().getString( BundleKey.UPDATE, "unknown" );
			String lastUpdateCheckText = lastUpdateCheck == 0 ? unknown : DateUtil.format( new Date( lastUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() );
			String nextUpdateCheckText = nextUpdateCheck == 0 ? unknown : DateUtil.format( new Date( nextUpdateCheck ), DateUtil.DEFAULT_DATE_FORMAT, TimeZone.getDefault() );

			Platform.runLater( () -> {
				lastUpdateCheckField.setText( lastUpdateCheckText );
				nextUpdateCheckField.setText( nextUpdateCheckText );
			} );
		}

		@Override
		public void handleEvent( SettingsEvent event ) {
			if( event.getType() != SettingsEvent.Type.UPDATED ) return;
			switch( event.getKey() ) {
				case UpdateManager.LAST_CHECK_TIME:
				case UpdateManager.NEXT_CHECK_TIME: {
					updateInfo();
				}
			}
		}

	}

}
