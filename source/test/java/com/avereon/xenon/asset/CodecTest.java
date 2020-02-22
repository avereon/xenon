package com.avereon.xenon.asset;

import com.avereon.xenon.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CodecTest extends BaseTestCase {

	private Codec codec;

	@BeforeEach
	void setUp() {
		codec = new MockCodec();
	}

	@Test
	void testGetSupportedFileNames() {
		// This should already have one for the extension
		assertThat( codec.getSupportedFileNames().size(), is( 1 ) );
		codec.addSupportedFileName( "test.mock" );
		assertThat( codec.getSupportedFileNames().size(), is( 2 ) );
	}

	@Test
	void testGetSupportedFirstLines() {
		assertThat( codec.getSupportedFirstLines().size(), is( 1 ) );
		codec.addSupportedFirstLine( "#!/bin/mock" );
		assertThat( codec.getSupportedFirstLines().size(), is( 2 ) );
	}

	@Test
	void testGetSupportedContentTypes() {
		assertThat( codec.getSupportedMediaTypes().size(), is( 1 ) );
		codec.addSupportedMediaType( "text/mock" );
		assertThat( codec.getSupportedMediaTypes().size(), is( 2 ) );
	}

	@Test
	void testIsSupportedExtension() {
		// Edge tests.
		assertThat( codec.isSupportedExtension( null ), is( false ) );
		assertThat( codec.isSupportedExtension( "" ), is( false ) );

		// Extension was already added
		codec.addSupportedExtension( "special" );

		// Positive test.
		assertThat( codec.isSupportedExtension( "test." + MockCodec.EXTENSION ), is( true ) );
		assertThat( codec.isSupportedExtension( "mock.special" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupportedExtension( "test.x" + MockCodec.EXTENSION ), is( false ) );
		assertThat( codec.isSupportedExtension( "mock.unsupported" ), is( false ) );
	}

	@Test
	void testIsSupportedFileName() {
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
	void testIsSupportedFirstLine() {
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
	void testIsSupportedContentType() {
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
