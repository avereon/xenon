package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.xenon.task.TaskManager;

public class ProgramTaskManager extends TaskManager {

	private Program program;

	public ProgramTaskManager( Program program ) {
		this.program = program;
	}

	public Program getProgram() {
		return program;
	}

	@Override
	public int getMaxThreadCount() {
		return getSettings().get( "thread-count", Integer.class, DEFAULT_MAX_THREAD_COUNT );
	}

	@Override
	public void setMaxThreadCount( int count ) {
		//super.setMaxThreadCount( count );
		getSettings().set( "thread-count", Math.min( Math.max( LOW_THREAD_COUNT, count ), HIGH_THREAD_COUNT ) );
	}

	public Settings getSettings() {
		if( getProgram() == null ) System.out.println( "Program is null" );
		if( getProgram().getSettingsManager() == null ) System.out.println( "SettingsManager is null" );
		return getProgram().getSettingsManager().getSettings( ManagerSettings.TASK );
	}

}
