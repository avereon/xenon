package com.avereon.xenon.tool.settings;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.SettingsManager;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import javafx.scene.control.ScrollPane;

import java.lang.System.Logger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsTool extends GuidedTool {

	public static final String GENERAL = "general";

	private static final Logger log = Log.get();

	private final Map<String, SettingsPanel> panelCache;

	private String currentPageId;

	public SettingsTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-settings" );

		panelCache = new ConcurrentHashMap<>();
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( getProduct().rb().text( "tool", "settings-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "settings" ) );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
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
		setPage( getProgram().getSettingsManager().getSettingsPage( pageId ) );
	}

	private void setPage( SettingsPage page ) {
		SettingsManager manager = getProgram().getSettingsManager();
		SettingsPanel panel = panelCache.computeIfAbsent( page.getId(), ( k ) -> new SettingsPanel( getProduct(), page, manager.getOptionProviders() ) );
		ScrollPane scroller = new ScrollPane( panel );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

}
