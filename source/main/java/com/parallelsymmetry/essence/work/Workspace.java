package com.parallelsymmetry.essence.work;

import com.parallelsymmetry.essence.product.Product;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.configuration2.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The workspace manages the menu bar, tool bar and workareas.
 */
public class Workspace {

	private Product product;

	private Stage stage;

	private Scene scene;

	private boolean active;

	private Pane menubarContainer;

	private MenuBar menubar;

	private ToolBar toolbar;

	private Set<Workarea> workareas;

	private Workarea activeWorkarea;

	private WorkareaPropertyWatcher activeWorkareaWatcher;

	private Configuration configuration;

	private String id;

	public Workspace( Product product ) {
		this.product = product;

		workareas = new CopyOnWriteArraySet<>();
		activeWorkareaWatcher = new WorkareaPropertyWatcher();

		stage = new Stage();

		// MENUBAR

		menubar = new MenuBar();

		Menu file = new Menu( "File" );
		file.getItems().add( new MenuItem( "A" ) );
		file.getItems().add( new MenuItem( "B" ) );
		file.getItems().add( new MenuItem( "C" ) );
		file.getItems().add( new MenuItem( "D" ) );
		Menu edit = new Menu( "Edit" );
		edit.getItems().add( new MenuItem( "A" ) );
		edit.getItems().add( new MenuItem( "B" ) );
		edit.getItems().add( new MenuItem( "C" ) );
		edit.getItems().add( new MenuItem( "D" ) );
		Menu view = new Menu( "View" );
		view.getItems().add( new MenuItem( "A" ) );
		view.getItems().add( new MenuItem( "B" ) );
		view.getItems().add( new MenuItem( "C" ) );
		view.getItems().add( new MenuItem( "D" ) );
		Menu help = new Menu( "Help" );
		help.getItems().add( new MenuItem( "A" ) );
		help.getItems().add( new MenuItem( "B" ) );
		help.getItems().add( new MenuItem( "C" ) );

		Menu spacer = new Menu( "" );
		spacer.setDisable( true );
		spacer.setStyle( "-fx-padding: 0 0 0 200" );

		Menu workareaMenu = new Menu( "Workarea" );
		workareaMenu.getItems().add( new MenuItem( "New" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( new MenuItem( "Rename" ) );
		workareaMenu.getItems().add( new SeparatorMenuItem() );
		workareaMenu.getItems().add( new MenuItem( "Close" ) );
		//view.getItems().add( 0, workareaMenu );

		ComboBox<String> workareaSelector = new ComboBox<>();
		workareaSelector.getItems().add( "Default" );
		workareaSelector.getSelectionModel().select( 0 );

		//		MenuBar workareaMenubar = new MenuBar();
		//		workareaMenubar.getMenus().add( workareaMenu );

		//		Pane workareaSelectorContainer = new Pane();
		//		workareaSelectorContainer.getStyleClass().add( "menu-bar" );
		//		workareaSelectorContainer.getChildren().addAll( workareaSelector );
		SplitMenuButton splitbutton = new SplitMenuButton();
		splitbutton.setText( "Default" );

		menubarContainer = new HBox();
		menubarContainer.getChildren().addAll( menubar );
		HBox.setHgrow( menubar, Priority.SOMETIMES );

		menubar.getMenus().addAll( file, edit, view, help, workareaMenu );

		// TOOLBAR

		toolbar = new ToolBar();
		toolbar.getItems().addAll( workareaSelector, splitbutton );
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
			activeWorkarea.addPropertyChangeListener( activeWorkareaWatcher );

			setStageTitle( activeWorkarea.getName() );

			// TODO Set the menu bar
			// TODO Set the tool bar
			// TODO Set the workpane
		}
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
		pane.getChildren().addAll( menubarContainer, toolbar );

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
		stage.setTitle( name + " - " + product.getMetadata().getName() );
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
