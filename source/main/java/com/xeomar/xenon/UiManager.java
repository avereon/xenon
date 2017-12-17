package com.xeomar.xenon;

import com.xeomar.settings.Settings;
import com.xeomar.util.IdGenerator;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.resource.OpenResourceRequest;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.*;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.BorderStroke;
import org.slf4j.Logger;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UiManager {

	public static final double DEFAULT_WIDTH = 960;

	public static final double DEFAULT_HEIGHT = 540;

	public static final double PAD = BorderStroke.THICK.getTop();

	public static final String PARENT_WORKSPACE_ID = "workspace-id";

	public static final String PARENT_WORKAREA_ID = "workarea-id";

	public static final String PARENT_WORKPANE_ID = "workpane-id";

	public static final String PARENT_WORKPANEVIEW_ID = "workpaneview-id";

	private static final int RESTORE_TOOL_TIMEOUT = 10;

	private static Logger log = LogUtil.get( UiManager.class );

	private Program program;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> areas = new HashMap<>();

	private Map<String, Workpane> panes = new HashMap<>();

	private Map<String, WorkpaneEdge> edges = new HashMap<>();

	private Map<String, WorkpaneView> views = new HashMap<>();

	private Map<String, Tool> tools = new HashMap<>();

	private Map<WorkpaneView, Set<ProgramTool>> viewTools = new HashMap<>();

	private boolean started;

	private Lock restoreLock = new ReentrantLock();

	private Condition startedCondition = restoreLock.newCondition();

	public UiManager( Program program ) {
		this.program = program;
	}

	//	public int getUiObjectCount() {
	//		int s = getSettingsFiles( Prefix.WORKSPACE ).length;
	//		int a = getSettingsFiles( Prefix.WORKAREA ).length;
	//		int p = getSettingsFiles( Prefix.WORKPANE ).length;
	//		int t = getSettingsFiles( Prefix.WORKTOOL ).length;
	//		return Math.max( 2, s + a + p + t );
	//	}

	public int getToolCount() {
		return getChildNodeNames( ProgramSettings.TOOL ).size();
	}

	public void restoreUi( SplashScreenPane splashScreen ) {
		restoreLock.lock();
		try {
			List<String> workspaceIds = getChildNodeNames( ProgramSettings.WORKSPACE );
			if( workspaceIds.size() == 0 ) {
				createDefaultWorkspace();
			} else {
				restoreWorkspaces( splashScreen, workspaceIds );
			}
		} finally {
			started = true;
			startedCondition.signalAll();
			restoreLock.unlock();
		}
	}

	public void awaitRestore( long timeout, TimeUnit unit ) throws InterruptedException {
		restoreLock.lock();
		try {
			while( !started ) {
				startedCondition.await( timeout, unit );
			}
		} finally {
			restoreLock.unlock();
		}
	}

	public Workspace newWorkspace() throws Exception {
		String id = IdGenerator.getId();
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );

		// Intentionally do not set the x property
		// Intentionally do not set the y property
		settings.set( "w", DEFAULT_WIDTH );
		settings.set( "h", DEFAULT_HEIGHT );

		Workspace workspace = new Workspace( program );
		workspace.setSettings( settings );
		return workspace;
	}

	public Workarea newWorkarea() throws Exception {
		String id = IdGenerator.getId();
		Workarea workarea = new Workarea();

		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, id );
		workarea.setSettings( settings );

		Settings workpaneSettings = program.getSettingsManager().getSettings( ProgramSettings.PANE, id );
		workpaneSettings.set( PARENT_WORKAREA_ID, id );
		workarea.getWorkpane().setSettings( workpaneSettings );

		Settings viewSettings = program.getSettingsManager().getSettings( ProgramSettings.VIEW, id );
		viewSettings.set( PARENT_WORKPANE_ID, id );
		viewSettings.set( "t", Side.TOP.name().toLowerCase() );
		viewSettings.set( "l", Side.LEFT.name().toLowerCase() );
		viewSettings.set( "r", Side.RIGHT.name().toLowerCase() );
		viewSettings.set( "b", Side.BOTTOM.name().toLowerCase() );
		workarea.getWorkpane().getDefaultView().setSettings( viewSettings );

		return workarea;
	}

	private void createDefaultWorkspace() {
		try {
			// Create the default workspace
			Workspace workspace = newWorkspace();
			program.getWorkspaceManager().setActiveWorkspace( workspace );
			//splashScreen.update();

			// Create the default workarea
			Workarea workarea = newWorkarea();
			workarea.setName( "Default" );
			workspace.setActiveWorkarea( workarea );
			//splashScreen.update();

			// TODO Create default workarea panes

			// TODO Create default tools

		} catch( Exception exception ) {
			log.error( "Error creating default workspace", exception );
		}
	}

	private void restoreWorkspaces( SplashScreenPane splashScreen, List<String> workspaceIds ) {
		List<String> areaIds = getChildNodeNames( ProgramSettings.AREA );
		List<String> edgeIds = getChildNodeNames( ProgramSettings.EDGE );
		List<String> viewIds = getChildNodeNames( ProgramSettings.VIEW );
		List<String> toolIds = getChildNodeNames( ProgramSettings.TOOL );

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
		cleanup();
	}

	private void linkWorkareas() {
		// Link the workareas to the workspaces
		for( Workarea workarea : areas.values() ) {
			Settings settings = workarea.getSettings();
			Workspace workspace = workspaces.get( settings.get( PARENT_WORKSPACE_ID ) );
			workspace.addWorkarea( workarea );
			if( workarea.isActive() ) workspace.setActiveWorkarea( workarea );
		}
	}

	private void linkEdgesAndViews() {
		Map<Workpane, Set<Node>> workpaneNodes = new HashMap<>();

		// Link the edges
		for( WorkpaneEdge edge : edges.values() ) {
			Settings settings = edge.getSettings();
			Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );
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
			Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );
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
		Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );

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
		Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );

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
				if( tool.getSettings().getBoolean( "active", false ) ) activeTool = tool;

				log.debug( "Tool restored: " + tool.getClass() + ": " + tool.getResource().getUri() );
			}
		}

		if( activeTool != null ) activeTool.getWorkpane().setActiveTool( activeTool );
	}

	private List<String> getChildNodeNames( String path ) {
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
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, id );
			Settings workpaneSettings = program.getSettingsManager().getSettings( ProgramSettings.PANE, id );

			Workspace workspace = workspaces.get( settings.get( PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( workspace == null ) {
				log.debug( "Removing orphaned workarea settings: " + id );
				workpaneSettings.delete();
				settings.delete();
				return;
			}

			Workarea workarea = new Workarea();
			workarea.setSettings( settings );

			workpaneSettings.set( PARENT_WORKAREA_ID, id );
			workarea.getWorkpane().setSettings( workpaneSettings );

			panes.put( id, workarea.getWorkpane() );
			areas.put( id, workarea );
		} catch( Exception exception ) {
			log.error( "Error restoring workarea", exception );
		}
	}

	private void restoreWorkpaneEdge( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.EDGE, id );

			Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );

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

			Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );

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
		String toolType = settings.get( "type" );
		String uriString = settings.get( "uri" );
		WorkpaneView view = views.get( settings.get( PARENT_WORKPANEVIEW_ID ) );

		try {
			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uriString == null ) {
				log.warn( "Removing orphaned tool: " + id );
				settings.delete();
				return;
			}

			// Create the resource
			URI uri = URI.create( uriString );
			Resource resource = program.getResourceManager().createResource( uri );
			program.getResourceManager().loadResource( resource );

			// Create an open tool request
			OpenToolRequest openToolRequest = new OpenToolRequest( new OpenResourceRequest().setUri( uri ) );
			openToolRequest.setResource( resource );

			// Restore the tool on a task thread
			ProgramTool tool = program.getExecutor().submit( () -> program.getToolManager().restoreTool( openToolRequest, toolType ) ).get( RESTORE_TOOL_TIMEOUT, TimeUnit.SECONDS );
			if( tool == null ) {
				log.warn( "Removing unknown tool: " + id );
				settings.delete();
				return;
			}
			tool.setSettings( settings );

			Set<ProgramTool> viewToolSet = viewTools.computeIfAbsent( view, k -> new HashSet<>() );
			viewToolSet.add( tool );

			tools.put( id, tool );
		} catch( TimeoutException exception ) {
			log.warn( "Timeout restoring tool: " + toolType, exception );
		} catch( Exception exception ) {
			log.error( "Error restoring tool", exception );
		}
	}

	private void cleanup() {
		// Clear the object maps when done
		viewTools.clear();
		tools.clear();
		views.clear();
		edges.clear();
		panes.clear();
		areas.clear();
		workspaces.clear();
	}

	private class ToolOrderComparator implements Comparator<ProgramTool> {

		@Override
		public int compare( ProgramTool tool1, ProgramTool tool2 ) {
			Settings settings1 = tool1.getSettings();
			Settings settings2 = tool2.getSettings();

			if( settings1 == null && settings2 == null ) return 0;
			if( settings1 == null ) return -1;
			if( settings2 == null ) return 1;

			Integer order1 = settings1.getInteger( "order" );
			Integer order2 = settings2.getInteger( "order" );

			if( order1 == null && order2 == null ) return 0;
			if( order1 == null ) return -1;
			if( order2 == null ) return 1;

			return order2 - order1;
		}
	}

}
