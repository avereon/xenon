package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.Actions;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.action.CloseWorkareaHandler;
import com.parallelsymmetry.essence.action.NewWorkareaHandler;
import com.parallelsymmetry.essence.action.RenameWorkareaHandler;
import com.parallelsymmetry.essence.event.WorkareaChangedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

	private Pane menubarContainer;

	private HBox toolbarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private ComboBox<Workarea> workareaSelector;

	private ObservableList<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Configuration configuration;

	private String id;

	private Workarea event;

	public Workspace( Program program ) {
		this.program = program;

		workareas = FXCollections.observableArrayList();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();

		stage = new Stage();
		stage.getIcons().addAll( program.getIconLibrary().getIconImages( "program" ) );

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
		//help.getItems().add( Actions.createMenuItem( program, "welcome" ) );
		help.getItems().add( new SeparatorMenuItem() );
		help.getItems().add( Actions.createMenuItem( program, "help-content" ) );
		help.getItems().add( new SeparatorMenuItem() );
		//help.getItems().add( Actions.createMenuItem( program, "update" ) );
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

		// Set the workarea actions
		program.getActionLibrary().getAction( "workarea-new" ).pushAction( new NewWorkareaHandler( program ) );
		program.getActionLibrary().getAction( "workarea-rename" ).pushAction( new RenameWorkareaHandler( program ) );
		program.getActionLibrary().getAction( "workarea-close" ).pushAction( new CloseWorkareaHandler( program ) );

		// Workarea selector
		workareaSelector = new ComboBox<>();
		workareaSelector.setItems( workareas );
		workareaSelector.valueProperty().addListener( ( value, oldValue, newValue ) -> setActiveWorkarea( newValue ) );

		toolbar = new ToolBar();
		toolbar.getItems().add( Actions.createToolBarButton( program, "new" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "open" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "save" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( Actions.createToolBarButton( program, "undo" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "redo" ) );
		toolbar.getItems().add( new Separator() );
		toolbar.getItems().add( Actions.createToolBarButton( program, "cut" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "copy" ) );
		toolbar.getItems().add( Actions.createToolBarButton( program, "paste" ) );

		toolbar.getItems().add( Actions.createSpring() );

		toolbar.getItems().add( workareaMenuBar );
		toolbar.getItems().add( workareaSelector );
	}

	private void selectWorkarea( ActionEvent event ) {

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

	public void addWorkArea( Workarea workarea ) {
		Workspace oldWorkspace = workarea.getWorkspace();
		if( oldWorkspace != null ) oldWorkspace.removeWorkArea( workarea );
		workareas.add( workarea );
		workarea.setWorkspace( this );
	}

	public void removeWorkArea( Workarea workarea ) {
		// Handle the situation where the work area is active

		workareas.remove( workarea );
		workarea.setWorkspace( null );

		// If needed set the active work area
	}

	public Workarea getActiveWorkarea() {
		return activeWorkarea;
	}

	public void setActiveWorkarea( Workarea workarea ) {
		if( activeWorkarea == workarea ) return;

		// If the workarea is not already added, add it
		if( !workareas.contains( workarea ) ) addWorkArea( workarea );

		// Disconnect the old active work area
		if( activeWorkarea != null ) {
			activeWorkarea.removePropertyChangeListener( activeWorkareaWatcher );
			activeWorkarea.setActive( false );
		}

		// Swap the work area on the stage
		activeWorkarea = workarea;

		// Connect the new active work area
		if( activeWorkarea != null ) {
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

		VBox pane = new VBox();
		pane.getChildren().addAll( menubar, toolbar );

		// Create the scene using the width and height
		stage.setScene( scene = new Scene( pane, w, h ) );
		stage.centerOnScreen();

		// Position the stage if x and y are specified
		if( x != null ) stage.setX( x );
		if( y != null ) stage.setY( y );

		stage.setMaximized( configuration.getBoolean( "maximized", false ) );
		setActive( configuration.getBoolean( "active", false ) );

		// Add the property listeners
		stage.maximizedProperty().addListener( ( observableValue, oldValue, newValue ) -> {
			configuration.setProperty( "maximized", newValue );
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

	private class WorkareaPropertyWatcher implements PropertyChangeListener {

		@Override
		public void propertyChange( PropertyChangeEvent event ) {
			switch( event.getPropertyName() ) {
				case "name": {
					setStageTitle( event.getNewValue().toString() );
					break;
				}
			}
		}

	}

}
