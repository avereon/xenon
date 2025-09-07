package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.product.Release;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.task.Task;
import javafx.stage.Screen;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.avereon.xenon.Xenon.PROGRAM_RELEASE_PRIOR;

public class ProgramChecks implements Runnable{

	private final Xenon program;

	public ProgramChecks( Xenon program ) {
		this.program = program;
	}

	public ProgramChecks register() {
		program.register( ProgramEvent.STARTED, (e) -> program.getTaskManager().submit( Task.of( this ) ) );
		return this;
	}

	public void run() {
		checkForHiDpi();
		checkForLinuxPkExec();
		checkForProgramUpdated();
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

	private void checkForLinuxPkExec() {
		if( !OperatingSystem.isLinux() ) return;

		Path pkexec = Path.of( "/usr/bin/pkexec" );
		if( !Files.exists( pkexec) ) {
			String title = Rb.text( "program", "program-pkexec-title" );
			String message = Rb.text( "program", "program-pkexec-message" );
			program.getNoticeManager().addNotice( new Notice( title, message ) );
		}
	}

	private void checkForProgramUpdated() {
		if( program.isProgramUpdated() ) {
			Release prior = Release.decode( program.getSettings().get( PROGRAM_RELEASE_PRIOR, (String)null ) );
			Release runtime = program.getCard().getRelease();
			String priorVersion = prior.getVersion().toHumanString();
			String runtimeVersion = runtime.getVersion().toHumanString();
			String title = Rb.text( RbKey.UPDATE, "updates" );
			String message = Rb.text( RbKey.UPDATE, "program-updated-message", priorVersion, runtimeVersion );
			Runnable action = () -> program.getAssetManager().openAsset( ProgramAboutType.URI );

			Notice notice = new Notice( title, message, action );
			notice.setBalloonStickiness( Notice.Balloon.NEVER );
			program.getNoticeManager().addNotice( notice );
		}
	}

}
