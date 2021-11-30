package com.avereon.xenon.notice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeTest {

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
		assertThat( notice.getThrowable() ).isEqualTo( throwable );
		assertThat( notice.getType() ).isEqualTo( Notice.Type.NORM );
	}

}
