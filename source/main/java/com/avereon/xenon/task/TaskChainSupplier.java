package com.avereon.xenon.task;

@FunctionalInterface
public interface TaskChainSupplier<T> {

	T get() throws Exception;

}
