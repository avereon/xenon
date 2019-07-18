package com.avereon.xenon.resource;

import com.avereon.xenon.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CodecTest extends BaseTestCase {

	private Codec codec;

	@Before
	public void setUp() throws Exception {
		codec = new MockCodec();
	}

	@Test
	public void testGetSupportedFileNames() {
		// This should already have one for the extension
		assertThat( codec.getSupportedFileNames().size(), is( 1 ) );
		codec.addSupportedFileName( "test.mock" );
		assertThat( codec.getSupportedFileNames().size(), is( 2 ) );
	}

	@Test
	public void testGetSupportedFirstLines() {
		assertThat( codec.getSupportedFirstLines().size(), is( 1 ) );
		codec.addSupportedFirstLine( "#!/bin/mock" );
		assertThat( codec.getSupportedFirstLines().size(), is( 2 ) );
	}

	@Test
	public void testGetSupportedContentTypes() {
		assertThat( codec.getSupportedMediaTypes().size(), is( 1 ) );
		codec.addSupportedMediaType( "text/mock" );
		assertThat( codec.getSupportedMediaTypes().size(), is( 2 ) );
	}

	@Test
	public void testIsSupportedFileName() {
		// Edge tests.
		assertThat( codec.isSupportedFileName( null ), is( false ) );
		assertThat( codec.isSupportedFileName( "" ), is( false ) );

		// Extension was already added
		codec.addSupportedFileName( "mock.special" );

		// Positive test.
		assertThat( codec.isSupportedFileName( "test." + MockCodec.EXTENSION ), is( true ) );
		assertThat( codec.isSupportedFileName( "mock.special" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupportedFileName( "test.x" + MockCodec.EXTENSION ), is( false ) );
		assertThat( codec.isSupportedFileName( "mock.unsupported" ), is( false ) );
	}

	@Test
	public void testIsSupportedFirstLine() {
		// Edge tests.
		assertThat( codec.isSupportedFirstLine( null ), is( false ) );
		assertThat( codec.isSupportedFirstLine( "" ), is( false ) );

		codec.addSupportedFirstLine( "#!/bin/mock" );

		// Positive test.
		assertThat( codec.isSupportedFirstLine( "#!/bin/mock" ), is( true ) );
		assertThat( codec.isSupportedFirstLine( "#!/bin/mock // With a comment" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupportedFirstLine( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ), is( false ) );
	}

	@Test
	public void testIsSupportedContentType() {
		// Edge tests.
		assertThat( codec.isSupportedMediaType( null ), is( false ) );
		assertThat( codec.isSupportedMediaType( "" ), is( false ) );

		codec.addSupportedMediaType( "text/mock" );

		// Positive test.
		assertThat( codec.isSupportedMediaType( "text/mock" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupportedMediaType( "text/notsupported" ), is( false ) );
	}

}
