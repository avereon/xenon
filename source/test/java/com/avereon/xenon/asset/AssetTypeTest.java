package com.avereon.xenon.asset;

import com.avereon.xenon.BaseXenonTestCase;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.mod.MockMod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetTypeTest extends BaseXenonTestCase {

	private XenonProgramProduct product;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		product = new MockMod();
	}

	@Test
	void testGetDefaultCodec() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getDefaultCodec() ).isNotNull();
	}

	@Test
	void testGetCodecs() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getCodecs() ).contains( type.getDefaultCodec() );
	}

	@Test
	void testGetName() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getName() ).isEqualTo( "Mock Asset (mock)" );
	}

	@Test
	void testAddCodec() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getCodecs().size() ).isEqualTo( 1 );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2" );
		type.addCodec( codec2 );

		assertThat( type.getCodecs() ).contains( codec1, codec2 );
	}

	@Test
	void testRemoveCodec() {
		AssetType type = new MockAssetType( product );
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
		AssetType type = new MockAssetType( product );
		assertThat( type.getSupportedCodecs( Codec.Pattern.URI, "mock:test" ).contains( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByScheme() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getSupportedCodecs( Codec.Pattern.SCHEME, "mock:test.mock" ).contains( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByMediaType() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getSupportedCodecs( Codec.Pattern.MEDIATYPE, "application/mock" ).contains( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByExtension() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getSupportedCodecs( Codec.Pattern.EXTENSION, "test.mock" ).contains( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByFileName() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getSupportedCodecs( Codec.Pattern.FILENAME, "test.mock" ).contains( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByFirstLine() {
		AssetType type = new MockAssetType( product );
		assertThat( type.getSupportedCodecs( Codec.Pattern.FIRSTLINE, "?mock" ).contains( type.getDefaultCodec() ) );
	}

}
