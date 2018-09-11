package com.xeomar.xenon.tool.settings;

import com.xeomar.settings.Settings;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.guide.GuideNode;
import com.xeomar.xenon.tool.guide.GuidedTool;
import com.xeomar.xenon.workarea.ToolException;
import com.xeomar.xenon.workarea.ToolParameters;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

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
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		log.debug( "Settings tool resource ready" );
		super.resourceReady( parameters );
		resourceRefreshed();
	}

	@Override
	public void resourceRefreshed() throws ToolException {
		super.resourceRefreshed();
	}

	@Override
	protected void guideNodeChanged( GuideNode oldNode, GuideNode newNode ) {
		if( newNode != null ) selectPage( newNode.getId() );
	}

	@Override
	public void setSettings( Settings settings ) {
		this.settings = settings;

		String id = settings.get( PAGE_ID );
		Platform.runLater( () -> {
			selectPage( id );
			getGuide().setSelected( id );
		} );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	private void selectPage( String id ) {
		if( id == null ) return;
		selectPage( getProgram().getSettingsManager().getSettingsPage( id ) );
	}

	private void selectPage( SettingsPage page ) {
		log.debug( "Settings page selected: " + page );
		if( page == null ) return;

		getSettings().set( PAGE_ID, page.getId() );

		SettingsPanel panel = new SettingsPanel( getProduct(), page );
		ScrollPane scroller = new ScrollPane( panel );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

}
