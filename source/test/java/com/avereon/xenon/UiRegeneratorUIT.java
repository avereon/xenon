package com.avereon.xenon;

import com.avereon.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith( MockitoExtension.class )
class UiRegeneratorUIT extends BaseFullXenonTestCase {

	@Test
	void createDefaultWorkspace() throws Exception {
		// There is technically a race condition here because the program is already
		// started and the UI is probably already restored, but the settings will
		// not be saved immediately.

		// given
		Path settingsFolder = getProgram().getDataFolder().resolve( SettingsManager.ROOT );
		Path uiSettingsFolder = settingsFolder.resolve( ProgramSettings.UI.substring( 1 ) );

		// when
		FileUtil.waitToExist( uiSettingsFolder, 1, TimeUnit.SECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "area" ), 1, TimeUnit.SECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "pane" ), 1, TimeUnit.SECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "view" ), 1, TimeUnit.SECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "workspace" ), 1, TimeUnit.SECONDS );

		// then
		// Check the settings folder for the expected files
		assertThat( uiSettingsFolder ).exists();
		assertThat( uiSettingsFolder.resolve( "area" ) ).exists();
		// No edges are needed for the default workspace
		assertThat( uiSettingsFolder.resolve( "pane" ) ).exists();
		// No tool is created due to the test parameters
		assertThat( uiSettingsFolder.resolve( "view" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "workspace" ) ).exists();
	}

}
