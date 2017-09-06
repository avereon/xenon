package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.ProductTool;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.SettingsManager;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.tool.Guide;
import com.xeomar.xenon.tool.GuideNode;
import com.xeomar.xenon.worktool.ToolException;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SettingsTool extends ProductTool {

	private static final Logger log = LoggerFactory.getLogger( SettingsTool.class );

	public SettingsTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-settings" );
		setTitle( product.getResourceBundle().getString( "tool", "settings-name" ) );

		// I suppose all the settings pages can be created here. The
		// pages are not part of the resource so they can be used here.
		//List<SettingsPage> pages = ((Program)getProduct()).getSettingsManager().getSettingsPages();
	}

	@Override
	protected void allocate() throws ToolException {
		log.debug( "Settings tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.debug( "Settings tool display" );
	}

	@Override
	protected void activate() throws ToolException {
		log.debug( "Settings tool activate" );
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.setActive( true );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.debug( "Settings tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.debug( "Settings tool conceal" );
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.setActive( false );
	}

	@Override
	protected void deallocate() throws ToolException {
		log.debug( "Settings tool deallocate" );
	}

	@Override
	protected void resourceReady() throws ToolException {
		log.debug( "Settings tool resource ready" );

		// Register the guide selection listener
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		guide.selectedItemProperty().addListener( ( obs, oldSelection, newSelection ) -> {
			selectedPage( newSelection );
		} );

		SettingsManager manager = ((Program)getProduct()).getSettingsManager();
		Map<String,SettingsPage> pages = manager.getSettingsPages();


		resourceRefreshed();
	}

	private void selectedPage( TreeItem<GuideNode> item ) {
		if( item == null ) return;
		selectedPage( item.getValue().getId() );
	}

	private void selectedPage( String item ) {
		getChildren().clear();
		log.warn( "Settings page selected: " + item );
		// TODO if( item != null ) getChildren().add( nodes.get( item ) );
	}

	public Set<String> getResourceDependencies() {
		Set<String> resources = new HashSet<>();
		resources.add( ProgramGuideType.URI );
		return resources;
	}

}
