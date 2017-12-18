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
import com.xeomar.xenon.update.CatalogCard;
import com.xeomar.xenon.update.UpdateManager;
import com.xeomar.xenon.util.ActionUtil;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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

		ProductMarketPage( Program program ) {
			setTitle( program.getResourceBundle().getString( BundleKey.TOOL, "artifact-" + ProgramArtifactType.SOURCES ) );

			Button addButton = new Button( "", program.getIconLibrary().getIcon( "add-market" ) );

			getButtonBox().addAll( addButton );
		}

		@Override
		protected void updateState() {
			log.trace( "Update product markets" );
			//getProgram().getExecutor().submit( new UpdateProductMarkets() );
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

		ProductPane( ProductSource source, ProductCard update ) {
			super( "fillx, hidemode 3, insets " + UiManager.PAD + " " + UiManager.PAD + ", gap " + UiManager.PAD + " " + UiManager.PAD );

			this.source = source;
			this.update = update;

			setId( "tool-artifact-product" );

			Program program = getProgram();

			String iconUri = source.getCard().getIconUri();
			Node productIcon = program.getIconLibrary().getIcon( "module", ICON_SIZE );
			//Node productIcon = program.getIconLibrary().getIcon( iconUri, ICON_SIZE );

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

			add( iconLabel, "spany, aligny center" );
			add( nameLabel );
			add( hyphenLabel );
			add( providerLabel, "pushx" );
			add( stateLabel, "tag right" );
			add( actionButtonBox );

			add( summaryLabel, "newline, spanx 3" );
			add( versionLabel, "tag right" );
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
					stateLabelKey = "enabled";
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
			List<CatalogCard> cards = new ArrayList<>( getProgram().getUpdateManager().getCatalogs() );
			//cards.sort( new CatalogCardComparator( getProgram(), CatalogCardComparator.Field.NAME ) );
			//Platform.runLater( () -> productMarketPage.setMarkets( cards ) );
			return null;
		}
	}

}
