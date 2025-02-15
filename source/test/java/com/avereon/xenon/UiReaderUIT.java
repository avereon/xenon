package com.avereon.xenon;

import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import com.avereon.util.FileUtil;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.xenon.tool.AboutTool;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEdge;
import com.avereon.xenon.workpane.WorkpaneView;
import com.avereon.xenon.workspace.Workarea;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith( MockitoExtension.class )
class UiReaderUIT extends BaseFullXenonTestCase {

	private UiReader reader;

	@BeforeEach
	protected void setup() throws Exception {
		ProgramTestConfig.removeFlag( XenonTestFlag.EMPTY_WORKSPACE );
		super.setup();
		reader = new UiReader( getProgram() );
	}

	@Test
	void createDefaultWorkspace() throws Exception {
		// There is technically a race condition here because the program is already
		// started and the UI is probably already restored, but the settings are
		// probably not saved yet. This test will wait for the settings files to be
		// saved before further assertions.

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
		//assertThat( uiSettingsFolder.resolve( "tool" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "area" ) ).exists();
		// No edges are needed for the default workspace
		assertThat( uiSettingsFolder.resolve( "pane" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "view" ) ).exists();
		assertThat( uiSettingsFolder.resolve( "workspace" ) ).exists();
	}

	@Test
	void load() {
		// Reads the settings from the UI settings
		//reader.load();
	}

	@Test
	void loadSpaceForLinking() throws Exception {
		// given
		Settings settings = spaceSettings();

		// when
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( settings ) );

		// then
		assertSpaceMatches( space, settings );
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
	void loadAreaForLinking() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Settings settings = areaSettings( space.getUid() );

