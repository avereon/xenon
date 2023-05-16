package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.product.Rb;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.PropertiesToolEvent;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.settings.SettingOptionProvider;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPanel;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import lombok.CustomLog;

import java.util.List;
import java.util.Map;

/**
 * This tool listens for "show properties" and "hide properties" events that
 * allow the user to edit the properties of an object using the settings API.
 */
@CustomLog
public class PropertiesTool extends ProgramTool {

	private final ScrollPane scroller;

	private final EventHandler<PropertiesToolEvent> showHandler;

	private final EventHandler<PropertiesToolEvent> hideHandler;

	public PropertiesTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-properties" );

		scroller = new ScrollPane();
		scroller.setFitToWidth( true );
		getChildren().addAll( scroller );
		this.showHandler = e -> Fx.run( () -> showPage( e.getPages() ) );
		this.hideHandler = e -> Fx.run( this::hidePage );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DEFAULT;
	}

	@Override
	public boolean changeCurrentAsset() {
		return false;
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( RbKey.TOOL, "properties-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "properties" ) );
	}

	@Override
	protected void allocate() {
		getWorkspace().getEventBus().register( PropertiesToolEvent.SHOW, showHandler );
		getWorkspace().getEventBus().register( PropertiesToolEvent.HIDE, hideHandler );
	}

	@Override
	protected void activate() {
		PropertiesToolEvent event = getWorkspace().getEventBus().getPriorEvent( PropertiesToolEvent.class );
		if( event != null && event.getEventType() == PropertiesToolEvent.SHOW && isEmpty() ) showPage( event.getPages() );
	}

	@Override
	protected void deallocate() {
		getWorkspace().getEventBus().unregister( PropertiesToolEvent.HIDE, hideHandler );
		getWorkspace().getEventBus().unregister( PropertiesToolEvent.SHOW, showHandler );
	}

	private boolean isEmpty() {
		return scroller.getContent() == null;
	}

	private void showPage( List<SettingsPage> pages ) {
		Map<String, SettingOptionProvider> optionProviders = getProgram().getSettingsManager().getOptionProviders();

		// Create a new VBox for all the pages
		VBox container = new VBox();
		container.getChildren().addAll( pages.stream().map( p -> {
			p.setOptionProviders( optionProviders );
			return new SettingsPanel( p );
		} ).toList() );

		// Add the container to the scroller
		Fx.run( () -> scroller.setContent( container ) );
	}

	private void hidePage() {
		Fx.run( () -> scroller.setContent( null ) );
	}

}
