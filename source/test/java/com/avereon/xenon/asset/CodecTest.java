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
	void testGetSupportedUris() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.URI ).size(), is( 1 ) );
		codec.addSupported( Codec.Pattern.URI, "mock:testuri.mock" );
		assertThat( codec.getSupported( Codec.Pattern.URI ).size(), is( 2 ) );
	}

	@Test
	void testGetSupportedSchemes() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.SCHEME ).size(), is( 1 ) );
		codec.addSupported( Codec.Pattern.SCHEME, "mocktest" );
		assertThat( codec.getSupported( Codec.Pattern.SCHEME ).size(), is( 2 ) );
	}

	@Test
	void testGetSupportedExtensions() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.EXTENSION ).size(), is( 1 ) );
		codec.addSupported( Codec.Pattern.EXTENSION, "mockext" );
		assertThat( codec.getSupported( Codec.Pattern.EXTENSION ).size(), is( 2 ) );
	}

	@Test
	void testGetSupportedFileNames() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.FILENAME ).size(), is( 1 ) );
		codec.addSupported( Codec.Pattern.FILENAME, "test.mock" );
		assertThat( codec.getSupported( Codec.Pattern.FILENAME ).size(), is( 2 ) );
	}

	@Test
	void testGetSupportedFirstLines() {
		assertThat( codec.getSupported( Codec.Pattern.FIRSTLINE ).size(), is( 1 ) );
		codec.addSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock" );
		assertThat( codec.getSupported( Codec.Pattern.FIRSTLINE ).size(), is( 2 ) );
	}

	@Test
	void testGetSupportedMediaTypes() {
		assertThat( codec.getSupported( Codec.Pattern.MEDIATYPE ).size(), is( 1 ) );
		codec.addSupported(Codec.Pattern.MEDIATYPE, "text/mock" );
		assertThat( codec.getSupported( Codec.Pattern.MEDIATYPE ).size(), is( 2 ) );
	}

	@Test
	void testIsSupportedUri() {
		// Edge tests.
		assertThat( codec.isSupported( Codec.Pattern.URI, null ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.URI, "" ), is( false ) );

		// Extension was already added
		codec.addSupported( Codec.Pattern.URI, "mock:special" );

		// Positive test.
		assertThat( codec.isSupported( Codec.Pattern.URI, "mock:special" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupported( Codec.Pattern.URI, "mock:unsupported" ), is( false ) );
	}

	@Test
	void testIsSupportedScheme() {
		// Edge tests.
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, null ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, "" ), is( false ) );

		// Extension was already added
		codec.addSupported( Codec.Pattern.SCHEME, "mock" );

		// Positive test.
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, "mock:special" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, "special:mock" ), is( false ) );
	}

	@Test
	void testIsSupportedExtension() {
		// Edge tests.
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, null ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "" ), is( false ) );

		// Extension was already added
		codec.addSupported( Codec.Pattern.EXTENSION, "special" );

		// Positive test.
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "test." + MockCodec.EXTENSION ), is( true ) );
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "mock.special" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "test.x" + MockCodec.EXTENSION ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "mock.unsupported" ), is( false ) );
	}

	@Test
	void testIsSupportedFileName() {
		// Edge tests.
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, null ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "" ), is( false ) );

		// Extension was already added
		codec.addSupported( Codec.Pattern.FILENAME, "mock.special" );

		// Positive test.
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "test." + MockCodec.EXTENSION ), is( true ) );
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "mock.special" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "test.x" + MockCodec.EXTENSION ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "mock.unsupported" ), is( false ) );
	}

	@Test
	void testIsSupportedFirstLine() {
		// Edge tests.
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, null ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "" ), is( false ) );

		codec.addSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock" );

		// Positive test.
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock" ), is( true ) );
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock // With a comment" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ), is( false ) );
	}

	@Test
	void testIsSupportedMediaType() {
		// Edge tests.
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, null ), is( false ) );
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, "" ), is( false ) );

		codec.addSupported( Codec.Pattern.MEDIATYPE, "text/mock" );

		// Positive test.
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, "text/mock" ), is( true ) );

		// Negative test.
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, "text/notsupported" ), is( false ) );
	}

}
