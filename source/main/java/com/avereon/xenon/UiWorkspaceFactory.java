package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.IdGenerator;
import com.avereon.xenon.workspace.Workspace;
import lombok.Getter;

public class UiWorkspaceFactory {

	public static final double DEFAULT_WIDTH = 960;

	public static final double DEFAULT_HEIGHT = 600;

	@Getter
	private final Xenon program;

	public UiWorkspaceFactory( Xenon program ) {
		this.program = program;
	}

	public Workspace create() {
		Workspace space = new Workspace( program );
		space.setUid( IdGenerator.getId() );
		return space;
	}

	public Workspace applyWorkspaceSettings( Workspace workspace, Settings settings ) {
		// Due to differences in how FX handles stage sizes (width and height) on
		// different operating systems, the width and height from the scene, not the
		// stage, are used. This includes the listeners for the width and height
		// properties below.
		Double w = settings.get( "w", Double.class, UiWorkspaceFactory.DEFAULT_WIDTH );
		Double h = settings.get( "h", Double.class, UiWorkspaceFactory.DEFAULT_HEIGHT );
		workspace.initializeScene( w, h );

		// Position the stage if x and y are specified
		// If not specified the stage is centered on the screen
		Double x = settings.get( "x", Double.class, null );
		Double y = settings.get( "y", Double.class, null );
		if( x != null ) workspace.setX( x );
		if( y != null ) workspace.setY( y );

		updateThemeFromSettings( workspace, settings );
		workspace.applySettings();

		return workspace;
	}

	public Workspace linkWorkspaceSettingsListeners( Workspace workspace, Settings settings ) {
		// Add the property listeners
		workspace.maximizedProperty().addListener( ( v, o, n ) -> {
			if( workspace.isShowing() ) settings.set( "maximized", n );
		} );
		workspace.xProperty().addListener( ( v, o, n ) -> {
			if( !workspace.isMaximized() ) settings.set( "x", n );
		} );
		workspace.yProperty().addListener( ( v, o, n ) -> {
			if( !workspace.isMaximized() ) settings.set( "y", n );
		} );
		workspace.getScene().widthProperty().addListener( ( v, o, n ) -> {
			if( !workspace.isMaximized() ) settings.set( "w", n );
		} );
		workspace.getScene().heightProperty().addListener( ( v, o, n ) -> {
			if( !workspace.isMaximized() ) settings.set( "h", n );
		} );
		return workspace;
	}

	private void updateThemeFromSettings( Workspace workspace, Settings settings ) {
		String themeId = settings.get( "theme", getProgram().getWorkspaceManager().getThemeId() );
		workspace.setTheme( getProgram().getThemeManager().getMetadata( themeId ).getUrl() );
	}

}
