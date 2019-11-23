package com.avereon.xenon.tool.product;

import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.util.LogUtil;
import com.avereon.xenon.IconLibrary;
import com.avereon.xenon.OpenToolRequestParameters;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workarea.ToolException;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class ProductTool extends GuidedTool {

	public static final String INSTALLED = "installed";

	public static final String AVAILABLE = "available";

	public static final String UPDATES = "updates";

	public static final String SOURCES = "sources";

	static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	static final int ICON_SIZE = 48;

	private BorderPane layoutPane;

	private Map<String, ProductToolPage> pages;

	private ProductToolPage currentPage;

	private UpdateCheckInformationPane checkInfo;

	private InstalledPage installedPage;

	private AvailablePage availablePage;

	private UpdatesPage updatesPage;

	private RepoPage repoPage;

	private Guide guide;

	public ProductTool( ProgramProduct product, Resource resource ) {
		super( product, resource );

		Program program = getProgram();

		setId( "tool-product" );
		setGraphic( program.getIconLibrary().getIcon( "product" ) );
		setTitle( product.getResourceBundle().getString( "tool", "product-name" ) );

		installedPage = new InstalledPage( program, this );
		availablePage = new AvailablePage( program, this );
		updatesPage = new UpdatesPage( program, this );
		repoPage = new RepoPage( program, this );

		pages = new HashMap<>();
		pages.put( INSTALLED, installedPage );
		pages.put( AVAILABLE, availablePage );
		pages.put( UPDATES, updatesPage );
		pages.put( SOURCES, repoPage );

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
		//Platform.runLater( () -> selectPage( settings.get( GUIDE_SELECTED_IDS, ProgramProductType.INSTALLED ).split( "," )[ 0 ] ) );
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

		String selected = getSettings().get( "selected", INSTALLED );
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
	protected void resourceReady( OpenToolRequestParameters parameters ) throws ToolException {
		log.debug( "Product tool resource ready" );
		super.resourceReady( parameters );

		String selected = parameters.getFragment();
		if( selected == null ) selected = INSTALLED;

		// Select the guide ids
		// FIXME Deprecated
		selectPage( selected );
		// TODO Eventually use getGuide().setSelectedIds( selected );
	}

	@Override
	protected void resourceRefreshed() throws ToolException {
		log.trace( "Product tool resource refreshed" );
		super.resourceRefreshed();
	}

	@Override
	protected Guide getGuide() {
		if( this.guide != null ) return this.guide;

		Guide guide = new Guide();
		IconLibrary library = getProgram().getIconLibrary();
		ProductBundle rb = getProduct().getResourceBundle();

		GuideNode installed = new GuideNode();
		installed.setId( INSTALLED );
		installed.setName( rb.getString( "tool", "product-installed" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( installed, library.getIcon( "product" ) ) );

		GuideNode available = new GuideNode();
		available.setId( AVAILABLE );
		available.setName( rb.getString( "tool", "product-available" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( available, library.getIcon( "product" ) ) );

		GuideNode updates = new GuideNode();
		updates.setId( UPDATES );
		updates.setName( rb.getString( "tool", "product-updates" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( updates, library.getIcon( "product" ) ) );

		GuideNode sources = new GuideNode();
		sources.setId( SOURCES );
		sources.setName( rb.getString( "tool", "product-sources" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( sources, library.getIcon( "product" ) ) );

		return this.guide = guide;
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.size() > 0 ) selectPage( newNodes.iterator().next().getId() );
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

	RepoPage getRepoPage() {
		return repoPage;
	}

	ProductToolPage getSelectedPage() {
		return currentPage;
	}

	private void selectPage( String pageId ) {
		log.warn( "Product page selected: " + pageId );

		if( pageId == null || pageId.isBlank() ) return;

		getSettings().set( "selected", pageId );

		currentPage = pages.get( pageId );
		if( currentPage == null ) throw new NullPointerException( "Page ID returned a null page: " + pageId );

		layoutPane.setTop( currentPage.getHeader() );
		layoutPane.setCenter( currentPage );

		currentPage.updateState( false );
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
				releases.sort( Collections.reverseOrder( new ProgramProductCardComparator( getProgram(), ProductCardComparator.Field.RELEASE ) ) );
				sources.add( releases.get( 0 ) );
			}
		}

		return sources;
	}

}
