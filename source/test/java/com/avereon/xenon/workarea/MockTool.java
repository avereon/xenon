package com.avereon.xenon.workarea;

import com.avereon.xenon.resource.Resource;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

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

	private Workpane.Placement placement;

	private List<MethodCall> events = new CopyOnWriteArrayList<>();

	private int eventIndex;

	public MockTool( Resource resource ) {
		super( resource, "MockTool-" + counter.getAndIncrement() );
		setPlacement( super.getPlacement() );
		setBackground( new Background( new BackgroundFill( new Color( 0,0.5,1,0.25), CornerRadii.EMPTY, Insets.EMPTY ) ) );
	}

	@Override
	public void allocate() {
		events.add( new MethodCall( ALLOCATE ) );
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

	public void setPlacement( Workpane.Placement placement) {
		this.placement = placement;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public List<MethodCall> getEvents() {
		return Collections.unmodifiableList( events );
	}

	public MethodCall getNextEvent() {
		return events.get( eventIndex++ );
	}

	public void listEvents() {
		for( MethodCall event : events ) {
			System.out.println( getTitle() + " " + event.method + "() called" );
		}
	}

	public static final class MethodCall {

		public final String method;

		public MethodCall( String method ) {
			this.method = method;
		}

		@Override
		public String toString() {
			return method;
		}

	}

}
