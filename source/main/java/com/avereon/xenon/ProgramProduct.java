package com.avereon.xenon;

import com.avereon.product.Product;
import com.avereon.xenon.task.Task;

import java.util.concurrent.Callable;

public interface ProgramProduct extends Product {

	Program getProgram();

	default <T> void task( String name, Callable<T> callable ) {
		getProgram().getTaskManager().submit( Task.of( name, callable ) );
	}

}
