package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.xenon.task.Task;
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
		if( getProgram() == null ) return super.getMaxThreadCount();
		return getSettings().get( "thread-count", Integer.class, DEFAULT_MAX_THREAD_COUNT );
	}

	@Override
	public TaskManager setMaxThreadCount( int count ) {
		if( getProgram() == null ) return super.setMaxThreadCount( count );
		getSettings().set( "thread-count", Math.min( Math.max( LOW_THREAD_COUNT, count ), HIGH_THREAD_COUNT ) );
		return this;
	}

	public Settings getSettings() {
		if( getProgram() == null ) throw new RuntimeException( "Program cannot be null" );
		if( getProgram().getSettingsManager() == null ) throw new RuntimeException( "SettingsManager cannot be null" );
		return getProgram().getSettingsManager().getSettings( ManagerSettings.TASK );
	}

	@Override
	protected void taskFailed( Task<?> task, Throwable throwable ) {
		getProgram().getNoticeManager().error( throwable );
	}

}
