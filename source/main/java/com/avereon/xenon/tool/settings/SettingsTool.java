package com.avereon.xenon.tool.settings;

import com.avereon.product.Rb;
import com.avereon.util.UriUtil;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import javafx.scene.control.ScrollPane;
import lombok.CustomLog;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class SettingsTool extends GuidedTool {

	public static final String GENERAL = "general";

	private final Map<String, SettingsPanel> panelCache;

	private final ScrollPane scroller;

	private String currentPageId;

	public SettingsTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-settings" );

		panelCache = new ConcurrentHashMap<>();

		scroller = new ScrollPane();
		scroller.setFitToWidth( true );
		getChildren().add( scroller );

		Guide guide = product.getProgram().getSettingsManager().getSettingsGuide();
		getGuideContext().getGuides().add( guide );
		getGuideContext().setCurrentGuide( guide );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( "tool", "settings-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "settings" ) );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		// TODO Can this be generalized in GuidedTool?
		String pageId = request.getFragment();
		if( pageId == null ) pageId = currentPageId;
		if( pageId == null ) pageId = GENERAL;
		selectPage( pageId );

		URI uri = request.getUri();
		if( uri != null ) {
			String name = UriUtil.parseFragment( uri );
			if( name != null ) selectPage( name );
		}
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( !newNodes.isEmpty() ) selectPage( newNodes.iterator().next().getId() );
	}

	private void selectPage( String pageId ) {
		currentPageId = pageId;
		if( pageId == null ) return;

		// Select the node in the guide
		getGuideContext().setExpandedIds( pageId );
		getGuideContext().setSelectedIds( pageId );

		setPage( getProgram().getSettingsManager().getSettingsPage( pageId ) );
	}

	private void setPage( SettingsPage page ) {
		SettingsPanel currentPanel = (SettingsPanel)scroller.getContent();
		if( currentPanel != null ) currentPanel.setSelected( false );

		SettingsPanel nextPanel = findOrCreatePanel( page );

		if( nextPanel != null ) {
			scroller.setContent( panelCache.computeIfAbsent( page.getId(), k -> nextPanel ) );
			nextPanel.setSelected( true );
		}
	}

	private SettingsPanel findOrCreatePanel( SettingsPage page ) {
		SettingsPanel panel;
		if( panelCache.containsKey( page.getId() ) ) {
			panel = panelCache.get( page.getId() );
		} else if( page.getPanel() == null ) {
			panel = createStandardPanel( page );
		} else {
			panel = createCustomPanel( page );
		}
		return panel;
	}

	private SettingsPanel createStandardPanel( SettingsPage page ) {
		return new SettingsPagePanel( page, true, getProgram().getSettingsManager().getOptionProviders() );
	}

	private SettingsPanel createCustomPanel( SettingsPage page ) {
		try {
			Class<? extends SettingsPanel> type = SettingsPage.getPanel( page.getPanel() );
			return type.getConstructor( XenonProgramProduct.class ).newInstance( page.getProduct() );
		} catch( NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}

}
