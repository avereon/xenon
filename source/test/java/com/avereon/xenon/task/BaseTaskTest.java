package com.avereon.xenon.task;

import com.avereon.xenon.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

public abstract class BaseTaskTest extends BaseTestCase {

	static final int DEFAULT_WAIT_TIME = 1;

	static final TimeUnit DEFAULT_WAIT_UNIT = TimeUnit.SECONDS;

	TaskManager manager;

	TaskWatcher taskWatcher;

	@BeforeEach
	@Override
	public void setup() {
		manager = new TaskManager().start();
		manager.getEventBus().register( TaskManagerEvent.ANY, taskWatcher = new TaskWatcher() );
	}

}
