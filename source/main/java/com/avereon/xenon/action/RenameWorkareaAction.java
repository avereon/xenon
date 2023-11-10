package com.avereon.xenon.action;

import com.avereon.product.Rb;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.workspace.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.util.Optional;

@CustomLog
public class RenameWorkareaAction extends ProgramAction {

	public static final String WORKAREA = "workarea";

	private Workarea workarea;

	public RenameWorkareaAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea() != null;
	}

	@Override
	public void handle( ActionEvent event ) {
		Xenon program = getProgram();
		workarea = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();

		TextInputDialog dialog = new TextInputDialog( workarea.getName() );
		dialog.setTitle( Rb.text( WORKAREA, "workarea-rename-title" ) );
		dialog.setHeaderText( Rb.text( WORKAREA, "workarea-rename-message" ) );
		dialog.setContentText( Rb.text( WORKAREA, "workarea-rename-prompt" ) );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<String> result = DialogUtil.showAndWait( stage, dialog );

		result.ifPresent( this::renameWorkarea );
	}

	private void renameWorkarea( String name ) {
		try {
			workarea.setName( name );
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error creating new workarea: %s", name );
		}
	}

}
