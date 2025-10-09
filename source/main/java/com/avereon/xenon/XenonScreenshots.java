package com.avereon.xenon;

import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import com.avereon.xenon.resource.type.ProgramWelcomeType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.zerra.javafx.Fx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class XenonScreenshots extends ProgramScreenshots {

	public static void main( String[] args ) {
		new XenonScreenshots().generate( args );
	}

	@Override
	protected void generateScreenshots() throws InterruptedException, TimeoutException, ExecutionException {
		screenshot( "default-workarea" );
		screenshot( ProgramWelcomeType.URI, "welcome-tool" );
		screenshot( ProgramAboutType.URI, "about-tool" );
		screenshotSettingsPages();
		//screenshotProductPages();
		screenshotThemes();
	}

	private void screenshotSettingsPages() throws InterruptedException, TimeoutException, ExecutionException {
		for( String id : getProgram().getSettingsManager().getPageIds() ) {
			screenshot( ProgramSettingsType.URI, id, "settings/settings-tool-" + id );
		}
	}

	private void screenshotThemes() throws InterruptedException, TimeoutException {
		// Set an example tool
		getProgram().getResourceManager().openAsset( ProgramAboutType.URI );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );

		getProgram().getThemeManager().getThemes().stream().map( ThemeMetadata::getId ).forEach( id -> {
			Fx.run( () -> getProgram().getWorkspaceManager().setTheme( id ) );
			screenshotNoReset( "themes/" + id );
		} );

		reset();
	}

}
