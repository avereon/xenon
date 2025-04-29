package com.avereon.xenon.action;

import com.avereon.product.Rb;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.task.Task;
import javafx.event.ActionEvent;
import lombok.CustomLog;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@CustomLog
public class DesktopBrowserAction extends ProgramAction {

	private final URI uri;

	public DesktopBrowserAction( Xenon program, URI uri ) {
		super( program );
		this.uri = uri;
	}

	@Override
	public void handle( ActionEvent event ) {
		String taskTitle = Rb.text( RbKey.ACTION, "browse.name" );
		taskTitle += " " + uri.toString();
		getProgram().getTaskManager().submit( Task.of(
			taskTitle, () -> {
				try {
					Desktop.getDesktop().browse( uri );
				} catch( IOException exception ) {
					log.atWarn( exception ).log( "Unable to browse uri=%s", uri );
				}
			}
		) );
	}

}
