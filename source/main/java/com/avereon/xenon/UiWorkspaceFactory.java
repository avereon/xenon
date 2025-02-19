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
		// NEXT Implement this settings logic

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

		Settings programSettings = program.getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		//		updateBackgroundFromSettings( programSettings );
		//		updateMemoryMonitorFromSettings( programSettings );
		//		updateTaskMonitorFromSettings( programSettings );
		//		updateFpsMonitorFromSettings( programSettings );

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

//	private void updateBackgroundFromSettings( Workspace workspace, Settings settings ) {
//		Fx.run( () -> {
//			settings.unregister( SettingsEvent.CHANGED, backgroundSettingsHandler );
//			workspace.getBackground().updateFromSettings( settings );
//			settings.register( SettingsEvent.CHANGED, backgroundSettingsHandler );
//		} );
//	}
//
//	private void updateMemoryMonitorFromSettings( Workspace workspace, Settings settings ) {
//		Boolean enabled = settings.get( "workspace-memory-monitor-enabled", Boolean.class, Boolean.TRUE );
//		Boolean showText = settings.get( "workspace-memory-monitor-text", Boolean.class, Boolean.TRUE );
//		Boolean showPercent = settings.get( "workspace-memory-monitor-percent", Boolean.class, Boolean.TRUE );
//
//		Fx.run( () -> {
//			settings.unregister( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
//			updateContainer( memoryMonitor, enabled );
//			memoryMonitor.setTextVisible( showText );
//			memoryMonitor.setShowPercent( showPercent );
//			settings.register( SettingsEvent.CHANGED, memoryMonitorSettingsHandler );
//		} );
//	}
//
//	private void updateTaskMonitorFromSettings( Workspace workspace, Settings settings ) {
//		Boolean enabled = settings.get( "workspace-task-monitor-enabled", Boolean.class, Boolean.TRUE );
//		Boolean showText = settings.get( "workspace-task-monitor-text", Boolean.class, Boolean.TRUE );
//		Boolean showPercent = settings.get( "workspace-task-monitor-percent", Boolean.class, Boolean.TRUE );
//		Fx.run( () -> {
//			settings.unregister( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
//			updateContainer( taskMonitor, enabled );
//			taskMonitor.setTextVisible( showText );
//			taskMonitor.setShowPercent( showPercent );
//			settings.register( SettingsEvent.CHANGED, taskMonitorSettingsHandler );
//		} );
//	}
//
//	private void updateFpsMonitorFromSettings( Workspace workspace, Settings settings ) {
//		Boolean enabled = settings.get( "workspace-fps-monitor-enabled", Boolean.class, Boolean.TRUE );
//		Fx.run( () -> {
//			settings.unregister( SettingsEvent.CHANGED, fpsMonitorSettingsHandler );
//			updateContainer( fpsMonitor, enabled );
//			settings.register( SettingsEvent.CHANGED, fpsMonitorSettingsHandler );
//		} );
//	}
}
