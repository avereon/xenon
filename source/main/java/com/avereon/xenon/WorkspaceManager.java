package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.settings.SettingsEvent;
import com.avereon.skill.Controllable;
import com.avereon.util.TextUtil;
import com.avereon.xenon.asset.Asset;
import com.avereon.zerra.stage.DialogUtil;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.javafx.Fx;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.CustomLog;
import lombok.Getter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@CustomLog
public class WorkspaceManager implements Controllable<WorkspaceManager> {

	public static final String DEFAULT_THEME_ID = "xenon-dark";

	@Getter
	private final Xenon program;

	private final Set<Workspace> workspaces;

	private final StringProperty themeId;

	private Workspace activeWorkspace;

	@Getter
	private boolean uiReady;

	WorkspaceManager( Xenon program ) {
		this.program = program;
		this.workspaces = new CopyOnWriteArraySet<>();
		this.themeId = new SimpleStringProperty( DEFAULT_THEME_ID );

		program.getSettings().register( SettingsEvent.CHANGED, e -> {
			if( "workspace-theme-id".equals( e.getKey() ) ) setTheme( (String)e.getNewValue() );
		} );
	}

	@Override
	public boolean isRunning() {
		return !workspaces.isEmpty();
	}

	@Override
	public WorkspaceManager start() {
		program.getFxEventHub().register( ProgramEvent.UI_READY, e -> setUiReady( true ) );
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
		// should close the stages, or they stay open during the duration of the
		// testing process.
		//
		// RESULT Do not close the stages in this method. The unit tests will just
		// have to close the stages as part of the cleanup.

		activeWorkspace = null;
		workspaces.clear();
		setUiReady( false );

		return this;
	}

	//	@Override
	//	public WorkspaceManager awaitStop( long timeout, TimeUnit unit ) {
	//		// This method intentionally does nothing. See explanation in stop() method.
	//		return this;
	//	}

	private void setUiReady( boolean uiReady ) {
		this.uiReady = uiReady;
	}

	public String getThemeId() {
		return themeId.get();
	}

	public ThemeMetadata getThemeMetadata() {
		return getProgram().getThemeManager().getMetadata( getThemeId() );
	}

	public void setTheme( String id ) {
		if( TextUtil.isEmpty( id ) ) id = DEFAULT_THEME_ID;
		ThemeMetadata theme = getProgram().getThemeManager().getMetadata( id );
		if( theme == null ) theme = getProgram().getThemeManager().getMetadata( id = DEFAULT_THEME_ID );

		this.themeId.set( id );

		final ThemeMetadata finalTheme = theme;
		workspaces.forEach( w -> w.setTheme( finalTheme.getUrl() ) );
	}

	public StringProperty themeIdProperty() {
		return themeId;
	}

	public Set<Workspace> getWorkspaces() {
		return new HashSet<>( workspaces );
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
		Fx.run( () -> {
			Workspace workspace = getActiveWorkspace();
			if( workspace == null ) return;
			workspace.show();
			workspace.requestFocus();
		} );
	}

	public Workspace getActiveWorkspace() {
		if( activeWorkspace == null && !workspaces.isEmpty() ) setActiveWorkspace( workspaces.iterator().next() );
		if( activeWorkspace != null ) return activeWorkspace;
		throw new IllegalStateException( "No workspaces available" );
	}

	public Stage getActiveStage() {
		if( activeWorkspace != null ) return activeWorkspace;
		throw new IllegalStateException( "No workspace stages available" );
	}

