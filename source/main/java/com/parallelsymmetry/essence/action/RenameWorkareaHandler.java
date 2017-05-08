package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.ProgramActionHandler;
import com.parallelsymmetry.essence.UiFactory;
import com.parallelsymmetry.essence.work.Workarea;
import javafx.event.Event;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RenameWorkareaHandler extends ProgramActionHandler {

	private static Logger log = LoggerFactory.getLogger( RenameWorkareaHandler.class );

	public RenameWorkareaHandler( Program program ) {
		super( program );
	}

	@Override
	public void handle( Event event ) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.initOwner( program.getWorkspaceManager().getActiveWorkspace().getStage() );
		dialog.setTitle( program.getResourceBundle().getString( "workarea", "workarea.rename.title" ) );
		dialog.setHeaderText( program.getResourceBundle().getString( "workarea", "workarea.rename.message" ) );
		dialog.setContentText( program.getResourceBundle().getString( "workarea", "workarea.rename.prompt" ) );

		Optional<String> result = dialog.showAndWait();
		result.ifPresent( this::renameWorkarea );
	}

	private void renameWorkarea( String name ) {
		UiFactory uiFactory = new UiFactory( program );
		try {
			Workarea workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();
			// FIXME Simply setting the name does not change the workarea selector
			workarea.setName( name );
		} catch( Exception exception ) {
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
