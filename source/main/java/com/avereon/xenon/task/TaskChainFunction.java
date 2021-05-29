package com.avereon.xenon.task;

@FunctionalInterface
public interface TaskChainFunction<T, R> {

	R apply(T t) throws Exception;

}
