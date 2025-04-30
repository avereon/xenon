package com.avereon.xenon.notice;

import com.avereon.zarra.javafx.Fx;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeTest {

	@BeforeAll
	static void beforeAll() {
		Fx.startup();
	}

	@Test
	void testConstructor() {
		Notice notice = new Notice( "title", "message" );
		assertThat( notice.getTitle() ).isEqualTo( "title" );
		assertThat( notice.getMessage() ).isEqualTo( "message" );
		assertThat( notice.getType() ).isEqualTo( Notice.Type.NORM );
	}

	@Test
	void testConstructorWithThrowable() {
		Throwable throwable = new Throwable();
		Notice notice = new Notice( "title", "message", throwable );
		assertThat( notice.getTitle() ).isEqualTo( "title" );
		assertThat( notice.getMessage() ).isEqualTo( "message" );
		assertThat( notice.getCause() ).isEqualTo( throwable );
		assertThat( notice.getType() ).isEqualTo( Notice.Type.NORM );
	}

	@Test
	void getMessageStringContent() {
		Notice notice = new Notice( "title", "message" );

		// Test with a plain string message
		String result = notice.getMessageStringContent( "Test message" );
		assertThat( result ).isEqualTo( "Test message" );

		// Test with a MessageFormat message
		result = notice.getMessageStringContent( "MockGuidedTool{ id=\"null\" title=\"mock\" }" );
		assertThat( result ).isEqualTo( "MockGuidedTool{ id=\"null\" title=\"mock\" }" );

		// Test with a null message
		result = notice.getMessageStringContent( null );
		assertThat( result ).isEqualTo( "null" );

		// Test with a TextInputControl message
		TextInputControl textInputControl = new javafx.scene.control.TextField( "Input message" );
		result = notice.getMessageStringContent( textInputControl );
		assertThat( result ).isEqualTo( "Input message" );

		// Test with a TextFlow message
		TextFlow textFlow = new TextFlow( new Text( "Part 1 " ), new Text( "Part 2" ) );
		result = notice.getMessageStringContent( textFlow );
		assertThat( result ).isEqualTo( "Part 1 Part 2" );

		// Test with a generic Text message
		Text node = new Text( "Node message" );
		result = notice.getMessageStringContent( node );
		assertThat( result ).isEqualTo( "Node message" );
	}

}
