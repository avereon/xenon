package com.avereon.xenon.action;

import com.avereon.product.Rb;
import com.avereon.util.Log;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workspace.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.lang.System.Logger;
import java.util.Optional;

public class RenameWorkareaAction extends ProgramAction {

	private static final Logger log = Log.get();

	private Workarea workarea;

	public RenameWorkareaAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() != null;
	}

	@Override
	public void handle( ActionEvent event ) {
		Program program = getProgram();
		workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		TextInputDialog dialog = new TextInputDialog( workarea.getName() );
		dialog.setTitle( Rb.text( "workarea", "workarea-rename-title" ) );
		dialog.setHeaderText( Rb.text( "workarea", "workarea-rename-message" ) );
		dialog.setContentText( Rb.text( "workarea", "workarea-rename-prompt" ) );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<String> result = DialogUtil.showAndWait( stage, dialog );

		result.ifPresent( this::renameWorkarea );
	}

	private void renameWorkarea( String name ) {
		try {
			workarea.setName( name );
		} catch( Exception exception ) {
			log.log( Log.ERROR,  "Error creating new workarea: " + name, exception );
		}
	}

}
