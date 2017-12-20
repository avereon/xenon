package com.xeomar.xenon.tool;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.DateUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramArtifactType;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskManager;
import com.xeomar.xenon.update.CatalogCardComparator;
import com.xeomar.xenon.update.MarketCard;
import com.xeomar.xenon.update.UpdateManager;
import com.xeomar.xenon.util.ActionUtil;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductTool extends GuidedTool {

	private static final Logger log = LoggerFactory.getLogger( ProductTool.class );

	private static final int ICON_SIZE = 48;

	private BorderPane layoutPane;

	private Map<String, ProductToolPage> pages;

	private ProductToolPage currentPage;

	private UpdateCheckInformationPane checkInfo;

	private InstalledPage installedPage;

	private AvailablePage availablePage;

	private UpdatesPage updatesPage;

	private ProductMarketPage productMarketPage;

	public ProductTool( ProgramProduct product, Resource resource ) {
		super( product, resource );

		Program program = getProgram();

		setId( "tool-artifact" );
		setGraphic( program.getIconLibrary().getIcon( "artifact" ) );
		setTitle( product.getResourceBundle().getString( "tool", "artifact-name" ) );

		installedPage = new InstalledPage( program );
		availablePage = new AvailablePage( program );
		updatesPage = new UpdatesPage( program );
		productMarketPage = new ProductMarketPage( program );

		pages = new HashMap<>();
		pages.put( ProgramArtifactType.INSTALLED, installedPage );
		pages.put( ProgramArtifactType.AVAILABLE, availablePage );
		pages.put( ProgramArtifactType.UPDATES, updatesPage );
		pages.put( ProgramArtifactType.SOURCES, productMarketPage );

		checkInfo = new UpdateCheckInformationPane( program );

		layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiManager.PAD ) );
		layoutPane.setCenter( installedPage );
		layoutPane.setBottom( checkInfo );
		getChildren().add( layoutPane );
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		log.debug( "Product tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Product tool display" );
		super.display();
		checkInfo.updateInfo();
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Product tool activate" );
		super.activate();

		String selected = getSettings().get( "selected", ProgramArtifactType.INSTALLED );
		// TODO Be sure the guide also changes selection
		//getGuide().setSelected( selected );
		selectPage( selected );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Product tool deactivate" );
		super.deactivate();
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Product tool conceal" );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Product tool deallocate" );
		super.deallocate();
	}

	@Override
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		log.debug( "Product tool resource ready" );
		super.resourceReady( parameters );

		getProgram().getExecutor().submit( new UpdateInstalledProducts() );
		getProgram().getExecutor().submit( new UpdateAvailableProducts() );
		getProgram().getExecutor().submit( new UpdateUpdatableProducts() );

		String selected = parameters.getFragment();
		// TODO Be sure the guide also changes selection
		//if( selected != null ) getGuide().setSelected( selected );
		if( selected != null ) selectPage( selected );
	}

	@Override
	protected void resourceRefreshed() throws ToolException {
		log.debug( "Product tool resource refreshed" );
		super.resourceRefreshed();
	}

	@Override
	protected void guideNodeChanged( GuideNode oldNode, GuideNode newNode ) {
		if( newNode != null ) selectPage( newNode.getId() );
	}

	private void selectPage( String pageId ) {
		log.debug( "Product page selected: " + pageId );

		if( pageId == null ) return;

		getSettings().set( "selected", pageId );

		currentPage = pages.get( pageId );
		currentPage.updateState();

		layoutPane.setCenter( currentPage );
	}

	private List<ProductCard> createSourceList( List<ProductCard> cards ) {
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
		List<ProductCard> sources = new ArrayList<>();
		for( ProductCard card : uniqueList ) {
			List<ProductCard> releases = cardMap.get( card.getProductKey() );
			if( releases != null ) {
				releases.sort( Collections.reverseOrder( new ProductCardComparator( getProgram(), ProductCardComparator.Field.RELEASE ) ) );
				sources.add( releases.get( 0 ) );
			}
		}

		return sources;
	}

	private abstract class ProductToolPage extends BorderPane {

		private Label title;

		private HBox buttonBox;

		private VBox nodes;

		public ProductToolPage() {
			setId( "tool-artifact-panel" );

			title = new Label( "" );
			title.setId( "tool-artifact-page-title" );

			buttonBox = new HBox( UiManager.PAD );

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

	private abstract class ProductPage extends ProductToolPage {

		private List<ProductPane> sources;

		private VBox productList;

		public ProductPage( Program program ) {
			sources = new CopyOnWriteArrayList<>();
			setCenter( productList = new VBox() );
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

			// Add a product pane for each card
			sources.clear();
			for( ProductCard source : createSourceList( cards ) ) {
				sources.add( new ProductPane( source, productUpdates.get( source.getProductKey() ) ) );
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
				if( panel.getSource().equals( card ) ) panel.updateProductState();
			}
		}

	}

	private class InstalledPage extends ProductPage {

		InstalledPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.INSTALLED ) );

			Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
			refreshButton.setOnAction( event -> updateState() );

			getButtonBox().addAll( refreshButton );
		}

		@Override
		protected void updateState() {
			log.trace( "Update installed products" );
			getProgram().getExecutor().submit( new UpdateInstalledProducts() );
		}

	}

	private class AvailablePage extends ProductPage {

		AvailablePage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.AVAILABLE ) );

			Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
			refreshButton.setOnAction( event -> getProgram().getExecutor().submit( new UpdateAvailableProducts( true ) ) );

			getButtonBox().addAll( refreshButton );
		}

		@Override
		protected void updateState() {
			log.trace( "Update available products" );
			getProgram().getExecutor().submit( new UpdateAvailableProducts() );
		}

	}

	private class UpdatesPage extends ProductPage {

		UpdatesPage( Program program ) {
			super( program );
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.UPDATES ) );

			Button refreshButton = new Button( "", program.getIconLibrary().getIcon( "refresh" ) );
			Button downloadAllButton = new Button( "", program.getIconLibrary().getIcon( "download" ) );

			refreshButton.setOnAction( event -> getProgram().getExecutor().submit( new UpdateUpdatableProducts( true ) ) );
			downloadAllButton.setOnAction( event -> downloadAll() );

			getButtonBox().addAll( refreshButton, downloadAllButton );
		}

		@Override
		protected void updateState() {
			log.trace( "Update available updates" );
			getProgram().getExecutor().submit( new UpdateUpdatableProducts() );
		}

		private void downloadAll() {
			log.trace( "Download all available updates" );
			try {
				getProgram().getUpdateManager().stagePostedUpdates();
			} catch( Exception exception ) {
				log.warn( "Error staging updates", exception );
			}
		}

	}

	private class ProductMarketPage extends ProductToolPage {

		private VBox marketList;

		ProductMarketPage( Program program ) {
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.SOURCES ) );

			Button addButton = new Button( "", program.getIconLibrary().getIcon( "add-market" ) );

			getButtonBox().addAll( addButton );
			setCenter( marketList = new VBox() );
		}

		@Override
		protected void updateState() {
			log.trace( "Update product markets" );
			getProgram().getExecutor().submit( new UpdateProductMarkets() );
		}

		void setMarkets( List<MarketCard> markets ) {
			// Add a product pane for each card
			List<MarketPane> panes = new ArrayList<>( markets.size() );
			for( MarketCard market : markets ) {
				panes.add( new MarketPane( market ) );
			}

			marketList.getChildren().clear();
			marketList.getChildren().addAll( panes );

			updateMarketStates();
		}

		void updateMarketStates() {
			for( Node node : marketList.getChildren() ) {
				((MarketPane)node).updateMarketState();
			}
		}

		public void updateMarketState( MarketCard card ) {
			for( Node node : marketList.getChildren() ) {
				MarketPane panel = (MarketPane)node;
				if( panel.getSource().equals( card ) ) panel.updateMarketState();
			}
		}

	}

	private class ProductPane extends MigPane {

		private ProductCard source;

		private ProductCard update;

		private Label iconLabel;

		private Label nameLabel;

		private Label versionLabel;

		private Label summaryLabel;

		private Label hyphenLabel;

		private Label providerLabel;

		private Label releaseLabel;

		private Label stateLabel;

		private Button actionButton;

		private Button removeButton;

		ProductPane( ProductCard source, ProductCard update ) {
			this.source = source;
			this.update = update;

			setId( "tool-product-artifact" );

			Program program = getProgram();

			String iconUri = source.getIconUri();
			Node productIcon = program.getIconLibrary().getIcon( "module", ICON_SIZE );
			//Node productIcon = program.getIconLibrary().getIcon( iconUri, ICON_SIZE );

			iconLabel = new Label( null, productIcon );
			iconLabel.setId( "tool-product-artifact-icon" );
			nameLabel = new Label( source.getName() );
			nameLabel.setId( "tool-product-artifact-name" );
			versionLabel = new Label( update == null ? source.getRelease().toHumanString( TimeZone.getDefault() ) : update.getRelease().toHumanString( TimeZone.getDefault() ) );
			versionLabel.setId( "tool-product-artifact-version" );
			summaryLabel = new Label( source.getSummary() );
			summaryLabel.setId( "tool-product-artifact-summary" );
			hyphenLabel = new Label( "-" );
			providerLabel = new Label( source.getProvider() );
			providerLabel.setId( "tool-product-artifact-provider" );
			releaseLabel = new Label( source.getRelease().toHumanString( TimeZone.getDefault() ) );
			releaseLabel.setId( "tool-product-artifact-release" );
			stateLabel = new Label( "State" );
			stateLabel.setId( "tool-product-artifact-state" );

			actionButton = ActionUtil.createToolBarButton( program, "enable" );
			removeButton = ActionUtil.createToolBarButton( program, "remove" );
			removeButton.setOnAction( ( event ) -> removeProduct() );

			add( iconLabel, "spany, aligny center" );
			add( nameLabel );
			add( hyphenLabel );
			add( providerLabel, "pushx" );
			add( stateLabel, "tag right" );
			add( actionButton );

			add( summaryLabel, "newline, spanx 3" );
			add( versionLabel, "tag right" );
			add( removeButton );

			// Trying to update the product state before being added to a page causes incorrect state
		}

		public ProductCard getSource() {
			return source;
		}

		public ProductCard getUpdate() {
			return update;
		}

		void updateProductState() {
			ProductCard card = source;
			Program program = getProgram();
			UpdateManager manager = program.getUpdateManager();

			boolean isStaged = update == null ? manager.isStaged( card ) : manager.isReleaseStaged( update );
			boolean isProgram = program.getCard().equals( card );
			boolean isEnabled = manager.isEnabled( card );
			boolean isInstalled = manager.isInstalled( card );
			boolean isInstalledProductsPanel = FxUtil.isChildOf( this, installedPage );
			boolean isAvailableProductsPanel = FxUtil.isChildOf( this, availablePage );
			boolean isUpdatableProductsPanel = FxUtil.isChildOf( this, updatesPage );

			// Determine state string key.
			String stateLabelKey = "not-installed";
			if( isInstalled ) {
				if( !isProgram && !isEnabled ) {
					stateLabelKey = "disabled";
				} else if( isUpdatableProductsPanel ) {
					stateLabelKey = "available";
				} else {
					stateLabelKey = "enabled";
				}
			}
			if( isStaged ) stateLabelKey = "downloaded";
			stateLabel.setText( program.getResourceBundle().getString( BundleKey.LABEL, stateLabelKey ) );

			// Configure the action button
			if( isInstalledProductsPanel ) {
				actionButton.setVisible( true );
				actionButton.setDisable( isProgram );
				actionButton.setGraphic( program.getIconLibrary().getIcon( isEnabled ? "disabled" : "enabled" ) );
				actionButton.setOnAction( ( event ) -> toggleEnabled() );

				removeButton.setVisible( true );
				removeButton.setDisable( isProgram );
			} else if( isAvailableProductsPanel ) {
				actionButton.setVisible( true );
				actionButton.setDisable( false );
				actionButton.setGraphic( program.getIconLibrary().getIcon( "install" ) );
				actionButton.setOnAction( ( event ) -> installProduct() );

				removeButton.setVisible( false );
				removeButton.setDisable( true );
			} else if( isUpdatableProductsPanel ) {
				actionButton.setVisible( true );
				actionButton.setDisable( false );
				actionButton.setGraphic( program.getIconLibrary().getIcon( "download" ) );
				actionButton.setOnAction( ( event ) -> updateProduct() );

				removeButton.setVisible( false );
				removeButton.setDisable( true );
			}
		}

		private void toggleEnabled() {
			getProgram().getUpdateManager().setEnabled( source, !getProgram().getUpdateManager().isEnabled( source ) );
		}

		private void installProduct() {
			getProgram().getExecutor().submit( () -> {
				try {
					getProgram().getUpdateManager().installProducts( source );
				} catch( Exception exception ) {
					log.warn( "Error installing product", exception );
				}
			} );
		}

		private void updateProduct() {
			getProgram().getExecutor().submit( () -> {
				try {
					getProgram().getUpdateManager().stageUpdates( source );
				} catch( Exception exception ) {
					log.warn( "Error updating product", exception );
				}
			} );
		}

		private void removeProduct() {
			getProgram().getExecutor().submit( () -> {
				try {
					getProgram().getUpdateManager().uninstallProducts( source );
				} catch( Exception exception ) {
					log.warn( "Error uninstalling product", exception );
				}
			} );
		}

	}

	private class MarketPane extends MigPane {

		private MarketCard source;

		private Label iconLabel;

		private Label nameLabel;

		private Label uriLabel;

		private Button enableButton;

		private Button removeButton;

		public MarketPane( MarketCard source ) {
			this.source = source;

			setId( "tool-product-market" );

			Program program = getProgram();

			String iconUri = source.getIconUri();
			Node marketIcon = program.getIconLibrary().getIcon( "market", ICON_SIZE );
			//Node marketIcon = program.getIconLibrary().getIcon( iconUri, ICON_SIZE );

			iconLabel = new Label( null, marketIcon );
			iconLabel.setId( "tool-product-market-icon" );
			nameLabel = new Label( source.getName() );
			nameLabel.setId( "tool-product-market-name" );
			uriLabel = new Label( source.getCardUri() );
			uriLabel.setId( "tool-product-market-uri" );

			enableButton = new Button( "", getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
			removeButton = new Button( "", program.getIconLibrary().getIcon( "remove" ) );

			add( iconLabel, "spany, aligny center" );
			add( nameLabel, "pushx" );
			add( enableButton );
			add( uriLabel, "newline" );
			add( removeButton );
		}

		MarketCard getSource() {
			return source;
		}

		void updateMarketState() {
			// TODO Update the market state
			enableButton.setGraphic( getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
			removeButton.setVisible( source.isRemovable() );
		}

	}

	private class UpdateCheckInformationPane extends HBox implements SettingsListener {

		private Program program;

		private Label lastUpdateCheckField;

		private Label nextUpdateCheckField;

		UpdateCheckInformationPane( Program program ) {
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

		void updateInfo() {
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

	private class UpdateAvailableProducts extends Task<Void> {

		private boolean force;

		UpdateAvailableProducts() {
			this( false );
		}

		UpdateAvailableProducts( boolean force ) {
			this.force = force;
		}

		@Override
		public Void call() {
			TaskManager.taskThreadCheck();
			//			List<ProductCard> cards = new ArrayList<>( getProgram().getUpdateManager().getAvailableProducts( force ) );
			//			cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
			//			Platform.runLater( () -> availablePage.setProducts( cards ) );
			return null;
		}

	}

	/**
	 * Should be run on the FX platform thread.
	 */
	private class UpdateInstalledProducts extends Task<Void> {

		@Override
		public Void call() {
			TaskManager.taskThreadCheck();
			List<ProductCard> cards = new ArrayList<>( getProgram().getUpdateManager().getProductCards() );
			cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
			Platform.runLater( () -> installedPage.setProducts( cards ) );
			return null;
		}

	}

	private class UpdateUpdatableProducts extends Task<Void> {

		private boolean force;

		UpdateUpdatableProducts() {
			this( false );
		}

		UpdateUpdatableProducts( boolean force ) {
			this.force = force;
		}

		@Override
		public Void call() {
			TaskManager.taskThreadCheck();
			try {
				List<ProductCard> cards = new ArrayList<>( getProgram().getUpdateManager().findPostedUpdates( force ) );
				cards.sort( new ProductCardComparator( getProgram(), ProductCardComparator.Field.NAME ) );
				Platform.runLater( () -> updatesPage.setProducts( cards ) );
			} catch( Exception exception ) {
				log.warn( "Error checking for updates", exception );
				// TODO Notify the user there was a problem getting posted updates
			}
			return null;
		}

	}

	private class UpdateProductMarkets extends Task<Void> {

		@Override
		public Void call() throws Exception {
			List<MarketCard> cards = new ArrayList<>( getProgram().getUpdateManager().getCatalogs() );
			cards.sort( new CatalogCardComparator( getProgram(), CatalogCardComparator.Field.NAME ) );
			Platform.runLater( () -> productMarketPage.setMarkets( cards ) );
			return null;
		}
	}

}
