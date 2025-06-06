package com.avereon.xenon.workspace;

import com.avereon.event.EventHandler;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.skill.Identity;
import com.avereon.skill.WritableIdentity;
import com.avereon.xenon.*;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.notice.NoticePane;
import com.avereon.xenon.ui.util.MenuBarFactory;
import com.avereon.xenon.ui.util.ToolBarFactory;
import com.avereon.xenon.util.TimerUtil;
import com.avereon.xenon.workpane.Tool;
import com.avereon.zerra.color.Colors;
import com.avereon.zerra.event.FxEventHub;
import com.avereon.zerra.javafx.Fx;
import com.avereon.zerra.javafx.FxUtil;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.CustomLog;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The workspace manages the menu bar, toolbar and workareas.
 */
@CustomLog
public class Workspace extends Stage implements WritableIdentity {

	public static final String WORKSPACE_PROPERTY_KEY = Workspace.class.getName();

	public static final String EDIT_ACTION = "edit";

	public static final String VIEW_ACTION = "view";

	public static final String ACTION_BAR = "action-bar";

	public static final String ACTIONS = "actions";

	public static final String NORMALIZE = "normalize";

	public static final String MAXIMIZE = "maximize";

	public static final String MINIMIZE = "minimize";

	public static final String NOTICE = "notice";

	public static final String ORDER = "order";

	public static final String ACTIVE = "active";

	/**
	 * Should the program menu be shown as a compact menu in the toolbar.
	 */
	private static final boolean COMPACT_MENU = true;

	private static final boolean TRANSPARENT_WINDOW_SUPPORTED = Platform.isSupported( ConditionalFeature.TRANSPARENT_WINDOW );

	private static final Timer timer = new Timer( true );

	@Getter
	private final Xenon program;

	private Scene scene;

	@Getter
	private boolean active;

	@Getter
	private final FxEventHub eventBus;

	private final Pane railPane;

	private final Set<Pane> rails;

	private final Region actionBar;

	private final Region workspaceActionBar;

	private final Node workareaMenu;

	private final MenuBar programMenuBar;

	// This menu is used to mark the beginning of the space where tools can push
	// their own actions as well as be a standard menu.
	private final Menu programMenuToolStart;

	// This menu is used to mark the end of the space where tools can push their
	// own actions as well as be a standard menu.
	private final Menu programMenuToolEnd;

	private final ToolBar toolbar;

	// This separator is also used to mark the beginning of the space where tools
	// can push their own actions as well as provide a separator between the
	// standard actions and the tool actions.
	private final Separator toolbarToolStart;

	// This region is also used to mark the end of the space where tools can push
	// their own actions as well as provide the space between the tool actions and
	// the workspace menu.
	private final Region toolbarToolEnd;

	@Getter
	private final StatusBar statusBar;

	@Getter
	private final WorkspaceBackground background;

	private final Pane workpaneContainer;

	private final VBox noticeBox;

	private final ObservableList<Workarea> workareas;

	private final BackgroundSettingsHandler backgroundSettingsHandler;

	private final MemoryMonitorSettingsHandler memoryMonitorSettingsHandler;

	private final TaskMonitorSettingsHandler taskMonitorSettingsHandler;

	private final FpsMonitorSettingsHandler fpsMonitorSettingsHandler;

	private final ToggleMinimizeAction toggleMinimizeAction;

	private final ToggleMaximizeAction toggleMaximizeAction;

	private final SimpleObjectProperty<Workarea> activeWorkareaProperty;

	private final SimpleIntegerProperty orderProperty;

	private MemoryMonitor memoryMonitor;

	private TaskMonitor taskMonitor;

	private FpsMonitor fpsMonitor;

	/**
	 * Create a new workspace.
	 *
	 * @param program the program
	 */
	public Workspace( final Xenon program ) {
		// Please create all workspaces from the UiWorkspaceFactory
		this( program, null );
	}

	public Workspace( final Xenon program, final String id ) {
		super( TRANSPARENT_WINDOW_SUPPORTED ? StageStyle.TRANSPARENT : StageStyle.UNDECORATED );
		if( !TRANSPARENT_WINDOW_SUPPORTED ) log.atWarn().log( "Transparent windows not supported" );

		this.program = program;
		this.eventBus = new FxEventHub();
		this.eventBus.parent( program.getFxEventHub() );

		setUid( id );
		getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );

