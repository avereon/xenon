package com.avereon.xenon.resource;

import com.avereon.product.Product;
import com.avereon.xenon.BaseTestCase;
import com.avereon.xenon.mod.MockMod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.nullValue;

public class ResourceTypeTest extends BaseTestCase {

	private Product product;

	@BeforeEach
	@Override
	public void setup() throws Exception {
		product = new MockMod();
	}

	@Test
	void testGetDefaultCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getDefaultCodec(), not( is( nullValue() ) ) );
	}

	@Test
	void testGetCodecs() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecs(), containsInAnyOrder( type.getDefaultCodec() ) );
	}

	@Test
	void testGetName() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getName(), is( "Mock Resource (mock)" ) );
	}

	@Test
	void testAddCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecs().size(), is( 1 ) );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2" );
		type.addCodec( codec2 );

		assertThat( type.getCodecs(), containsInAnyOrder( codec1, codec2 ) );
	}

	@Test
	void testRemoveCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecs().size(), is( 1 ) );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2" );
		type.addCodec( codec2 );
		assertThat( type.getCodecs(), containsInAnyOrder( codec1, codec2 ) );

		type.removeCodec( codec1 );
		assertThat( type.getCodecs(), containsInAnyOrder( codec2 ) );
		assertThat( type.getDefaultCodec(), is( nullValue() ) );
	}

	@Test
	void testGetCodecByMediaType() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecByMediaType( "application/mock" ), is( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByFileName() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecByFileName( "test.mock" ), is( type.getDefaultCodec() ) );
	}

	@Test
	void testGetCodecByFirstLine() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecByFirstLine( "?mock" ), is( type.getDefaultCodec() ) );
	}

}
