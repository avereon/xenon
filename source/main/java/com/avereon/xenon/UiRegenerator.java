package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.util.TestUtil;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * This class is intended only to regenerate the UI before
 */
class UiRegenerator {

	private static final Logger log = Log.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private UiFactory factory;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> areas = new HashMap<>();

	private Map<String, Workpane> panes = new HashMap<>();

	private Map<String, WorkpaneEdge> edges = new HashMap<>();

	private Map<String, WorkpaneView> views = new HashMap<>();

	private Map<String, Tool> tools = new HashMap<>();

	private Map<WorkpaneView, Set<ProgramTool>> viewTools = new HashMap<>();

	private boolean restored;

	private Lock restoreLock = new ReentrantLock();

	private Condition restoredCondition = restoreLock.newCondition();

	UiRegenerator( Program program ) {
		this.program = program;
		this.factory = new UiFactory( program );
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
			program.getAssetManager().openAssetsAndWait( assets );
			program.getAssetManager().loadAssets( assets );
		} catch( Exception exception ) {
			program.getNoticeManager().error( exception );
		}
	}

	private void createDefaultWorkspace() {
		// Create the default workspace
		Workspace workspace = factory.newWorkspace();
		program.getWorkspaceManager().setActiveWorkspace( workspace );

		// Create the default workarea
		Workarea workarea = factory.newWorkarea();
		workarea.setName( "Default" );
		workspace.setActiveWorkarea( workarea );

		if( !TestUtil.isTest() ) program.getAssetManager().openAsset( ProgramWelcomeType.URI );
	}

	private void restoreWorkspaces( SplashScreenPane splashScreen, List<String> workspaceIds ) {
		List<String> areaIds = getUiSettingsIds( ProgramSettings.AREA );
		List<String> edgeIds = getUiSettingsIds( ProgramSettings.EDGE );
		List<String> viewIds = getUiSettingsIds( ProgramSettings.VIEW );
		List<String> toolIds = getUiSettingsIds( ProgramSettings.TOOL );

		// Create the workspaces (includes the window)
		for( String id : workspaceIds ) {
			restoreWorkspace( id );
			//splashScreen.update();
		}

		// Create the workareas (includes the workpane)
		for( String id : areaIds ) {
			restoreWorkarea( id );
			//splashScreen.update();
		}

		// Create the workpane edges
		for( String id : edgeIds ) {
			restoreWorkpaneEdge( id );
			//splashScreen.update();
		}

		// Create the workpane views
		for( String id : viewIds ) {
			restoreWorkpaneView( id );
			//splashScreen.update();
		}

		// Create the tools
		for( String id : toolIds ) {
			restoreWorktool( id );
			splashScreen.update();
		}

		linkWorkareas();
		linkEdgesAndViews();
		linkTools();
	}

	private void linkWorkareas() {
		// Link the workareas to the workspaces
		for( Workarea workarea : areas.values() ) {
			Settings settings = workarea.getSettings();
			Workspace workspace = workspaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );
			workspace.addWorkarea( workarea );
			if( workarea.isActive() ) workspace.setActiveWorkarea( workarea );
		}
	}

	private void linkEdgesAndViews() {
		Map<Workpane, Set<Node>> workpaneNodes = new HashMap<>();

		// Link the edges
		for( WorkpaneEdge edge : edges.values() ) {
			Settings settings = edge.getSettings();
			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkEdge( edge ) ) {
					Set<Node> nodes = workpaneNodes.computeIfAbsent( workpane, k -> new HashSet<>() );
					nodes.add( edge );
				} else {
					log.debug( "Removing invalid workpane edge settings: " + settings.getName() );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.warn( "Error linking edge: " + edge.getEdgeId(), exception );
				return;
			}
		}

		// Link the views
		for( WorkpaneView view : views.values() ) {
			Settings settings = view.getSettings();
			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );
			try {
				if( linkView( view ) ) {
					Set<Node> nodes = workpaneNodes.computeIfAbsent( workpane, k -> new HashSet<>() );
					nodes.add( view );
				} else {
					log.debug( "Removing invalid workpane edge settings: " + settings.getName() );
					settings.delete();
				}
			} catch( Exception exception ) {
				log.warn( "Error linking view: " + view.getViewId(), exception );
				return;
			}
		}

		// Restore edges and views to workpane
		for( Workpane pane : panes.values() ) {
			Set<Node> nodes = workpaneNodes.get( pane );
			if( nodes != null ) pane.restoreNodes( nodes );
		}
	}

	private boolean linkEdge( WorkpaneEdge edge ) {
		Settings settings = edge.getSettings();
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
		Settings settings = view.getSettings();
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

				Settings settings = program.getSettingsManager().getSettings( ProgramSettings.TOOL, tool.getUid() );
				if( settings.get( "active", Boolean.class, false ) ) activeTool = tool;

				log.debug( "Tool restored: " + tool.getClass() + ": " + tool.getAsset().getUri() );
			}
		}

		if( activeTool != null ) activeTool.getWorkpane().setActiveTool( activeTool );
	}

	private List<String> getUiSettingsIds( String path ) {
		return program.getSettingsManager().getSettings( path ).getNodes();
	}

	private void restoreWorkspace( String id ) {
		log.debug( "Restoring workspace: " + id );
		try {
			Workspace workspace = new Workspace( program );

			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );
			workspace.setSettings( settings );

			if( workspace.isActive() ) {
				program.getWorkspaceManager().setActiveWorkspace( workspace );
			} else {
				program.getWorkspaceManager().addWorkspace( workspace );
			}

			workspaces.put( id, workspace );
		} catch( Exception exception ) {
			log.error( "Error restoring workspace", exception );
		}
	}

	private void restoreWorkarea( String id ) {
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, id );
		Workspace workspace = workspaces.get( settings.get( UiFactory.PARENT_WORKSPACE_ID ) );

		// If the workspace is not found, then the workarea is orphaned...delete the settings
		if( workspace == null ) {
			log.debug( "Removing orphaned workarea settings: " + id );
			program.getSettingsManager().getSettings( ProgramSettings.PANE, id ).delete();
			settings.delete();
			return;
		}

		Workarea workarea = factory.newWorkarea( id );
		panes.put( id, workarea.getWorkpane() );
		areas.put( id, workarea );
	}

	private void restoreWorkpaneEdge( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.EDGE, id );

			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( workpane == null ) {
				log.debug( "Removing orphaned workpane edge settings: " + id );
				settings.delete();
				return;
			}

			Orientation orientation = Orientation.valueOf( settings.get( "orientation" ).toUpperCase() );
			WorkpaneEdge edge = new WorkpaneEdge( orientation );
			edge.setSettings( settings );

			edges.put( id, edge );
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
	}

	private void restoreWorkpaneView( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.VIEW, id );

			Workpane workpane = panes.get( settings.get( UiFactory.PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( workpane == null ) {
				log.debug( "Removing orphaned workpane view settings: " + id );
				settings.delete();
				return;
			}

			WorkpaneView view = new WorkpaneView();
			view.setSettings( settings );

			views.put( id, view );
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
	}

	private void restoreWorktool( String id ) {
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.TOOL, id );
		String toolType = settings.get( Tool.SETTINGS_TYPE_KEY );
		URI uri = settings.get( Asset.SETTINGS_URI_KEY, URI.class );
		String assetTypeKey = settings.get( Asset.SETTINGS_TYPE_KEY );
		AssetType assetType = assetTypeKey == null ? null : program.getAssetManager().getAssetType( assetTypeKey );
		WorkpaneView view = views.get( settings.get( UiFactory.PARENT_WORKPANEVIEW_ID ) );

		try {
			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				log.warn( "Removing orphaned tool: " + "id=" + id + " type=" + toolType );
				settings.delete();
				return;
			}

			// Create the open asset request
			OpenAssetRequest openAssetRequest = new OpenAssetRequest();

			// Create an open tool request
			OpenToolRequest openToolRequest = new OpenToolRequest( openAssetRequest ).setId( id );
			openToolRequest.setAsset( program.getAssetManager().createAsset( assetType, uri ) );

			// Restore the tool
			ProgramTool tool = program.getToolManager().restoreTool( openToolRequest, toolType );
			if( tool == null ) {
				log.warn( "Removing unknown tool: " + "id=" + id + " type=" + toolType );
				settings.delete();
				return;
			}

			Set<ProgramTool> viewToolSet = viewTools.computeIfAbsent( view, k -> new HashSet<>() );
			viewToolSet.add( tool );

			tools.put( tool.getUid(), tool );
		} catch( Exception exception ) {
			log.error( "Error restoring tool: type=" + toolType, exception );
		}
	}

}
