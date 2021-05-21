package com.avereon.xenon.workspace;

import com.avereon.event.EventHandler;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.util.Log;
import com.avereon.xenon.Profile;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.util.ActionUtil;
import com.avereon.xenon.util.TimerUtil;
import com.avereon.xenon.workpane.Tool;
import com.avereon.zerra.event.FxEventHub;
import com.avereon.zerra.javafx.Fx;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.avereon.xenon.util.ActionUtil.SEPARATOR;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace implements WritableIdentity {

	public static final String WORKSPACE_PROPERTY_KEY = Workspace.class.getName();

	private static final System.Logger log = Log.get();

	private final Program program;

	private final Stage stage;

	private Scene scene;

	private boolean active;

	private final FxEventHub eventBus;

	private final BorderPane workareaLayout;

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private final MenuBar menubar;

	private final ToolBar toolbar;

	private final Map<String, Button> toolbarToolButtons;

	private final Separator toolbarToolButtonSeparator;

	private final Region toolbarToolSpring;

	private final StatusBar statusBar;

	private MemoryMonitor memoryMonitor;

	private TaskMonitor taskMonitor;

	private FpsMonitor fpsMonitor;

	private final WorkspaceBackground background;

	private final Pane workpaneContainer;

	private final VBox noticeContainer;

	private ComboBox<Workarea> workareaSelector;

	private final ObservableList<Workarea> workareas;

	private final WorkareaNameWatcher workareaNameWatcher;

	private final BackgroundSettingsHandler backgroundSettingsHandler;

	private final MemoryMonitorSettingsHandler memoryMonitorSettingsHandler;

	private final TaskMonitorSettingsHandler taskMonitorSettingsHandler;

	private final FpsMonitorSettingsHandler fpsMonitorSettingsHandler;

	private Workarea activeWorkarea;

	public Workspace( final Program program ) {
		this.program = program;
		this.eventBus = new FxEventHub();

		workareas = FXCollections.observableArrayList();
		workareaNameWatcher = new WorkareaNameWatcher();
		backgroundSettingsHandler = new BackgroundSettingsHandler();
		memoryMonitorSettingsHandler = new MemoryMonitorSettingsHandler();
		taskMonitorSettingsHandler = new TaskMonitorSettingsHandler();
		fpsMonitorSettingsHandler = new FpsMonitorSettingsHandler();

		// FIXME Should this default setup be defined in config files or something else?
		menubar = createMenuBar( program );

		toolbarToolButtons = new ConcurrentHashMap<>();
		toolbarToolButtonSeparator = new Separator();
		toolbarToolSpring = ActionUtil.createSpring();
		toolbar = createProgramToolBar( program );

		statusBar = createStatusBar( program );

		noticeContainer = new VBox();
		noticeContainer.getStyleClass().add( "notice-container" );
		noticeContainer.setPickOnBounds( false );

		BorderPane noticeLayout = new BorderPane( null, null, noticeContainer, null, null );
		noticeLayout.setPickOnBounds( false );

		// Workpane Container
		workpaneContainer = new StackPane( background = new WorkspaceBackground() );
		workpaneContainer.getStyleClass().add( "workspace" );

		StackPane workspaceStack = new StackPane( workpaneContainer, noticeLayout );
		workspaceStack.setPickOnBounds( false );

		workareaLayout = new BorderPane();
		workareaLayout.getProperties().put( WORKSPACE_PROPERTY_KEY, this );
		workareaLayout.setTop( new VBox( menubar, toolbar ) );
		workareaLayout.setCenter( workspaceStack );
		workareaLayout.setBottom( statusBar );

		// Create the stage
		stage = new Stage();
		stage.getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );
		stage.setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
		} );
		stage.focusedProperty().addListener( ( p, o, n ) -> {
			if( n ) program.getWorkspaceManager().setActiveWorkspace( this );
		} );

		//stage.outputScaleXProperty().addListener( (p,o,n) -> {
		//	log.log( Log.WARN, "The window output scale X changed to " + n );
		//} );
		//stage.outputScaleYProperty().addListener( (p,o,n) -> {
		//	log.log( Log.WARN, "The window output scale Y changed to " + n );
		//} );

		// This worked, just not the way I expected. The UI was rendered at a lower
		// resolution, but then just scaled back up so it looked fuzzy.
		//stage.renderScaleXProperty().bind( stage.outputScaleXProperty().multiply( 0.5 ) );
		//stage.renderScaleYProperty().bind( stage.outputScaleYProperty().multiply( 0.5 ) );

		memoryMonitor.start();
		taskMonitor.start();
		fpsMonitor.start();
	}

	public void setTheme( String url ) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add( Program.STYLESHEET );
		if( url != null ) scene.getStylesheets().add( url );
	}

	public FxEventHub getEventBus() {
		return eventBus;
	}

	private MenuBar createMenuBar( Program program ) {
		MenuBar menubar = new MenuBar();
		Menu menu;

		// FIXME This does not work if there are two menu bars (like this program uses)
		// This generally affects MacOS users
		menubar.setUseSystemMenuBar( true );

		menu = ActionUtil.createMenu( program, "file" );
		menu.getItems().add( ActionUtil.createMenuItem( program, "new" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "open" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "save" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "save-as" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "copy-as" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "close" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "exit" ) );
		menubar.getMenus().add( menu );

		menu = ActionUtil.createMenu( program, "edit" );
		menu.getItems().add( ActionUtil.createMenuItem( program, "undo" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "redo" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "cut" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "copy" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "paste" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "delete" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "indent" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "unindent" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "properties" ) );
		menubar.getMenus().add( menu );

		menu = ActionUtil.createMenu( program, "view" );
		menu.getItems().add( ActionUtil.createMenuItem( program, "workspace-new" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "workspace-close" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "statusbar-show" ) );
		menubar.getMenus().add( menu );

		Menu tools = ActionUtil.createSubMenu( program, "tools" );
		tools.getItems().add( ActionUtil.createMenuItem( program, "task" ) );
		tools.getItems().add( ActionUtil.createMenuItem( program, "mock-update" ) );
		tools.getItems().add( ActionUtil.createMenuItem( program, "restart" ) );

		menu = ActionUtil.createMenu( program, "help" );
		menu.getItems().add( ActionUtil.createMenuItem( program, "welcome" ) );
		//menu.getItems().add( ActionUtil.createMenuItem( program, "help-content" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "settings" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "product" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( tools );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "update" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "about" ) );
		menubar.getMenus().add( menu );

		menu = ActionUtil.createMenu( program, "development" );
		menu.getItems().add( ActionUtil.createMenuItem( program, "test-action-1" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "test-action-2" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "test-action-3" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "test-action-4" ) );
		menu.getItems().add( ActionUtil.createMenuItem( program, "test-action-5" ) );
		menu.getItems().add( new SeparatorMenuItem() );
		menu.getItems().add( ActionUtil.createMenuItem( program, "mock-update" ) );
		if( Profile.DEV.equals( program.getProfile() ) ) menubar.getMenus().add( menu );

		return menubar;
	}

	private static Menu createMenu( Program program, Object[] items ) {
		if( items.length == 0 ) return null;
		Menu menu = ActionUtil.createMenu( program, String.valueOf( items[ 0 ] ) );
		for( Object item : items ) {
			if( item instanceof Object[] ) {
				menu.getItems().add( createMenu( program, (Object[])item ) );
			} else if( item == SEPARATOR ) {
				menu.getItems().add( new SeparatorMenuItem() );
			} else {
				menu.getItems().add( ActionUtil.createMenuItem( program, String.valueOf( item ) ) );
			}
		}
		return menu;
	}

	private ToolBar createProgramToolBar( Program program ) {
		ToolBar toolbar = new ToolBar();
		Object[] ids = new Object[]{ "new", "open", "save", "properties", SEPARATOR, "undo", "redo", SEPARATOR, "cut", "copy", "paste" };
		toolbar.getItems().addAll( createToolBar( program, ids ).getItems() );
		toolbar.getItems().add( toolbarToolSpring );

		toolbar.getItems().add( createWorkareaMenu( program ) );
		toolbar.getItems().add( workareaSelector = createWorkareaSelector() );

		toolbar.getItems().add( ActionUtil.createPad() );
		Button noticeButton = ActionUtil.createToolBarButton( program, "notice" );
		noticeButton.setContentDisplay( ContentDisplay.RIGHT );
		noticeButton.setText( "0" );
		program.getNoticeManager().unreadCountProperty().addListener( ( event, oldValue, newValue ) -> {
			int count = newValue.intValue();
			Fx.run( () -> {
				program.getActionLibrary().getAction( "notice" ).setIcon( program.getNoticeManager().getUnreadNoticeType().getIcon() );
				noticeButton.setText( String.valueOf( count ) );
			} );
		} );
		toolbar.getItems().add( noticeButton );

		return toolbar;
	}

	private static ToolBar createToolBar( Program program, Object[] items ) {
		ToolBar toolbar = new ToolBar();
		for( Object item : items ) {
			if( item instanceof Object[] ) {
				toolbar.getItems().add( ActionUtil.createToolBarItem( program, createToolBar( program, (Object[])item ) ) );
			} else if( item == SEPARATOR ) {
				toolbar.getItems().add( new Separator() );
			} else {
				toolbar.getItems().add( ActionUtil.createToolBarItem( program, String.valueOf( item ) ) );
			}
		}
		return toolbar;
	}

	private static MenuBar createWorkareaMenu( Program program ) {
		Object[] items = new Object[]{ "workarea", "workarea-new", SEPARATOR, "workarea-rename", SEPARATOR, "workarea-close" };

		MenuBar workareaMenuBar = new MenuBar();
		workareaMenuBar.getStyleClass().addAll( "workarea-menu-bar" );
		workareaMenuBar.getMenus().add( createMenu( program, items ) );
		return workareaMenuBar;
	}

	private ComboBox<Workarea> createWorkareaSelector() {
		ComboBox<Workarea> selector = new ComboBox<>();
		selector.setItems( workareas );
		selector.setButtonCell( new WorkareaPropertyCell() );
		selector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );
		return selector;
	}

	private StatusBar createStatusBar( Program program ) {
		StatusBar statusBar = new StatusBar();

		// Task Monitor
		taskMonitor = new TaskMonitor( program );

		// Memory Monitor
		memoryMonitor = new MemoryMonitor();

		// FPS Monitor
		fpsMonitor = new FpsMonitor();

		// If the memory monitor is clicked then call the garbage collector
		memoryMonitor.setOnMouseClicked( ( event ) -> Runtime.getRuntime().gc() );

		statusBar.addRightItems( memoryMonitor.getMonitorGroup() );
		statusBar.addRightItems( taskMonitor.getMonitorGroup() );
		statusBar.addRightItems( fpsMonitor.getMonitorGroup() );

		return statusBar;
	}

	public void pushToolbarActions( String... ids ) {
		pullToolbarActions();
		int index = toolbar.getItems().indexOf( toolbarToolSpring );
		toolbar.getItems().add( index++, toolbarToolButtonSeparator );
		toolbar.getItems().addAll( index, createToolBar( getProgram(), ids ).getItems() );
	}

	public void pullToolbarActions() {
		int index = toolbar.getItems().indexOf( toolbarToolButtonSeparator );
		if( index < 0 ) return;

		Node node = toolbar.getItems().get( index );
		while( node != toolbarToolSpring ) {
			toolbar.getItems().remove( index );
			node = toolbar.getItems().get( index );
		}
	}

	public Program getProgram() {
		return program;
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
		getSettings().set( "active", active );
	}

	public Set<Workarea> getWorkareas() {
		return new HashSet<>( workareas );
	}

	public void addWorkarea( Workarea workarea ) {
		Workspace oldWorkspace = workarea.getWorkspace();
		if( oldWorkspace != null ) oldWorkspace.removeWorkarea( workarea );
		workareas.add( workarea );
		workarea.setWorkspace( this );
	}

	public void removeWorkarea( Workarea workarea ) {
		// If there is only one workarea, don't close it
		if( workareas.size() == 1 ) return;

		// Handle the situation where the workarea area is active
		if( workarea.isActive() ) setActiveWorkarea( determineNextActiveWorkarea() );

		workareas.remove( workarea );
		workarea.setWorkspace( null );
	}

	public Workarea getActiveWorkarea() {
		return activeWorkarea;
	}

	public void setActiveWorkarea( Workarea workarea ) {
		if( activeWorkarea == workarea ) return;

		// Disconnect the old active workarea
		if( activeWorkarea != null ) {
			activeWorkarea.nameProperty().removeListener( workareaNameWatcher );
			activeWorkarea.setActive( false );
			// TODO Remove the menu bar
			// TODO Remove the tool bar
			workpaneContainer.getChildren().remove( activeWorkarea.getWorkpane() );

			// TODO Can I have the workarea "conceal" the tools instead of directly setting the current asset
			getProgram().getAssetManager().setCurrentAsset( null );
		}

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkarea( workarea );
		// Set the new active workarea
		Workarea priorWorkarea = activeWorkarea;
		activeWorkarea = workarea;

		// Connect the new active workarea
		if( activeWorkarea != null ) {
			workpaneContainer.getChildren().add( activeWorkarea.getWorkpane() );
			// TODO Set the menu bar
			// TODO Set the tool bar
			activeWorkarea.setActive( true );
			activeWorkarea.nameProperty().addListener( workareaNameWatcher );
			workareaSelector.getSelectionModel().select( activeWorkarea );
			setStageTitle( activeWorkarea.getName() );
			Tool activeTool = activeWorkarea.getWorkpane().getActiveTool();
			if( activeTool != null ) getProgram().getAssetManager().setCurrentAsset( activeTool.getAsset() );
		}

		// Send a program event when active area changes
		getEventBus().dispatch( new WorkareaSwitchedEvent( this, WorkareaSwitchedEvent.SWITCHED, this, priorWorkarea, activeWorkarea ) );
	}

	public void showNotice( Notice notice ) {
		if( Objects.equals( notice.getBalloonStickiness(), Notice.Balloon.NEVER ) ) return;

		NoticePane pane = new NoticePane( program, notice, true );
		noticeContainer.getChildren().removeIf( node -> Objects.equals( ((NoticePane)node).getNotice().getId(), notice.getId() ) );
		noticeContainer.getChildren().add( 0, pane );

		pane.setOnMouseClicked( ( event ) -> {
			getProgram().getNoticeManager().readNotice( notice );
			noticeContainer.getChildren().remove( pane );
			pane.executeNoticeAction();
			event.consume();
		} );

		pane.getCloseButton().setOnMouseClicked( ( event ) -> {
			getProgram().getNoticeManager().readNotice( notice );
			noticeContainer.getChildren().remove( pane );
			event.consume();
		} );

		int balloonTimeout = getProgram().getSettings().get( "notice-balloon-timeout", Integer.class, 5000 );

		if( Objects.equals( notice.getBalloonStickiness(), Notice.Balloon.NORMAL ) ) {
			TimerUtil.fxTask( () -> noticeContainer.getChildren().remove( pane ), balloonTimeout );
		}
	}

	public void hideNotices() {
		noticeContainer.getChildren().clear();
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public String getUid() {
		return stage.getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setUid( String id ) {
		stage.getProperties().put( Identity.KEY, id );
	}

	Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.WORKSPACE, getUid() );
	}

	public void updateFromSettings( Settings settings ) {
		// Due to differences in how FX handles stage sizes (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		Double w = settings.get( "w", Double.class, UiFactory.DEFAULT_WIDTH );
		Double h = settings.get( "h", Double.class, UiFactory.DEFAULT_HEIGHT );
		scene = new Scene( workareaLayout, w, h );
		scene.setFill( Color.BLACK );
		getProgram().getActionLibrary().registerScene( scene );

		// Setup the stage
		stage.setScene( scene );
		stage.sizeToScene();

		// Position the stage if x and y are specified
		// If not specified the stage is centered on the screen
		Double x = settings.get( "x", Double.class, null );
		Double y = settings.get( "y", Double.class, null );
		if( x != null ) stage.setX( x );
		if( y != null ) stage.setY( y );

		// On Linux, setWidth() and setHeight() incorrectly do not take the stage
		// window decorations into account. The way to deal with this is to watch
		// the scene size and set the scene size on creation.
		// Do not use the following.
		// if( w != null ) stage.setWidth( w );
		// if( h != null ) stage.setHeight( h );

		stage.setMaximized( settings.get( "maximized", Boolean.class, false ) );
		setActive( settings.get( "active", Boolean.class, false ) );

		// Add the property listeners
		stage.maximizedProperty().addListener( ( v, o, n ) -> {
			if( stage.isShowing() ) settings.set( "maximized", n );
		} );
		stage.xProperty().addListener( ( v, o, n ) -> {
			if( !stage.isMaximized() ) settings.set( "x", n );
		} );
		stage.yProperty().addListener( ( v, o, n ) -> {
			if( !stage.isMaximized() ) settings.set( "y", n );
		} );
		scene.widthProperty().addListener( ( v, o, n ) -> {
			if( !stage.isMaximized() ) settings.set( "w", n );
		} );
		scene.heightProperty().addListener( ( v, o, n ) -> {
			if( !stage.isMaximized() ) settings.set( "h", n );
		} );

		updateBackgroundFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateMemoryMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateTaskMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateFpsMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
	}

	public void screenshot( Path file ) {
		Fx.waitFor( 5, 1000 );
		Fx.run( () -> {
			double renderScaleX = getStage().getRenderScaleX();
			double renderScaleY = getStage().getRenderScaleY();

			WritableImage buffer = new WritableImage( (int)Math.rint( renderScaleX * scene.getWidth() ), (int)Math.rint( renderScaleY * scene.getHeight() ) );
			SnapshotParameters spa = new SnapshotParameters();
			spa.setTransform( Transform.scale( renderScaleX, renderScaleY ) );

			WritableImage image = scene.getRoot().snapshot( spa, buffer );

			try {
				Files.createDirectories( file.getParent() );
				ImageIO.write( SwingFXUtils.fromFXImage( image, null ), "png", file.toFile() );
			} catch( IOException exception ) {
				exception.printStackTrace();
			}
		} );
		Fx.waitFor( 5, 1000 );
	}

	public void close() {
		getProgram().getActionLibrary().unregisterScene( scene );
		memoryMonitor.close();
		taskMonitor.close();
		fpsMonitor.close();
		getStage().close();
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + getProgram().getCard().getName() );
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
	}

	private void updateBackgroundFromSettings( Settings settings ) {
		settings.unregister( SettingsEvent.CHANGED, backgroundSettingsHandler );
		background.updateFromSettings( settings );
		settings.register( SettingsEvent.CHANGED, backgroundSettingsHandler );
	}

	private void updateMemoryMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-memory-monitor-enabled", Boolean.class, Boolean.TRUE );
		Boolean showText = settings.get( "workspace-memory-monitor-text", Boolean.class, Boolean.TRUE );
		Boolean showPercent = settings.get( "workspace-memory-monitor-percent", Boolean.class, Boolean.TRUE );

		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
			updateContainer( memoryMonitor, enabled );
			memoryMonitor.setTextVisible( showText );
			memoryMonitor.setShowPercent( showPercent );
			settings.register( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
		} );
	}

	private void updateTaskMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-task-monitor-enabled", Boolean.class, Boolean.TRUE );
		Boolean showText = settings.get( "workspace-task-monitor-text", Boolean.class, Boolean.TRUE );
		Boolean showPercent = settings.get( "workspace-task-monitor-percent", Boolean.class, Boolean.TRUE );
		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
			updateContainer( taskMonitor, enabled );
			taskMonitor.setTextVisible( showText );
			taskMonitor.setShowPercent( showPercent );
			settings.register( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
		} );
	}

	private void updateFpsMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-fps-monitor-enabled", Boolean.class, Boolean.TRUE );
		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, fpsMonitorSettingsHandler );
			updateContainer( fpsMonitor, enabled );
			settings.register( SettingsEvent.CHANGED, fpsMonitorSettingsHandler );
		} );
	}

	private void updateContainer( AbstractMonitor monitor, boolean enabled ) {
		Group container = monitor.getMonitorGroup();
		container.getChildren().clear();
		if( enabled ) container.getChildren().add( monitor );
	}

	private class BackgroundSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			updateBackgroundFromSettings( event.getSettings() );
		}

	}

	private class MemoryMonitorSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			updateMemoryMonitorFromSettings( event.getSettings() );
		}

	}

	private class TaskMonitorSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			updateTaskMonitorFromSettings( event.getSettings() );
		}

	}

	private class FpsMonitorSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			updateFpsMonitorFromSettings( event.getSettings() );
		}

	}

	private class WorkareaNameWatcher implements ChangeListener<String> {

		@Override
		public void changed( ObservableValue<? extends String> name, String oldValue, String newValue ) {
			setStageTitle( newValue );
		}

	}

	public static class WorkareaPropertyCell extends ListCell<Workarea> {

		@Override
		protected void updateItem( Workarea item, boolean empty ) {
			super.updateItem( item, empty );
			textProperty().unbind();
			if( item != null && !empty ) textProperty().bind( item.nameProperty() );
		}

	}

}
