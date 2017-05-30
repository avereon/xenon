package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Actions;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.UiFactory;
import com.parallelsymmetry.essence.action.CloseWorkareaHandler;
import com.parallelsymmetry.essence.action.NewWorkareaHandler;
import com.parallelsymmetry.essence.action.RenameWorkareaHandler;
import com.parallelsymmetry.essence.event.WorkareaChangedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.commons.configuration2.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace {

	private Program program;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private BorderPane layout;

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private Pane workpaneContainer;

	private ComboBox<Workarea> workareaSelector;

	private ObservableList<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Configuration configuration;

	private String id;

	public Workspace( Program program ) {
		this.program = program;

		workareas = FXCollections.observableArrayList();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();

		// FIXME Should this default setup be defined in config files or something else?

		// MENUBAR
		menubar = new MenuBar();

		Menu file = Actions.createMenu( program, "file" );
		file.getItems().add( Actions.createMenuItem( program, "new" ) );
		file.getItems().add( Actions.createMenuItem( program, "open" ) );
		file.getItems().add( Actions.createMenuItem( program, "save" ) );
		file.getItems().add( Actions.createMenuItem( program, "save-as" ) );
		file.getItems().add( Actions.createMenuItem( program, "copy-as" ) );
		file.getItems().add( Actions.createMenuItem( program, "close" ) );
		file.getItems().add( new SeparatorMenuItem() );
		file.getItems().add( Actions.createMenuItem( program, "exit" ) );

		Menu edit = Actions.createMenu( program, "edit" );
		edit.getItems().add( Actions.createMenuItem( program, "undo" ) );
		edit.getItems().add( Actions.createMenuItem( program, "redo" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( Actions.createMenuItem( program, "cut" ) );
		edit.getItems().add( Actions.createMenuItem( program, "copy" ) );
		edit.getItems().add( Actions.createMenuItem( program, "paste" ) );
		edit.getItems().add( Actions.createMenuItem( program, "delete" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( Actions.createMenuItem( program, "indent" ) );
		edit.getItems().add( Actions.createMenuItem( program, "unindent" ) );
		edit.getItems().add( new SeparatorMenuItem() );
		edit.getItems().add( Actions.createMenuItem( program, "settings" ) );

		Menu view = Actions.createMenu( program, "view" );
		view.getItems().add( Actions.createMenuItem( program, "workspace-new" ) );
		view.getItems().add( new SeparatorMenuItem() );
		view.getItems().add( Actions.createMenuItem( program, "statusbar-show" ) );

		Menu help = Actions.createMenu( program, "help" );
		help.getItems().add( Actions.createMenuItem( program, "welcome" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( Actions.createMenuItem( program, "help-content" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( Actions.createMenuItem( program, "update" ) );
		help.getItems().add( Actions.createMenuItem( program, "about" ) );

		menubar.getMenus().addAll( file, edit, view, help );

		// TOOLBAR

		Menu workareaMenu = Actions.createMenu( program, "workarea" );
		workareaMenu.getItems().add( Actions.createMenuItem( program, "workarea-new" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( Actions.createMenuItem( program, "workarea-rename" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( Actions.createMenuItem( program, "workarea-close" ) );

		MenuBar workareaMenuBar = new MenuBar();
		workareaMenuBar.getMenus().add( workareaMenu );
		workareaMenuBar.setBackground( Background.EMPTY );
		workareaMenuBar.setPadding( Insets.EMPTY );
		workareaMenuBar.setBorder( Border.EMPTY );

		// Set the workarea actions
		program.getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaHandler( program ) );
		program.getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaHandler( program ) );
		program.getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaHandler( program ) );

		// Workarea selector
		workareaSelector = new ComboBox<>();
		workareaSelector.setItems( workareas );
		workareaSelector.setButtonCell( new WorkareaPropertyCell() );
		workareaSelector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );

		toolbar = new ToolBar();
		toolbar.getItems().add( Actions.createToolBarButton( program, "new" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "open" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "save" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( Actions.createToolBarButton( program, "undo" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "redo" ) );
		//		toolbar.getItems().add( new Separator() );
		//		toolbar.getItems().add( Actions.createToolBarButton( program, "cut" ) );
		//		toolbar.getItems().add( Actions.createToolBarButton( program, "copy" ) );
		//		toolbar.getItems().add( Actions.createToolBarButton( program, "paste" ) );

		toolbar.getItems().add( Actions.createSpring() );

		toolbar.getItems().add( workareaMenuBar );
		toolbar.getItems().add( workareaSelector );

		// Workarea Container
		workpaneContainer = new StackPane();

		// TODO Remove the development background image
		// The following background image is for development purposes.
		Image image = new Image( getClass().getResourceAsStream( "/purple-sunset-wallpaper@2x.jpg" ) );
		BackgroundSize backgroundSize = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true );
		workpaneContainer.setBackground( new Background( new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize ) ) );


		VBox pane = new VBox();
		pane.getChildren().addAll( menubar, toolbar );

		layout = new BorderPane();
		layout.setTop( pane );
		layout.setCenter( workpaneContainer );

		scene = new Scene( layout, UiFactory.DEFAULT_WIDTH, UiFactory.DEFAULT_HEIGHT );
		scene.getStylesheets().add( Program.STYLESHEET );

		// Create the scene
		stage = new Stage();
		stage.setScene( scene );
		stage.getIcons().addAll( program.getIconLibrary().getIconImages( "program" ) );
		stage.setOnCloseRequest( event -> {
			program.getWorkspaceManager().requestCloseWorkspace( this );
			event.consume();
		} );
	}

	public String getId() {
		return id;
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
		configuration.setProperty( "active", active );
	}

	public Set<Workarea> getWorkareas() {
		return new HashSet<Workarea>( workareas );
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

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkarea( workarea );

		// Disconnect the old active workarea area
		if( activeWorkarea != null ) {
			activeWorkarea.removePropertyChangeListener( activeWorkareaWatcher );
			activeWorkarea.setActive( false );
			workpaneContainer.getChildren().remove( activeWorkarea.getWorkpane() );
		}

		// Swap the workarea area on the stage
		activeWorkarea = workarea;

		// Connect the new active workarea area
		if( activeWorkarea != null ) {
			workpaneContainer.getChildren().add( activeWorkarea.getWorkpane() );
			activeWorkarea.setActive( true );
			setStageTitle( activeWorkarea.getName() );
			workareaSelector.getSelectionModel().select( activeWorkarea );
			activeWorkarea.addPropertyChangeListener( activeWorkareaWatcher );

			// TODO Set the menu bar
			// TODO Set the tool bar
			// TODO Set the workpane
		}

		// Send a program event when active area changes
		program.fireEvent( new WorkareaChangedEvent( this, activeWorkarea ) );
	}

	public void setConfiguration( Configuration configuration ) {
		if( this.configuration != null ) return;

		this.configuration = configuration;
		id = configuration.getString( "id" );

		Double x = configuration.getDouble( "x", null );
		Double y = configuration.getDouble( "y", null );
		Double w = configuration.getDouble( "w" );
		Double h = configuration.getDouble( "h" );

		stage.setWidth( w );
		stage.setHeight( h );
		// Position the stage if x and y are specified
		if( x != null ) stage.setX( x );
		if( y != null ) stage.setY( y );

		stage.setMaximized( configuration.getBoolean( "maximized", false ) );
		setActive( configuration.getBoolean( "active", false ) );

		// Add the property listeners
		stage.maximizedProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( stage.isShowing() ) configuration.setProperty( "maximized", newValue );
		} );
		stage.xProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "x", newValue );
		} );
		stage.yProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "y", newValue );
		} );
		scene.widthProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "w", newValue );
		} );
		scene.heightProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			if( !stage.isMaximized() ) configuration.setProperty( "h", newValue );
		} );
	}

	private void setStageTitle( String name ) {
		stage.setTitle( name + " - " + program.getMetadata().getName() );
	}

	private Workarea determineNextActiveWorkarea() {
		int index = workareas.indexOf( getActiveWorkarea() );
		return workareas.get( index == 0 ? 1 : index - 1 );
	}

	private class WorkareaPropertyWatcher implements PropertyChangeListener {

		@Override
		public void propertyChange( PropertyChangeEvent event ) {
			switch( event.getPropertyName() ) {
				case "name": {
					//workareaSelector.setValue( getActiveWorkarea() );
					setStageTitle( event.getNewValue().toString() );
					break;
				}
			}
		}

	}

	public static class WorkareaPropertyCell extends ListCell<Workarea> {

		@Override
		protected void updateItem( Workarea item, boolean empty ) {
			super.updateItem( item, empty );
			textProperty().unbind();
			if( item != null && !empty ) textProperty().bind( item.getNameValue() );
		}

	}

}
