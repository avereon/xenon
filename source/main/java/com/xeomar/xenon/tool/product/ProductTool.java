package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.tool.guide.GuideNode;
import com.xeomar.xenon.tool.guide.GuidedTool;
import com.xeomar.xenon.util.DialogUtil;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class ProductTool extends GuidedTool {

	static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	static final int ICON_SIZE = 48;

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

		installedPage = new InstalledPage( program, this );
		availablePage = new AvailablePage( program, this );
		updatesPage = new UpdatesPage( program, this );
		productMarketPage = new ProductMarketPage( program, this );

		pages = new HashMap<>();
		pages.put( ProgramProductType.INSTALLED, installedPage );
		pages.put( ProgramProductType.AVAILABLE, availablePage );
		pages.put( ProgramProductType.UPDATES, updatesPage );
		pages.put( ProgramProductType.SOURCES, productMarketPage );

		checkInfo = new UpdateCheckInformationPane( program );
		checkInfo.setId( "tool-product-page-footer" );

		layoutPane = new BorderPane();
		layoutPane.getStyleClass().add( "padded" );
		layoutPane.setTop( installedPage.getHeader() );
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

		getProgram().getExecutor().submit( new UpdateInstalledProducts( this ) );
		getProgram().getExecutor().submit( new UpdateAvailableProducts() );
		getProgram().getExecutor().submit( new UpdateUpdatableProducts( this ) );

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

	InstalledPage getInstalledPage() {
		return installedPage;
	}

	AvailablePage getAvailablePage() {
		return availablePage;
	}

	UpdatesPage getUpdatesPage() {
		return updatesPage;
	}

	ProductMarketPage getProductMarketPage() {
		return productMarketPage;
	}

	private void selectPage( String pageId ) {
		log.trace( "Product page selected: " + pageId );

		if( pageId == null ) return;

		getSettings().set( "selected", pageId );

		currentPage = pages.get( pageId );
		currentPage.updateState();

		layoutPane.setTop( currentPage.getHeader() );
		layoutPane.setCenter( currentPage );
	}

	List<ProductCard> createSourceList( List<ProductCard> cards ) {
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

	/**
	 * Nearly identical to ProgramUpdateManager.handleStagedUpdates()
	 */
	void handleStagedUpdates() {
		// Run on the FX thread
		updatesPage.updateState();

		// Ask the user about restarting.
		if( getProgram().getUpdateManager().areUpdatesStaged() ) {
			String title = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "updates" );
			String header = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "restart-required" );
			String message = getProgram().getResourceBundle().getString( BundleKey.UPDATE, "restart-recommended" );

			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( title );
			alert.setHeaderText( header );
			alert.setContentText( message );

			Stage stage = getProgram().getWorkspaceManager().getActiveWorkspace().getStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() == ButtonType.YES ) {
				getWorkpane().closeTool( this );
				getProgram().getTaskManager().submit( () -> {
					getProgram().getUpdateManager().applyStagedUpdates();
				} );
			}
		}
	}

}
