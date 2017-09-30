package com.xeomar.xenon;

import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.workarea.Workarea;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.Workspace;
import com.xeomar.xenon.worktool.Tool;
import javafx.scene.layout.BorderStroke;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UiFactory {

	public static final double DEFAULT_WIDTH = 960;

	public static final double DEFAULT_HEIGHT = 540;

	public static final double PAD = BorderStroke.THICK.getTop();

	enum Prefix {
		WORKSPACE,
		WORKAREA,
		WORKPANE,
		WORKTOOL
	}

	private static Logger log = LogUtil.get( UiFactory.class );

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

	//	public int getUiObjectCount() {
	//		int s = getSettingsFiles( Prefix.WORKSPACE ).length;
	//		int a = getSettingsFiles( Prefix.WORKAREA ).length;
	//		int p = getSettingsFiles( Prefix.WORKPANE ).length;
	//		int t = getSettingsFiles( Prefix.WORKTOOL ).length;
	//		return Math.max( 2, s + a + p + t );
	//	}

	public int getToolCount() {
		return getSettingsFiles( Prefix.WORKTOOL ).length;
	}

	public void restoreUi( SplashScreen splashScreen ) {
		File[] workspaceFiles = getSettingsFiles( Prefix.WORKSPACE );
		File[] workareaFiles = getSettingsFiles( Prefix.WORKAREA );
		File[] workpaneFiles = getSettingsFiles( Prefix.WORKPANE );
		File[] worktoolFiles = getSettingsFiles( Prefix.WORKTOOL );

		if( workspaceFiles.length == 0 ) {
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
			// Create the workspaces
			for( File file : workspaceFiles ) {
				restoreWorkspace( file );
				//splashScreen.update();
			}

			// Create the workareas
			for( File file : workareaFiles ) {
				restoreWorkarea( file );
				// NOTE Workareas contain the workpane
				//splashScreen.update();
			}

			// Create the workviews
			for( File file : workpaneFiles ) {
				//restoreWorkpane( file );
				//splashScreen.update();
			}

			// Create the tools
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

		Settings settings = program.getSettingsManager().getSettings( getSettingsFile( Prefix.WORKSPACE, id ), Prefix.WORKSPACE.name() );
		settings.set( "id", id );
		// Intentionally do not set the x property
		// Intentionally do not set the y property
		settings.set( "w", DEFAULT_WIDTH );
		settings.set( "h", DEFAULT_HEIGHT );

		Workspace workspace = new Workspace( program );
		workspace.loadSettings( settings );
		return workspace;
	}

	public Workarea newWorkarea() throws Exception {
		String id = IdGenerator.getId();

		Settings settings = program.getSettingsManager().getSettings( getSettingsFile( Prefix.WORKAREA, id ), Prefix.WORKAREA.name() );
		settings.set( "id", id );

		Workarea workarea = new Workarea();
		workarea.loadSettings( settings );
		return workarea;
	}

	private Workspace restoreWorkspace( File file ) {
		try {
			Workspace workspace = new Workspace( program );

			Settings settings = program.getSettingsManager().getSettings( file, Prefix.WORKSPACE.name() );
			workspace.loadSettings( settings );

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

	private Workarea restoreWorkarea( File file ) {
		try {
			Workarea workarea = new Workarea();

			Settings settings = program.getSettingsManager().getSettings( file, Prefix.WORKAREA.name() );
			workarea.loadSettings( settings );

			Workspace workspace = workspaces.get( settings.get( "workspaceId" ) );
			if( workarea.isActive() ) {
				workspace.setActiveWorkarea( workarea );
			} else {
				workspace.addWorkarea( workarea );
			}

			workareas.put( settings.get( "id" ), workarea );
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
			//					Settings settings = getSettings( file );
			//					workpane.loadSettings( Settings );
			//
			//					workpanes.put( Settings.getString("id"), workpane );
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

	private File[] getSettingsFiles( Prefix prefix ) {
		File[] files = paths.get( prefix ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	private File getSettingsFile( Prefix prefix, String id ) {
		return new File( paths.get( prefix ), id + SettingsManager.SETTINGS_EXTENSION );
	}

}
