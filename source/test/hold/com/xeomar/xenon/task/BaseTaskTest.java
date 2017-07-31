package com.xeomar.xenon.task;

import com.xeomar.xenon.BaseTestCase;
import org.junit.Before;

import java.util.concurrent.TimeUnit;

public abstract class BaseTaskTest extends BaseTestCase {

	static final int DEFAULT_WAIT_TIME = 1;

	static final TimeUnit DEFAULT_WAIT_UNIT = TimeUnit.SECONDS;

	protected TaskManager manager;

	protected TaskWatcher taskWatcher;

	@Before
	@Override
	public void setup() throws Exception {
		manager = new TaskManager().start();
		manager.addTaskListener( taskWatcher = new TaskWatcher() );
	}

}
