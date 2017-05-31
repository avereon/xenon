package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.UiFactory;
import com.parallelsymmetry.essence.workarea.Workarea;
import javafx.event.Event;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RenameWorkareaAction extends Action {

	private static Logger log = LoggerFactory.getLogger( RenameWorkareaAction.class );

	private Workarea workarea;

	public RenameWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() != null;
	}

	@Override
	public void handle( Event event ) {
		workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		TextInputDialog dialog = new TextInputDialog( workarea.getName() );
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
			workarea.setName( name );
		} catch( Exception exception ) {
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
