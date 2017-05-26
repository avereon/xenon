package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import javafx.geometry.Side;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkpaneTest extends WorkpaneTestCase {

	private Resource resource = new Resource( URI.create( "" ) );

	@Test
	public void testAddTool() throws Exception {
		MockTool tool = new MockTool( resource );

		workpane.addTool( tool );

		Workpane.ToolView view = tool.getToolView();
		assertThat( view.getEdge( Side.TOP ).isWall(), is( true ) );
	}

	@Test
	public void testAddRemoveToolEvents() {
		int index = 0;
		MockTool tool = new MockTool( resource );
		assertThat( tool.getEvents().size(), is( 0 ) );

		// Add the tool but do not set it active.
		workpane.addTool( tool, false );
		assertThat( tool.getEvents().size(), is( 2 ) );

		assertThat( tool, hasEvent( 14, ofType( MockTool.ALLOCATE ) ) );
		assertThat( tool, hasEvent( 1, ofType( MockTool.DISPLAY ) ) );

		//		assertToolEvent( index++, tool, MockTool.ALLOCATE );
		//		assertToolEvent( index++, tool, MockTool.DISPLAY );
		//
		//		// Remove the tool.
		//		workarea.removeTool( tool );
		//		assertEquals( 4, MockTool.events.size() );
		//		assertToolEvent( index++, tool, MockTool.CONCEAL );
		//		assertToolEvent( index++, tool, MockTool.DEALLOCATE );
	}

	//	// FIXME Turn this into matchers
	//	private void assertToolEvent( int index, Tool tool, String type ) {
	//		MockTool.ToolEvent datum = MockTool.events.get( index );
	//		assertEquals( "Tool mismatch", tool, datum.tool );
	//		assertEquals( "Event mismatch", type, datum.type );
	//	}

//	public static <T> Matcher<MockTool> hasEvent( int index ) {
//
//	}

//	public static <T> Matcher<MockTool.ToolEvent> ofType( String type ) {
//		return new FeatureMatcher<MockTool.ToolEvent, T>( null, "", "" ) {
//
//			@Override
//			@SuppressWarnings( "unchecked" )
//			protected T featureValueOf( MockTool.ToolEvent data ) {
//				return (T)data.type;
//			}
//
//		};
//	}

	public static Matcher<MockTool.ToolEvent> ofType( String pattern ) {
		return new CustomTypeSafeMatcher<>( "matching type " + pattern ) {

			@Override
			protected boolean matchesSafely( MockTool.ToolEvent item ) {
				return item != null && item.type.equals( pattern );
			}

		};
	}

	public static <T> Matcher<MockTool> hasEvent( int index, Matcher<T> valueMatcher ) {
		return new FeatureMatcher<>( valueMatcher, "Tool event", "\"event\"" ) {

			@Override
			@SuppressWarnings( "unchecked" )
			protected T featureValueOf( MockTool tool ) {
				try {
					return (T)tool.getEvents().get( index );
				} catch( ArrayIndexOutOfBoundsException exceptino ) {
					return null;
				}
			}

		};
	}

	//	/**
	//	 * @param valueMatcher like {@link org.hamcrest.Matchers#is(Object)}
	//	 * @param <T> type to cast payload as, probably String
	//	 * @return message matcher
	//	 */
	//	public static <T> Matcher<MuleMessage> hasMsgPayload( Matcher<T> valueMatcher ) {
	//		return new FeatureMatcher<MuleMessage, T>( valueMatcher, "Message payload", "\"payload\"" ) {
	//
	//			@Override
	//			@SuppressWarnings( "unchecked" )
	//			protected T featureValueOf( MuleMessage msg ) {
	//				return (T)msg.getPayload();
	//			}
	//		};
	//	}

	//	/**
	//	 * @param header outbound property to lookup
	//	 * @param valueMatcher like {@link org.hamcrest.Matchers#is(Object)}
	//	 * @param <T> type to cast header as, probably String
	//	 * @return message matcher
	//	 */
	//	public static <T> Matcher<MuleMessage> hasMsgHeader( String header, Matcher<T> valueMatcher ) {
	//		return new FeatureMatcher<MuleMessage, T>( valueMatcher, "Message header \"" + header + "\"", "\"" + header + "\"" ) {
	//
	//			@Override
	//			@SuppressWarnings( "unchecked" )
	//			protected T featureValueOf( MuleMessage msg ) {
	//				return (T)msg.getOutboundProperty( header );
	//			}
	//		};
	//	}

	//	/**
	//	 * @param header outbound property to lookup
	//	 * @return message matcher
	//	 */
	//	public static Matcher<MuleMessage> hasMsgHeader( String header ) {
	//		return hasMsgHeader( header, not( isEmptyOrNullString() ) );
	//	}

}
