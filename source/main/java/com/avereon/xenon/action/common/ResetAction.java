package com.avereon.xenon.action.common;

import com.avereon.skill.Resettable;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import javafx.event.ActionEvent;

public class ResetAction extends ProgramAction {

	private Resettable resettable;

	public ResetAction( Xenon program, Resettable resettable ) {
		super( program );
		this.resettable = resettable;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent actionEvent ) {
		resettable.reset();
	}

}
