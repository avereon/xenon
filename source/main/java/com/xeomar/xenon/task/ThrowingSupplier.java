package com.xeomar.xenon.task;

@FunctionalInterface
public interface ThrowingSupplier<T> {

	T get() throws Exception;

}
