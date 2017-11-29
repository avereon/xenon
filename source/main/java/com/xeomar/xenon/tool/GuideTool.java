package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import com.xeomar.xenon.workarea.WorkpaneToolEvent;
import com.xeomar.xenon.workarea.Tool;
import com.xeomar.xenon.workarea.ToolException;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class GuideTool extends AbstractTool {

	private TreeView guideView;

	private SelectedItemListener selectedItemListener;

	private ActiveGuideListener activeGuideListener;

	public GuideTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-guide" );
		setTitle( product.getResourceBundle().getString( "tool", "guide-name" ) );
		getChildren().add( guideView = new TreeView() );
		guideView.setShowRoot( false );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	protected void resourceReady() throws ToolException {
		// Connect to the resource guide
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		if( guide == null ) return;

		// Set guide tree root
		TreeItem root = guide.getRoot();
		if( root != null ) guideView.setRoot( root );
	}

	@Override
	protected void resourceRefreshed() {
		// Update the guide? Or will the guide be updated by the resource?
	}

	@Override
	protected void allocate() throws ToolException {
		// Attach to the workpane and listen for current tool changes
		getWorkpane().addWorkpaneListener( this::switchGuide );

		Tool activeTool = getWorkpane().getActiveTool();
		if( activeTool != null ) setResourceGuide( activeTool.getResource() );
	}

	@Override
	protected void deallocate() throws ToolException {
		// Disconnect from the resource guide
	}

	private void switchGuide( WorkpaneEvent event ) {
		if( !(event instanceof WorkpaneToolEvent) ) return;

		WorkpaneToolEvent toolEvent = (WorkpaneToolEvent)event;
		switch( event.getType() ) {
			case TOOL_ACTIVATED: {
				setResourceGuide( toolEvent.getTool().getResource() );
				break;
			}
			case TOOL_CONCEALED: {
				setResourceGuide( null );
			}
		}

		if( event.getType() != WorkpaneEvent.Type.TOOL_ACTIVATED ) return;

		setResourceGuide( toolEvent.getTool().getResource() );
	}

	@SuppressWarnings( "unchecked" )
	private void setResourceGuide( Resource resource ) {
		if( resource == null ) {
			guideView.setRoot( null );
			return;
		}

		Guide guide = resource.getResource( Guide.GUIDE_KEY );
		if( guide == null ) return;

		// Set the guide view root
		guideView.setRoot( guide.getRoot() );

		// Set the guide view selection mode
		guideView.getSelectionModel().setSelectionMode( guide.getSelectionMode() );

		// Add the selected item listener
		if( selectedItemListener != null ) guideView.getSelectionModel().selectedItemProperty().removeListener( selectedItemListener );
		guideView.getSelectionModel().selectedItemProperty().addListener( selectedItemListener = new SelectedItemListener( guide ) );

		// Add the guide active property listener
		if( activeGuideListener != null ) guide.activeProperty().removeListener( activeGuideListener );
		guide.activeProperty().addListener( activeGuideListener = new ActiveGuideListener( guide ) );
	}

	private static class SelectedItemListener implements javafx.beans.value.ChangeListener<TreeItem> {

		private Guide guide;

		SelectedItemListener( Guide guide ) {
			this.guide = guide;
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public void changed( ObservableValue observable, TreeItem oldSelection, TreeItem newSelection ) {
			guide.setSelectedItem( newSelection );
		}

	}

	private class ActiveGuideListener implements javafx.beans.value.ChangeListener<Boolean> {

		private Guide guide;

		ActiveGuideListener( Guide guide ) {
			this.guide = guide;
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public void changed( ObservableValue observable, Boolean oldSelection, Boolean newSelection ) {
			if( !newSelection ) guideView.setRoot( null );
		}

	}

}
