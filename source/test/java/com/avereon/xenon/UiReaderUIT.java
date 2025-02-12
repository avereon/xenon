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
	void loadSpace() throws Exception {
		// given
		Settings settings = spaceSettings();

		// when
		Workspace space = Fx.call( () -> reader.loadSpace( settings ) );

		// then
		assertSpaceMatches( space, settings );
	}

	@Test
	void loadSpaceFromSettings() throws Exception {
		// given
		Settings settings = spaceSettings();

		// when
		Workspace space = Fx.call( () -> reader.loadSpaceFromSettings( settings ) );

		// then
		assertSpaceMatches( space, settings );
	}

	@Test
	void loadArea() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpace( spaceSettings() ) );
		Settings settings = areaSettings( space.getUid() );

		// when
		Workarea area = Fx.call( () -> reader.loadArea( settings ) );

		// then
		assertAreaMatches( area, settings );
	}

	@Test
	void loadAreaFromSettings() throws Exception {
		// given
		Settings settings = areaSettings( IdGenerator.getId() );

		// when
		Workarea area = Fx.call( () -> reader.loadAreaFromSettings( settings ) );

		// then
		assertAreaMatches( area, settings );
	}

	@Test
	void loadView() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpace( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadArea( areaSettings( space.getUid() ) ) );
		Settings settings = viewSettings( area.getUid() );

		// when
		WorkpaneView view = Fx.call( () -> reader.loadView( settings ) );

		// then
		assertViewMatches( view, settings );
	}

	@Test
	void loadViewFromSettings() throws Exception {
		// given
		Settings settings = viewSettings( IdGenerator.getId() );

		// when
		WorkpaneView view = Fx.call( () -> reader.loadViewFromSettings( settings ) );

		// then
		assertViewMatches( view, settings );
	}

	@Test
	void loadEdge() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpace( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadArea( areaSettings( space.getUid() ) ) );
		Settings settings = edgeSettings( area.getUid() );

		// when
		WorkpaneEdge edge = Fx.call( () -> reader.loadEdge( settings ) );

		// then
		assertEdgeMatches( edge, settings );
	}

	@Test
	void loadEdgeFromSettings() throws Exception {
		// given
		Settings settings = edgeSettings( IdGenerator.getId() );

		// when
		WorkpaneEdge edge = Fx.call( () -> reader.loadEdgeFromSettings( settings ) );

		// then
		assertEdgeMatches( edge, settings );
	}

	@Test
	void loadToolFromSettings() throws Exception {
		// given
		Settings settings = toolSettings( getProgram() );

		// when
		Tool tool = reader.loadToolFromSettings( settings );

		// then
		assertToolMatches( tool, settings );
	}

	private static Settings spaceSettings() {
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( "x", "428" );
		settings.set( "y", "174" );
		settings.set( "w", "1224" );
		settings.set( "h", "840" );
		settings.set( "maximized", false );
		settings.set( "active", true );
		return settings;
	}

	private static void assertSpaceMatches( Workspace space, Settings settings ) {
		assertThat( space.getUid() ).isEqualTo( settings.getName() );
		assertThat( space.getX() ).isEqualTo( 428 );
		assertThat( space.getY() ).isEqualTo( 174 );
		assertThat( space.getScene().getWidth() ).isEqualTo( 1224 );
		assertThat( space.getScene().getHeight() ).isEqualTo( 840 );
		assertThat( space.isMaximized() ).isFalse();
		assertThat( space.isActive() ).isTrue();
	}

	private static Settings areaSettings( String spaceId ) {
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( UiFactory.PARENT_WORKSPACE_ID, spaceId );
		return settings;
	}

	private static void assertAreaMatches( Workarea area, Settings settings ) {
		assertThat( area ).isNotNull();
		assertThat( area.getUid() ).isEqualTo( settings.getName() );
	}

	private static Settings viewSettings( String areaId ) {
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( "placement", Workpane.Placement.DEFAULT.name() );
		settings.set( UiFactory.PARENT_WORKPANE_ID, areaId );
		return settings;
	}

	private static void assertViewMatches( WorkpaneView view, Settings settings ) {
		assertThat( view.getUid() ).isEqualTo( settings.getName() );
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DEFAULT );
	}

	private static Settings edgeSettings( String areaId ) {
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( "orientation", Orientation.VERTICAL.name().toLowerCase() );
		settings.set( "position", "73" );
		settings.set( UiFactory.PARENT_WORKPANE_ID, areaId );
		return settings;
	}

	private static void assertEdgeMatches( WorkpaneEdge edge, Settings settings ) {
		assertThat( edge.getUid() ).isEqualTo( settings.getName() );
		assertThat( edge.getOrientation() ).isEqualTo( settings.get( "orientation", Orientation.class ) );
		assertThat( edge.getPosition() ).isEqualTo( settings.get( "position", Double.class ) );
	}

	private static Settings toolSettings( XenonProgramProduct program ) {
		String id = IdGenerator.getId();
		Settings settings = new MapSettings().getNode( id );
		settings.set( Tool.SETTINGS_TYPE_KEY, AboutTool.class.getName() );

		String assetTypeKey = new ProgramAboutType( program ).getKey();
		System.out.println( "assetTypeKey=" + assetTypeKey );
		settings.set( Asset.SETTINGS_TYPE_KEY, assetTypeKey );
		settings.set( Asset.SETTINGS_URI_KEY, ProgramAboutType.URI.toString() );

		return settings;
	}

	private static void assertToolMatches( Tool tool, Settings settings ) {
		assertThat( tool ).isNotNull();
		assertThat( tool.getUid() ).isEqualTo( settings.getName() );
		assertThat( tool.getClass().getName() ).isEqualTo( settings.get( Tool.SETTINGS_TYPE_KEY ) );

		assertThat( tool.getAsset() ).isNotNull();
		assertThat( tool.getAsset().getType().getKey() ).isEqualTo( settings.get( Asset.SETTINGS_TYPE_KEY, String.class ) );
		assertThat( tool.getAsset().getUri().toString() ).isEqualTo( settings.get( Asset.SETTINGS_URI_KEY, String.class ) );
	}

}
