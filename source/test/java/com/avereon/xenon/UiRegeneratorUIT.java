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

		long timeout = getProgram().getSettingsManager().getMaxFlushLimit() * 3;

		// when
		FileUtil.waitToExist( uiSettingsFolder, timeout, TimeUnit.MILLISECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "area" ), timeout, TimeUnit.MILLISECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "pane" ), timeout, TimeUnit.MILLISECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "view" ), timeout, TimeUnit.MILLISECONDS );
		FileUtil.waitToExist( uiSettingsFolder.resolve( "workspace" ), timeout, TimeUnit.MILLISECONDS );

		// then
		// Check the settings folder for the expected files
		assertThat( uiSettingsFolder ).exists();
		assertThat( uiSettingsFolder.resolve( "area" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "pane" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "view" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "workspace" ) ).exists();

		// No edges are needed for the default workspace
		// No tool is created due to the test parameters
	}

}
