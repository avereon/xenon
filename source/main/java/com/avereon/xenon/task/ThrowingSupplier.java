package com.avereon.xenon.task;

@FunctionalInterface
public interface ThrowingSupplier<T> {

	T get() throws Exception;

}
