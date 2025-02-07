package com.avereon.xenon;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import com.avereon.util.FileUtil;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith( MockitoExtension.class )
class UiReaderUIT extends BaseFullXenonTestCase {

	private UiReader reader;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		reader = new UiReader( getProgram() );
	}

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
		FileUtil.waitToExist( uiSettingsFolder.resolve( "workarea" ), 1, TimeUnit.SECONDS );

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

	@Test
	void loadSpaceFromSettings() {
		// given
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( "x", "428" );
		settings.set( "y", "174" );
		settings.set( "w", "1224" );
		settings.set( "h", "840" );
		settings.set( "maximized", false );
		settings.set( "active", true );

		// More settings in Background, MemoryMonitor, TaskMonitor, FpsMonitor, but
		// they are not important in this test

		// when
		AtomicReference<Workspace> space = new AtomicReference<>();
		Fx.run( () -> space.set( reader.loadSpaceFromSettings( settings ) ) );
		Fx.waitFor( 1, TimeUnit.SECONDS );

		// then
		assertThat( space ).isNotNull();
		assertThat( space.get().getUid() ).isEqualTo( id );
		assertThat( space.get().getX() ).isEqualTo( 428 );
		assertThat( space.get().getY() ).isEqualTo( 174 );
		assertThat( space.get().getScene().getWidth() ).isEqualTo( 1224 );
		assertThat( space.get().getScene().getHeight() ).isEqualTo( 840 );
		assertThat( space.get().isMaximized() ).isFalse();
		assertThat( space.get().isActive() ).isTrue();
	}

	@Test
	void loadAreaFromSettings() {
		// given
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );

		// when
		AtomicReference<Workarea> area = new AtomicReference<>();
		Fx.run( () -> area.set( reader.loadAreaFromSettings( settings ) ) );
		Fx.waitFor( 1, TimeUnit.SECONDS );

		// then
		assertThat( area ).isNotNull();
		assertThat( area.get().getUid() ).isEqualTo( id );
	}

	// NEXT Continue testing atomic UiReader operations
}
