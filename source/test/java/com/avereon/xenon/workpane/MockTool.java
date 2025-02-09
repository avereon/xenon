package com.avereon.xenon.workpane;

import com.avereon.xenon.asset.Asset;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MockTool extends Tool {

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

	@Getter
	@Setter
	private String uid;

	private boolean canFindSelfFromWorkpane;

	private boolean canFindWorkpaneFromSelf;

	public MockTool( Asset asset ) {
		super( asset );
		setTitle( "MockTool-" + counter.getAndIncrement() );
		setBackground( new Background( new BackgroundFill( new Color( 0, 0.5, 1, 0.25 ), CornerRadii.EMPTY, Insets.EMPTY ) ) );
		setPlacement( super.getPlacement() );
	}

	void setWorkpane( Workpane workpane ) {
		this.workpane = workpane;
	}

	public boolean canFindSelfFromWorkpane() {
		return canFindSelfFromWorkpane;
	}

	public boolean canFindWorkpaneFromSelf() {
		return canFindWorkpaneFromSelf;
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

	void setPlacement( Workpane.Placement placement ) {
		this.placement = placement;
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
