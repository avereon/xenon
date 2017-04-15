package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.event.SettingsLoadedEvent;
import com.parallelsymmetry.essence.event.SettingsSavedEvent;
import com.parallelsymmetry.essence.tool.WorkTool;
import com.parallelsymmetry.essence.work.Workarea;
import com.parallelsymmetry.essence.work.Workpane;
import com.parallelsymmetry.essence.work.Workspace;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UiFactory {

	enum Prefix {
		WORKSPACE,
		WORKAREA,
		WORKPANE,
		WORKTOOL
	}

	private static final String UI_SETTINGS_PATH = "settings/ui";

	private static Logger log = LoggerFactory.getLogger( UiFactory.class );

	private Program program;

	private Map<Prefix, File> paths;

	private Map<String, Workspace> workspaces = new HashMap<>();

	private Map<String, Workarea> workareas = new HashMap<>();

	private Map<String, Workpane> workpanes = new HashMap<>();

	private Map<String, WorkTool> worktools = new HashMap<>();

	public UiFactory( Program program ) {
		this.program = program;
		paths = new ConcurrentHashMap<>();

		File uiSettingsFolder = new File( program.getProgramDataFolder(), UI_SETTINGS_PATH );
		paths.put( Prefix.WORKSPACE, new File( uiSettingsFolder, Prefix.WORKSPACE.name().toLowerCase() ) );
		paths.put( Prefix.WORKAREA, new File( uiSettingsFolder, Prefix.WORKAREA.name().toLowerCase() ) );
		paths.put( Prefix.WORKPANE, new File( uiSettingsFolder, Prefix.WORKPANE.name().toLowerCase() ) );
		paths.put( Prefix.WORKTOOL, new File( uiSettingsFolder, Prefix.WORKTOOL.name().toLowerCase() ) );
	}

	public int getUiObjectCount() {
		int s = getConfigurationFiles(Prefix.WORKSPACE).length;
		int a = getConfigurationFiles(Prefix.WORKAREA).length;
		int p = getConfigurationFiles(Prefix.WORKPANE).length;
		int t = getConfigurationFiles(Prefix.WORKTOOL).length;
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

	private File[] getConfigurationFiles( Prefix prefix ) {
		File[] files = paths.get( prefix ).listFiles();
		return files == null ? new File[ 0 ] : files;
	}

	private Workspace newWorkspace() throws Exception {
		String id = IdGenerator.getId();

		Configuration configuration = getConfiguration( Prefix.WORKSPACE, id );
		configuration.setProperty( "id", id );
		configuration.setProperty( "x", 0 );
		configuration.setProperty( "y", 0 );
		configuration.setProperty( "w", 800 );
		configuration.setProperty( "h", 600 );
		configuration.setProperty( "active", true );
		configuration.setProperty( "maximized", true );

		Workspace workspace = new Workspace( program );
		workspace.setConfiguration( configuration );
		return workspace;
	}

	private Workarea newWorkarea() throws Exception {
		String id = IdGenerator.getId();

		Configuration configuration = getConfiguration( Prefix.WORKAREA, id );
		configuration.setProperty( "id", id );
		configuration.setProperty( "name", "Default" );
		configuration.setProperty( "active", true );

		Workarea workarea = new Workarea();
		workarea.setConfiguration( configuration );
		return workarea;
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
		return new File( paths.get( prefix ), id + ".properties" );
	}

	private Configuration getConfiguration( File file ) throws Exception {
		PropertiesBuilderParameters params = new Parameters().properties();
		params.setFile( file );

		ConfigurationEventWatcher watcher = new ConfigurationEventWatcher( file );
		SettingsConfiguration<PropertiesConfiguration> builder = new SettingsConfiguration<>( PropertiesConfiguration.class, null, true, program.getExecutor() );
		builder.addEventListener( SettingsConfiguration.LOAD, watcher );
		builder.addEventListener( SettingsConfiguration.SAVE, watcher );
		builder.configure( params );
		builder.setAutoSave( true );

		return builder.getConfiguration();
	}

	private class ConfigurationEventWatcher implements EventListener<ConfigurationBuilderEvent> {

		private File file;

		public ConfigurationEventWatcher( File file ) {
			this.file = file;
		}

		@Override
		public void onEvent( ConfigurationBuilderEvent configurationEvent ) {
			if( configurationEvent.getEventType() == SettingsConfiguration.SAVE ) {
				program.dispatchEvent( new SettingsSavedEvent( configurationEvent.getSource(), file ) );
			} else if( configurationEvent.getEventType() == SettingsConfiguration.LOAD ) {
				program.dispatchEvent( new SettingsLoadedEvent( configurationEvent.getSource(), file ) );
			}
		}
	}

}
