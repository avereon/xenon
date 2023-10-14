package com.avereon.xenon.workspace;

import com.avereon.event.EventHandler;
import com.avereon.product.Profile;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.ui.util.MenuFactory;
import com.avereon.xenon.ui.util.ToolBarFactory;
import com.avereon.xenon.util.TimerUtil;
import com.avereon.xenon.workpane.Tool;
import com.avereon.zarra.event.FxEventHub;
import com.avereon.zarra.javafx.Fx;
import com.avereon.zarra.javafx.FxUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
import javafx.stage.StageStyle;
import lombok.CustomLog;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The workspace manages the menu bar, toolbar and workareas.
 */
@CustomLog
public class Workspace extends Stage implements WritableIdentity {

	public static final String WORKSPACE_PROPERTY_KEY = Workspace.class.getName();

	public static final String EDIT_ACTION = "edit";

	public static final String VIEW_ACTION = "view";

	/**
	 * Should the program menu be shown as a compact menu in the toolbar.
	 */
	private static final boolean COMPACT_MENU = true;

	public static final String TOOL_BAR = "tool-bar";

	private final Xenon program;

	private Scene scene;

	private boolean active;

	private final FxEventHub eventBus;

	private final BorderPane workareaLayout;

	private final MenuBar programMenuBar;

	private final ContextMenu verticalProgramMenu;

	// This menu is used to mark the beginning of the space where tools can push
	// their own actions as well as be a standard menu.
	private final MenuItem programMenuToolStart;

	// This menu is used to mark the end of the space where tools can push their
	// own actions as well as be a standard menu.
	private final MenuItem programMenuToolEnd;

	private final ToolBar toolbar;

	// This separator is also used to mark the beginning of the space where tools
	// can push their own actions as well as provide a separator between the
	// standard actions and the tool actions.
	private final Separator toolbarToolStart;

	// This region is also used to mark the end of the space where tools can push
	// their own actions as well as provide the space between the tool actions and
	// the workspace menu.
	private final Region toolbarToolEnd;

	private final StatusBar statusBar;

	private final WorkspaceBackground background;

	private final Pane workpaneContainer;

	private final VBox noticeBox;

	private final ObservableList<Workarea> workareas;

	private final WorkareaNameWatcher workareaNameWatcher;

	private final BackgroundSettingsHandler backgroundSettingsHandler;

	private final MemoryMonitorSettingsHandler memoryMonitorSettingsHandler;

	private final TaskMonitorSettingsHandler taskMonitorSettingsHandler;

	private final FpsMonitorSettingsHandler fpsMonitorSettingsHandler;

	private final ToggleMinimizeAction toggleMinimizeAction;

	private final ToggleMaximizeAction toggleMaximizeAction;

	private ComboBox<Workarea> workareaSelector;

	private Workarea activeWorkarea;

	private MemoryMonitor memoryMonitor;

	private TaskMonitor taskMonitor;

	private FpsMonitor fpsMonitor;

