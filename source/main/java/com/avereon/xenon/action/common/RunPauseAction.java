package com.avereon.xenon.action.common;

import com.avereon.skill.RunPauseResettable;
import com.avereon.util.Log;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import javafx.event.ActionEvent;

public class RunPauseAction extends ProgramAction {

	private static final System.Logger log = Log.get();

	private RunPauseResettable target;

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
		log.log( Log.WARN, "state=" + getState() );
		switch( getState() ) {
			case "run" : {
				target.run();
				break;
			}
			case "pause" : {
				target.pause();
				break;
			}
		}
	}

}
