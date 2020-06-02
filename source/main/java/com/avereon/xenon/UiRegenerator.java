package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.util.TestUtil;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

import java.lang.System.Logger;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class is intended only to regenerate the UI before
 */
class UiRegenerator {

	private static final Logger log = Log.get();

	private final Program program;

	private final UiFactory factory;

	private final Map<String, Workspace> workspaces = new HashMap<>();

	private final Map<String, Workarea> areas = new HashMap<>();

	private final Map<String, Workpane> panes = new HashMap<>();

	private final Map<String, WorkpaneEdge> edges = new HashMap<>();

	private final Map<String, WorkpaneView> views = new HashMap<>();

	private final Map<String, Tool> tools = new HashMap<>();

	private final Map<WorkpaneView, Set<ProgramTool>> viewTools = new HashMap<>();

	private final Lock restoreLock = new ReentrantLock();

	private final Condition restoredCondition = restoreLock.newCondition();

	private boolean restored;

	UiRegenerator( Program program ) {
		this.program = program;
		this.factory = new UiFactory( program );
	}

	private Program getProgram() {
		return program;
	}

	//	int getUiObjectCount() {
	//		int s = getSettingsFiles( Prefix.WORKSPACE ).length;
	//		int a = getSettingsFiles( Prefix.WORKAREA ).length;
	//		int p = getSettingsFiles( Prefix.WORKPANE ).length;
	//		int t = getSettingsFiles( Prefix.WORKTOOL ).length;
	//		return Math.max( 2, s + a + p + t );
	//	}

	int getToolCount() {
		return getUiSettingsIds( ProgramSettings.TOOL ).size();
	}

	void restore( SplashScreenPane splashScreen ) {
		restoreLock.lock();
		try {
			List<String> workspaceIds = getUiSettingsIds( ProgramSettings.WORKSPACE );
			if( workspaceIds.size() == 0 ) {
				createDefaultWorkspace();
			} else {
				restoreWorkspaces( splashScreen, workspaceIds );
			}
			getProgram().getWorkspaceManager().setUiReady( true );
		} finally {
			restored = true;
			restoredCondition.signalAll();
			restoreLock.unlock();
		}
	}

	@SuppressWarnings( "SameParameterValue" )
	void awaitRestore( long timeout, TimeUnit unit ) throws InterruptedException {
		restoreLock.lock();
		try {
			while( !restored ) {
				restoredCondition.await( timeout, unit );
			}
		} finally {
			restoreLock.unlock();
		}
	}

	void startAssetLoading() {
		Collection<Asset> assets = tools.values().stream().map( Tool::getAsset ).collect( Collectors.toList() );
		try {
			getProgram().getAssetManager().openAssetsAndWait( assets );
			getProgram().getAssetManager().loadAssets( assets );
		} catch( Exception exception ) {
			log.log( Log.ERROR, exception );
		}
	}

	private Workspace createDefaultWorkspace() {
		// Create the default workspace
		Workspace workspace = getProgram().getWorkspaceManager().newWorkspace();
		getProgram().getWorkspaceManager().setActiveWorkspace( workspace );

		// Create the default workarea
		Workarea workarea = factory.newWorkarea();
		workarea.setName( "Default" );
		workspace.setActiveWorkarea( workarea );

		if( !TestUtil.isTest() ) getProgram().getAssetManager().openAsset( ProgramWelcomeType.URI );

		return workspace;
	}

	private void restoreWorkspaces( SplashScreenPane splashScreen, List<String> workspaceIds ) {
		// Create the workspaces (includes the window)
		workspaceIds.forEach( this::restoreWorkspace );

		// Create the workareas (includes the workpane)
		getUiSettingsIds( ProgramSettings.AREA ).forEach( this::restoreWorkarea );

		// Create the workpane edges
		getUiSettingsIds( ProgramSettings.EDGE ).forEach( this::restoreWorkpaneEdge );

		// Create the workpane views
		getUiSettingsIds( ProgramSettings.VIEW ).forEach( this::restoreWorkpaneView );

		// Create the tools
		getUiSettingsIds( ProgramSettings.TOOL ).forEach( id -> {
			restoreWorktool( id );
			splashScreen.update();
		} );

		linkWorkareas();
		linkEdgesAndViews();
		linkTools();
	}

