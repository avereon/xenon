package com.xeomar.xenon.tool;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.settings.SettingsEvent;
import com.xeomar.settings.SettingsListener;
import com.xeomar.util.DateUtil;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.*;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.task.Task;
import com.xeomar.xenon.task.TaskManager;
import com.xeomar.xenon.tool.guide.GuideNode;
import com.xeomar.xenon.tool.guide.GuidedTool;
import com.xeomar.xenon.update.MarketCard;
import com.xeomar.xenon.update.MarketCardComparator;
import com.xeomar.xenon.update.UpdateManager;
import com.xeomar.xenon.util.DialogUtil;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.tbee.javafx.scene.layout.MigPane;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductTool extends GuidedTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

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

		setId( "tool-product" );
		setGraphic( program.getIconLibrary().getIcon( "product" ) );
		setTitle( product.getResourceBundle().getString( "tool", "product-name" ) );

		installedPage = new InstalledPage( program );
		availablePage = new AvailablePage( program );
		updatesPage = new UpdatesPage( program );
		productMarketPage = new ProductMarketPage( program );

		pages = new HashMap<>();
		pages.put( ProgramProductType.INSTALLED, installedPage );
		pages.put( ProgramProductType.AVAILABLE, availablePage );
		pages.put( ProgramProductType.UPDATES, updatesPage );
		pages.put( ProgramProductType.SOURCES, productMarketPage );

		checkInfo = new UpdateCheckInformationPane( program );

		layoutPane = new BorderPane();
		layoutPane.setPadding( new Insets( UiFactory.PAD ) );
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

		String selected = getSettings().get( "selected", ProgramProductType.INSTALLED );
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
		log.trace( "Product tool resource refreshed" );
		super.resourceRefreshed();
	}

	@Override
	protected void guideNodeChanged( GuideNode oldNode, GuideNode newNode ) {
		if( newNode != null ) selectPage( newNode.getId() );
	}

	private void selectPage( String pageId ) {
		log.trace( "Product page selected: " + pageId );

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

	private void handleStagedUpdates() {
		// Run on the FX thread
		updatesPage.updateState();

		// Ask the user about restarting.
		if( getProgram().getUpdateManager().areUpdatesStaged() ) {
			String title = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates" );
			String header = "";
			String message = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

			Stage stage = getProgram().getWorkspaceManager().getActiveWorkspace().getStage();
			stage.requestFocus();

			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( title );
			alert.setHeaderText( header );
			alert.setContentText( message );

			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() == ButtonType.YES ) {
				getWorkpane().closeTool( this );
				getProgram().getTaskManager().submit( new RequestProgramRestart() );
			}
		}
	}

	private abstract class ProductToolPage extends BorderPane {

		private Label title;

		private HBox buttonBox;

		private VBox nodes;

		public ProductToolPage() {
			setId( "tool-product-panel" );

			title = new Label( "" );
			title.setId( "tool-product-page-title" );

			buttonBox = new HBox( UiFactory.PAD );

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
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.INSTALLED ) );

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
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.AVAILABLE ) );

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
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.UPDATES ) );

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
			getProgram().getExecutor().submit( () -> {
				try {
					getProgram().getUpdateManager().stagePostedUpdates();
					Platform.runLater( ProductTool.this::handleStagedUpdates );
				} catch( Exception exception ) {
					log.warn( "Error staging updates", exception );
				}
			} );
		}
	}

	private class ProductMarketPage extends ProductToolPage {

		private VBox marketList;

		ProductMarketPage( Program program ) {
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "product-" + ProgramProductType.SOURCES ) );

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

		private Button actionButton1;

		private Button actionButton2;

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

			actionButton1 = new Button( "", program.getIconLibrary().getIcon( "remove" ) );
			actionButton2 = new Button( "", program.getIconLibrary().getIcon( "enable" ) );

			add( iconLabel, "spany, aligny center" );
			add( nameLabel );
			add( hyphenLabel );
			add( providerLabel, "pushx" );
			add( versionLabel, "tag right" );
			add( actionButton1 );

			add( summaryLabel, "newline, spanx 3" );
			add( stateLabel, "tag right" );
			add( actionButton2 );

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
				actionButton1.setVisible( true );
				actionButton1.setDisable( isProgram );
				actionButton1.setOnAction( ( event ) -> removeProduct() );

				actionButton2.setVisible( true );
				actionButton2.setDisable( isProgram );
				actionButton2.setGraphic( program.getIconLibrary().getIcon( isEnabled ? "disable" : "enable" ) );
				actionButton2.setOnAction( ( event ) -> toggleEnabled() );
			} else if( isAvailableProductsPanel ) {
				actionButton1.setVisible( true );
				actionButton1.setDisable( false );
				actionButton1.setGraphic( program.getIconLibrary().getIcon( "install" ) );
				actionButton1.setOnAction( ( event ) -> installProduct() );

				actionButton2.setVisible( false );
				actionButton2.setDisable( true );
			} else if( isUpdatableProductsPanel ) {
				actionButton1.setVisible( true );
				actionButton1.setDisable( false );
				actionButton1.setGraphic( program.getIconLibrary().getIcon( "download" ) );
				actionButton1.setOnAction( ( event ) -> updateProduct() );

				actionButton2.setVisible( false );
				actionButton2.setDisable( true );
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
					Platform.runLater( ProductTool.this::handleStagedUpdates );
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
			add( removeButton );
			add( uriLabel, "newline" );
			add( enableButton );
		}

		MarketCard getSource() {
			return source;
		}

		void updateMarketState() {
			// TODO Update the market state
			enableButton.setGraphic( getProgram().getIconLibrary().getIcon( source.isEnabled() ? "disable" : "enable" ) );
			enableButton.setDisable( !source.isRemovable() );
			removeButton.setDisable( !source.isRemovable() );
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
			if( event.getType() != SettingsEvent.Type.CHANGED ) return;
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
				Platform.runLater( () -> updatesPage.setProducts( cards, true ) );
			} catch( Exception exception ) {
				log.warn( "Error checking for updates", exception );
				// TODO Notify the user there was a problem getting posted updates
			}
			return null;
		}

	}

	private class UpdateProductMarkets extends Task<Void> {

		@Override
		public Void call() {
			List<MarketCard> cards = new ArrayList<>( getProgram().getUpdateManager().getCatalogs() );
			cards.sort( new MarketCardComparator( getProgram(), MarketCardComparator.Field.NAME ) );
			Platform.runLater( () -> productMarketPage.setMarkets( cards ) );
			return null;
		}

	}

	private class RequestProgramRestart implements Runnable {

		@Override
		public void run() {
			Platform.runLater( () -> getProgram().restart( ProgramFlag.NOUPDATECHECK ) );
		}

	}

}
