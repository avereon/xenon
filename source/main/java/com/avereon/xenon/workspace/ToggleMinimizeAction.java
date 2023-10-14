package com.avereon.xenon.workspace;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.util.Objects;

public class ToggleMinimizeAction extends ProgramAction {

	private final Stage stage;

	public ToggleMinimizeAction( Xenon program, Workspace workspace ) {
		super( program );
		this.stage = Objects.requireNonNull( workspace );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		stage.setIconified( !stage.isIconified() );
	}

}
