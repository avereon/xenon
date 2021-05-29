package com.avereon.xenon.tool;

import com.avereon.event.EventHandler;
import com.avereon.product.Rb;
import com.avereon.util.Log;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.PropertiesToolEvent;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.settings.SettingsPage;
import com.avereon.xenon.tool.settings.SettingsPanel;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.ScrollPane;

/**
 * This tool listens for "show properties" and "hide properties" events that allow the user to edit the properties of an object using the settings API.
 */
public class PropertiesTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private final ScrollPane scroller;

	private final EventHandler<PropertiesToolEvent> showHandler;

	private final EventHandler<PropertiesToolEvent> hideHandler;

	public PropertiesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-properties" );

		scroller = new ScrollPane();
		scroller.setFitToWidth( true );
		getChildren().addAll( scroller );
		this.showHandler = e -> Fx.run( () -> showPage( e.getPage() ) );
		this.hideHandler = e -> Fx.run( this::hidePage );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

	@Override
	public boolean changeCurrentAsset() {
		return false;
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( BundleKey.TOOL, "properties-name" ) );
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
		if( event != null && event.getEventType() == PropertiesToolEvent.SHOW && isEmpty() ) showPage( event.getPage() );
	}

	@Override
	protected void deallocate() {
		getWorkspace().getEventBus().unregister( PropertiesToolEvent.HIDE, hideHandler );
		getWorkspace().getEventBus().unregister( PropertiesToolEvent.SHOW, showHandler );
	}

	private boolean isEmpty() {
		return scroller.getContent() == null;
	}

	private void showPage( SettingsPage page ) {
		page.setOptionProviders( getProgram().getSettingsManager().getOptionProviders() );
		scroller.setContent( new SettingsPanel( page ) );
	}

	private void hidePage() {
		scroller.setContent( null );
	}

}
