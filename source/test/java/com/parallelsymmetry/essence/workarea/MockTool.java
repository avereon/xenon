package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.worktool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MockTool extends Tool {

	private static Logger log = LoggerFactory.getLogger( MockTool.class );

	public static final String ALLOCATE = "allocate";

	public static final String DISPLAY = "display";

	public static final String ACTIVATE = "activate";

	public static final String DEACTIVATE = "deactivate";

	public static final String CONCEAL = "conceal";

	public static final String DEALLOCATE = "deallocate";

	private static final AtomicInteger counter = new AtomicInteger();

	private List<ToolEvent> events = new CopyOnWriteArrayList<ToolEvent>();

	public MockTool( Resource resource ) {
		super( resource, "MockTool " + counter.getAndIncrement() );
	}

	@Override
	public void allocate() {
		events.add( new ToolEvent( this, ALLOCATE ) );
	}

	@Override
	public void display() {
		events.add( new ToolEvent( this, DISPLAY ) );
	}

	@Override
	public void activate() {
		events.add( new ToolEvent( this, ACTIVATE ) );
	}

	@Override
	public void deactivate() {
		events.add( new ToolEvent( this, DEACTIVATE ) );
	}

	@Override
	public void conceal() {
		events.add( new ToolEvent( this, CONCEAL ) );
	}

	@Override
	public void deallocate() {
		events.add( new ToolEvent( this, DEALLOCATE ) );
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public void resetEvents() {
		events.clear();
	}

	public List<ToolEvent> getEvents() {
		return Collections.unmodifiableList( events );
	}

	public void listEvents() {
		for( ToolEvent event : events ) {
			log.info( event.tool.getTitle() + " " + event.type );
		}
	}

	public static final class ToolEvent {

		public final MockTool tool;

		public final String type;

		public ToolEvent( MockTool tool, String type ) {
			this.tool = tool;
			this.type = type;
		}

	}

}
