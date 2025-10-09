package com.avereon.xenon.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceTypeTest extends BaseResourceTestCase {

	@Test
	void testGetDefaultCodec() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getDefaultCodec() ).isNotNull();
	}

	@Test
	void testGetCodecs() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getCodecs() ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetName() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getName() ).isEqualTo( "Mock Asset (mock)" );
	}

	@Test
	void testAddCodec() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getCodecs().size() ).isEqualTo( 1 );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2" );
		type.addCodec( codec2 );

		assertThat( type.getCodecs() ).contains( codec1, codec2 );
	}

	@Test
	void testRemoveCodec() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getCodecs().size() ).isEqualTo( 1 );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2" );
		type.addCodec( codec2 );
		assertThat( type.getCodecs() ).contains( codec1, codec2 );

		type.removeCodec( codec1 );
		assertThat( type.getCodecs() ).contains( codec2 );
		assertThat( type.getDefaultCodec() ).isNull();
	}

	@Test
	void testGetCodecByUri() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getSupportedCodecs( Codec.Pattern.URI, "mock:test" ) ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetCodecByScheme() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getSupportedCodecs( Codec.Pattern.SCHEME, "mock:test.mock" ) ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetCodecByMediaType() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getSupportedCodecs( Codec.Pattern.MEDIATYPE, "application/mock" ) ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetCodecByExtension() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getSupportedCodecs( Codec.Pattern.EXTENSION, "test.mock" ) ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetCodecByFileName() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getSupportedCodecs( Codec.Pattern.FILENAME, "test.mock" ) ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetCodecByFirstLine() {
		ResourceType type = new MockResourceType( getProduct() );
		assertThat( type.getSupportedCodecs( Codec.Pattern.FIRSTLINE, "?mock" ) ).contains( type.getDefaultCodec() );
	}

}
