package com.xeomar.xenon;

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

public class UiFactory {

	public static final double DEFAULT_WIDTH = 960;

	public static final double DEFAULT_HEIGHT = 540;

	public static final double PAD = BorderStroke.THICK.getTop();

	public static final String PARENT_WORKAREA_ID = "workarea-id";

	public static final String PARENT_WORKPANE_ID = "workpane-id";

	private static Logger log = LogUtil.get( UiFactory.class );

	private Program program;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> workareas = new HashMap<>();

	private Map<String, Workpane> workpanes = new HashMap<>();

	private Map<String, WorkpaneEdge> workpaneEdges = new HashMap<>();

	private Map<String, WorkpaneView> workpaneViews = new HashMap<>();

	private Map<String, Tool> worktools = new HashMap<>();

	private boolean restored;

	private Lock restoreLock = new ReentrantLock();

	private Condition restoreCondition = restoreLock.newCondition();

	public UiFactory( Program program ) {
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
		return getSettingsNames( ProgramSettings.TOOL ).length;
	}

	public void restoreUi( SplashScreen splashScreen ) {
		restoreLock.lock();
		try {
			String[] workspaceIds = getSettingsNames( ProgramSettings.WORKSPACE );
			String[] workareaIds = getSettingsNames( ProgramSettings.WORKAREA );
			String[] edgeIds = getSettingsNames( ProgramSettings.WORKPANEEDGE );
			String[] viewIds = getSettingsNames( ProgramSettings.WORKPANEVIEW );
			String[] toolIds = getSettingsNames( ProgramSettings.TOOL );

			if( workspaceIds.length == 0 ) {
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
			} else {
				// Create the workspaces (includes the window)
				for( String id : workspaceIds ) {
					restoreWorkspace( id );
					//splashScreen.update();
				}

				// Create the workareas (includes the workpane)
				for( String id : workareaIds ) {
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
					//splashScreen.update();
				}
			}

			// Clear the object maps when done
			worktools.clear();
			workpaneViews.clear();
			workpaneEdges.clear();
			workpanes.clear();
			workareas.clear();
			workspaces.clear();

			restoreCondition.signalAll();
		} finally {
			restoreLock.unlock();
		}
	}

	public void awaitRestore( long timeout, TimeUnit unit ) throws InterruptedException {
		restoreLock.lock();
		try {
			while( !restored ) {
				restoreCondition.await( timeout, unit );
			}
		} finally {
			restoreLock.unlock();
		}
	}

	public Workspace newWorkspace() throws Exception {
		String id = IdGenerator.getId();
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );
		settings.set( "id", id );

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
		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKAREA, id );
		settings.set( "id", id );

		Workarea workarea = new Workarea();
		workarea.setSettings( settings );
		return workarea;
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

			workspaces.put( settings.get( "id" ), workspace );
			return workspace;
		} catch( Exception exception ) {
			log.error( "Error restoring workspace", exception );
		}
		return null;
	}

	private Workarea restoreWorkarea( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKAREA, id );

			Workspace workspace = workspaces.get( settings.get( Workarea.PARENT_WORKSPACE_ID ) );

			// If the workspace is not found, then the workarea is orphaned...delete the settings
			if( workspace == null ) {
				log.debug( "Removing orphaned workarea settings: " + id );
				settings.delete();
				return null;
			}

			Workarea workarea = new Workarea();
			workarea.setSettings( settings );
			workspace.addWorkarea( workarea );
			if( workarea.isActive() ) workspace.setActiveWorkarea( workarea );

			workareas.put( settings.get( "id" ), workarea );
			workpanes.put( settings.get( "id" ), workarea.getWorkpane() );
			return workarea;
		} catch( Exception exception ) {
			log.error( "Error restoring workarea", exception );
		}
		return null;
	}

	private WorkpaneEdge restoreWorkpaneEdge( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKPANEEDGE, id );

			Workpane workpane = workpanes.get( settings.get( PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the edge is orphaned...delete the settings
			if( workpane == null ) {
				log.debug( "Removing orphaned workpane edge settings: " + id );
				settings.delete();
				return null;
			}

			Orientation orientation = Orientation.valueOf( settings.get( "orientation" ).toUpperCase() );
			WorkpaneEdge edge = new WorkpaneEdge( orientation );
			edge.setPosition( settings.getDouble( "position" ) );

			// NEXT Add the edge to the workpane
			//workpane.getChildren().add( edge );

			workpaneEdges.put( settings.get( "id" ), edge );
			return edge;
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
		return null;
	}

	private WorkpaneView restoreWorkpaneView( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKPANEVIEW, id );

			Workpane workpane = workpanes.get( settings.get( PARENT_WORKPANE_ID ) );

			// If the workpane is not found, then the view is orphaned...delete the settings
			if( workpane == null ) {
				log.debug( "Removing orphaned workpane view settings: " + id );
				settings.delete();
				return null;
			}

			WorkpaneView view = new WorkpaneView();

			// NEXT Get all the edges linked
			// NEXT Add the view to the workpane

			workpaneViews.put( settings.get( "id" ), view );
			return view;
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
		return null;
	}

	private Tool restoreWorktool( String id ) {
		try {
			Settings settings = program.getSettingsManager().getSettings( ProgramSettings.TOOL, id );

			//			return loadWorktool( file );
		} catch( Exception exception ) {
			log.error( "Error restoring tool", exception );
		}
		return null;
	}

	private String[] getSettingsNames( String path ) {
		return program.getSettingsManager().getSettings( path ).getNodes();
	}

}
