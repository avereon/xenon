package com.avereon.xenon.action;

import com.avereon.product.Rb;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import com.avereon.zerra.stage.DialogUtil;
import com.avereon.xenon.workspace.Workarea;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import lombok.CustomLog;

import java.util.Optional;

@CustomLog
public class NewWorkareaAction extends ProgramAction {

	public NewWorkareaAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Xenon program = getProgram();

		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle( Rb.text( RbKey.WORKAREA, "workarea-new-title" ) );
		dialog.setHeaderText( Rb.text( RbKey.WORKAREA, "workarea-new-message" ) );
		dialog.setContentText( Rb.text( RbKey.WORKAREA, "workarea-new-prompt" ) );

		// TODO Allow the user to select or specify an icon
		// TODO Allow the user to select a color

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<String> result = DialogUtil.showAndWait( stage, dialog );

		result.ifPresent( this::createNewWorkarea );
	}

	private void createNewWorkarea( String name ) {
		UiFactory uiFactory = new UiFactory( getProgram() );
		try {
			Workarea workarea = uiFactory.create();
			workarea.setName( name );
			getProgram().getWorkspaceManager().getActiveWorkspace().setActiveWorkarea( workarea );
		} catch( Exception exception ) {
			log.atError().withCause( exception ).log( "Error creating new workarea: %s", name );
		}
	}

}
