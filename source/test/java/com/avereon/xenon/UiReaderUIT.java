package com.avereon.xenon;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import com.avereon.util.FileUtil;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.tool.AboutTool;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Orientation;
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
		AtomicReference<Workspace> spaceReference = new AtomicReference<>();
		Fx.run( () -> spaceReference.set( reader.loadSpaceFromSettings( settings ) ) );
		Fx.waitFor( 1, TimeUnit.SECONDS );

		// then
		Workspace space = spaceReference.get();
		assertThat( space ).isNotNull();
		assertThat( space.getUid() ).isEqualTo( id );
		assertThat( space.getX() ).isEqualTo( 428 );
		assertThat( space.getY() ).isEqualTo( 174 );
		assertThat( space.getScene().getWidth() ).isEqualTo( 1224 );
		assertThat( space.getScene().getHeight() ).isEqualTo( 840 );
		assertThat( space.isMaximized() ).isFalse();
		assertThat( space.isActive() ).isTrue();
	}

	@Test
	void loadAreaFromSettings() {
		// given
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );

		// when
		AtomicReference<Workarea> areaReference = new AtomicReference<>();
		Fx.run( () -> areaReference.set( reader.loadAreaFromSettings( settings ) ) );
		Fx.waitFor( 1, TimeUnit.SECONDS );

		// then
		Workarea area = areaReference.get();
		assertThat( area ).isNotNull();
		assertThat( area.getUid() ).isEqualTo( id );
	}

	@Test
	void loadViewFromSettings() {
		// given
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( "placement", Workpane.Placement.DEFAULT.name() );

		// when
		AtomicReference<WorkpaneView> viewReference = new AtomicReference<>();
		Fx.run( () -> viewReference.set( reader.loadViewFromSettings( settings ) ) );
		Fx.waitFor( 1, TimeUnit.SECONDS );

		// then
		WorkpaneView view = viewReference.get();
		assertThat( view ).isNotNull();
		assertThat( view.getUid() ).isEqualTo( id );
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DEFAULT );
	}

	@Test
	void loadEdgeFromSettings() {
		// given
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( "orientation", Orientation.VERTICAL.name().toLowerCase() );
		settings.set( "position", "73" );

		// when
		AtomicReference<WorkpaneEdge> edgeReference = new AtomicReference<>();
		Fx.run( () -> edgeReference.set( reader.loadEdgeFromSettings( settings ) ) );
		Fx.waitFor( 1, TimeUnit.SECONDS );

		// then
		WorkpaneEdge edge = edgeReference.get();
		assertThat( edge ).isNotNull();
		assertThat( edge.getUid() ).isEqualTo( id );
		assertThat( edge.getOrientation() ).isEqualTo( Orientation.VERTICAL );
		assertThat( edge.getPosition() ).isEqualTo( 73 );
	}

	@Test
	void loadToolFromSettings() throws Exception{
		// given
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( Tool.SETTINGS_TYPE_KEY, AboutTool.class.getName() );

		String assetTypeKey = new ProgramAboutType(getProgram()).getKey();
		settings.set( Asset.SETTINGS_TYPE_KEY, new ProgramAboutType(getProgram()).getKey() );
		settings.set( Asset.SETTINGS_URI_KEY, ProgramAboutType.URI.toString() );

		// when
		Tool tool = reader.loadToolFromSettings( settings );

		// then
		assertThat( tool ).isNotNull();
		assertThat( tool.getUid() ).isEqualTo( id );
		assertThat( tool ).isInstanceOf( AboutTool.class );
		assertThat( tool.getAsset() ).isNotNull();
		assertThat( tool.getAsset().getType() ).isEqualTo( getProgram().getAssetManager().getAssetType( assetTypeKey ) );
		assertThat( tool.getAsset().getUri() ).isEqualTo( ProgramAboutType.URI );
	}

}
