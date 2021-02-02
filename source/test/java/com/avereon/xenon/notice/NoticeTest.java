package com.avereon.xenon.notice;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NoticeTest {

	@Test
	void testConstructor() {
		Notice notice = new Notice( "title", "message" );
		assertThat( notice.getTitle(), is( "title" ) );
		assertThat( notice.getMessage(), is( "message" ) );
		assertThat( notice.getType(), is( Notice.Type.NORM ) );
	}

	@Test
	void testConstructorWithThrowable() {
		Throwable throwable = new Throwable();
		Notice notice = new Notice( "title", "message", throwable );
		assertThat( notice.getTitle(), is( "title" ) );
		assertThat( notice.getMessage(), is( "message" ) );
		assertThat( notice.getThrowable(), is( throwable ) );
		assertThat( notice.getType(), is( Notice.Type.NORM ) );
	}

}
