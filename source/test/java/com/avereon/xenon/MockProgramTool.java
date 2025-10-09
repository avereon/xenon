package com.avereon.xenon;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.workpane.Workpane;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class MockProgramTool extends ProgramTool {

	static final String ALLOCATE = "allocate";

	static final String DISPLAY = "display";

	static final String ACTIVATE = "activate";

	static final String DEACTIVATE = "deactivate";

	static final String CONCEAL = "conceal";

	static final String DEALLOCATE = "deallocate";

	private static final AtomicInteger counter = new AtomicInteger();

	private Workpane.Placement placement;

	private final List<MethodCall> events = new CopyOnWriteArrayList<>();

	private int eventIndex;

	private Workpane workpane;

	private String uid;

	private boolean canFindSelfFromWorkpane;

	private boolean canFindWorkpaneFromSelf;

	private Set<URI> assetDependencies = new CopyOnWriteArraySet<>();

	public MockProgramTool( XenonProgramProduct product, Resource resource ) {
		super( product, resource );
	}

	@Override
	public void allocate() {
		events.add( new MethodCall( ALLOCATE ) );
		canFindSelfFromWorkpane = workpane != null && workpane.getTools().contains( this );
		canFindWorkpaneFromSelf = getWorkpane() == workpane;
	}

	@Override
	public void display() {
		events.add( new MethodCall( DISPLAY ) );
	}

	@Override
	public void activate() {
		events.add( new MethodCall( ACTIVATE ) );
	}

	@Override
	public void deactivate() {
		events.add( new MethodCall( DEACTIVATE ) );
	}

	@Override
	public void conceal() {
		events.add( new MethodCall( CONCEAL ) );
	}

	@Override
	public void deallocate() {
		events.add( new MethodCall( DEALLOCATE ) );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return placement;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public List<MethodCall> getEvents() {
		return Collections.unmodifiableList( events );
	}

	MethodCall getNextEvent() {
		return events.get( eventIndex++ );
	}

	@SuppressWarnings( "unused" )
	public void listEvents() {
		for( MethodCall event : events ) {
			System.out.println( getTitle() + " " + event.method + "() called" );
		}
	}

	public static final class MethodCall {

		final String method;

		MethodCall( String method ) {
			this.method = method;
		}

		@Override
		public String toString() {
			return method;
		}

	}

}
