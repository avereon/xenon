package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.xenon.notice.Notice;
import javafx.stage.Screen;

public class ProgramChecks {

	private final Xenon program;

	public ProgramChecks( Xenon program ) {
		this.program = program;
	}

	public ProgramChecks register() {
		program.register( ProgramEvent.STARTED, ( e ) -> checkForHiDpi() );
		return this;
	}

	private void checkForHiDpi() {
		Screen primary = Screen.getPrimary();
		boolean hiDpi = primary.getDpi() > 120;
		boolean noScale = primary.getOutputScaleX() == 1.0;
		boolean largeSize = primary.getBounds().getWidth() > 1920;

		if( noScale && hiDpi && largeSize ) {
			String title = Rb.text( "program", "program-hidpi-title" );
			String message = Rb.text( "program", "program-hidpi-message" );
			program.getNoticeManager().addNotice( new Notice( title, message ) );
		}
	}

}
