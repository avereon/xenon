package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.ProgramActionHandler;
import javafx.event.Event;

public class ExitProgramHandler extends ProgramActionHandler {

	public ExitProgramHandler( Program program ) {
		super( program );
	}

	@Override
	public void handle( Event event ) {
		program.requestExit();
	}

}
