package com.avereon.xenon.asset;

import com.avereon.xenon.BasePartXenonTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodecTest extends BasePartXenonTestCase {

	private Codec codec;

	@BeforeEach
	void setUp() {
		codec = new MockCodec();
	}

	@Test
	void testGetSupportedUris() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.URI ).size() ).isEqualTo( 2 );
		codec.addSupported( Codec.Pattern.URI, "mock:testuri.mock" );
		assertThat( codec.getSupported( Codec.Pattern.URI ).size() ).isEqualTo( 3 );
	}

	@Test
	void testGetSupportedSchemes() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.SCHEME ).size() ).isEqualTo( 1 );
		codec.addSupported( Codec.Pattern.SCHEME, "mocktest" );
		assertThat( codec.getSupported( Codec.Pattern.SCHEME ).size() ).isEqualTo( 2 );
	}

	@Test
	void testGetSupportedExtensions() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.EXTENSION ).size() ).isEqualTo( 1 );
		codec.addSupported( Codec.Pattern.EXTENSION, "mockext" );
		assertThat( codec.getSupported( Codec.Pattern.EXTENSION ).size() ).isEqualTo( 2 );
	}

	@Test
	void testGetSupportedFileNames() {
		// This should already have one for the extension
		assertThat( codec.getSupported( Codec.Pattern.FILENAME ).size() ).isEqualTo( 1 );
		codec.addSupported( Codec.Pattern.FILENAME, "test.mock" );
		assertThat( codec.getSupported( Codec.Pattern.FILENAME ).size() ).isEqualTo( 2 );
	}

	@Test
	void testGetSupportedFirstLines() {
		assertThat( codec.getSupported( Codec.Pattern.FIRSTLINE ).size() ).isEqualTo( 1 );
		codec.addSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock" );
		assertThat( codec.getSupported( Codec.Pattern.FIRSTLINE ).size() ).isEqualTo( 2 );
	}

	@Test
	void testGetSupportedMediaTypes() {
		assertThat( codec.getSupported( Codec.Pattern.MEDIATYPE ).size() ).isEqualTo( 1 );
		codec.addSupported( Codec.Pattern.MEDIATYPE, "text/mock" );
		assertThat( codec.getSupported( Codec.Pattern.MEDIATYPE ).size() ).isEqualTo( 2 );
	}

	@Test
	void testIsSupportedUri() {
		// Edge tests
		assertThat( codec.isSupported( Codec.Pattern.URI, null ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.URI, "" ) ).isFalse();

		// Pattern was already added
		codec.addSupported( Codec.Pattern.URI, "mock:special" );

		// Positive test
		assertThat( codec.isSupported( Codec.Pattern.URI, "mock:special" ) ).isTrue();

		// Negative test
		assertThat( codec.isSupported( Codec.Pattern.URI, "mock:unsupported" ) ).isFalse();
	}

	@Test
	void testIsSupportedScheme() {
		// Edge tests
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, null ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, "" ) ).isFalse();

		// Pattern was already added
		codec.addSupported( Codec.Pattern.SCHEME, "mock" );

		// Positive test
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, "mock:special" ) ).isTrue();

		// Negative test
		assertThat( codec.isSupported( Codec.Pattern.SCHEME, "special:mock" ) ).isFalse();
	}

	@Test
	void testIsSupportedExtension() {
		// Edge tests
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, null ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "" ) ).isFalse();

		// Add pattern
		codec.addSupported( Codec.Pattern.EXTENSION, "special" );

		// Positive test
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "test." + MockCodec.EXTENSION ) ).isTrue();
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "mock.special" ) ).isTrue();

		// Negative test
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "test.x" + MockCodec.EXTENSION ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.EXTENSION, "mock.unsupported" ) ).isFalse();
	}

	@Test
	void testIsSupportedFileName() {
		// Edge tests
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, null ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "" ) ).isFalse();

		// Pattern was already added
		codec.addSupported( Codec.Pattern.FILENAME, "mock.special" );

		// Positive test
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "test." + MockCodec.EXTENSION ) ).isTrue();
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "mock.special" ) ).isTrue();

		// Negative test
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "test.x" + MockCodec.EXTENSION ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.FILENAME, "mock.unsupported" ) ).isFalse();
	}

	@Test
	void testIsSupportedFirstLine() {
		// Edge tests
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, null ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "" ) ).isFalse();

		// Pattern was already added
		codec.addSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock" );

		// Positive test
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock" ) ).isTrue();
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "#!/bin/mock // With a comment" ) ).isTrue();

		// Negative test
		assertThat( codec.isSupported( Codec.Pattern.FIRSTLINE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ) ).isFalse();
	}

	@Test
	void testIsSupportedMediaType() {
		// Edge tests
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, null ) ).isFalse();
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, "" ) ).isFalse();

		codec.addSupported( Codec.Pattern.MEDIATYPE, "text/mock" );

		// Positive test
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, "text/mock" ) ).isTrue();

		// Negative test
		assertThat( codec.isSupported( Codec.Pattern.MEDIATYPE, "text/notsupported" ) ).isFalse();
	}

}
