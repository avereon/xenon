package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import javafx.geometry.Side;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WorkpaneTest extends WorkpaneTestCase {

	private Resource resource = new Resource( URI.create( "" ) );

	@Test
	public void testAddTool() throws Exception {
		MockTool tool = new MockTool( resource );

		workpane.addTool( tool );

		ToolView view = tool.getToolView();
		assertThat( view.getEdge( Side.TOP ).isWall(), is( true ) );
	}

	@Test
	public void testAddRemoveToolEvents() {
		int index = 0;
		MockTool tool = new MockTool( resource );
		assertThat( tool.getEvents().size(), is( 0 ) );

		// Add the tool but do not set it active.
		workpane.addTool( tool, false );
		//assertThat( tool.getEvents().size(), is( 2 ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.ALLOCATE ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.DISPLAY ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.ACTIVATE ) ) );

		// Remove the tool.
		workpane.removeTool( tool );
		tool.listEvents();
		//assertThat( tool.getEvents().size(), is( 4 ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.DEACTIVATE ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.CONCEAL ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.DEALLOCATE ) ) );
	}

	@Test
	public void testAddSelectRemoveToolEvents() {
		int index = 0;
		MockTool tool = new MockTool( resource );
		assertThat( tool.getEvents().size(), is( 0 ) );
		assertThat( workpane.getActiveTool(), is( nullValue() ) );

		// Add the tool and set it active.
		workpane.addTool( tool, true );
		tool.listEvents();
		assertThat( tool.getEvents().size(), is( 3 ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.ALLOCATE ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.DISPLAY ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.ACTIVATE ) ) );

		workpane.removeTool( tool );
		tool.listEvents();
		assertThat( tool.getEvents().size(), is( 6 ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.DEACTIVATE ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.CONCEAL ) ) );
		assertThat( tool, hasEvent( index++, ofType( MockTool.DEALLOCATE ) ) );
	}

	public static Matcher<MockTool> hasEvent( int index ) {
		return hasEvent( index, not( nullValue() ) );
	}

	public static <T> Matcher<MockTool> hasEvent( int index, Matcher<T> valueMatcher ) {
		return new FeatureMatcher<>( valueMatcher, "Tool method call", "method call" ) {

			@Override
			@SuppressWarnings( "unchecked" )
			protected T featureValueOf( MockTool tool ) {
				try {
					return (T)tool.getEvents().get( index );
				} catch( ArrayIndexOutOfBoundsException exception ) {
					return null;
				}
			}

		};
	}

	public static Matcher<MockTool.MethodCall> ofType( String name ) {
		return new CustomTypeSafeMatcher<>( "matching " + name ) {

			@Override
			protected boolean matchesSafely( MockTool.MethodCall event ) {
				return event != null && event.name.equals( name );
			}

		};
	}

}
