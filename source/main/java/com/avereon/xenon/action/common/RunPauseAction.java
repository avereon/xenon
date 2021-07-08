package com.avereon.xenon.action.common;

import com.avereon.skill.RunPauseResettable;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class RunPauseAction extends ProgramAction {

	private final RunPauseResettable target;

	public RunPauseAction( Program program, RunPauseResettable target ) {
		super( program );
		this.target = target;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		switch( getState() ) {
			case "run" -> target.run();
			case "pause" -> target.pause();
		}
	}

}
