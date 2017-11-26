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
		List<String> products = new ArrayList<>();
		products.add( "http://xeomar.com/download/xenon/product/card/{0}" );

		InputStream input = getClass().getResourceAsStream( CatalogCard.CARD );
		CatalogCard card = YmlUtil.load( input, CatalogCard.class );
		assertThat( card.getName(), is( "Xeomar" ) );
		assertThat( card.getIcon(), is( URI.create( "http://xeomar.com/download/image/xeomar.png" ) ) );
		assertThat( card.getProducts(), contains( products.toArray() ) );
	}

}
