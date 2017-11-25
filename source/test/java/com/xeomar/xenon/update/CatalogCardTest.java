package com.xeomar.xenon.update;

import com.xeomar.util.YmlUtil;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CatalogCardTest {

	// Test that the catalog card can be de-serialized
	@Test
	public void testReadCardFromYaml() throws Exception {
		List<URI> products = new ArrayList<>();
		products.add( URI.create( "http://xeomar.com/download/product/xenon/card" ) );

		InputStream input = getClass().getResourceAsStream( CatalogCard.CARD );
		CatalogCard card = YmlUtil.load( input, CatalogCard.class );
		assertThat( card.getName(), is( "Xeomar" ) );
		assertThat( card.getIcon(), is( URI.create( "http://xeomar.com/download/image/xeomar.png" ) ) );
		assertThat( card.getProducts(), contains( products.toArray() ) );
	}

}
