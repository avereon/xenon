package com.avereon.xenon;

import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workspace.Workspace;

import java.nio.file.Path;

public class Screenshots {

	private static String PROFILE = "screenshots";

	private int scale;

	private Path screenshots;

	private Program program;

	private Workspace workspace;

	private Workpane workpane;

	//private WorkpaneWatcher workpaneWatcher;

	Screenshots( int scale ) {
		this.scale = scale;
	}

	public static void main( String[] args ) {
		System.out.println( "MAKE SCREENSHOTS..." );
	}

	// NEXT Continue work on screenshots

//	public void run() {
//		try {
//			this.screenshots = Paths.get( "target" ).resolve( PROFILE );
//			Files.createDirectories( screenshots );
//
//			setup();
//
//			snapshotWelcomeTool();
//			snapshotDefaultWorkarea();
//			snapshotAboutTool();
//			snapshotSettingsTool();
//		} catch( Throwable throwable ) {
//			throwable.printStackTrace( System.err );
//		} finally {
//			Platform.runLater( this::shutdown );
//		}
//	}
//
//	private void setup() throws InterruptedException {
//		if( FxUtil.isFxRunning() ) FxUtil.fxWait( 2000 );
//		startup();
//		workspace = program.getWorkspaceManager().getActiveWorkspace();
//		workpane = workspace.getActiveWorkarea().getWorkpane();
//		workpaneWatcher = new WorkpaneWatcher();
//		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
//		FxUtil.fxWait( 2000 );
//	}

}
