package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.BaseTestCase;
import com.parallelsymmetry.essence.product.Product;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceTypeTest extends BaseTestCase {

	private Product product;

	@Override
	public void setup() {
		product = new MockModule( null, null );
	}

	@Test
	public void testGetDefaultCodec() {
		ResourceType type = new MockResourceType( product );
		assertThat( type.getDefaultCodec(), not( is( nullValue() ) ) );
	}

	@Test
	public void testGetCodecs() {
		ResourceType type = new MockResourceType( product );

		assertThat( type.getCodecs().contains( product ), is( false ) );
		assertThat( type.getCodecs().contains( type.getDefaultCodec() ), is( true ) );
	}

	// NEXT Should there be more unit tests for ResourceType

	// Test get name
	// Test add codec
	// Test remove codec
	// Test getting a coded based on a resource...?

}
