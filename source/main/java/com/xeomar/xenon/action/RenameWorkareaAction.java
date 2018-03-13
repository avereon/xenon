package com.xeomar.xenon.action;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.util.DialogUtil;
import com.xeomar.xenon.workarea.Workarea;
import javafx.event.Event;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class RenameWorkareaAction extends Action {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Workarea workarea;

	public RenameWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() != null;
	}

	@Override
	public void handle( Event event ) {
		Program program = getProgram();
		workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		TextInputDialog dialog = new TextInputDialog( workarea.getName() );
		dialog.setTitle( program.getResourceBundle().getString( "workarea", "workarea.rename.title" ) );
		dialog.setHeaderText( program.getResourceBundle().getString( "workarea", "workarea.rename.message" ) );
		dialog.setContentText( program.getResourceBundle().getString( "workarea", "workarea.rename.prompt" ) );

		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		Optional<String> result = DialogUtil.showAndWait( stage, dialog );

		result.ifPresent( this::renameWorkarea );
	}

	private void renameWorkarea( String name ) {
		UiManager uiManager = new UiManager( getProgram() );
		try {
			workarea.setName( name );
		} catch( Exception exception ) {
			log.error( "Error creating new workarea: " + name, exception );
		}
	}

}
