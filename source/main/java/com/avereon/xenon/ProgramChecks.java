package com.avereon.xenon;

import com.avereon.product.Rb;
import com.avereon.product.Release;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.task.Task;
import javafx.stage.Screen;
import lombok.CustomLog;
import lombok.Getter;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.avereon.xenon.Xenon.PROGRAM_RELEASE_PRIOR;

@CustomLog
public class ProgramChecks implements Runnable {

	@Getter
	private final Xenon program;

	public ProgramChecks( Xenon program ) {
		this.program = program;
	}

	public ProgramChecks register() {
		program.register( ProgramEvent.STARTED, ( _ ) -> program.getTaskManager().submit( Task.of( this ) ) );
		return this;
	}

	public void run() {
		String mode = program.getMode();
		boolean isTestMode = XenonMode.TEST.equals( mode );
		boolean isScreenshotMode = XenonMode.SCREENSHOT.equals( mode );

		if( isTestMode || isScreenshotMode ) return;

		checkForHiDpi();
		checkForLinuxPkExec();
		checkForProgramUpdated();
	}

	public static boolean isHiDpiCapable() {
		Screen primary = Screen.getPrimary();
		double scale = primary.getOutputScaleX();
		boolean hiDpi = primary.getDpi() * scale > 120;
		boolean largeSize = primary.getBounds().getWidth() * scale > 1920;
		return hiDpi && largeSize;
	}

	public static boolean isHiDpiEnabled() {
		Screen primary = Screen.getPrimary();
		boolean scaled = primary.getOutputScaleX() > 1.0;
		return scaled && isHiDpiCapable();
	}

	public static boolean isPkExecInstalled() {
		return Files.exists( Paths.get( "/usr/bin/pkexec" ) );
	}

	private void checkForHiDpi() {
		if( !isHiDpiEnabled() ) {
			String title = Rb.text( RbKey.PROGRAM, "program-hidpi-title" );
			//String message = Rb.text( RbKey.PROGRAM, "program-hidpi-message" );
			String message = Rb.text( RbKey.SETTINGS, "advanced-linux-hidpi-assist" );
			String uriString = ProgramSettingsType.ADVANCED + "-" + OperatingSystem.getFamily().name().toLowerCase();
			Runnable action = () -> getProgram().getResourceManager().openAsset( URI.create( uriString ) );
			program.getNoticeManager().addNotice( new Notice( title, message, action ) );
		}
	}

	private void checkForLinuxPkExec() {
		if( !OperatingSystem.isLinux() ) return;

		boolean pkexecInstalled = isPkExecInstalled();

		if( !pkexecInstalled ) {
			String title = Rb.text( RbKey.PROGRAM, "program-linux-no-pkexec-title" );
			//String message = Rb.text( RbKey.PROGRAM, "program-linux-no-pkexec-message" );
			String message = Rb.text( RbKey.SETTINGS, "advanced-linux-pkexec-assist", getProgram().getCard().getName() );
			String uriString = ProgramSettingsType.ADVANCED + "-" + OperatingSystem.getFamily().name().toLowerCase();
			Runnable action = () -> getProgram().getResourceManager().openAsset( URI.create( uriString ) );
			program.getNoticeManager().addNotice( new Notice( title, message, action ) );
		}
	}

	private void checkForProgramUpdated() {
		if( program.isProgramUpdated() ) {
			Release prior = Release.decode( program.getSettings().get( PROGRAM_RELEASE_PRIOR, "" ) );
			Release runtime = program.getCard().getRelease();
			String priorVersion = prior.version().toHumanString();
			String runtimeVersion = runtime.version().toHumanString();
			String title = Rb.text( RbKey.UPDATE, "updates" );
			String message = Rb.text( RbKey.UPDATE, "program-updated-message", priorVersion, runtimeVersion );
			Runnable action = () -> program.getResourceManager().openAsset( ProgramAboutType.URI );

			Notice notice = new Notice( title, message, action );
			notice.setBalloonStickiness( Notice.Balloon.NEVER );
			program.getNoticeManager().addNotice( notice );
		}
	}

}