	public Set<Tool> getAssetTools( Asset asset ) {
		return workspaces.stream().flatMap( w -> w.getWorkareas().stream() ).flatMap( a -> a.getTools().stream() ).filter( t -> t.getAsset() == asset ).collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets() {
		return workspaces
			.stream()
			.flatMap( w -> w.getWorkareas().stream() )
			.flatMap( a -> a.getTools().stream() )
			.map( Tool::getAsset )
			.filter( Asset::isNewOrModified )
			.collect( Collectors.toSet() );
	}

	/**
	 * Get the modified assets in the workspace.
	 *
	 * @param workspace This workspace to check
	 * @return The modified assets in the workspace
	 */
	public Set<Asset> getModifiedAssets( Workspace workspace ) {
		return workspace.getWorkareas().stream().flatMap( a -> a.getTools().stream() ).map( Tool::getAsset ).filter( Asset::isNewOrModified ).collect( Collectors.toSet() );
	}

	/**
	 * Handle modified assets by asking the user what to do with them. The assets
	 * can be provided from any scope (program, workspace, workarea, tool, etc.).
	 *
	 * @param assets The modified assets to handle
	 * @return False if the user chooses to cancel the operation
	 */
	public boolean handleModifiedAssets( ProgramScope scope, Set<Asset> assets ) {
		if( assets.isEmpty() ) return true;

		boolean autoSave = getProgram().getSettings().get( "shutdown-autosave", Boolean.class, false );
		if( autoSave ) {
			getProgram().getResourceManager().saveAssets( assets );
			return true;
		}

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
		alert.setTitle( Rb.text( RbKey.PROGRAM, "asset-modified" ) );
		alert.setHeaderText( Rb.text( RbKey.PROGRAM, "asset-modified-message" ) );
		alert.setContentText( Rb.text( RbKey.PROGRAM, "asset-modified-prompt" ) );
		alert.initOwner( getActiveWorkspace() );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() ) {
			if( result.get() == ButtonType.YES ) getProgram().getResourceManager().saveAssets( assets );
			return result.get() == ButtonType.YES || result.get() == ButtonType.NO;
		}

		return false;
	}

	public void requestCloseWorkspace( Workspace workspace ) {
		long visibleWorkspaces = workspaces.stream().filter( Window::isShowing ).count();
		boolean closeProgram = visibleWorkspaces == 1;
		boolean shutdownVerify = getProgram().getSettings().get( "shutdown-verify", Boolean.class, true );

		if( closeProgram ) {
			program.requestExit( false, false );
		} else {
			if( !handleModifiedAssets( ProgramScope.WORKSPACE, getModifiedAssets( workspace ) ) ) return;

			boolean shouldContinue = !shutdownVerify;
			if( shutdownVerify ) {
				Alert alert = new Alert( Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO );
				alert.setTitle( Rb.text( "workspace", "workspace-close-title" ) );
				alert.setHeaderText( Rb.text( "workspace", "workspace-close-message" ) );
				alert.setContentText( Rb.text( "workspace", "workspace-close-prompt" ) );
				alert.initOwner( workspace );

				Stage stage = program.getWorkspaceManager().getActiveStage();
				Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

				shouldContinue = result.isPresent() && result.get() == ButtonType.YES;
			}

			if( shouldContinue ) closeWorkspace( workspace );
		}
	}

	public Workpane getActiveWorkpane() {
		Workspace workspace = getActiveWorkspace();
		if( workspace == null ) return null;
		Workarea workarea = workspace.getActiveWorkarea();
		if( workarea == null ) return null;
		return getActiveWorkspace().getActiveWorkarea();
	}

	public Set<Tool> getActiveWorkpaneTools( Class<? extends Tool> type ) {
		Workpane workpane = getActiveWorkpane();
		if( workpane == null ) return Set.of();
		return workpane.getTools( type );
	}

	public void requestCloseTools( Class<? extends ProgramTool> type ) {
		Fx.run( () -> getActiveWorkpaneTools( type ).forEach( Tool::close ) );
	}

	public Workspace findWorkspace( ProgramTool tool ) {
		for( Workspace workspace : getWorkspaces() ) {
			for( Workarea workarea : workspace.getWorkareas() ) {
				for( Tool check : workarea.getTools() ) {
					if( check == tool ) return workspace;
				}
			}
		}
		return null;
	}

	void hideWindows() {
		getWorkspaces().forEach( Window::hide );
	}

	private void closeWorkspace( Workspace workspace ) {
		removeWorkspace( workspace );
		workspace.close();
	}

}