		// when
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( settings ) );

		// then
		assertAreaMatches( area, settings );
	}

	@Test
	void loadArea() throws Exception {
		// given
		Settings settings = areaSettings( IdGenerator.getId() );

		// when
		Workarea area = Fx.call( () -> reader.loadArea( settings ) );

		// then
		assertAreaMatches( area, settings );
	}

	@Test
	void loadViewForLinking() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( areaSettings( space.getUid() ) ) );
		Settings settings = viewSettings( area.getUid() );

		// when
		WorkpaneView view = Fx.call( () -> reader.loadViewForLinking( settings ) );

		// then
		assertViewMatches( view, settings );
	}

	@Test
	void loadView() throws Exception {
		// given
		Settings settings = viewSettings( IdGenerator.getId() );

		// when
		WorkpaneView view = Fx.call( () -> reader.loadView( settings ) );

		// then
		assertViewMatches( view, settings );
	}

	@Test
	void loadEdgeForLinking() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( areaSettings( space.getUid() ) ) );
		Settings settings = edgeSettings( area.getUid() );

		// when
		WorkpaneEdge edge = Fx.call( () -> reader.loadEdgeForLinking( settings ) );

		// then
		assertEdgeMatches( edge, settings );
	}

	@Test
	void loadEdge() throws Exception {
		// given
		Settings settings = edgeSettings( IdGenerator.getId() );

		// when
		WorkpaneEdge edge = Fx.call( () -> reader.loadEdge( settings ) );

		// then
		assertEdgeMatches( edge, settings );
	}

	@Test
	void loadToolForLinking() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( areaSettings( space.getUid() ) ) );
		WorkpaneView view = Fx.call( () -> reader.loadViewForLinking( viewSettings( area.getUid() ) ) );
		Settings settings = toolSettings( getProgram(), view.getUid() );

		// when
		Tool tool = reader.loadToolForLinking( settings );

		// then
		assertToolMatches( tool, settings );
	}

	@Test
	void loadTool() throws Exception {
		// given
		Settings settings = toolSettings( getProgram(), IdGenerator.getId() );

		// when
		Tool tool = reader.loadTool( settings );

		// then
		assertToolMatches( tool, settings );
	}

	@Test
	void linkEdge() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( areaSettings( space.getUid() ) ) );
		Settings settings = edgeSettings( area.getUid() );
		WorkpaneEdge edge = Fx.call( () -> reader.loadEdge( settings ) );

		// when
		reader.linkEdge( area, edge, settings );

		// then
		assertThat( edge.getEdge( Side.TOP ) ).isEqualTo( area.getWallEdge( Side.TOP ) );
		assertThat( edge.getEdge( Side.BOTTOM ) ).isEqualTo( area.getWallEdge( Side.BOTTOM ) );
		// The area should not have the edge yet
		assertThat( area.getEdges() ).doesNotContain( edge );
	}

	@Test
	void linkView() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( areaSettings( space.getUid() ) ) );
		Settings settings = viewSettings( area.getUid() );
		WorkpaneView view = Fx.call( () -> reader.loadView( settings ) );

		// when
		reader.linkView( area, view, settings );

		// then
		assertThat( view.getEdge( Side.TOP ) ).isEqualTo( area.getWallEdge( Side.TOP ) );
		assertThat( view.getEdge( Side.BOTTOM ) ).isEqualTo( area.getWallEdge( Side.BOTTOM ) );
		assertThat( view.getEdge( Side.LEFT ) ).isEqualTo( area.getWallEdge( Side.LEFT ) );
		assertThat( view.getEdge( Side.RIGHT ) ).isEqualTo( area.getWallEdge( Side.RIGHT ) );
		// The area should not have the view yet
		assertThat( area.getViews() ).doesNotContain( view );
	}

	@Test
	void linkArea() throws Exception {
		// given
		Workspace space = Fx.call( () -> reader.loadSpaceForLinking( spaceSettings() ) );
		Workarea area = Fx.call( () -> reader.loadAreaForLinking( areaSettings( space.getUid() ) ) );
		Settings edgeSettings = edgeSettings( area.getUid() );
		WorkpaneEdge edge = Fx.call( () -> reader.loadEdge( edgeSettings ) );
		Settings viewSettings = viewSettings( area.getUid() );
		WorkpaneView view = Fx.call( () -> reader.loadView( viewSettings ) );

		// when
		reader.linkArea( area, Set.of( edge ), Set.of( view ) );

		// then
		assertThat( area.getEdges() ).contains( edge );
		assertThat( area.getViews() ).contains( view );
	}

	private static Settings spaceSettings() {
		Settings settings = new MapSettings().getNode( IdGenerator.getId() );

		settings.set( "x", "428" );
		settings.set( "y", "174" );
		settings.set( "w", "1224" );
		settings.set( "h", "840" );
		settings.set( "active", true );
		settings.set( "maximized", false );

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
		Settings settings = new MapSettings().getNode( IdGenerator.getId() );

		settings.set( UiFactory.PARENT_WORKSPACE_ID, spaceId );

		return settings;
	}

	private static void assertAreaMatches( Workarea area, Settings settings ) {
		assertThat( area ).isNotNull();
		assertThat( area.getUid() ).isEqualTo( settings.getName() );
	}

	private static Settings viewSettings( String areaId ) {
		Settings settings = new MapSettings().getNode( IdGenerator.getId() );

		settings.set( UiFactory.PARENT_WORKPANE_ID, areaId );
		settings.set( "placement", Workpane.Placement.DEFAULT.name() );
		settings.set( "maximized", false );
		settings.set( "default", true );
		settings.set( "active", true );
		settings.set( "t", "t" );
		settings.set( "b", "b" );
		settings.set( "l", "l" );
		settings.set( "r", "r" );

		return settings;
	}

	private static void assertViewMatches( WorkpaneView view, Settings settings ) {
		assertThat( view.getUid() ).isEqualTo( settings.getName() );
		assertThat( view.getPlacement() ).isEqualTo( Workpane.Placement.DEFAULT );
	}

	private static void assertViewFlags( WorkpaneView view, Settings settings ) {
		assertThat( view.isMaximized() ).isEqualTo( settings.get( "maximized", Boolean.class ) );
		assertThat( view.isDefault() ).isEqualTo( settings.get( "default", Boolean.class ) );
		assertThat( view.isActive() ).isEqualTo( settings.get( "active", Boolean.class ) );
	}

	private static Settings edgeSettings( String areaId ) {
		Settings settings = new MapSettings().getNode( IdGenerator.getId() );

		settings.set( UiFactory.PARENT_WORKPANE_ID, areaId );
		settings.set( "orientation", Orientation.VERTICAL.name().toLowerCase() );
		settings.set( "position", "73" );
		settings.set( "t", "t" );
		settings.set( "b", "b" );

		return settings;
	}

	private static void assertEdgeMatches( WorkpaneEdge edge, Settings settings ) {
		assertThat( edge.getUid() ).isEqualTo( settings.getName() );
		assertThat( edge.getOrientation() ).isEqualTo( settings.get( "orientation", Orientation.class ) );
		assertThat( edge.getPosition() ).isEqualTo( settings.get( "position", Double.class ) );
	}

	private static Settings toolSettings( XenonProgramProduct program, String viewId ) {
		Settings settings = new MapSettings().getNode( IdGenerator.getId() );

		settings.set( UiFactory.PARENT_WORKPANEVIEW_ID, viewId );
		settings.set( Tool.SETTINGS_TYPE_KEY, AboutTool.class.getName() );
		settings.set( Tool.ORDER, 5 );

		String assetTypeKey = new ProgramAboutType( program ).getKey();
		settings.set( Asset.SETTINGS_TYPE_KEY, assetTypeKey );
		settings.set( Asset.SETTINGS_URI_KEY, ProgramAboutType.URI.toString() );

		return settings;
	}

	private static void assertToolMatches( Tool tool, Settings settings ) {
		assertThat( tool ).isNotNull();
		assertThat( tool.getUid() ).isEqualTo( settings.getName() );
		assertThat( tool.getOrder() ).isEqualTo( settings.get( Tool.ORDER, Integer.class ) );
		assertThat( tool.getClass().getName() ).isEqualTo( settings.get( Tool.SETTINGS_TYPE_KEY ) );

		assertThat( tool.getAsset() ).isNotNull();
		assertThat( tool.getAsset().getType().getKey() ).isEqualTo( settings.get( Asset.SETTINGS_TYPE_KEY, String.class ) );
		assertThat( tool.getAsset().getUri().toString() ).isEqualTo( settings.get( Asset.SETTINGS_URI_KEY, String.class ) );
	}

}
