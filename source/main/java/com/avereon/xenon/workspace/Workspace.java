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
import com.avereon.zarra.color.Colors;
import com.avereon.zarra.event.FxEventHub;
import com.avereon.zarra.javafx.Fx;
import com.avereon.zarra.javafx.FxUtil;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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

	public static final String ACTION_BAR = "action-bar";

	private static final boolean TRANSPARENT_WINDOW_SUPPORTED = Platform.isSupported( ConditionalFeature.TRANSPARENT_WINDOW );

	public static final String NORMALIZE = "normalize";

	public static final String MAXIMIZE = "maximize";

	public static final String NOTICE = "notice";

	@Getter
	private final Xenon program;

	private Scene scene;

	@Getter
	private boolean active;

	@Getter
	private final FxEventHub eventBus;

	private final BorderPane workspaceLayout;

	private final Pane railPane;

	private final Set<Pane> rails;

	private final Pane workspaceSelectionContainer;

	private final MenuBar programMenuBar;

	private final Node workareaMenu;

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

	@Getter
	private final StatusBar statusBar;

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

	private MemoryMonitor memoryMonitor;

	private TaskMonitor taskMonitor;

	private FpsMonitor fpsMonitor;

	public Workspace( final Xenon program, final String id ) {
		super( TRANSPARENT_WINDOW_SUPPORTED ? StageStyle.TRANSPARENT : StageStyle.UNDECORATED );
		if( !TRANSPARENT_WINDOW_SUPPORTED ) log.atWarn().log( "Transparent windows not supported" );

		this.program = program;
		this.eventBus = new FxEventHub();
		this.eventBus.parent( program.getFxEventHub() );

		getIcons().addAll( program.getIconLibrary().getStageIcons( "program" ) );
		setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
		} );
		focusedProperty().addListener( ( p, o, n ) -> {
			if( Boolean.TRUE.equals( n ) ) program.getWorkspaceManager().setActiveWorkspace( this );
		} );

		setUid( id );

		activeWorkareaProperty = new SimpleObjectProperty<>();
		workareas = FXCollections.observableArrayList();
		backgroundSettingsHandler = new BackgroundSettingsHandler();
		memoryMonitorSettingsHandler = new MemoryMonitorSettingsHandler();
		taskMonitorSettingsHandler = new TaskMonitorSettingsHandler();
		fpsMonitorSettingsHandler = new FpsMonitorSettingsHandler();

		toggleMinimizeAction = new ToggleMinimizeAction( program, this );
		toggleMaximizeAction = new ToggleMaximizeAction( program, this );

		programMenuBar = createProgramMenuBar( program );
		verticalProgramMenu = createProgramContextMenu( program );
		programMenuToolStart = FxUtil.findMenuItemById( verticalProgramMenu.getItems(), MenuFactory.MENU_ID_PREFIX + EDIT_ACTION );
		programMenuToolEnd = FxUtil.findMenuItemById( verticalProgramMenu.getItems(), MenuFactory.MENU_ID_PREFIX + VIEW_ACTION );

		workareaMenu = createWorkareaMenu( program );
		workspaceSelectionContainer = new HBox();
		Pane actionBar = createActionBar( program );

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

		Pane workspaceStack = new StackPane( workpaneContainer, noticePane );

		// Create the workspace layout pane
		workspaceLayout = new BorderPane();
		workspaceLayout.getStyleClass().add( "workspace" );
		workspaceLayout.getProperties().put( WORKSPACE_PROPERTY_KEY, this );

		// Set the workspace layout components
		workspaceLayout.setTop( actionBar );
		workspaceLayout.setCenter( workspaceStack );
		workspaceLayout.setBottom( statusPane );

		rails = new HashSet<>();
		railPane = buildRailPane( workspaceLayout );

		// Bind the stage title property to the active workarea name
		activeWorkareaProperty.addListener( ( p, o, n ) -> {
			if( n == null ) {
				titleProperty().unbind();
				actionBar.backgroundProperty().unbind();
			} else {
				titleProperty().bind( n.nameProperty().map( this::calcStageTitle ) );
				actionBar.backgroundProperty().bind( n.colorProperty().map( c -> {
					Color mix = Colors.mix( c, Color.TRANSPARENT, 0.6 );
					LinearGradient gradient = new LinearGradient( 0, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop( 0, mix ), new Stop( 1, Color.TRANSPARENT ) );
					return Background.fill( gradient );
				} ) );
			}
		} );

		maximizedProperty().addListener( ( p, o, n ) -> {
			// Toggle the maximize/normalize icon
			String icon = Boolean.TRUE.equals( n ) ? NORMALIZE : MAXIMIZE;
			getProgram().getActionLibrary().getAction( MAXIMIZE ).setIcon( icon );

			// Toggle the rails
			rails.forEach( r -> r.setVisible( !n ) );
		} );

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

	private Pane createActionBar( Xenon program ) {
		workspaceSelectionContainer.getChildren().setAll( workareaMenu );

		// The left toolbar options
		ToolBar leftToolBar = ToolBarFactory.createToolBar( program );
		//leftToolBar.getItems().add( createProgramMenuButton( program ) );
		leftToolBar.getItems().add( ToolBarFactory.createToolBarButton( program, "menu" ));
		leftToolBar.getItems().add( workspaceSelectionContainer );

		// The stage mover
		Pane stageMover = new Pane();
		stageMover.getStyleClass().add( "stage-mover" );
		HBox.setHgrow( stageMover, Priority.ALWAYS );
		new StageMover( stageMover );

		// The right toolbar options
		ToolBar rightToolBar = ToolBarFactory.createToolBar( program, "notice|minimize,maximize,workspace-close" );

		// The boxes
		HBox leftBox = new HBox( leftToolBar );
		HBox centerBox = new HBox( stageMover );
		HBox rightBox = new HBox( rightToolBar );

		// The tool pane
		Pane actionBar = new BorderPane( centerBox, null, rightBox, null, leftBox );
		actionBar.getStyleClass().add( ACTION_BAR );

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

	public void setTheme( String url ) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add( Xenon.STYLESHEET );
		if( url != null ) scene.getStylesheets().add( url );
	}

	private MenuBar createProgramMenuBar( Xenon program ) {
		// Load the menu descriptors
		String defaultDescriptor = program.getSettings().get( "workspace-menu" );
		String customDescriptor = getSettings().get( "menubar", defaultDescriptor );

		// Build the program menu
		List<Menu> menus = MenuFactory.createMenus( program, customDescriptor, false );

		// Add the dev menu if using the dev profile
		if( Profile.DEV.equals( program.getProfile() ) ) insertDevMenu( program, menus );

		return new MenuBar( menus.toArray( new Menu[ 0 ] ) );
	}

	private ContextMenu createProgramContextMenu( Xenon program ) {
		// Load the menu descriptors
		String defaultDescriptor = program.getSettings().get( "workspace-menu" );
		String customDescriptor = getSettings().get( "menubar", defaultDescriptor );

		// Build the program menu
		ContextMenu menu = MenuFactory.createContextMenu( program, customDescriptor, COMPACT_MENU );

		// Add the dev menu if using the dev profile
		if( Profile.DEV.equals( program.getProfile() ) ) insertDevMenu( program, menu );

		return menu;
	}

	private MenuButton createProgramMenuButton( Xenon program ) {
		// Load the menu descriptors
		String defaultDescriptor = program.getSettings().get( "workspace-menu" );
		String customDescriptor = getSettings().get( "menubar", defaultDescriptor );

		// Build the program menu
		MenuButton menu = MenuFactory.createMenuButton( program, "menu[" + customDescriptor + "]", COMPACT_MENU, false );

		// Add the dev menu if using the dev profile
		if( Profile.DEV.equals( program.getProfile() ) ) insertDevMenu( program, menu );

		return menu;
	}

	private void insertDevMenu( Xenon program, List<Menu> menus ) {
		int index = menus.stream().filter( item -> (MenuFactory.MENU_ID_PREFIX + "maintenance").equals( item.getId() ) ).mapToInt( menus::indexOf ).findFirst().orElse( -1 );
		if( index >= 0 ) menus.add( index, generateDevMenu( program ) );
	}

	private void insertDevMenu( Xenon program, ContextMenu menu ) {
		menu.getItems().add( generateDevMenu( program ) );
	}

	private void insertDevMenu( Xenon program, MenuButton menu ) {
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

	private Button createNoticeToolbarButton() {
		Button noticeButton = ToolBarFactory.createToolBarButton( program, NOTICE );
		noticeButton.setContentDisplay( ContentDisplay.RIGHT );
		program.getNoticeManager().unreadCountProperty().addListener( ( event, oldValue, newValue ) -> {
			int count = newValue.intValue();
			String icon = count == 0 ? NOTICE : program.getNoticeManager().getUnreadNoticeType().getUnreadIcon();
			Fx.run( () -> program.getActionLibrary().getAction( NOTICE ).setIcon( icon ) );
		} );
		return noticeButton;
	}

	private Node createWorkareaMenu( Xenon program ) {
		MenuButton menu = MenuFactory.createMenuButton( program, "workarea", true );
		menu.getStyleClass().addAll( "workarea-menu" );

		// Link the active workarea property to the menu
		activeWorkareaProperty().addListener( ( p, o, n ) -> {
			if( n == null ) {
				menu.graphicProperty().unbind();
				menu.textProperty().unbind();
			} else {
				menu.graphicProperty().bind( n.iconProperty().map( i -> program.getIconLibrary().getIcon( i ) ) );
				menu.textProperty().bind( n.nameProperty() );
			}
		} );

		// Create the workarea action menu items
		MenuItem create = MenuFactory.createMenuItem( program, "workarea-new" );
		MenuItem rename = MenuFactory.createMenuItem( program, "workarea-rename" );
		MenuItem close = MenuFactory.createMenuItem( program, "workarea-close" );
		SeparatorMenuItem workareaSeparator = new SeparatorMenuItem();

		// Add the workarea action menu items
		menu.getItems().addAll( create, rename, close, workareaSeparator );

		// Update the workarea menu when the workareas change
		workareasProperty().addListener( (ListChangeListener<Workarea>)c -> {
			int startIndex = menu.getItems().indexOf( workareaSeparator );
			if( startIndex < 0 ) return;

			// Remove existing workarea menu items
			menu.getItems().remove( startIndex + 1, menu.getItems().size() );

			// Update the list of workarea menu items
			menu.getItems().addAll( c.getList().stream().map( this::createWorkareaMenuItem ).toList() );
		} );

		return menu;
	}

	private MenuItem createWorkareaMenuItem( Workarea workarea ) {
		MenuItem item = new MenuItem();
		item.textProperty().bind( workarea.nameProperty() );
		item.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i ) ) );
		item.getStyleClass().addAll( "workarea-menu-item" );
		item.setOnAction( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
		return item;
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

	public void showProgramMenu( ActionEvent event ) {
		// Option one
		//verticalProgramMenu.show( (Node)event.getSource(), Side.BOTTOM, 0, 0 );

		// Option two
		toggleProgramWorkspace();
	}

	private void toggleProgramWorkspace() {
		if( workspaceSelectionContainer.getChildren().get( 0 ) == workareaMenu ) {
			workspaceSelectionContainer.getChildren().setAll( programMenuBar );
		} else {
			workspaceSelectionContainer.getChildren().setAll( workareaMenu );
		}
	}

	public void pushMenuActions( String descriptor ) {
		pullMenuActions();
		descriptor = "tool[" + descriptor + "]";
		int index = verticalProgramMenu.getItems().indexOf( programMenuToolEnd );
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

	public void setActive( boolean active ) {
		if( !active ) {
			getProgram().getActionLibrary().getAction( "minimize" ).pullAction( toggleMinimizeAction );
			getProgram().getActionLibrary().getAction( MAXIMIZE ).pullAction( toggleMaximizeAction );
		}

		this.active = active;

		if( active ) {
			getProgram().getActionLibrary().getAction( MAXIMIZE ).pushAction( toggleMaximizeAction );
			getProgram().getActionLibrary().getAction( "minimize" ).pushAction( toggleMinimizeAction );
		}

		getSettings().set( "active", active );
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
		if( activeWorkareaProperty.isNull().get() && workareas.size() == 1 ) setActiveWorkarea( workareas.get( 0 ) );
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
			workpaneContainer.getChildren().remove( activeWorkarea.getWorkpane() );
			activeWorkarea.getWorkpane().setVisible( false );
		}

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkarea( workarea );
		// Set the new active workarea
		Workarea priorWorkarea = activeWorkarea;
		activeWorkareaProperty.set( workarea );

		// Connect the new active workarea
		activeWorkarea = getActiveWorkarea();
		if( activeWorkarea != null ) {
			workpaneContainer.getChildren().add( activeWorkarea.getWorkpane() );
			activeWorkarea.getWorkpane().setVisible( true );
			activeWorkarea.setActive( true );
			Tool activeTool = activeWorkarea.getWorkpane().getActiveTool();
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
		noticeBox.getChildren().add( 0, pane );

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
		scene = new Scene( railPane, w, h, Color.TRANSPARENT );
		getProgram().getActionLibrary().registerScene( scene );

		// Set up the stage
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

	private String calcStageTitle( String name ) {
		return name + " - " + getProgram().getCard().getName();
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

}
