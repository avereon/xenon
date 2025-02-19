package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.RestartJob;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class UiResetAction extends ProgramAction {

	public UiResetAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		// Reset the UI settings before restarting
		new UiFactory( getProgram() ).reset();

		// Restart the application
		try {
			getProgram().requestRestart( RestartJob.Mode.RESTART );
		} catch( Throwable throwable ) {
			log.atError( throwable ).log( "Error requesting restart" );
		}
	}

}
