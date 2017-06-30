package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.worktool.Tool;
import com.parallelsymmetry.essence.workarea.Workarea;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.workarea.Workspace;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UiFactory {

	public static final int DEFAULT_WIDTH = 960;

	public static final int DEFAULT_HEIGHT = 540;

	enum Prefix {
		WORKSPACE,
		WORKAREA,
		WORKPANE,
		WORKTOOL
	}

	private static Logger log = LoggerFactory.getLogger( UiFactory.class );

	private Program program;

	private Map<Prefix, File> paths;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> workareas = new HashMap<>();

	private Map<String, Workpane> workpanes = new HashMap<>();

	private Map<String, Tool> worktools = new HashMap<>();

	public UiFactory( Program program ) {
		this.program = program;
		paths = new ConcurrentHashMap<>();

		File uiSettingsFolder = new File( program.getDataFolder(), ProgramSettings.UI );
		paths.put( Prefix.WORKSPACE, new File( uiSettingsFolder, Prefix.WORKSPACE.name().toLowerCase() ) );
		paths.put( Prefix.WORKAREA, new File( uiSettingsFolder, Prefix.WORKAREA.name().toLowerCase() ) );
		paths.put( Prefix.WORKPANE, new File( uiSettingsFolder, Prefix.WORKPANE.name().toLowerCase() ) );
		paths.put( Prefix.WORKTOOL, new File( uiSettingsFolder, Prefix.WORKTOOL.name().toLowerCase() ) );
	}

	public int getUiObjectCount() {
		int s = getConfigurationFiles( Prefix.WORKSPACE ).length;
		int a = getConfigurationFiles( Prefix.WORKAREA ).length;
		int p = getConfigurationFiles( Prefix.WORKPANE ).length;
		int t = getConfigurationFiles( Prefix.WORKTOOL ).length;
		return Math.max( 2, s + a + p + t );
	}

	public void restoreUi( SplashScreen splashScreen ) {
		File[] workspaceFiles = getConfigurationFiles( Prefix.WORKSPACE );
		File[] workareaFiles = getConfigurationFiles( Prefix.WORKAREA );
		File[] workpaneFiles = getConfigurationFiles( Prefix.WORKPANE );
		File[] worktoolFiles = getConfigurationFiles( Prefix.WORKTOOL );

		if( workspaceFiles.length == 0 ) {
			try {
				// Create the default workspace
				Workspace workspace = newWorkspace();
				program.getWorkspaceManager().setActiveWorkspace( workspace );
				splashScreen.update();

				// Create the default workarea
				Workarea workarea = newWorkarea();
				workarea.setName( "Default" );
				workspace.setActiveWorkarea( workarea );
				splashScreen.update();

				// TODO Create default workarea panes

				// TODO Create default tools

			} catch( Exception exception ) {
				log.error( "Error creating default workspace", exception );
			}
		} else {
			// Create the workspaces
			for( File file : workspaceFiles ) {
				restoreWorkspace( file );
				splashScreen.update();
			}

			// Create the workareas
			for( File file : workareaFiles ) {
				restoreWorkarea( file );
				splashScreen.update();
			}

			// Create the workpanes
			for( File file : workpaneFiles ) {
				//restoreWorkpane( file );
				//splashScreen.update();
			}

			// Create the worktools
			for( File file : worktoolFiles ) {
				restoreWorktool( file );
				splashScreen.update();
			}
		}

		// Clear the object maps
		worktools.clear();
		workpanes.clear();
		workareas.clear();
		workspaces.clear();
	}

	public Workspace newWorkspace() throws Exception {
		String id = IdGenerator.getId();

		Settings settings = new Settings( getConfigurationFile( Prefix.WORKSPACE, id ), Prefix.WORKSPACE.name() );
		settings.addProgramEventListener( program.getEventWatcher() );
		settings.setExecutor( program.getExecutor() );
		Configuration configuration = settings.getConfiguration();
		configuration.setProperty( "id", id );
		// Intentionally do not set the x property
		// Intentionally do not set the y property
		configuration.setProperty( "w", DEFAULT_WIDTH );
		configuration.setProperty( "h", DEFAULT_HEIGHT );

		Workspace workspace = new Workspace( program );
		workspace.setConfiguration( configuration );
		return workspace;
	}

	public Workarea newWorkarea() throws Exception {
		String id = IdGenerator.getId();

		Settings settings = new Settings( getConfigurationFile( Prefix.WORKAREA, id ), Prefix.WORKAREA.name() );
		settings.addProgramEventListener( program.getEventWatcher() );
		settings.setExecutor( program.getExecutor() );
		Configuration configuration = settings.getConfiguration();
		configuration.setProperty( "id", id );

		Workarea workarea = new Workarea();
		workarea.setConfiguration( configuration );
		return workarea;
	}

	private Workspace restoreWorkspace( File file ) {
		try {
			Workspace workspace = new Workspace( program );

			Settings settings = new Settings( file, Prefix.WORKSPACE.name() );
			settings.addProgramEventListener( program.getEventWatcher() );
			settings.setExecutor( program.getExecutor() );
			Configuration configuration = settings.getConfiguration();
			workspace.setConfiguration( configuration );

			if( workspace.isActive() ) {
				program.getWorkspaceManager().setActiveWorkspace( workspace );
			} else {
				program.getWorkspaceManager().addWorkspace( workspace );
			}

			workspaces.put( configuration.getString( "id" ), workspace );
			return workspace;
		} catch( Exception exception ) {
			log.error( "Error restoring workspace", exception );
		}
		return null;
	}

	private Workarea restoreWorkarea( File file ) {
		try {
			Workarea workarea = new Workarea();

			Settings settings = new Settings( file, Prefix.WORKAREA.name() );
			settings.addProgramEventListener( program.getEventWatcher() );
			settings.setExecutor( program.getExecutor() );
			Configuration configuration = settings.getConfiguration();
			workarea.setConfiguration( configuration );

			Workspace workspace = workspaces.get( configuration.getString( "workspaceId" ) );
			if( workarea.isActive() ) {
				workspace.setActiveWorkarea( workarea );
			} else {
				workspace.addWorkarea( workarea );
			}

			workareas.put( configuration.getString( "id" ), workarea );
			return workarea;
		} catch( Exception exception ) {
			log.error( "Error restoring workarea", exception );
		}
		return null;
	}

	private Workpane restoreWorkpane( File file ) {
		try {
			//					Workpane workpane = new Workpane();
			//
			//					Configuration configuration = getConfiguration( file );
			//					workpane.setConfiguration( configuration );
			//
			//					workpanes.put( configuration.getString("id"), workpane );
			//					return workpane;
		} catch( Exception exception ) {
			log.error( "Error restoring workpane", exception );
		}
		return null;
	}

	private Tool restoreWorktool( File file ) {
		//		try {
		//			return loadWorktool( file );
		//		}catch( Exception exception ) {
		//			log.error( "Error restoring worktool", exception );
		//		}
		return null;
	}

	private File[] getConfigurationFiles( Prefix prefix ) {
		File[] files = paths.get( prefix ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	private File getConfigurationFile( Prefix prefix, String id ) {
		return new File( paths.get( prefix ), id + Program.SETTINGS_EXTENSION );
	}

}
