package com.avereon.xenon.tool.product;

import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import javafx.scene.layout.BorderPane;

import java.lang.System.Logger;
import java.util.*;

public class ProductTool extends GuidedTool {

	public static final String INSTALLED = "installed";

	public static final String AVAILABLE = "available";

	public static final String UPDATES = "updates";

	public static final String SOURCES = "sources";

	static final Logger log = Log.get();

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

	private String currentPageId;

	public ProductTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-product" );

		Program program = getProgram();
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
	protected void ready( OpenAssetRequest request ) {
		setTitle( getProduct().rb().text( "tool", "product-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "product" ) );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		// TODO Can this be generalized in GuidedTool?
		String pageId = request.getFragment();
		if( pageId == null ) pageId = currentPageId;
		if( pageId == null ) pageId = INSTALLED;
		selectPage( pageId );
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		log.log( Log.DEBUG, "Product tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.log( Log.DEBUG, "Product tool display" );
		super.display();
		checkInfo.updateInfo();
	}

	@Override
	protected void activate() throws ToolException {
		log.log( Log.DEBUG, "Product tool activate" );
		super.activate();
	}

	@Override
	protected void deactivate() throws ToolException {
		log.log( Log.DEBUG, "Product tool deactivate" );
		super.deactivate();
	}

	@Override
	protected void conceal() throws ToolException {
		log.log( Log.DEBUG, "Product tool conceal" );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		log.log( Log.DEBUG, "Product tool deallocate" );
		super.deallocate();
	}

	@Override
	protected Guide getGuide() {
		if( this.guide != null ) return this.guide;

		Guide guide = new Guide();
		ProductBundle rb = getProduct().rb();

		GuideNode installed = new GuideNode( getProgram(), INSTALLED, rb.text( "tool", "product-installed" ), "module" );
		guide.addNode( installed );

		GuideNode available = new GuideNode( getProgram(), AVAILABLE, rb.text( "tool", "product-available" ), "module" );
		guide.addNode( available );

		GuideNode updates = new GuideNode( getProgram(), UPDATES, rb.text( "tool", "product-updates" ), "download" );
		guide.addNode( updates );

		GuideNode sources = new GuideNode( getProgram(), SOURCES, rb.text( "tool", "product-sources" ), "market" );
		guide.addNode( sources );

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
		currentPageId = pageId;
		if( pageId == null ) return;

		ProductToolPage page = pages.get( pageId );
		if( page == null ) page = pages.get( INSTALLED );
		currentPage = page;

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