		// Stage listeners
		setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
		} );
		focusedProperty().addListener( ( p, o, n ) -> {
			if( Boolean.TRUE.equals( n ) ) program.getWorkspaceManager().setActiveWorkspace( this );
		} );

		activeWorkareaProperty = new SimpleObjectProperty<>();
		orderProperty = new SimpleIntegerProperty();
		workareas = FXCollections.observableArrayList();
		backgroundSettingsHandler = new BackgroundSettingsHandler();
		memoryMonitorSettingsHandler = new MemoryMonitorSettingsHandler();
		taskMonitorSettingsHandler = new TaskMonitorSettingsHandler();
		fpsMonitorSettingsHandler = new FpsMonitorSettingsHandler();

		toggleMinimizeAction = new ToggleMinimizeAction( program, this );
		toggleMaximizeAction = new ToggleMaximizeAction( program, this );

		// Create the workarea menu
		workareaMenu = createWorkareaMenu( program );

		// Create the program menu bar
		String defaultDescriptor = program.getSettings().get( "workspace-menu" );
		String menuDescriptor = program.getSettings().get( "menubar", defaultDescriptor );
		programMenuBar = MenuBarFactory.createMenuBar( program, menuDescriptor, false );
		if( XenonMode.DEV.equals( program.getMode() ) ) insertDevMenu( program, programMenuBar.getMenus() );
		programMenuToolStart = FxUtil.findMenuItemById( programMenuBar.getMenus(), MenuBarFactory.MENU_ID_PREFIX + EDIT_ACTION );
		programMenuToolEnd = FxUtil.findMenuItemById( programMenuBar.getMenus(), MenuBarFactory.MENU_ID_PREFIX + VIEW_ACTION );

		// Create the workspace action bar
		workspaceActionBar = new StackPane( workareaMenu, programMenuBar );

		toolbarToolStart = new Separator();
		toolbarToolEnd = ToolBarFactory.createSpring();
		toolbar = createProgramToolBar( program, toolbarToolStart, toolbarToolEnd );

		// Create the action bar. Depends on workspaceSelectionContainer and toolbar.
		actionBar = createActionBar( program );

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

		Pane workspaceStack = new StackPane( workpaneContainer, noticePane );

		// Create the workspace layout pane
		BorderPane workspaceLayout = new BorderPane();
		workspaceLayout.getStyleClass().add( "workspace" );
		workspaceLayout.getProperties().put( WORKSPACE_PROPERTY_KEY, this );

		// Set the workspace layout components
		workspaceLayout.setTop( actionBar );
		workspaceLayout.setCenter( workspaceStack );
		workspaceLayout.setBottom( statusPane );

		rails = new HashSet<>();
		railPane = buildRailPane( workspaceLayout );

		orderProperty().addListener( ( p, o, n ) -> {
			getSettings().set( "order", n );
		} );

		showingProperty().addListener( ( p, o, n ) -> {
			if( n ) hideProgramMenuBar();
		} );

		// Bind the stage title property to the active workarea name
		activeWorkareaProperty.addListener( ( p, o, n ) -> {
			if( n == null ) {
				titleProperty().unbind();
				actionBar.backgroundProperty().unbind();
			} else {
				titleProperty().bind( n.nameProperty().map( this::generateStageTitle ) );
				actionBar.backgroundProperty().bind( n.colorProperty().map( c -> {
					Color mix = Colors.mix( c, Color.TRANSPARENT, 0.6 );
					LinearGradient gradient = new LinearGradient( 0, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop( 0, mix ), new Stop( 1, Color.TRANSPARENT ) );
					return Background.fill( gradient );
				} ) );
			}
		} );

		// Maximized property listener
		maximizedProperty().addListener( ( p, o, n ) -> {
			// Toggle the maximize/normalize icon
			String icon = Boolean.TRUE.equals( n ) ? NORMALIZE : MAXIMIZE;
			getProgram().getActionLibrary().getAction( MAXIMIZE ).setIcon( icon );

			// Toggle the rails
			rails.forEach( r -> r.setVisible( !n ) );
		} );

		// Show the first menu when the program menu bar shows
		programMenuBar.visibleProperty().addListener( ( p, o, n ) -> {
			if( Boolean.TRUE.equals( n ) ) programMenuBar.getMenus().getFirst().show();
		} );

		// This catches when the user presses ESC, but not when they select a menu item
		programMenuBar.addEventHandler(
			MenuButton.ON_HIDING, e -> {
				// Pressing ESC causes an extra MenuButton.ON_HIDING event with the menu already hidden
				MenuButton button = (MenuButton)e.getTarget();
				if( !button.isShowing() ) Fx.run( this::hideProgramMenuBar );
			}
		);

		// This catches when menus are hidden and the mouse is not hovering over the menu bar
		programMenuBar.addEventFilter(
			MenuButton.ON_HIDDEN, e -> Fx.run( () -> {
				// It's important that this run as a different runnable on the FX thread
				// If no other menus are showing, hide the program menu bar
				if( allMenusAreHidden( programMenuBar ) ) Fx.run( this::hideProgramMenuBar );
			} )
		);

		memoryMonitor.start();
		taskMonitor.start();
		fpsMonitor.start();
	}

	private Pane buildRailPane( Node workspaceLayout ) {
		Pane t = new WorkspaceRail( Side.TOP );
		Pane r = new WorkspaceRail( Side.RIGHT );
		Pane b = new WorkspaceRail( Side.BOTTOM );
		Pane l = new WorkspaceRail( Side.LEFT );

		rails.add( t );
		rails.add( r );
		rails.add( b );
		rails.add( l );

		return new BorderPane( workspaceLayout, t, r, b, l );
	}

	private HBox createActionBar( Xenon program ) {
		ToolBar programActionBar = ToolBarFactory.createToolBar( program, "program{minimize,maximize|workspace-close}" );
		programActionBar.getStyleClass().add( ACTIONS );

		// The action bar spring
		Node spring = StageMover.of( ToolBarFactory.createSpring() );

		// The workspace actions
		ToolBar workspaceActions = ToolBarFactory.createToolBar( program, "notice-toggle,search-toggle,settings{settings,modules|theme|update}|minimize,maximize,workspace-close" );
		workspaceActions.getStyleClass().add( ACTIONS );

		// Create the action bar
		HBox actionBar = new HBox();
		actionBar.getStyleClass().add( ACTION_BAR );
		actionBar.getChildren().addAll( programActionBar, workspaceActionBar, toolbar, spring, workspaceActions );

		return actionBar;
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

	public void initializeScene( double width, double height ) {
		if( scene != null ) return;
		scene = new Scene( railPane, width, height, Color.TRANSPARENT );
		getProgram().getActionLibrary().registerScene( scene );
		setScene( scene );
		sizeToScene();
	}

	public void setTheme( String url ) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add( Xenon.STYLESHEET );
		if( url != null ) scene.getStylesheets().add( url );
	}

	private void insertDevMenu( Xenon program, List<Menu> menus ) {
		menus.add( generateDevMenu( program ) );
	}

	private void insertDevMenu( Xenon program, Menu menu ) {
		menu.getItems().add( generateDevMenu( program ) );
	}

	private void insertDevMenu( Xenon program, ContextMenu menu ) {
		menu.getItems().add( generateDevMenu( program ) );
	}

	private void insertDevMenu( Xenon program, MenuButton menu ) {
		menu.getItems().add( generateDevMenu( program ) );
	}

	private Menu generateDevMenu( Xenon program ) {
		String development = "";
		development += "development[";
		development += "restart,uireset,mock-update";
		development += "|show-updates-posted,show-updates-staged";
		development += "|test-action-1,test-action-2,test-action-3,test-action-4,test-action-5";
		development += "]";
		return MenuBarFactory.createMenu( program, development, true );
	}

	private ToolBar createProgramToolBar( Xenon program, Node toolbarToolStart, Node toolbarToolEnd ) {
		ToolBar toolbar = ToolBarFactory.createToolBar( program );
		toolbar.getItems().addFirst( toolbarToolStart );
		toolbar.getItems().addLast( toolbarToolEnd );
		return toolbar;
	}

	private Node createWorkareaMenu( Xenon program ) {
		// The menu button
		Button menuButton = ToolBarFactory.createToolBarButton( program, "menu" );
		menuButton.setId( "menu-button-menu" );

		MenuButton workareaMenu = MenuBarFactory.createMenuButton( program, "workarea", true );
		workareaMenu.getStyleClass().addAll( "workarea-menu" );
		StackPane.setAlignment( workareaMenu, Pos.CENTER_LEFT );

		// Link the active workarea property to the menu
		activeWorkareaProperty().addListener( ( p, o, n ) -> {
			if( n == null ) {
				workareaMenu.graphicProperty().unbind();
				workareaMenu.textProperty().unbind();
			} else {
				workareaMenu.graphicProperty().bind( n.iconProperty().map( i -> program.getIconLibrary().getIcon( i ) ) );
				workareaMenu.textProperty().bind( n.nameProperty() );
			}
		} );

		// Create the workarea action menu items
		MenuItem create = MenuBarFactory.createMenuBarItem( program, "workarea-new" );
		MenuItem rename = MenuBarFactory.createMenuBarItem( program, "workarea-rename" );
		MenuItem close = MenuBarFactory.createMenuBarItem( program, "workarea-close" );
		SeparatorMenuItem workareaSeparator = new SeparatorMenuItem();

		// Add the workarea action menu items
		workareaMenu.getItems().addAll( create, rename, close, workareaSeparator );

		// Update the workarea menu when the workareas change
		workareasProperty().addListener( (ListChangeListener<Workarea>)c -> {
			int startIndex = workareaMenu.getItems().indexOf( workareaSeparator );
			if( startIndex < 0 ) return;

			// Remove existing workarea menu items
			workareaMenu.getItems().remove( startIndex + 1, workareaMenu.getItems().size() );

			// Update the list of workarea menu items
			workareaMenu.getItems().addAll( c.getList().stream().map( MenuBarFactory::createWorkareaMenuItem ).toList() );
		} );

		// The menu button and workarea menu should be put in a toolbar for proper layout
		ToolBar workareaToolbar = ToolBarFactory.createToolBar( program );
		workareaToolbar.getItems().addAll( menuButton, workareaMenu );

		return workareaToolbar;
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
		memoryMonitor.setOnMouseClicked( e -> System.gc() );

		statusBar.addRightItems( memoryMonitor.getMonitorGroup() );
		statusBar.addRightItems( taskMonitor.getMonitorGroup() );
		statusBar.addRightItems( fpsMonitor.getMonitorGroup() );

		return statusBar;
	}

	@SuppressWarnings( "unused" )
	public void showProgramMenu( ActionEvent event ) {
		toggleProgramWorkspaceActions();
	}

	private void toggleProgramWorkspaceActions() {
		if( programMenuBar.isVisible() ) {
			hideProgramMenuBar();
		} else {
			showProgramMenuBar();
		}
	}

	private void showProgramMenuBar() {
		// Make sure all the menus are closed
		// This has improved the behavior of the menu bar
		programMenuBar.getMenus().forEach( Menu::hide );

		workareaMenu.setVisible( false );
		programMenuBar.setManaged( true );
		programMenuBar.setVisible( true );
	}

	private void hideProgramMenuBar() {
		// Make sure all the menus are closed
		// This line improved the behavior of the menu bar
		programMenuBar.getMenus().forEach( Menu::hide );

		programMenuBar.setManaged( false );
		programMenuBar.setVisible( false );
		workareaMenu.setVisible( true );
	}

	public void pushMenuActions( String descriptor ) {
		pullMenuActions();
		descriptor = "tool[" + descriptor + "]";
		int index = programMenuBar.getMenus().indexOf( programMenuToolEnd );
		programMenuBar.getMenus().addAll( index, MenuBarFactory.createMenus( getProgram(), descriptor, COMPACT_MENU ) );
	}

	public void pullMenuActions() {
		int index = programMenuBar.getMenus().indexOf( programMenuToolStart );
		if( index < 0 ) return;
		index++;

		MenuItem node = programMenuBar.getMenus().get( index );
		while( node != programMenuToolEnd ) {
			programMenuBar.getMenus().remove( index );
			node = programMenuBar.getMenus().get( index );
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

	public void setActive( boolean active ) {
		if( !active ) {
			getProgram().getActionLibrary().getAction( MINIMIZE ).pullAction( toggleMinimizeAction );
			getProgram().getActionLibrary().getAction( MAXIMIZE ).pullAction( toggleMaximizeAction );
		}

		this.active = active;
		getSettings().set( ACTIVE, active );

		if( active ) {
			getProgram().getActionLibrary().getAction( MAXIMIZE ).pushAction( toggleMaximizeAction );
			getProgram().getActionLibrary().getAction( MINIMIZE ).pushAction( toggleMinimizeAction );
		}
	}

	public IntegerProperty orderProperty() {
		return orderProperty;
	}

	public void setOrder( int order ) {
		orderProperty.set( order );
	}

	public int getOrder() {
		return orderProperty.get();
	}

	public ObservableList<Workarea> workareasProperty() {
		return workareas;
	}

	public Set<Workarea> getWorkareas() {
		return new HashSet<>( workareas );
	}

	public void addWorkarea( Workarea workarea ) {
		Workspace oldWorkspace = workarea.getWorkspace();
		if( oldWorkspace != null ) oldWorkspace.removeWorkarea( workarea );
		workarea.setWorkspace( this );
		workareas.add( workarea );
	}

	public void removeWorkarea( Workarea workarea ) {
		// If there is only one workarea, don't close it
		if( workareas.size() == 1 ) return;

		// Handle the situation where the workarea area is active
		if( workarea.isActive() ) setActiveWorkarea( determineNextActiveWorkarea() );

		workareas.remove( workarea );
		workarea.setWorkspace( null );
	}

	public ObjectProperty<Workarea> activeWorkareaProperty() {
		return activeWorkareaProperty;
	}

	public Workarea getActiveWorkarea() {
		if( activeWorkareaProperty.isNull().get() && workareas.size() == 1 ) setActiveWorkarea( workareas.getFirst() );
		return activeWorkareaProperty.get();
	}

	public void setActiveWorkarea( Workarea workarea ) {
		if( activeWorkareaProperty.get() == workarea ) return;

		// Get open workspace behavior setting
		String openWorkspaceIn = getProgram().getSettings().get( "workspace-open-in", "current" );

		switch( openWorkspaceIn ) {
			case "current" -> setActiveWorkareaInCurrentWorkspace( workarea );
			case "new" -> setActiveWorkareaInNewWorkspace( workarea );
			default -> askUserWhatToDoWithActiveWorkarea( workarea );
		}
	}

	private void setActiveWorkareaInCurrentWorkspace( Workarea workarea ) {
		// Disconnect the old active workarea
		Workarea activeWorkarea = activeWorkareaProperty.get();
		if( activeWorkarea != null ) {
			activeWorkarea.setActive( false );
			workpaneContainer.getChildren().remove( activeWorkarea );
		}

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkarea( workarea );

		// Set the new active workarea
		Workarea priorWorkarea = activeWorkarea;
		activeWorkareaProperty.set( workarea );

		// Connect the new active workarea
		activeWorkarea = getActiveWorkarea();
		if( activeWorkarea != null ) {
			workpaneContainer.getChildren().add( activeWorkarea );
			activeWorkarea.setActive( true );
			Tool activeTool = activeWorkarea.getActiveTool();
			if( activeTool != null ) getProgram().getAssetManager().setCurrentAsset( activeTool.getAsset() );
		}

		// Send a program event when active area changes
		getEventBus().dispatch( new WorkareaSwitchedEvent( this, WorkareaSwitchedEvent.SWITCHED, this, priorWorkarea, activeWorkarea ) );
	}

	private void setActiveWorkareaInNewWorkspace( Workarea workarea ) {
		// TODO Just open a new workspace, set the active workarea and return
	}

	private void askUserWhatToDoWithActiveWorkarea( Workarea workarea ) {
		// TODO Ask the user what to do and then do that
	}

	public Pane getNoticePane() {
		return noticeBox;
	}

	public void showNotice( final Notice notice ) {
		if( Objects.equals( notice.getBalloonStickiness(), Notice.Balloon.NEVER ) ) return;

		NoticePane pane = new NoticePane( program, notice, true );
		noticeBox.getChildren().removeIf( node -> Objects.equals( ((NoticePane)node).getNotice().getId(), notice.getId() ) );
		noticeBox.getChildren().addFirst( pane );

		pane.setOnMouseClicked( event -> {
			getProgram().getNoticeManager().readNotice( notice );
			noticeBox.getChildren().remove( pane );
			if( noticeBox.getChildren().isEmpty() ) noticeBox.setVisible( false );
			pane.executeNoticeAction();
			event.consume();
		} );

		pane.getCloseButton().setOnMouseClicked( e -> {
			getProgram().getNoticeManager().readNotice( notice );
			noticeBox.getChildren().remove( pane );
			if( noticeBox.getChildren().isEmpty() ) noticeBox.setVisible( false );
			e.consume();
		} );

		int balloonTimeout = getProgram().getSettings().get( "notice-balloon-timeout", Integer.class, 5000 );

		if( Objects.equals( notice.getBalloonStickiness(), Notice.Balloon.NORMAL ) ) {
			TimerUtil.fxTask(
				() -> {
					noticeBox.getChildren().remove( pane );
					if( noticeBox.getChildren().isEmpty() ) noticeBox.setVisible( false );
					getEventBus().dispatch( new NoticeEvent( this, NoticeEvent.REMOVED, this, notice ) );
				}, balloonTimeout
			);
		}

		noticeBox.setVisible( true );

		getEventBus().dispatch( new NoticeEvent( this, NoticeEvent.ADDED, this, notice ) );
	}

	public void hideNotices() {
		noticeBox.getChildren().clear();
		noticeBox.setVisible( false );
	}

	@Override
	public String getUid() {
		return getProperties().get( Identity.KEY ).toString();
	}

	@Override
	public void setUid( String id ) {
		getProperties().put( Identity.KEY, id );
	}

	/**
	 * Convenience method to get the workspace settings.
	 *
	 * @return The workspace settings
	 */
	private Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.WORKSPACE, getUid() );
	}

	public void applySettings() {
		updateBackgroundFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateMemoryMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateTaskMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		updateFpsMonitorFromSettings( getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
	}

	// TODO Remove in 1.8
	@Deprecated
	public void updateFromSettings( Settings settings ) {
		// Due to differences in how FX handles stage sizes (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		Double w = settings.get( "w", Double.class, UiWorkspaceFactory.DEFAULT_WIDTH );
		Double h = settings.get( "h", Double.class, UiWorkspaceFactory.DEFAULT_HEIGHT );
		initializeScene( w, h );

		// Position the stage if x and y are specified
		// If not specified the stage is centered on the screen
		Double x = settings.get( "x", Double.class, null );
		Double y = settings.get( "y", Double.class, null );
		if( x != null ) setX( x );
		if( y != null ) setY( y );

		setActive( settings.get( "active", Boolean.class, false ) );
		setMaximized( settings.get( "maximized", Boolean.class, false ) );

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
		updateThemeFromSettings( settings );
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

	private String generateStageTitle( String name ) {
		return name + " - " + getProgram().getCard().getName();
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
	}

	// TODO Remove in 1.8
	@Deprecated
	private void updateThemeFromSettings( Settings settings ) {
		String themeId = settings.get( "theme", getProgram().getWorkspaceManager().getThemeId() );
		setTheme( getProgram().getThemeManager().getMetadata( themeId ).getUrl() );
	}

	@Deprecated
	private void updateBackgroundFromSettings( Settings settings ) {
		// FIXME Arguably we should not need to unregister and re-register the settings listener
		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, backgroundSettingsHandler );
			background.updateFromSettings( settings );
			settings.register( SettingsEvent.CHANGED, backgroundSettingsHandler );
		} );
	}

	@Deprecated
	private void updateMemoryMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-memory-monitor-enabled", Boolean.class, Boolean.TRUE );
		Boolean showText = settings.get( "workspace-memory-monitor-text", Boolean.class, Boolean.TRUE );
		Boolean showPercent = settings.get( "workspace-memory-monitor-percent", Boolean.class, Boolean.TRUE );

		// FIXME Arguably we should not need to unregister and re-register the settings listener
		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
			updateContainer( memoryMonitor, enabled );
			memoryMonitor.setTextVisible( showText );
			memoryMonitor.setShowPercent( showPercent );
			settings.register( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
		} );
	}

	@Deprecated
	private void updateTaskMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-task-monitor-enabled", Boolean.class, Boolean.TRUE );
		Boolean showText = settings.get( "workspace-task-monitor-text", Boolean.class, Boolean.TRUE );
		Boolean showPercent = settings.get( "workspace-task-monitor-percent", Boolean.class, Boolean.TRUE );
		// FIXME Arguably we should not need to unregister and re-register the settings listener
		Fx.run( () -> {
			settings.unregister( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
			updateContainer( taskMonitor, enabled );
			taskMonitor.setTextVisible( showText );
			taskMonitor.setShowPercent( showPercent );
			settings.register( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
		} );
	}

	@Deprecated
	private void updateFpsMonitorFromSettings( Settings settings ) {
		Boolean enabled = settings.get( "workspace-fps-monitor-enabled", Boolean.class, Boolean.TRUE );
		// FIXME Arguably we should not need to unregister and re-register the settings listener
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

	private static boolean allMenusAreHidden( MenuBar menuBar ) {
		return !isAnyMenuShowing( menuBar );
	}

	private static boolean isAnyMenuShowing( MenuBar menuBar ) {
		return menuBar.getMenus().stream().anyMatch( Menu::isShowing );
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

}
