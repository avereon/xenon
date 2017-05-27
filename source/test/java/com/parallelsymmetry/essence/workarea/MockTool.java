package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.worktool.Tool;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MockTool extends Tool {

	public static final String ALLOCATE = "allocate";

	public static final String DISPLAY = "display";

	public static final String ACTIVATE = "activate";

	public static final String DEACTIVATE = "deactivate";

	public static final String CONCEAL = "conceal";

	public static final String DEALLOCATE = "deallocate";

	private static final AtomicInteger counter = new AtomicInteger();

	private List<MethodCall> events = new CopyOnWriteArrayList<>();

	public MockTool( Resource resource ) {
		super( resource, "MockTool-" + counter.getAndIncrement() );
	}

	@Override
	public void allocate() {
		events.add( new MethodCall(  ALLOCATE ) );
	}

	@Override
	public void display() {
		events.add( new MethodCall(  DISPLAY ) );
	}

	@Override
	public void activate() {
		events.add( new MethodCall(  ACTIVATE ) );
	}

	@Override
	public void deactivate() {
		events.add( new MethodCall(  DEACTIVATE ) );
	}

	@Override
	public void conceal() {
		events.add( new MethodCall(  CONCEAL ) );
	}

	@Override
	public void deallocate() {
		events.add( new MethodCall(  DEALLOCATE ) );
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public List<MethodCall> getEvents() {
		return Collections.unmodifiableList( events );
	}

	public void listEvents() {
		for( MethodCall event : events ) {
			System.out.println( getTitle() + " " + event.name + "() called" );
		}
	}

	public static final class MethodCall {

		public final String name;

		public MethodCall(  String name ) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

}
