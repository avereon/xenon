package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Controllable;
import com.avereon.util.IdGenerator;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workspace.Workspace;
import javafx.application.Platform;
import javafx.css.CssParser;
import javafx.css.Selector;
import javafx.css.Stylesheet;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class WorkspaceManager implements Controllable<WorkspaceManager> {

	private static final Logger log = Log.get();

	private Program program;

	private String currentTheme;

	private Set<Workspace> workspaces;

	private Workspace activeWorkspace;

	private boolean uiReady;

	WorkspaceManager( Program program ) {
		this.program = program;
		workspaces = new CopyOnWriteArraySet<>();

//		program.getProgramSettings().register( SettingsEvent.CHANGED, e -> {
//			if( "workspace-theme-name".equals( e.getKey() ) ) setTheme( (String)e.getNewValue() );
//		} );
	}

	public Program getProgram() {
		return program;
	}

	@Override
	public boolean isRunning() {
		return workspaces.size() > 0;
	}

	@Override
	public WorkspaceManager start() {
		return this;
	}

	//	@Override
	//	public WorkspaceManager awaitStart( long timeout, TimeUnit unit ) {
	//		return this;
	//	}
	//
	//	@Override
	//	public WorkspaceManager restart() {
	//		return this;
	//	}
	//
	//	@Override
	//	public WorkspaceManager awaitRestart( long timeout, TimeUnit unit ) {
	//		return this;
	//	}

	public WorkspaceManager stop() {
		// If this is called after Platform.exit(), which is usually the case
		// then the result of closing the stages is unpredictable. Not trying to
		// close the stages seems to work fine and the program exits normally.
		//
		// ... But during unit testing, Platform.exit() cannot be called or
		// it hangs the tests. Furthermore, the tests will need to call
		// Program.stop() which, in turn, calls WorkspaceManager.stop(), which
		// should close the stages or they stay open during the duration of the
		// testing process.
		//
		// RESULT Do not close the stages in this method. The unit tests will just
		// have to close the stages as part of the cleanup.

		activeWorkspace = null;
		workspaces.clear();
		setUiReady( false );

		return this;
	}

	public boolean isUiReady() {
		return uiReady;
	}

	void setUiReady( boolean uiReady ) {
		this.uiReady = uiReady;
	}

	//	@Override
	//	public WorkspaceManager awaitStop( long timeout, TimeUnit unit ) {
	//		// This method intentionally does nothing. See explanation in stop() method.
	//		return this;
	//	}

	public void setTheme( String key ) {
		Path path = getProgram().getHomeFolder().resolve( "themes" ).resolve( key ).resolve( key + ".css" );
		if( Files.notExists( path ) ) path = getProgram().getDataFolder().resolve( "themes" ).resolve( key ).resolve( key + ".css" );

		currentTheme = Files.exists( path ) ? path.toUri().toString() : null;
		workspaces.forEach( w -> w.setTheme( currentTheme ) );

		studyStylesheets( currentTheme );
	}

	private void studyStylesheets( String url ) {
		// NEXT Study Stylesheets
		try {
			Stylesheet stylesheet = new CssParser().parse( new URL( url ) );

			stylesheet.getRules().forEach( r -> {
				int index = 0;
				for( Selector s : r.getSelectors() ) {
					if( index > 0 ) log.log( Log.WARN, "selector=" + s.toString() );
					index++;
				}
			} );
		} catch( IOException exception ) {
			log.log( Log.ERROR, exception );
		}
	}

	public Set<Workspace> getWorkspaces() {
		return new HashSet<>( workspaces );
	}

	public Workspace newWorkspace() {
		String id = IdGenerator.getId();

		Workspace workspace = new Workspace( program );
		workspace.getEventBus().parent( program.getFxEventHub() );
		workspace.setTheme( currentTheme );

		Settings settings = program.getSettingsManager().getSettings( ProgramSettings.WORKSPACE, id );
		// Intentionally do not set the x property
		// Intentionally do not set the y property
		settings.set( "w", UiFactory.DEFAULT_WIDTH );
		settings.set( "h", UiFactory.DEFAULT_HEIGHT );
		workspace.setSettings( settings );

		return workspace;
	}

	public void addWorkspace( Workspace workspace ) {
		workspaces.add( workspace );
	}

	public void removeWorkspace( Workspace workspace ) {
		workspaces.remove( workspace );
	}

	public void setActiveWorkspace( Workspace workspace ) {
		// If the workspace is not already added, add it
		if( !workspaces.contains( workspace ) ) addWorkspace( workspace );

		if( activeWorkspace != null ) {
			activeWorkspace.setActive( false );
		}

		activeWorkspace = workspace;

		if( activeWorkspace != null ) {
			activeWorkspace.setActive( true );
		}
	}

	public void showActiveWorkspace() {
		Platform.runLater( () -> {
			Workspace workspace = getActiveWorkspace();
			if( workspace == null ) return;
			Stage stage = workspace.getStage();
			if( stage == null ) return;
			stage.show();
			stage.requestFocus();
		} );
	}

	public Workspace getActiveWorkspace() {
		if( activeWorkspace == null && workspaces.size() > 0 ) setActiveWorkspace( workspaces.iterator().next() );
		if( activeWorkspace != null ) return activeWorkspace;
		throw new IllegalStateException( "No workspaces available" );
	}

	public Stage getActiveStage() {
		if( activeWorkspace != null ) return getActiveWorkspace().getStage();
		throw new IllegalStateException( "No workspace stages available" );
	}

	public Set<Tool> getAssetTools( Asset asset ) {
		return workspaces
			.stream()
			.flatMap( w -> w.getWorkareas().stream() )
			.flatMap( a -> a.getWorkpane().getTools().stream() )
			.filter( t -> t.getAsset() == asset )
			.collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets() {
		return workspaces
			.stream()
			.flatMap( w -> w.getWorkareas().stream() )
			.flatMap( a -> a.getWorkpane().getTools().stream() )
			.map( Tool::getAsset )
			.filter( Asset::isNewOrModified )
			.collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets( Workspace workspace ) {
		return workspace
			.getWorkareas()
			.stream()
			.flatMap( a -> a.getWorkpane().getTools().stream() )
			.map( Tool::getAsset )
			.filter( Asset::isNewOrModified )
			.collect( Collectors.toSet() );
	}

	/**
	 * Handle modified assets by asking the user what to do with them. The assets
	 * can be provided from any scope (program, workspace, workarea, tool, etc.).
	 *
	 * @param assets The modified assets to handle
	 * @return False if the user chooses to cancel the operation
	 */
	public boolean handleModifiedAssets( ProgramScope scope, Set<Asset> assets ) {
		log.log( Log.WARN, "Modified asset count: " + assets.size() );
		if( assets.isEmpty() ) return true;

		boolean autoSave = getProgram().getProgramSettings().get( "shutdown-autosave", Boolean.class, false );
		if( autoSave ) {
			getProgram().getAssetManager().saveAssets( assets );
			return true;
		}

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
		alert.setTitle( program.rb().text( BundleKey.PROGRAM, "asset-modifed" ) );
		alert.setHeaderText( program.rb().text( BundleKey.PROGRAM, "asset-modifed-message" ) );
		alert.setContentText( program.rb().text( BundleKey.PROGRAM, "asset-modifed-prompt" ) );
		alert.initOwner( getActiveWorkspace().getStage() );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() ) {
			if( result.get() == ButtonType.YES ) {
				for( Asset asset : assets ) {
					if( !getProgram().getAssetManager().saveAsset( asset ) ) return false;
				}
			}
			return result.get() == ButtonType.YES || result.get() == ButtonType.NO;
		}

		return false;
	}

	public void requestCloseWorkspace( Workspace workspace ) {
		boolean closeProgram = workspaces.size() == 1;
		if( closeProgram ) {
			program.requestExit( false, false );
		} else {
			handleModifiedAssets( ProgramScope.WORKSPACE, getModifiedAssets( workspace ) );

			Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
			alert.setTitle( program.rb().text( "workspace", "workspace-close-title" ) );
			alert.setHeaderText( program.rb().text( "workspace", "workspace-close-message" ) );
			alert.setContentText( program.rb().text( "workspace", "workspace-close-prompt" ) );
			alert.initOwner( workspace.getStage() );

			Stage stage = program.getWorkspaceManager().getActiveStage();
			Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

			if( result.isPresent() && result.get() == ButtonType.YES ) closeWorkspace( workspace );
		}
	}

	private void closeWorkspace( Workspace workspace ) {
		removeWorkspace( workspace );
		workspace.close();
	}

	public Workpane getActiveWorkpane() {
		return getActiveWorkspace().getActiveWorkarea().getWorkpane();
	}

	public Set<Tool> getActiveWorkpaneTools( Class<? extends Tool> type ) {
		return getActiveWorkpane().getTools( type );
	}

	public void requestCloseTools( Class<? extends ProgramTool> type ) {
		Platform.runLater( () -> {
			Set<Tool> tools = getActiveWorkpaneTools( type );
			tools.forEach( Tool::close );
		} );
	}

	void hideWindows() {
		for( Workspace workspace : getWorkspaces() ) {
			workspace.getStage().hide();
		}
	}

}
