package com.avereon.xenon;

import com.avereon.product.ProgramProduct;
import com.avereon.xenon.task.Task;

import java.util.concurrent.Callable;

public interface XenonProgramProduct extends ProgramProduct {

	Xenon getProgram();

	default <T> void task( String name, Callable<T> callable ) {
		getProgram().getTaskManager().submit( Task.of( name, callable ) );
	}
}
