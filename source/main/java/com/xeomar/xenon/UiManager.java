package com.xeomar.xenon;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.workarea.*;
import com.xeomar.xenon.worktool.Tool;
import javafx.geometry.Orientation;
import javafx.scene.layout.BorderStroke;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

	private static Logger log = LogUtil.get( UiManager.class );

	private Program program;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> areas = new HashMap<>();

	private Map<String, Workpane> panes = new HashMap<>();

	private Map<String, WorkpaneEdge> edges = new HashMap<>();

	private Map<String, WorkpaneView> views = new HashMap<>();

	private Map<String, Tool> tools = new HashMap<>();

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
		return getChildNodeNames( ProgramSettings.TOOL ).length;
	}

	public void restoreUi( SplashScreen splashScreen ) {
		restoreLock.lock();
		try {
			String[] workspaceIds = getChildNodeNames( ProgramSettings.WORKSPACE );
			if( workspaceIds.length == 0 ) {
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

	private void restoreWorkspaces( SplashScreen splashScreen, String[] workspaceIds ) {
		String[] areaIds = getChildNodeNames( ProgramSettings.AREA );
		String[] edgeIds = getChildNodeNames( ProgramSettings.EDGE );
		String[] viewIds = getChildNodeNames( ProgramSettings.VIEW );
		String[] toolIds = getChildNodeNames( ProgramSettings.TOOL );

		// Create the workspaces (includes the window)
		for( String id : workspaceIds ) {
			workspaces.put( id, restoreWorkspace( id ) );
			//splashScreen.update();
		}

		// Create the workareas (includes the workpane)
		for( String id : areaIds ) {
			areas.put( id, restoreWorkarea( id ) );
			panes.put( id, areas.get( id ).getWorkpane() );
			//splashScreen.update();
		}

		// Create the workpane edges
		for( String id : edgeIds ) {
			edges.put( id, restoreWorkpaneEdge( id ) );
			//splashScreen.update();
		}

		// Create the workpane views
		for( String id : viewIds ) {
			views.put( id, restoreWorkpaneView( id ) );
			//splashScreen.update();
		}

		// Create the tools
		for( String id : toolIds ) {
			tools.put( id, restoreWorktool( id ) );
			//splashScreen.update();
		}

		linkComponents();
	}

	private void linkComponents() {
		// Link the workareas to the workspaces
		for( Workarea workarea : areas.values() ) {
			Settings settings = workarea.getSettings();
			Workspace workspace = workspaces.get( settings.get( PARENT_WORKSPACE_ID ) );
			workspace.addWorkarea( workarea );
			if( workarea.isActive() ) workspace.setActiveWorkarea( workarea );
		}

		// Link the edges
		for( WorkpaneEdge edge : edges.values() ) {
			linkEdges( edge );
		}

		// Link the views
		for( WorkpaneView view : views.values() ) {
			linkEdges( view );
		}

		// NEXT Add the tools to the views, in the correct order, of course

		cleanup();
	}

	private void linkEdges( WorkpaneEdge edge  ) {
		Settings settings = edge.getSettings();
		// NEXT Implement
	}

	private void linkEdges( WorkpaneView view  ) {
		Settings settings = view.getSettings();
		// NEXT Implement
	}

	private String[] getChildNodeNames( String path ) {
		return program.getSettingsManager().getSettings( path ).getNodes();
	}

	private Workspace restoreWorkspace( String id ) {
		log.trace( "Restoring workspace: " + id );
		try {
			Workspace workspace = new Workspace( program );

			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );
			workspace.setSettings( settings );

			log.warn( "Workspace active( " + workspace.getId() + " ): " + workspace.isActive() );

			if( workspace.isActive() ) {
				program.getWorkspaceManager().setActiveWorkspace( workspace );
			} else {
				program.getWorkspaceManager().addWorkspace( workspace );
			}

			return workspace;
		} catch( Exception exception ) {
			log.error( "Error restoring workspace", exception );
		}
		return null;
	}

	private Workarea restoreWorkarea( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.AREA, id );
			Settings workpaneSettings = program.getSettingsManager().getSettings( ProgramSettings.PANE, id );

			Workspace workspace = workspaces.get( settings.get( PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( workspace == null ) {
				log.debug( "Removing orphaned workarea settings: " + id );
				workpaneSettings.delete();
				settings.delete();
				return null;
			}

			Workarea workarea = new Workarea();
			workarea.setSettings( settings );

			workpaneSettings.set( PARENT_WORKAREA_ID, id );
			workarea.getWorkpane().setSettings( workpaneSettings );

			return workarea;
		} catch( Exception exception ) {
			log.error( "Error restoring workarea", exception );
		}
		return null;
	}

	private WorkpaneEdge restoreWorkpaneEdge( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.EDGE, id );

			Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( workpane == null ) {
				log.debug( "Removing orphaned workpane edge settings: " + id );
				settings.delete();
				return null;
			}

			Settings.print( settings );

			Orientation orientation = Orientation.valueOf( settings.get( "orientation" ).toUpperCase() );
			WorkpaneEdge edge = new WorkpaneEdge( orientation );
			edge.setSettings( settings );

			return edge;
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
		return null;
	}

	private WorkpaneView restoreWorkpaneView( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.VIEW, id );

			Workpane workpane = panes.get( settings.get( PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( workpane == null ) {
				log.debug( "Removing orphaned workpane view settings: " + id );
				settings.delete();
				return null;
			}

			WorkpaneView view = new WorkpaneView();
			view.setSettings( settings );

			return view;
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
		return null;
	}

	private Tool restoreWorktool( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.TOOL, id );

			WorkpaneView view = views.get( settings.get( PARENT_WORKPANEVIEW_ID ) );
			String uri = settings.get( "uri" );

			// If the view is not found, then the tool is orphaned...delete the settings
			if( view == null || uri == null ) {
				log.debug( "Removing orphaned tool settings: " + id );
				settings.delete();
				return null;
			}

			// Determine and load the resource
			Resource resource = program.getResourceManager().createResource( uri );
			program.getResourceManager().loadResources( resource );

			ProductTool tool = program.getToolManager().getTool( resource );
			tool.setSettings( settings );
			return tool;
		} catch( Exception exception ) {
			log.error( "Error restoring tool", exception );
		}
		return null;
	}

	private void cleanup() {
		// Clear the object maps when done
		tools.clear();
		views.clear();
		edges.clear();
		panes.clear();
		areas.clear();
		workspaces.clear();
	}

}
