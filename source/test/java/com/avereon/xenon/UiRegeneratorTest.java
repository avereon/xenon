package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.ThreadUtil;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith( MockitoExtension.class )
class UiRegeneratorTest extends BaseFullXenonTestCase {

	private WorkspaceManager workspaceManager;

	private UiRegenerator regenerator;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		super.setup();
		regenerator = new UiRegenerator( getProgram() );
//		workspaceManager = new WorkspaceManager( getProgram() );
//		regenerator = new UiRegenerator( getProgram() );
//
//		lenient().when( getProgram().getWorkspaceManager() ).thenReturn( workspaceManager );
	}

	@Test
	void createDefaultWorkspace() {
		// given
		Path settingsFolder = getProgram().getDataFolder().resolve( SettingsManager.ROOT );
		Path uiSettingsFolder = settingsFolder.resolve( ProgramSettings.UI.substring( 1 ) );
		assertThat( uiSettingsFolder ).doesNotExist();

		// NEXT Add a listener to the UI settings to watch when everything is saved
		// FIXME Apparently settings events are not bubbled up to parents?
		Settings uiSettings = getProgram().getSettingsManager().getSettings(ProgramSettings.UI);
		uiSettings.register( SettingsEvent.ANY, e -> {
			System.out.println( "Settings saved: " + e.getEventType() + " " + e.getPath() );
		} );
		uiSettings.set( "workspace-theme-id", "default" );
		uiSettings.flush();

		// when
		Fx.run( () -> regenerator.createDefaultWorkspace() );
		ThreadUtil.pause( 1000 );

		// Check the settings folder for the expected files
		assertThat( uiSettingsFolder ).exists();
	}

}
