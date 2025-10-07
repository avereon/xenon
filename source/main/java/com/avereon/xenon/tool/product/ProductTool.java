package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductCardComparator;
import com.avereon.product.Rb;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.product.ProgramProductCardComparator;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import javafx.scene.layout.BorderPane;
import lombok.CustomLog;

import java.util.*;

/**
 * @deprecated This functionality has moved to the SettingsTool
 */
@Deprecated
@CustomLog
public class ProductTool extends GuidedTool {

	public static final String INSTALLED = "installed";

	public static final String AVAILABLE = "available";

	public static final String UPDATES = "updates";

	public static final String SOURCES = "sources";

	static final int ICON_SIZE = 48;

	private BorderPane layoutPane;

	private Map<String, ProductToolPage> pages;

	private ProductToolPage currentPage;

	private UpdateCheckInformationPane checkInfo;

	private InstalledPage installedPage;

	private AvailablePage availablePage;

	private UpdatesPage updatesPage;

	private RepoPage repoPage;

	private String currentPageId;

	public ProductTool( XenonProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-product" );

		Xenon program = getProgram();
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

		Guide guide = createGuide();
		getGuideContext().getGuides().add( guide );
		getGuideContext().setCurrentGuide( guide );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text(getProduct(), "tool", "product-name" ) );
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
		log.atFine().log( "Product tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.atFine().log( "Product tool display" );
		super.display();
		checkInfo.updateInfo();
	}

	@Override
	protected void activate() throws ToolException {
		log.atFine().log( "Product tool activate" );
		super.activate();
	}

	@Override
	protected void deactivate() throws ToolException {
		log.atFine().log( "Product tool deactivate" );
		super.deactivate();
	}

	@Override
	protected void conceal() throws ToolException {
		log.atFine().log( "Product tool conceal" );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		log.atFine().log( "Product tool deallocate" );
		super.deallocate();
	}

	private Guide createGuide() {
		Guide guide = new Guide();

		GuideNode installed = new GuideNode( getProgram(), INSTALLED, Rb.text(getProduct(), "tool", "product-installed" ), "module" );
		installed.setOrder( 0 );
		guide.addNode( installed );

		GuideNode available = new GuideNode( getProgram(), AVAILABLE, Rb.text(getProduct(), "tool", "product-available" ), "module" );
		available.setOrder( 1 );
		guide.addNode( available );

		GuideNode updates = new GuideNode( getProgram(), UPDATES, Rb.text(getProduct(), "tool", "product-updates" ), "download" );
		updates.setOrder( 2 );
		guide.addNode( updates );

		GuideNode sources = new GuideNode( getProgram(), SOURCES, Rb.text(getProduct(), "tool", "product-sources" ), "market" );
		sources.setOrder( 3 );
		guide.addNode( sources );

		return guide;
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

	protected List<ProductCard> createSourceList( List<ProductCard> cards ) {
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
				sources.add( releases.getFirst() );
			}
		}

		return sources;
	}

}

