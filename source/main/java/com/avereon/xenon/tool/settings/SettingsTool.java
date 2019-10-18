package com.avereon.xenon.tool.settings;

import com.avereon.settings.Settings;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workarea.ToolException;
import com.avereon.xenon.OpenToolRequestParameters;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Set;

public class SettingsTool extends GuidedTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String PAGE_ID = "page-id";

	private Settings settings;

	public SettingsTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-settings" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "settings" ) );
		setTitle( product.getResourceBundle().getString( "tool", "settings-name" ) );
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
	protected void resourceReady( OpenToolRequestParameters parameters ) throws ToolException {
		log.debug( "Settings tool resource ready" );
		super.resourceReady( parameters );
		resourceRefreshed();
	}

	@Override
	public void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
	}

	@Override
	public void setSettings( Settings settings ) {
		super.setSettings( settings );

		Platform.runLater( () -> selectPage( settings.get( GUIDE_SELECTED_IDS, ProgramSettingsType.GENERAL ).split( "," )[ 0 ] ) );
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.size() > 0 ) selectPage( newNodes.iterator().next().getId() );
	}

	private void selectPage( String id ) {
		if( id == null ) return;
		selectPage( getProgram().getSettingsManager().getSettingsPage( id ) );
	}

	private void selectPage( SettingsPage page ) {
		if( page == null ) return;

		SettingsPanel panel = new SettingsPanel( getProduct(), page );
		ScrollPane scroller = new ScrollPane( panel );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

}
