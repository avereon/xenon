package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.tool.WorkTool;
import com.parallelsymmetry.essence.work.Workarea;
import com.parallelsymmetry.essence.work.Workpane;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkspaceFactory {

	enum Prefix {
		WORKSPACE,
		WORKAREA,
		WORKPANE,
		WORKTOOL
	}

	private static Logger log = LoggerFactory.getLogger( WorkspaceFactory.class );

	private Program program;

	private File uiSettingsPath;

	private Map<Prefix, File> paths;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> workareas = new HashMap<>();

	private Map<String, Workpane> workpanes = new HashMap<>();

	private Map<String, WorkTool> worktools = new HashMap<>();

	public WorkspaceFactory( Program program ) {
		this.program = program;
		uiSettingsPath = new File( program.getProgramDataFolder(), "settings" );

		paths = new ConcurrentHashMap<>();
		paths.put( Prefix.WORKSPACE, new File( uiSettingsPath, Prefix.WORKSPACE.name().toLowerCase() ) );
		paths.put( Prefix.WORKAREA, new File( uiSettingsPath, Prefix.WORKAREA.name().toLowerCase() ) );
		paths.put( Prefix.WORKPANE, new File( uiSettingsPath, Prefix.WORKPANE.name().toLowerCase() ) );
		paths.put( Prefix.WORKTOOL, new File( uiSettingsPath, Prefix.WORKTOOL.name().toLowerCase() ) );
	}

	private String newId() {
		return IdGenerator.getId();
	}

	public Workspace newWorkspace() throws Exception {
		String id = newId();

		Configuration configuration = getConfiguration( Prefix.WORKSPACE, id );
		configuration.setProperty( "id", id );
		configuration.setProperty( "x", 0 );
		configuration.setProperty( "y", 0 );
		configuration.setProperty( "w", 800 );
		configuration.setProperty( "h", 600 );
		configuration.setProperty( "active", true );

		Workspace workspace = new Workspace( program );
		workspace.setConfiguration( configuration );
		return workspace;
	}

	public Workarea newWorkarea() throws Exception {
		String id = newId();

		Configuration configuration = getConfiguration( Prefix.WORKAREA, id );
		configuration.setProperty( "id", id );
		configuration.setProperty( "name", "Default" );
		configuration.setProperty( "active", true );

		Workarea workarea = new Workarea();
		workarea.setConfiguration( configuration );
		return workarea;
	}

	public File[] getConfigurationFiles( Prefix prefix ) {
		File[] files = paths.get( prefix ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	@Deprecated
	public File[] getWorkspaceConfigurationFiles() {
		File[] files = paths.get( Prefix.WORKSPACE ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	@Deprecated
	public File[] getWorkareaConfigurationFiles() {
		File[] files = paths.get( Prefix.WORKAREA ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	@Deprecated
	public File[] getWorkpaneConfigurationFiles() {
		File[] files = paths.get( Prefix.WORKPANE ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	@Deprecated
	public File[] getWorktoolConfigurationFiles() {
		File[] files = paths.get( Prefix.WORKTOOL ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	public int getWorkObjectsToRestoreCount() {
		int s = getWorkspaceConfigurationFiles().length;
		int a = getWorkareaConfigurationFiles().length;
		int t = getWorktoolConfigurationFiles().length;
		return Math.max( 2, s + a + t );
	}

	public void restoreWorkspaceObjects( SplashScreen splashScreen ) {
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
				workspace.setActiveWorkarea( workarea );
				splashScreen.update();

				// TODO Create default work panes

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

	private Workspace restoreWorkspace( File file ) {
		try {
			Workspace workspace = new Workspace( program );

			Configuration configuration = getConfiguration( file );
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

			Configuration configuration = getConfiguration( file );
			workarea.setConfiguration( configuration );

			Workspace workspace = workspaces.get( configuration.getString( "workspaceId" ) );
			if( workarea.isActive() ) {
				workspace.setActiveWorkarea( workarea );
			} else {
				workspace.addWorkArea( workarea );
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

	private WorkTool restoreWorktool( File file ) {
		//		try {
		//			return loadWorktool( file );
		//		}catch( Exception exception ) {
		//			log.error( "Error restoring worktool", exception );
		//		}
		return null;
	}

	private Configuration getConfiguration( Prefix prefix, String id ) throws Exception {
		return getConfiguration( getConfigurationFile( prefix, id ) );
	}

	private File getConfigurationFile( Prefix prefix, String id ) {
		return new File( paths.get( prefix ), prefix.name().toLowerCase() + "-" + id + ".properties" );
	}

	private Configuration getConfiguration( File file ) throws Exception {
		// If parent folder does not exist create it
		if( !file.getParentFile().exists() ) file.getParentFile().mkdirs();

		PropertiesBuilderParameters params = new Parameters().properties();
		params.setFile( file );

		FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new SoftFileConfigurationBuilder<>( PropertiesConfiguration.class, null, true );
		builder.configure( params );
		builder.setAutoSave( true );
		return builder.getConfiguration();
	}

}