	private void linkWorkareas() {
		// Link the workareas to the workspaces
		for( Workarea workarea : areas.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, workarea.getProductId() );
			Workspace workspace = workspaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );
			workspace.addWorkarea( workarea );
			if( workarea.isActive() ) workspace.setActiveWorkarea( workarea );
		}
	}

	private void linkEdgesAndViews() {
		Map<Workpane, Set<WorkpaneEdge>> workpaneEdges = new HashMap<>();
		Map<Workpane, Set<WorkpaneView>> workpaneViews = new HashMap<>();

		// Link the edges
		for( WorkpaneEdge edge : edges.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getProductId() );
			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkEdge( edge ) ) {
					workpaneEdges.computeIfAbsent( workpane, k -> new HashSet<>() ).add( edge );
				} else {
					log.log( Log.DEBUG, "Removing invalid workpane edge settings: " + settings.getName() );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.log( Log.WARN, "Error linking edge: " + edge.getProductId(), exception );
				return;
			}
		}

		// Link the views
		for( WorkpaneView view : views.values() ) {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, view.getProductId() );
			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkView( view ) ) {
					workpaneViews.computeIfAbsent( workpane, k -> new HashSet<>() ).add( view );
				} else {
					log.log( Log.DEBUG, "Removing invalid workpane edge settings: " + settings.getName() );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.log( Log.WARN, "Error linking view: " + view.getProductId(), exception );
				return;
			}
		}

		//		Set<Workpane> panes = new HashSet<>();
		//		panes.addAll( workpaneEdges.keySet() );
		//		panes.addAll( workpaneViews.keySet() );

		// Restore edges and views to workpane
		for( Workpane pane : panes.values() ) {
			Set<WorkpaneEdge> edges = workpaneEdges.computeIfAbsent( pane, k -> new HashSet<>() );
			Set<WorkpaneView> views = workpaneViews.computeIfAbsent( pane, k -> new HashSet<>() );
			pane.restoreNodes( edges, views );

			// FIXME Default view has already been overwritten in the settings and is getting lost
			// Active, default and maximized views
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, pane.getProductId() );
			setView( settings, "view-active", pane::setActiveView );
			setView( settings, "view-default", pane::setDefaultView );
			setView( settings, "view-maximized", pane::setMaximizedView );
		}
	}

	private void setView( Settings settings, String key, Consumer<WorkpaneView> handler ) {
		String viewId = settings.get( key );
		WorkpaneView view = viewId == null ? null : views.get( viewId );
		if( view != null ) handler.accept( view );
		if( "view-default".equals( key ) && view == null ) log.log( Log.ERROR, "The default view was not restored. This will cause a UI problem." );
	}

	private boolean linkEdge( WorkpaneEdge edge ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, edge.getProductId() );
		Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

		switch( edge.getOrientation() ) {
			case VERTICAL: {
				WorkpaneEdge t = lookupEdge( workpane, settings.get( "t" ) );
				WorkpaneEdge b = lookupEdge( workpane, settings.get( "b" ) );
				if( t == null || b == null ) return false;
				edge.setEdge( Side.TOP, t );
				edge.setEdge( Side.BOTTOM, b );
				break;
			}
			case HORIZONTAL: {
				WorkpaneEdge l = lookupEdge( workpane, settings.get( "l" ) );
				WorkpaneEdge r = lookupEdge( workpane, settings.get( "r" ) );
				if( l == null || r == null ) return false;
				edge.setEdge( Side.LEFT, l );
				edge.setEdge( Side.RIGHT, r );
				break;
			}
		}

		return true;
	}

	private boolean linkView( WorkpaneView view ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, view.getProductId() );
		Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

		WorkpaneEdge t = lookupEdge( workpane, settings.get( "t" ) );
		WorkpaneEdge l = lookupEdge( workpane, settings.get( "l" ) );
		WorkpaneEdge r = lookupEdge( workpane, settings.get( "r" ) );
		WorkpaneEdge b = lookupEdge( workpane, settings.get( "b" ) );

		if( t == null || l == null || r == null || b == null ) return false;

		view.setEdge( Side.TOP, t );
		view.setEdge( Side.LEFT, l );
		view.setEdge( Side.RIGHT, r );
		view.setEdge( Side.BOTTOM, b );

		return true;
	}

	private WorkpaneEdge lookupEdge( Workpane pane, String name ) {
		if( pane == null ) throw new NullPointerException( "Workpane cannot be null" );
		if( name == null ) throw new NullPointerException( "Edge name cannot be null" );

		WorkpaneEdge edge = edges.get( name );

		if( edge == null ) {
			try {
				edge = pane.getWallEdge( Side.valueOf( name.toUpperCase() ) );
			} catch( IllegalArgumentException exception ) {
				// Intentionally ignore exception
			}
		}

		return edge;
	}

	private void linkTools() {
		Tool activeTool = null;

		for( WorkpaneView view : viewTools.keySet() ) {
			Workpane pane = view.getWorkpane();
			if( pane == null ) continue;

			List<ProgramTool> tools = new ArrayList<>( viewTools.get( view ) );

			// Sort the tools
			tools.sort( new ToolOrderComparator() );

			// Add the tools to the view
			for( ProgramTool tool : tools ) {
				pane.addTool( tool, view, tool.isActive() );

				Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, tool.getUid() );
				if( settings.get( "active", Boolean.class, false ) ) activeTool = tool;

				log.log( Log.DEBUG, "Tool restored: " + tool.getClass() + ": " + tool.getAsset().getUri() );
			}
		}

		if( activeTool != null ) activeTool.getWorkpane().setActiveTool( activeTool );
	}

	private List<String> getUiSettingsIds( String path ) {
		return getProgram().getSettingsManager().getSettings( path ).getNodes();
	}

	private void restoreWorkspace( String id ) {
		log.log( Log.DEBUG, "Restoring workspace: " + id );
		try {
			Workspace workspace = getProgram().getWorkspaceManager().newWorkspace( id );
			getProgram().getWorkspaceManager().addWorkspace( workspace );
			if( workspace.isActive() ) getProgram().getWorkspaceManager().setActiveWorkspace( workspace );

			workspaces.put( id, workspace );
		} catch( Exception exception ) {
			log.log( Log.ERROR, "Error restoring workspace", exception );
		}
	}

	private void restoreWorkarea( String id ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.AREA, id );
		Workspace workspace = workspaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

		// If the workspace is not found, then the workarea is orphaned...delete the settings
		if( workspace == null ) {
			log.log( Log.DEBUG, "Removing orphaned workarea settings: " + id );
			getProgram().getSettingsManager().getSettings( ProgramSettings.PANE, id ).delete();
			settings.delete();
			return;
		}

		Workarea workarea = factory.newWorkarea( id, true );
		panes.put( id, workarea.getWorkpane() );
		areas.put( id, workarea );
	}

	private void restoreWorkpaneEdge( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.EDGE, id );
			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( workpane == null ) {
				log.log( Log.DEBUG, "Removing orphaned workpane edge settings: " + id );
				settings.delete();
				return;
			}

			Orientation orientation = Orientation.valueOf( settings.get( "orientation" ).toUpperCase() );
			WorkpaneEdge edge = new WorkpaneEdge( orientation );
			edge.setProductId( id );
			if( settings.exists( "position" ) ) edge.setPosition( settings.get( "position", Double.class ) );
			edge.setPosition( settings.get( "position", Double.class ) );
			edges.put( edge.getProductId(), edge );
		} catch( Exception exception ) {
			log.log( Log.ERROR, "Error restoring workpane", exception );
		}
	}

	private void restoreWorkpaneView( String id ) {
		try {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.VIEW, id );
			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( workpane == null ) {
				log.log( Log.DEBUG, "Removing orphaned workpane view settings: " + id );
				settings.delete();
				return;
			}

			WorkpaneView view = new WorkpaneView();
			view.setProductId( id );
			if( settings.exists( "placement" ) ) view.setPlacement( Workpane.Placement.valueOf( settings.get( "placement" ).toUpperCase() ) );

			views.put( view.getProductId(), view );
		} catch( Exception exception ) {
			log.log( Log.ERROR, "Error restoring workpane", exception );
		}
	}

	private void restoreWorktool( String id ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, id );
		String toolType = settings.get( Tool.SETTINGS_TYPE_KEY );
		URI uri = settings.get( Asset.SETTINGS_URI_KEY, URI.class );
		String assetTypeKey = settings.get( Asset.SETTINGS_TYPE_KEY );
		AssetType assetType = assetTypeKey == null ? null : getProgram().getAssetManager().getAssetType( assetTypeKey );
		WorkpaneView view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );

		try {
			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				log.log( Log.WARN, "Removing orphaned tool: " + "id=" + id + " type=" + toolType );
				settings.delete();
				return;
			}

			// Create the open asset request
			OpenAssetRequest openAssetRequest = new OpenAssetRequest();
			openAssetRequest.setAsset( getProgram().getAssetManager().createAsset( assetType, uri ) );
			openAssetRequest.setToolId( id );

			// Restore the tool
			ProgramTool tool = getProgram().getToolManager().restoreTool( openAssetRequest, toolType );
			if( tool == null ) {
				log.log( Log.WARN, "Removing unknown tool: " + "id=" + id + " type=" + toolType );
				settings.delete();
				return;
			}

			Set<ProgramTool> viewToolSet = viewTools.computeIfAbsent( view, k -> new HashSet<>() );
			viewToolSet.add( tool );

			tools.put( tool.getUid(), tool );
		} catch( Exception exception ) {
			log.log( Log.ERROR, "Error restoring tool: type=" + toolType, exception );
		}
	}

}
