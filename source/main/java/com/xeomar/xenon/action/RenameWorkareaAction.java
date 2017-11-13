package com.xeomar.xenon.action;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.workarea.Workarea;
import javafx.event.Event;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;

import java.util.Optional;

public class RenameWorkareaAction extends Action {

	private static Logger log = LogUtil.get( RenameWorkareaAction.class );

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
		UiManager uiManager = new UiManager( program );
		try {
			workarea.setName( name );
		} catch( Exception exception ) {
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
