package com.xeomar.xenon.task.chain;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.task.Task;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class TaskChainContext {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private TaskChain<?> first;

	TaskChainContext() {}

	<R> TaskChain<R> init( TaskChain<R> link ) {
		this.first = link;
		return link;
	}

	TaskChain<?> getFirstLink() {
		return first;
	}

	<P, R> Task<R> submit( Program program, P parameter, AbstractFunctionalTask<R> task ) {
		if( task instanceof FunctionTask ) ((FunctionTask<P, R>)task).setParameter( parameter );
		task.setProgram( program );
		return program.getTaskManager().submit( task );
	}

}
