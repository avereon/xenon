package com.xeomar.xenon.tool;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.DateUtil;
import com.xeomar.xenon.*;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramArtifactType;
import com.xeomar.xenon.update.UpdateManager;
import com.xeomar.xenon.util.ActionUtil;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArtifactTool extends GuidedTool {

	private static final Logger log = LoggerFactory.getLogger( ArtifactTool.class );

	private static final int ICON_SIZE = 48;

	private Action addSourceAction;

	private Action refreshStateAction;

	private BorderPane layoutPane;

	private Map<String, ArtifactPage> pages;

	private ArtifactPage currentPage;

	private CheckInfoPane checkInfo;

	private InstalledPage installedPage;

	private AvailablePage availablePage;

	private UpdatesPage updatesPage;

	private SourcesPage sourcesPage;

	public ArtifactTool( ProgramProduct product, Resource resource ) {
		super( product, resource );

		Program program = getProgram();

		setId( "tool-artifact" );
		setGraphic( program.getIconLibrary().getIcon( "artifact" ) );
		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );

		addSourceAction = new AddSourceAction( program );
		refreshStateAction = new RefreshStateAction( program );

		installedPage = new InstalledPage( program );
		availablePage = new AvailablePage( program );
		updatesPage = new UpdatesPage( program );
		sourcesPage = new SourcesPage( program );

		pages = new HashMap<>();
		pages.put( ProgramArtifactType.INSTALLED, installedPage );
		pages.put( ProgramArtifactType.AVAILABLE, availablePage );
		pages.put( ProgramArtifactType.UPDATES, updatesPage );
		pages.put( ProgramArtifactType.SOURCES, sourcesPage );

		checkInfo = new CheckInfoPane( program );

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

		getProgram().getActionLibrary().getAction( "add-market" ).pushAction( addSourceAction );
		getProgram().getActionLibrary().getAction( "refresh" ).pushAction( refreshStateAction );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Artifact tool deactivate" );
		super.deactivate();

		getProgram().getActionLibrary().getAction( "refresh" ).pullAction( refreshStateAction );
		getProgram().getActionLibrary().getAction( "add-market" ).pullAction( addSourceAction );
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
		getGuide().setSelected( parameters.getFragment() );

		//		EventQueue.invokeLater( new UpdateAvailableProductPanelState() );
		//Platform.runLater( new UpdateInstalledProducts() );
		//		EventQueue.invokeLater( new UpdateUpdatableProductPanelState() );
	}

	@Override
	public void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
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

	private List<ProductSource> createSourceList( List<ProductCard> cards ) {
		// Clean out duplicate releases and create unique product list.
		List<ProductCard> uniqueList = new ArrayList<>();
		Map<String, List<ProductCard>> cardMap = new HashMap<>();
		for( ProductCard card : cards ) {
			List<ProductCard> productReleaseCards = cardMap.get( card.getProductKey() );
			if( productReleaseCards == null ) {
				productReleaseCards = new ArrayList<>();
				productReleaseCards.add( card );
				cardMap.put( card.getProductKey(), productReleaseCards );
				uniqueList.add( card );
			} else {
				boolean found = false;
				for( ProductCard releaseCard : productReleaseCards ) {
					found = found | card.getRelease().equals( releaseCard.getRelease() );
				}
				if( !found ) productReleaseCards.add( card );
			}
		}

		// Create the sources.
		List<ProductSource> sources = new ArrayList<>();
		for( ProductCard card : uniqueList ) {
			List<ProductCard> releases = cardMap.get( card.getProductKey() );
			if( releases != null ) {
				releases.sort( Collections.reverseOrder( new ProductCardComparator( getProgram(), ProductCardComparator.Field.RELEASE ) ) );
				sources.add( new ProductSource( releases.get( 0 ) ) );
			}
		}

		return sources;
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

	private abstract class ArtifactPage extends BorderPane {

		private Label title;

		private HBox buttonBox;

		private VBox nodes;

		public ArtifactPage( Program program ) {
			setId( "tool-artifact-panel" );

			title = new Label( "" );
			title.setId( "tool-artifact-page-title" );

			buttonBox = new HBox();

			BorderPane header = new BorderPane();
			header.prefWidthProperty().bind( this.widthProperty() );
			header.setLeft( title );
			header.setRight( buttonBox );

			setTop( header );
		}

		protected void setTitle( String title ) {
			this.title.setText( title );
		}

		protected ObservableList<Node> getButtonBox() {
			return buttonBox.getChildren();
		}

		protected abstract void updateState();

	}

	private abstract class ProductPage extends ArtifactPage {

		private Button refreshButton;

		private VBox productList;

		private List<ProductPane> sources;

		public ProductPage( Program program ) {
			super( program );
			sources = new CopyOnWriteArrayList<>();

			refreshButton = ActionUtil.createButton( program, program.getActionLibrary().getAction( "refresh" ) );
			refreshButton.setId( "tool-artifact-page-refresh" );

			getButtonBox().addAll( refreshButton );

			setCenter( productList = new VBox() );
		}

		Button getRefreshButton() {
			return refreshButton;
		}

		public List<ProductPane> getSourcePanels() {
			return Collections.unmodifiableList( sources );
		}

		void setProducts( List<ProductCard> cards ) {
			setProducts( cards, false );
		}

		public void setProducts( List<ProductCard> cards, boolean isUpdate ) {
			// Create a map of the updates
			Map<String, ProductCard> installedProducts = new HashMap<>();
			Map<String, ProductCard> productUpdates = new HashMap<>();
			if( isUpdate ) {
				// Installed product map
				for( ProductCard card : getProgram().getUpdateManager().getProductCards() ) {
					installedProducts.put( card.getProductKey(), card );
				}

				// Product update map
				for( ProductCard card : cards ) {
					productUpdates.put( card.getProductKey(), card );
				}

				// Installed product list
				List<ProductCard> newCards = new ArrayList<>();
				for( ProductCard card : cards ) {
					newCards.add( installedProducts.get( card.getProductKey() ) );
				}
				cards = newCards;
			}

			// Create a valid list of product sources
			List<ProductSource> check = createSourceList( cards );
			List<ProductSource> valid = new ArrayList<>( check.size() );

			// Filter out sources with invalid sources.
			for( ProductSource source : check ) {
				if( source.getCard() != null ) valid.add( source );
			}

			// Add a product pane for each card
			sources.clear();
			for( ProductSource source : valid ) {
				sources.add( new ProductPane( source, productUpdates.get( source.getCard().getProductKey() ) ) );
			}

			productList.getChildren().clear();
			productList.getChildren().addAll( sources );

			updateProductStates();
		}

		void updateProductStates() {
			for( Node node : productList.getChildren() ) {
				((ProductPane)node).updateProductState();
			}
		}

		public void updateProductState( ProductCard card ) {
			for( Node node : productList.getChildren() ) {
				ProductPane panel = (ProductPane)node;
				if( panel.getSource().getCard().equals( card ) ) panel.updateProductState();
			}
		}

	}

	private class InstalledPage extends ProductPage {

		InstalledPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.INSTALLED ) );
		}

		@Override
		protected void updateState() {
			System.out.println( "Update state for installed products" );
			Platform.runLater( new UpdateInstalledProducts() );
		}

	}

	private class AvailablePage extends ProductPage {

		AvailablePage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.AVAILABLE ) );
		}

		@Override
		protected void updateState() {
			System.out.println( "Update state for available products" );
			Platform.runLater( new UpdateAvailableSources() );
		}

	}

	private class UpdatesPage extends ProductPage {

		UpdatesPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.UPDATES ) );
		}

		@Override
		protected void updateState() {
			System.out.println( "Update state for available updates" );
		}

	}

	private class SourcesPage extends ArtifactPage {

		private Button addButton;

		SourcesPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.SOURCES ) );

			addButton = ActionUtil.createButton( program, program.getActionLibrary().getAction( "add-market" ) );
			addButton.setId( "tool-artifact-page-add" );

			getButtonBox().addAll( addButton );

		}

		Button getAddButton() {
			return addButton;
		}

		@Override
		protected void updateState() {
			System.out.println( "Update state for product sources" );

			//Set<UpdateSource> updateSources = getProgram().getUpdateManager().getUpdateSources();
		}

	}

	private class ProductPane extends MigPane {

		private ProductSource source;

		private ProductCard update;

		private CheckBox selectCheckBox;

		private CheckBox enableCheckBox;

		private Label iconLabel;

		private Label nameLabel;

		private Label versionLabel;

		private Label summaryLabel;

		private Label hyphenLabel;

		private Label providerLabel;

		private Label releaseLabel;

		private Label stateLabel;

		private Pane actionButtonBox;

		private Pane removeButtonBox;

		private Button enableButton;

		private Button removeButton;

		private Button installButton;

		public ProductPane( ProductSource source, ProductCard update ) {
			super( "fillx, hidemode 3, insets " + UiManager.PAD + " " + UiManager.PAD + ", gap " + UiManager.PAD + " " + UiManager.PAD );

			this.source = source;
			this.update = update;

			setId( "tool-artifact-product" );

			Program program = getProgram();
			Node productIcon = program.getIconLibrary().getIcon( source.getCard().getIconUri(), ICON_SIZE );
			if( productIcon == null ) productIcon = program.getIconLibrary().getIcon( "product", ICON_SIZE );

			iconLabel = new Label( null, productIcon );
			iconLabel.setId( "tool-artifact-product-icon" );
			nameLabel = new Label( source.getCard().getName() );
			nameLabel.setId( "tool-artifact-product-name" );
			versionLabel = new Label( update == null ? source.getCard().getRelease().toHumanString( TimeZone.getDefault() ) : update.getRelease().toHumanString( TimeZone.getDefault() ) );
			versionLabel.setId( "tool-artifact-product-version" );
			summaryLabel = new Label( source.getCard().getSummary() );
			summaryLabel.setId( "tool-artifact-product-summary" );
			hyphenLabel = new Label( "-" );
			providerLabel = new Label( source.getCard().getProvider() );
			providerLabel.setId( "tool-artifact-product-provider" );
			releaseLabel = new Label( source.getCard().getRelease().toHumanString( TimeZone.getDefault() ) );
			releaseLabel.setId( "tool-artifact-product-release" );
			stateLabel = new Label( "State" );
			stateLabel.setId( "tool-artifact-product-state" );
			selectCheckBox = new CheckBox();
			selectCheckBox.setId( "tool-artifact-product-select" );
			enableCheckBox = new CheckBox();
			enableCheckBox.setId( "tool-artifact-product-enable" );

			// Try to do all actions without a selection box
			// Or use the selection checkbox as an enabled checkbox
			//add( selectCheckBox, "spany, aligny center" );

			actionButtonBox = new HBox();
			((HBox)actionButtonBox).setAlignment( Pos.CENTER );
			removeButtonBox = new HBox();
			((HBox)removeButtonBox).setAlignment( Pos.CENTER );

			enableButton = ActionUtil.createToolBarButton( program, "enable" );
			removeButton = ActionUtil.createToolBarButton( program, "remove" );
			installButton = ActionUtil.createToolBarButton( program, "install" );

			add( iconLabel, "spany, aligny top" );
			add( nameLabel );
			add( hyphenLabel );
			add( providerLabel, "pushx" );
			add( versionLabel );
			add( actionButtonBox );

			add( summaryLabel, "newline, spanx 3" );
			add( stateLabel, "tag right" );
			add( removeButtonBox );

			// Trying to update the product state before being added to a page causes incorrect state
		}

		public ProductSource getSource() {
			return source;
		}

		public ProductCard getUpdate() {
			return update;
		}

		public boolean isSelected() {
			return selectCheckBox.isSelected();
		}

		public void setSelected( boolean selected ) {
			selectCheckBox.setSelected( selected );
		}

		void updateProductState() {
			ProductCard card = source.getCard();
			UpdateManager manager = getProgram().getUpdateManager();

			boolean isStaged = update == null ? manager.isStaged( card ) : manager.isReleaseStaged( update );
			boolean isProgram = getProgram().getCard().equals( card );
			boolean isEnabled = manager.isEnabled( card );
			boolean isInstalled = manager.isInstalled( card );
			boolean isInstalledProductsPanel = FxUtil.isChildOf( this, installedPage );
			boolean isUpdatableProductsPanel = FxUtil.isChildOf( this, updatesPage );

			// Determine state string key.
			String stateLabelKey = "not-installed";
			if( isInstalled ) {
				if( !isProgram && !isEnabled ) {
					stateLabelKey = "disabled";
				} else if( isUpdatableProductsPanel ) {
					stateLabelKey = "available";
				} else {
					stateLabelKey = "installed";
				}
			}
			if( isStaged ) stateLabelKey = "downloaded";
			stateLabel.setText( getProgram().getResourceBundle().getString( BundleKey.LABEL, stateLabelKey ) );

			if( isInstalledProductsPanel ) {
				actionButtonBox.getChildren().clear();
				actionButtonBox.getChildren().add( enableButton );
				removeButtonBox.getChildren().add( removeButton );
				removeButton.setDisable( isProgram );
				selectCheckBox.setSelected( !isProgram );
				selectCheckBox.setDisable( isProgram );
			} else if( isUpdatableProductsPanel ) {
				actionButtonBox.getChildren().clear();
				actionButtonBox.getChildren().add( installButton );
				removeButtonBox.getChildren().clear();
				selectCheckBox.setSelected( true );
			}
		}

	}

	private class CheckInfoPane extends HBox implements SettingsListener {

		private Program program;

		private Label lastUpdateCheckField;

		private Label nextUpdateCheckField;

		public CheckInfoPane( Program program ) {
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

	private class UpdateAvailableSources implements Runnable {

		@Override
		public void run() {
//			List<ProductCard> cards = new ArrayList<>( getProgram().getUpdateManager().getAvailableProducts() );
//			cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
//			availablePage.setProducts( cards );
		}

	}

	/**
	 * Should be run on the FX platform thread.
	 */
	private class UpdateInstalledProducts implements Runnable {

		@Override
		public void run() {
			List<ProductCard> cards = new ArrayList<>( getProgram().getUpdateManager().getProductCards() );
			cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
			installedPage.setProducts( cards );
		}

	}

	private class UpdateUpdateableProducts implements Runnable {

		private List<ProductCard> cards;

		public UpdateUpdateableProducts( Set<ProductCard> cards ) {
			this.cards = new ArrayList<>( cards );
		}

		@Override
		public void run() {
			this.cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
			updatesPage.setProducts( cards );
		}

	}

}
