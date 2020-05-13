package com.avereon.xenon;

import com.avereon.xenon.notice.Notice;
import javafx.stage.Screen;

public class ProgramChecks {

	private Program program;

	public ProgramChecks( Program program ) {
		this.program = program;
		program.register( ProgramEvent.STARTED, ( e ) -> checkForHiDpi() );
	}

	private void checkForHiDpi() {
		Screen primary = Screen.getPrimary();
		boolean hiDpi = primary.getDpi() > 120;
		boolean noScale = primary.getOutputScaleX() == 1.0;
		boolean largeSize = primary.getBounds().getWidth() > 1920;

		if( noScale && hiDpi && largeSize ) {
			String title = program.rb().text( "program", "program-hidpi-title" );
			String message = program.rb().text( "program", "program-hidpi-message" );
			program.getNoticeManager().addNotice( new Notice( title, message ) );
		}
	}

}