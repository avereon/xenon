package com.xeomar.xenon.resource;

import com.xeomar.xenon.BaseTestCase;
import com.xeomar.product.Product;
import com.xeomar.xenon.mod.MockMod;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceTypeTest extends BaseTestCase {

	private Product product;

	@Override
	public void setup() throws Exception {
		product = new MockMod();
	}

	@Test
	public void testGetDefaultCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getDefaultCodec(), not( is( nullValue() ) ) );
	}

	@Test
	public void testGetCodecs() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecs(), containsInAnyOrder( type.getDefaultCodec() ) );
	}

	@Test
	public void testGetName() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getName(), is( "Mock Resource (mock)" ) );
	}

	@Test
	public void testAddCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecs().size(), is( 1 ) );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2");
		type.addCodec( codec2 );

		assertThat( type.getCodecs(), containsInAnyOrder( codec1, codec2 ) );
	}

	@Test
	public void testRemoveCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecs().size(), is( 1 ) );
		Codec codec1 = type.getDefaultCodec();

		MockCodec codec2 = new MockCodec( "2");
		type.addCodec( codec2 );
		assertThat( type.getCodecs(), containsInAnyOrder( codec1, codec2 ) );

		type.removeCodec( codec1 );
		assertThat( type.getCodecs(), containsInAnyOrder( codec2 ) );
		assertThat( type.getDefaultCodec(), is( nullValue() ) );
	}

	@Test
	public void testGetCodecByMediaType() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecByMediaType( "application/mock" ), is( type.getDefaultCodec() ) );
	}

	@Test
	public void testGetCodecByFileName() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecByFileName( "test.mock" ), is( type.getDefaultCodec() ) );
	}

	@Test
	public void testGetCodecByFirstLine() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getCodecByFirstLine( "?mock" ), is( type.getDefaultCodec() ) );
	}

}