	public Workspace( final Xenon program, final String id ) {
		// FIXME Cannot resize an undecorated stage
		super(StageStyle.TRANSPARENT);

		this.program = program;
		this.eventBus = new FxEventHub();
		this.eventBus.parent( program.getFxEventHub() );

		// Create the stage
		getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );
		setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
		} );
		focusedProperty().addListener( ( p, o, n ) -> {
			if( Boolean.TRUE.equals( n ) ) program.getWorkspaceManager().setActiveWorkspace( this );
		} );

		setUid( id );

		workareas = FXCollections.observableArrayList();
		workareaNameWatcher = new WorkareaNameWatcher();
		backgroundSettingsHandler = new BackgroundSettingsHandler();
		memoryMonitorSettingsHandler = new MemoryMonitorSettingsHandler();
		taskMonitorSettingsHandler = new TaskMonitorSettingsHandler();
		fpsMonitorSettingsHandler = new FpsMonitorSettingsHandler();

		toggleMinimizeAction = new ToggleMinimizeAction( program, this );
		toggleMaximizeAction = new ToggleMaximizeAction( program, this );

		programMenuBar = createProgramMenuBar( program );
		verticalProgramMenu = createProgramMenu( program );
		programMenuToolStart = FxUtil.findMenuItemById( verticalProgramMenu.getItems(), MenuFactory.MENU_ID_PREFIX + EDIT_ACTION );
		programMenuToolEnd = FxUtil.findMenuItemById( verticalProgramMenu.getItems(), MenuFactory.MENU_ID_PREFIX + VIEW_ACTION );

		workareaSelector = createWorkareaSelector();
		Pane toolPane = createToolPane( program, this, workareaSelector );

		toolbarToolStart = new Separator();
		toolbarToolEnd = ToolBarFactory.createSpring();
		toolbar = createProgramToolBar( program );

		noticeBox = createNoticeBox();
		BorderPane noticePane = new BorderPane( null, null, noticeBox, null, null );
		// Setting pickOnBounds here is important for mouse events to pass to the
		// workarea. When the notice pane is showing, it captures mouse events, even
		// though it is transparent. This makes sense since mouse events need to be
		// passed to the notices. In order to pass events through the transparent
		// area, pickOnBounds is set to false.
		noticePane.setPickOnBounds( false );

		statusBar = createStatusBar( program );
		Pane statusPane = createStatusPane( statusBar );

		// Workpane container
		background = new WorkspaceBackground();
		workpaneContainer = new StackPane( background );
		workpaneContainer.getStyleClass().add( "workspace" );

		Pane workspaceStack = new StackPane( workpaneContainer, noticePane );

		workareaLayout = new BorderPane();
		workareaLayout.getProperties().put( WORKSPACE_PROPERTY_KEY, this );
		workareaLayout.setTop( toolPane );
		workareaLayout.setCenter( workspaceStack );
		workareaLayout.setBottom( statusPane );

		maximizedProperty().addListener( ( v, o, n ) -> {
			String icon = n? "normalize" : "maximize";
			getProgram().getActionLibrary().getAction( "maximize" ).setIcon( icon );

			// TODO Also toggle the resize border
		} );


		memoryMonitor.start();
		taskMonitor.start();
		fpsMonitor.start();
	}

	private static Pane createToolPane( Xenon program, Stage stage, Node workareaSelector ) {
		StageMover stageMover = new StageMover( stage );
		stageMover.getStyleClass().add( TOOL_BAR );
		ToolBar leftToolBar = ToolBarFactory.createToolBar( program, "menu" );
		ToolBar rightToolBar = ToolBarFactory.createToolBar( program, "notice|minimize,maximize,workspace-close" );

		Pane workareaSelectorPane = new BorderPane( workareaSelector );
		workareaSelectorPane.getStyleClass().add( TOOL_BAR );
		HBox leftBox = new HBox( leftToolBar, workareaSelectorPane );
		HBox rightBox = new HBox( rightToolBar );

		return new BorderPane( stageMover, null, rightBox, null, leftBox );
	}

	private static VBox createNoticeBox() {
		VBox box = new VBox();
		box.getStyleClass().addAll( "flyout" );
		box.setVisible( false );
		return box;
	}

	private static Pane createStatusPane( StatusBar statusBar ) {
		return new BorderPane( statusBar );
	}

	public void setTheme( String url ) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add( Xenon.STYLESHEET );
		if( url != null ) scene.getStylesheets().add( url );
	}

	public FxEventHub getEventBus() {
		return eventBus;
	}

	private MenuBar createProgramMenuBar( Xenon program ) {
		// Load the menu descriptors
		String defaultDescriptor = program.getSettings().get( "workspace-menubar" );
		String customDescriptor = getSettings().get( "menubar", defaultDescriptor );

		// Build the program menu
		List<Menu> menus = MenuFactory.createMenus( program, customDescriptor, false );

		// Add the dev menu if using the dev profile
		if( Profile.DEV.equals( program.getProfile() ) ) insertDevMenu( program, menus );

		return new MenuBar( menus.toArray( new Menu[ 0 ] ) );
	}

	private ContextMenu createProgramMenu( Xenon program ) {
		// Load the menu descriptors
		String defaultDescriptor = program.getSettings().get( "workspace-menu" );
		String customDescriptor = getSettings().get( "menubar", defaultDescriptor );

		// Build the program menu
		ContextMenu menu = MenuFactory.createContextMenu( program, customDescriptor, COMPACT_MENU );

		// Add the dev menu if using the dev profile
		if( Profile.DEV.equals( program.getProfile() ) ) insertDevMenu( program, menu );

		return menu;
	}

	private void insertDevMenu( Xenon program, List<Menu> menus ) {
		int index = menus.stream().filter( ( item ) -> (MenuFactory.MENU_ID_PREFIX + "maintenance").equals( item.getId() ) ).mapToInt( menus::indexOf ).findFirst().orElse( -1 );
		if( index >= 0 ) menus.add( index, generateDevMenu( program ) );
	}

	private void insertDevMenu( Xenon program, ContextMenu menu ) {
		menu.getItems().add( generateDevMenu( program ) );
	}

	private Menu generateDevMenu( Xenon program ) {
		String development = "development[restart,uireset,mock-update|test-action-1,test-action-2,test-action-3,test-action-4,test-action-5|mock-update]";
		return MenuFactory.createMenu( program, development, true );
	}

	private ToolBar createProgramToolBar( Xenon program ) {
		String defaultDescriptor = program.getSettings().get( "workspace-toolbar" );
		String descriptor = getSettings().get( "toolbar", defaultDescriptor );

		ToolBar toolbar = ToolBarFactory.createToolBar( program, descriptor );
		toolbar.getItems().add( toolbarToolEnd );

		return toolbar;
	}

	@Deprecated( since = "1.7", forRemoval = true )
	private HBox getWorkareaTools() {
		HBox box = new HBox();
		box.setAlignment( Pos.CENTER_RIGHT );
		box.getStyleClass().addAll( "menu-bar" );

		return box;
	}

	private HBox createToolbarRightArea() {
		HBox box = new HBox();
		box.getStyleClass().addAll( TOOL_BAR );
		box.setPadding( Insets.EMPTY );

		// Add the workarea menu and selector
		workareaSelector = createWorkareaSelector();
		box.getChildren().add( createWorkareaMenu( program ) );
		box.getChildren().add( workareaSelector );

		// Add the notice button
		box.getChildren().add( ToolBarFactory.createPad() );
		box.getChildren().add( createNoticeToolbarButton() );

		return box;
	}

	private void addProgramTools( ToolBar toolbar ) {
		// Add the workarea menu and selector
		toolbar.getItems().add( createWorkareaMenu( program ) );
		toolbar.getItems().add( workareaSelector = createWorkareaSelector() );

		// Add the notice button
		toolbar.getItems().add( ToolBarFactory.createPad() );
		toolbar.getItems().add( createNoticeToolbarButton() );
	}

	private Button createNoticeToolbarButton() {
		Button noticeButton = ToolBarFactory.createToolBarButton( program, "notice" );
		noticeButton.setContentDisplay( ContentDisplay.RIGHT );
		//noticeButton.setText( "0" );
		program.getNoticeManager().unreadCountProperty().addListener( ( event, oldValue, newValue ) -> {
			int count = newValue.intValue();
			String icon = count == 0 ? "notice" : program.getNoticeManager().getUnreadNoticeType().getUnreadIcon();
			Fx.run( () -> {
				program.getActionLibrary().getAction( "notice" ).setIcon( icon );
				//noticeButton.setText( String.valueOf( count ) );program.getNoticeManager().getUnreadNoticeType().getUnreadIcon()
			} );
		} );
		return noticeButton;
	}

	private static MenuBar createWorkareaMenu( Xenon program ) {
		String descriptor = "workarea[workarea-new|workarea-rename|workarea-close]";

		MenuBar workareaMenuBar = new MenuBar();
		workareaMenuBar.getMenus().add( MenuFactory.createMenu( program, descriptor, COMPACT_MENU ) );
		workareaMenuBar.getStyleClass().addAll( "workarea-menu-bar" );
		return workareaMenuBar;
	}

	private ComboBox<Workarea> createWorkareaSelector() {
		ComboBox<Workarea> selector = new ComboBox<>();
		selector.setItems( workareas );
		selector.setButtonCell( new WorkareaPropertyCell() );
		selector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );
		return selector;
	}

	private StatusBar createStatusBar( Xenon program ) {
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

	public void showProgramMenu( ActionEvent event ) {
		verticalProgramMenu.show( (Node)event.getSource(), Side.BOTTOM, 0, 0 );
	}

	public void pushMenuActions( String descriptor ) {
		pullMenuActions();
		descriptor = "tool[" + descriptor + "]";
		int index = verticalProgramMenu.getItems().indexOf( programMenuToolEnd );
		//programMenu.getItems().add( index++, programMenuToolStart );
		verticalProgramMenu.getItems().addAll( index, MenuFactory.createMenus( getProgram(), descriptor, COMPACT_MENU ) );
	}

	public void pullMenuActions() {
		int index = verticalProgramMenu.getItems().indexOf( programMenuToolStart );
		if( index < 0 ) return;
		index++;

		MenuItem node = verticalProgramMenu.getItems().get( index );
		while( node != programMenuToolEnd ) {
			verticalProgramMenu.getItems().remove( index );
			node = verticalProgramMenu.getItems().get( index );
		}
	}

	public void pushToolbarActions( String descriptor ) {
		pullToolbarActions();
		int index = toolbar.getItems().indexOf( toolbarToolEnd );
		toolbar.getItems().add( index++, toolbarToolStart );
		toolbar.getItems().addAll( index, ToolBarFactory.createToolBar( getProgram(), descriptor ).getItems() );
	}

	public void pullToolbarActions() {
		int index = toolbar.getItems().indexOf( toolbarToolStart );
		if( index < 0 ) return;

		Node node = toolbar.getItems().get( index );
		while( node != toolbarToolEnd ) {
			toolbar.getItems().remove( index );
			node = toolbar.getItems().get( index );
		}
	}

	public Xenon getProgram() {
		return program;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		if( !active ) {
			getProgram().getActionLibrary().getAction( "minimize" ).pullAction( toggleMinimizeAction );
			getProgram().getActionLibrary().getAction( "maximize" ).pullAction( toggleMaximizeAction );
		}

		this.active = active;

		if( active ) {
			getProgram().getActionLibrary().getAction( "maximize" ).pushAction( toggleMaximizeAction );
			getProgram().getActionLibrary().getAction( "minimize" ).pushAction( toggleMinimizeAction );
		}

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
		if( activeWorkarea == null && workareas.size() == 1 ) setActiveWorkarea( workareas.get( 0 ) );
		return activeWorkarea;
	}

	public void setActiveWorkarea( Workarea workarea ) {
		if( activeWorkarea == workarea ) return;

		// Disconnect the old active workarea
		if( activeWorkarea != null ) {
			activeWorkarea.nameProperty().removeListener( workareaNameWatcher );
			activeWorkarea.setActive( false );
			// TODO Remove the program menu
			// TODO Remove the tool bar
			workpaneContainer.getChildren().remove( activeWorkarea.getWorkpane() );
			activeWorkarea.getWorkpane().setVisible( false );
		}

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkarea( workarea );
		// Set the new active workarea
		Workarea priorWorkarea = activeWorkarea;
		activeWorkarea = workarea;

		// Connect the new active workarea
		if( activeWorkarea != null ) {
			workpaneContainer.getChildren().add( activeWorkarea.getWorkpane() );
			activeWorkarea.getWorkpane().setVisible( true );
			// TODO Set the program menu
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

	public Pane getNoticePane() {
		return noticeBox;
	}

	public void showNotice( final Notice notice ) {
		if( Objects.equals( notice.getBalloonStickiness(), Notice.Balloon.NEVER ) ) return;

		NoticePane pane = new NoticePane( program, notice, true );
		noticeBox.getChildren().removeIf( node -> Objects.equals( ((NoticePane)node).getNotice().getId(), notice.getId() ) );
		noticeBox.getChildren().add( 0, pane );

		pane.setOnMouseClicked( event -> {
			getProgram().getNoticeManager().readNotice( notice );
			noticeBox.getChildren().remove( pane );
			if( noticeBox.getChildren().isEmpty() ) noticeBox.setVisible( false );
			pane.executeNoticeAction();
			event.consume();
		} );

		pane.getCloseButton().setOnMouseClicked( ( event ) -> {
			getProgram().getNoticeManager().readNotice( notice );
			noticeBox.getChildren().remove( pane );
			if( noticeBox.getChildren().isEmpty() ) noticeBox.setVisible( false );
			event.consume();
		} );

		int balloonTimeout = getProgram().getSettings().get( "notice-balloon-timeout", Integer.class, 5000 );

		if( Objects.equals( notice.getBalloonStickiness(), Notice.Balloon.NORMAL ) ) {
			TimerUtil.fxTask( () -> {
				noticeBox.getChildren().remove( pane );
				if( noticeBox.getChildren().isEmpty() ) noticeBox.setVisible( false );
				getEventBus().dispatch( new NoticeEvent( this, NoticeEvent.REMOVED, this, notice ) );
			}, balloonTimeout );
		}

		noticeBox.setVisible( true );

		getEventBus().dispatch( new NoticeEvent( this, NoticeEvent.ADDED, this, notice ) );
	}

	public void hideNotices() {
		noticeBox.getChildren().clear();
		noticeBox.setVisible( false );
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public String getUid() {
		return getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setUid( String id ) {
		getProperties().put( Identity.KEY, id );
	}

	Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.WORKSPACE, getUid() );
	}

	@SuppressWarnings( "CommentedOutCode" )
	public void updateFromSettings( Settings settings ) {
		// Due to differences in how FX handles stage sizes (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		Double w = settings.get( "w", Double.class, UiFactory.DEFAULT_WIDTH );
		Double h = settings.get( "h", Double.class, UiFactory.DEFAULT_HEIGHT );
		scene = new Scene( workareaLayout, w, h );
		scene.setFill( Color.TRANSPARENT );
		getProgram().getActionLibrary().registerScene( scene );

		// Setup the stage
		setScene( scene );
		sizeToScene();

		// Position the stage if x and y are specified
		// If not specified the stage is centered on the screen
		Double x = settings.get( "x", Double.class, null );
		Double y = settings.get( "y", Double.class, null );
		if( x != null ) setX( x );
		if( y != null ) setY( y );

		// On Linux, setWidth() and setHeight() do not take the stage window
		// decorations into account. The way to deal with this is to watch
		// the scene size and set the scene size on creation.
		// Do not use the following:
		// if( w != null ) stage.setWidth( w );
		// if( h != null ) stage.setHeight( h );

		setMaximized( settings.get( "maximized", Boolean.class, false ) );
		setActive( settings.get( "active", Boolean.class, false ) );

		// Add the property listeners
		maximizedProperty().addListener( ( v, o, n ) -> {
			if( isShowing() ) settings.set( "maximized", n );
		} );
		xProperty().addListener( ( v, o, n ) -> {
			if( !isMaximized() ) settings.set( "x", n );
		} );
		yProperty().addListener( ( v, o, n ) -> {
			if( !isMaximized() ) settings.set( "y", n );
		} );
		scene.widthProperty().addListener( ( v, o, n ) -> {
			if( !isMaximized() ) settings.set( "w", n );
		} );
		scene.heightProperty().addListener( ( v, o, n ) -> {
			if( !isMaximized() ) settings.set( "h", n );
		} );

		updateBackgroundFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateMemoryMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateTaskMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateFpsMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
	}

	public void screenshot( Path file ) {
		Fx.waitFor( 5, TimeUnit.SECONDS );
		Fx.run( () -> {
			double renderScaleX = getRenderScaleX();
			double renderScaleY = getRenderScaleY();

			WritableImage buffer = new WritableImage( (int)Math.rint( renderScaleX * scene.getWidth() ), (int)Math.rint( renderScaleY * scene.getHeight() ) );
			SnapshotParameters spa = new SnapshotParameters();
			spa.setTransform( Transform.scale( renderScaleX, renderScaleY ) );

			WritableImage image = scene.getRoot().snapshot( spa, buffer );

			try {
				Files.createDirectories( file.getParent() );
				ImageIO.write( SwingFXUtils.fromFXImage( image, null ), "png", file.toFile() );
			} catch( IOException exception ) {
				log.atWarn( exception );
			}
		} );
		Fx.waitFor( 5, TimeUnit.SECONDS );
	}

	@Override
	public void close() {
		getProgram().getActionLibrary().unregisterScene( scene );
		memoryMonitor.close();
		taskMonitor.close();
		fpsMonitor.close();
		super.close();
	}

	private void setStageTitle( String name ) {
		setTitle( name + " - " + getProgram().getCard().getName() );
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
	}

	private void updateBackgroundFromSettings( Settings settings ) {
		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, backgroundSettingsHandler );
			background.updateFromSettings( settings );
			settings.register( SettingsEvent.CHANGED, backgroundSettingsHandler );
		} );
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
			if( item == null || empty ) {
				textProperty().unbind();
			} else {
				textProperty().bind( item.nameProperty() );
			}
		}

	}

}
