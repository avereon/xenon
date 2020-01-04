package com.avereon.xenon.tool.settings;

import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Set;

public class SettingsTool extends GuidedTool {

	public static final String GENERAL = "general";

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String PAGE_ID = "page-id";

	private SettingsPage currentPage;

	private String currentPageId;

	public SettingsTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-settings" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "settings" ) );
		setTitle( product.rb().text( "tool", "settings-name" ) );
	}

	@Override
	protected void allocate() throws ToolException {
		log.debug( "Settings tool allocate" );
		super.allocate();
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Settings tool display" );
		super.display();
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Settings tool activate" );
		super.activate();
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Settings tool deactivate" );
		super.deactivate();
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Settings tool conceal" );
		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Settings tool deallocate" );
		super.deallocate();
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		log.debug( "Settings tool asset ready" );
		super.assetReady( request );

		// TODO Can this be generalized in GuidedTool?
		String pageId = request.getFragment();
		if( pageId == null ) pageId = currentPageId;
		if( pageId == null ) pageId = GENERAL;
		selectPage( pageId );
	}

	@Override
	protected Guide getGuide() {
		return getProgram().getSettingsManager().getSettingsGuide();
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.size() > 0 ) selectPage( newNodes.iterator().next().getId() );
	}

	private void selectPage( String pageId ) {
		currentPageId = pageId;
		if( pageId == null ) return;

		SettingsPage page = getProgram().getSettingsManager().getSettingsPage( pageId );
		if( page == null ) page = getProgram().getSettingsManager().getSettingsPage( GENERAL );
		currentPage = page;

		setPage( getProgram().getSettingsManager().getSettingsPage( pageId ) );
	}

	private void setPage( SettingsPage page ) {
		SettingsPanel panel = new SettingsPanel( getProduct(), page );
		ScrollPane scroller = new ScrollPane( panel );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

}
