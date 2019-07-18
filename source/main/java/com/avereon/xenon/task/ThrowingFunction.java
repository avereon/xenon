package com.avereon.xenon.task;

@FunctionalInterface
public interface ThrowingFunction<T, R> {

	R apply(T t) throws Exception;

}
