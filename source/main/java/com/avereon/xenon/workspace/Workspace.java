package com.avereon.xenon.workspace;

import com.avereon.event.EventHandler;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.Configurable;
import com.avereon.util.Log;
import com.avereon.xenon.Profile;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.type.ProgramTaskType;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.util.ActionUtil;
import com.avereon.venza.event.FxEventHub;
import com.avereon.xenon.util.TimerUtil;
import com.avereon.xenon.workpane.Tool;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace implements Configurable {

	private static final System.Logger log = Log.get();

	private Program program;

	private String id;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private FxEventHub eventBus;

	private StackPane workspaceStack;

	private BorderPane workareaLayout;

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private Map<String, Button> toolbarToolButtons;

	private Separator toolbarToolButtonSeparator;

	private Region toolbarToolSpring;

	private StatusBar statusBar;

	private Group memoryMonitorContainer;

	private MemoryMonitor memoryMonitor;

	private Group taskMonitorContainer;

	private TaskMonitor taskMonitor;

	private WorkspaceBackground background;

	private Pane dropHintLayer;

	private Pane workpaneContainer;

	private VBox noticeContainer;

	private BorderPane noticeLayout;

	private ComboBox<Workarea> workareaSelector;

	private ObservableList<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaNameWatcher workareaNameWatcher;

	private Settings settings;

	private Settings backgroundSettings;

	private BackgroundSettingsHandler backgroundSettingsHandler;

	private Settings memoryMonitorSettings;

	private MemoryMonitorSettingsHandler memoryMonitorSettingsHandler;

	private Settings taskMonitorSettings;

	private TaskMonitorSettingsHandler taskMonitorSettingsHandler;

	private static Timer timer = new Timer( true );

	public Workspace( final Program program ) {
		this.program = program;
		this.eventBus = new FxEventHub();

		workareas = FXCollections.observableArrayList();
		workareaNameWatcher = new WorkareaNameWatcher();
		backgroundSettingsHandler = new BackgroundSettingsHandler();
		memoryMonitorSettingsHandler = new MemoryMonitorSettingsHandler();
		taskMonitorSettingsHandler = new TaskMonitorSettingsHandler();


		// FIXME Should this default setup be defined in config files or something else?
		menubar = createMenuBar( program );

		toolbarToolButtons = new ConcurrentHashMap<>();
		toolbarToolButtonSeparator = new Separator();
		toolbarToolSpring = ActionUtil.createSpring();
		toolbar = createToolBar( program );

		statusBar = createStatusBar( program );

		noticeContainer = new VBox();
		noticeContainer.getStyleClass().add( "notice-container" );
		noticeContainer.setPickOnBounds( false );

		noticeLayout = new BorderPane( null, null, noticeContainer, null, null );
		noticeLayout.setPickOnBounds( false );

		// Workpane Container
		workpaneContainer = new StackPane( background = new WorkspaceBackground() );
		workpaneContainer.getStyleClass().add( "workspace" );

		workspaceStack = new StackPane( workpaneContainer, noticeLayout );
		workspaceStack.setPickOnBounds( false );

		VBox bars = new VBox( menubar, toolbar );

		workareaLayout = new BorderPane();
		workareaLayout.setTop( bars );
		workareaLayout.setCenter( workspaceStack );
		workareaLayout.setBottom( statusBar );

		// Create the stage
		stage = new Stage();
		stage.getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );
		stage.setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
		} );
		stage.focusedProperty().addListener( (p,o,n ) -> {
			if( n ) program.getWorkspaceManager().setActiveWorkspace( this );
		} );
	}

	public FxEventHub getEventBus() {
		return eventBus;
	}

	private MenuBar createMenuBar( Program program ) {
		MenuBar menubar = new MenuBar();

		// FIXME This does not work if there are two menu bars (like this program uses)
		// This generally affects MacOS users
		menubar.setUseSystemMenuBar( true );

		Menu prog = ActionUtil.createMenu( program, "program" );
		prog.getItems().add( ActionUtil.createMenuItem( program, "workspace-new" ) );
		prog.getItems().add( ActionUtil.createMenuItem( program, "workspace-close" ) );
		prog.getItems().add( new SeparatorMenuItem() );
		prog.getItems().add( ActionUtil.createMenuItem( program, "settings" ) );
		prog.getItems().add( new SeparatorMenuItem() );
		prog.getItems().add( ActionUtil.createMenuItem( program, "exit" ) );

		Menu file = ActionUtil.createMenu( program, "file" );
		file.getItems().add( ActionUtil.createMenuItem( program, "new" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "open" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "save" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "save-as" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "copy-as" ) );
		file.getItems().add( ActionUtil.createMenuItem( program, "close" ) );
		file.getItems().add( new SeparatorMenuItem() );
		file.getItems().add( ActionUtil.createMenuItem( program, "properties" ) );

		Menu edit = ActionUtil.createMenu( program, "edit" );
		edit.getItems().add( ActionUtil.createMenuItem( program, "undo" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "redo" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( ActionUtil.createMenuItem( program, "cut" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "copy" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "paste" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "delete" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( ActionUtil.createMenuItem( program, "indent" ) );
		edit.getItems().add( ActionUtil.createMenuItem( program, "unindent" ) );

		Menu view = ActionUtil.createMenu( program, "view" );
		view.getItems().add( ActionUtil.createMenuItem( program, "statusbar-show" ) );

		Menu help = ActionUtil.createMenu( program, "help" );
		help.getItems().add( ActionUtil.createMenuItem( program, "help-content" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( ActionUtil.createMenuItem( program, "welcome" ) );
		help.getItems().add( ActionUtil.createMenuItem( program, "task" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( ActionUtil.createMenuItem( program, "product" ) );
		help.getItems().add( ActionUtil.createMenuItem( program, "update" ) );
		help.getItems().add( ActionUtil.createMenuItem( program, "about" ) );

		Menu dev = ActionUtil.createMenu( program, "development" );
		dev.getItems().add( ActionUtil.createMenuItem( program, "test-action-1" ) );
		dev.getItems().add( ActionUtil.createMenuItem( program, "test-action-2" ) );
		dev.getItems().add( ActionUtil.createMenuItem( program, "test-action-3" ) );
		dev.getItems().add( ActionUtil.createMenuItem( program, "test-action-4" ) );
		dev.getItems().add( ActionUtil.createMenuItem( program, "test-action-5" ) );
		help.getItems().add( new SeparatorMenuItem() );
		dev.getItems().add( ActionUtil.createMenuItem( program, "restart" ) );
		dev.setId( "menu-development" );

		menubar.getMenus().addAll( prog, file, edit, view, help );
		if( Profile.DEV.equals( program.getProfile() ) ) menubar.getMenus().add( dev );

		return menubar;
	}

	private ToolBar createToolBar( Program program ) {
		ToolBar toolbar = new ToolBar();
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "new" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "open" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "save" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "properties" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "undo" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "redo" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "cut" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "copy" ) );
		toolbar.getItems().add( ActionUtil.createToolBarButton( program, "paste" ) );

		toolbar.getItems().add( toolbarToolSpring );

		// Workarea menu
		Menu workareaMenu = ActionUtil.createMenu( program, "workarea" );
		workareaMenu.getItems().add( ActionUtil.createMenuItem( program, "workarea-new" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( ActionUtil.createMenuItem( program, "workarea-rename" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( ActionUtil.createMenuItem( program, "workarea-close" ) );

		MenuBar workareaMenuBar = new MenuBar();
		workareaMenuBar.getMenus().add( workareaMenu );
		workareaMenuBar.setBackground( Background.EMPTY );
		workareaMenuBar.setPadding( Insets.EMPTY );
		workareaMenuBar.setBorder( Border.EMPTY );

		// Workarea selector
		workareaSelector = new ComboBox<>();
		workareaSelector.setItems( workareas );
		workareaSelector.setButtonCell( new WorkareaPropertyCell() );
		workareaSelector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );

		toolbar.getItems().add( workareaMenuBar );
		toolbar.getItems().add( workareaSelector );

		toolbar.getItems().add( ActionUtil.createPad() );
		Button noticeButton = ActionUtil.createToolBarButton( program, "notice" );
		noticeButton.setContentDisplay( ContentDisplay.RIGHT );
		noticeButton.setText( "0" );
		program.getNoticeManager().unreadCountProperty().addListener( ( event, oldValue, newValue ) -> {
			int count = newValue.intValue();
			Platform.runLater( () -> {
				program.getActionLibrary().getAction( "notice" ).setIcon( program.getNoticeManager().getUnreadNoticeType().getIcon() );
				noticeButton.setText( String.valueOf( count ) );
			} );
		} );
		toolbar.getItems().add( noticeButton );

		return toolbar;
	}

	private StatusBar createStatusBar( Program program ) {
		StatusBar statusBar = new StatusBar();

		// Task Monitor
		taskMonitor = new TaskMonitor( program.getTaskManager() );
		taskMonitorContainer = new Group();

		// If the task monitor is clicked then open the task tool
		taskMonitor.setOnMouseClicked( ( event ) -> program.getAssetManager().openAsset( ProgramTaskType.URI ) );

		// Memory Monitor
		memoryMonitor = new MemoryMonitor();
		memoryMonitorContainer = new Group();

		// If the memory monitor is clicked then call the garbage collector
		memoryMonitor.setOnMouseClicked( ( event ) -> Runtime.getRuntime().gc() );

		statusBar.addRight( memoryMonitorContainer );
		statusBar.addRight( taskMonitorContainer );

		return statusBar;
	}

	public void pushToolbarActions( String... ids ) {
		pullToolbarActions();
		int index = toolbar.getItems().indexOf( toolbarToolSpring );
		toolbar.getItems().add( index++, toolbarToolButtonSeparator );
		for( String id : ids ) {
			Node node;
			if( "separator".equals( id ) ) {
				node = new Separator();
			} else {
				node = ActionUtil.createToolBarButton( getProgram(), id );
			}
			toolbar.getItems().add( index++, node );
		}
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
		settings.set( "active", active );
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
			noticeContainer.getChildren().remove( pane );
			getProgram().getNoticeManager().readNotice( notice );
			pane.executeNoticeAction();
			event.consume();
		} );

		pane.getCloseButton().setOnMouseClicked( ( event ) -> {
			noticeContainer.getChildren().remove( pane );
			getProgram().getNoticeManager().readNotice( notice );
			event.consume();
		} );

		int balloonTimeout = getProgram().getProgramSettings().get( "notice-balloon-timeout", Integer.class, 5000 );

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
	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;

		// The incoming settings are the workspace settings

		this.settings = settings;
		this.id = settings.get( "id" );

		Double x = settings.get( "x", Double.class, null );
		Double y = settings.get( "y", Double.class, null );
		Double w = settings.get( "w", Double.class, UiFactory.DEFAULT_WIDTH );
		Double h = settings.get( "h", Double.class, UiFactory.DEFAULT_HEIGHT );

		// Due to differences in how FX handles stage size (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		stage.setScene( scene = new Scene( workareaLayout, w, h ) );
		scene.getStylesheets().add( Program.STYLESHEET );
		stage.sizeToScene();

		// Position the stage if x and y are specified
		// If not specified the stage is centered on the screen
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
		stage.maximizedProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( stage.isShowing() ) settings.set( "maximized", newValue );
		} );
		stage.xProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "x", newValue );
		} );
		stage.yProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "y", newValue );
		} );
		scene.widthProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "w", newValue );
		} );
		scene.heightProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) settings.set( "h", newValue );
		} );

		backgroundSettings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		backgroundSettings.unregister( SettingsEvent.CHANGED, backgroundSettingsHandler );
		background.updateBackgroundFromSettings( backgroundSettings );
		backgroundSettings.register( SettingsEvent.CHANGED, backgroundSettingsHandler );

		memoryMonitorSettings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		memoryMonitorSettings.unregister( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
		updateMemoryMonitorFromSettings( memoryMonitorSettings );
		memoryMonitorSettings.register( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );

		taskMonitorSettings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		taskMonitorSettings.unregister( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
		updateTaskMonitorFromSettings( taskMonitorSettings );
		taskMonitorSettings.register( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	public void snapshot( Path file ) {
		Platform.runLater( () -> {
			WritableImage image = getStage().getScene().snapshot( null );
			try {
				ImageIO.write( SwingFXUtils.fromFXImage( image, null ), "png", file.toFile() );
			} catch( IOException exception ) {
				exception.printStackTrace();
			}
		} );
	}

	public void close() {
		memoryMonitor.close();
		taskMonitor.close();
		getStage().close();
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + getProgram().getCard().getName() );
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
	}

	private void updateMemoryMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-memory-monitor-enabled", Boolean.class, Boolean.TRUE );
		Boolean showText = settings.get( "workspace-memory-monitor-text", Boolean.class, Boolean.TRUE );
		Boolean showPercent = settings.get( "workspace-memory-monitor-percent", Boolean.class, Boolean.TRUE );

		Platform.runLater( () -> {
			updateContainer( memoryMonitorContainer, memoryMonitor, enabled );
			memoryMonitor.setTextVisible( showText );
			memoryMonitor.setShowPercent( showPercent );
		} );
	}

	private void updateTaskMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-task-monitor-enabled", Boolean.class, Boolean.TRUE );
		Boolean showText = settings.get( "workspace-task-monitor-text", Boolean.class, Boolean.TRUE );
		Boolean showPercent = settings.get( "workspace-task-monitor-percent", Boolean.class, Boolean.TRUE );
		Platform.runLater( () -> {
			updateContainer( taskMonitorContainer, taskMonitor, enabled );
			taskMonitor.setTextVisible( showText );
			taskMonitor.setShowPercent( showPercent );
		} );
	}

	private void updateContainer( Group container, Node tool, boolean enabled ) {
		if( enabled ) {
			if( !container.getChildren().contains( tool ) ) container.getChildren().add( tool );
		} else {
			container.getChildren().remove( tool );
		}
	}

	//	private class BackgroundSettingsHandler implements SettingsListener {
	//
	//		@Override
	//		public void handle( SettingsEvent event ) {
	//			if( event.getEventType() != SettingsEvent.CHANGED ) return;
	//			background.updateBackgroundFromSettings( backgroundSettings );
	//		}
	//
	//	}
	//
	private class BackgroundSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			background.updateBackgroundFromSettings( backgroundSettings );
		}
	}

	private class MemoryMonitorSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			updateMemoryMonitorFromSettings( memoryMonitorSettings );
		}

	}

	private class TaskMonitorSettingsHandler implements EventHandler<SettingsEvent> {

		@Override
		public void handle( SettingsEvent event ) {
			updateTaskMonitorFromSettings( taskMonitorSettings );
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
