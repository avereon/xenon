package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.ProgramActionHandler;
import com.parallelsymmetry.essence.UiFactory;
import com.parallelsymmetry.essence.work.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class NewWorkareaHandler extends ProgramActionHandler<ActionEvent> {

	private static Logger log = LoggerFactory.getLogger( NewWorkareaHandler.class );

	public NewWorkareaHandler( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.initOwner( program.getWorkspaceManager().getActiveWorkspace().getStage() );
		dialog.setTitle( "New Workarea" );
		dialog.setHeaderText( "Let's create a new workarea" );
		dialog.setContentText( "Workarea name:" );

		Optional<String> result = dialog.showAndWait();
		result.ifPresent( this::createNewWorkarea );
	}

	private void createNewWorkarea( String name ) {
		UiFactory uiFactory = new UiFactory( program );
		try {
			Workarea workarea = uiFactory.newWorkarea();
			workarea.setName( name );
			program.getWorkspaceManager().getActiveWorkspace().setActiveWorkarea( workarea );
		} catch( Exception exception ) {
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
