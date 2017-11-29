package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.tool.AbstractTool;
import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.tool.Guide;
import com.xeomar.xenon.tool.GuideNode;
import com.xeomar.xenon.workarea.ToolException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class SettingsTool extends AbstractTool {

	private static final Logger log = LoggerFactory.getLogger( SettingsTool.class );

	private GuideListener guideListener = new GuideListener();

	public SettingsTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-settings" );
		setTitle( product.getResourceBundle().getString( "tool", "settings-name" ) );
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
		guide.selectedItemProperty().addListener( guideListener );

		resourceRefreshed();
	}

	@Override
	public void resourceRefreshed() {
	}

	private void selectItem( TreeItem<GuideNode> item ) {
		if( item == null ) return;
		selectPage( item.getValue().getPage() );
	}

	private void selectPage( SettingsPage page ) {
		log.debug( "Settings page selected: " + page );
		if( page == null ) return;

		SettingsPanel panel = new SettingsPanel( getProduct(), page );
		ScrollPane scroller = new ScrollPane( panel );
		scroller.setFitToWidth( true );
		getChildren().clear();
		getChildren().add( scroller );
	}

	public Set<URI> getResourceDependencies() {
		Set<URI> resources = new HashSet<>();
		resources.add( ProgramGuideType.uri );
		return resources;
	}

	private class GuideListener implements ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldSelection, TreeItem<GuideNode> newSelection ) {
			selectItem( newSelection );
		}

	}

}
