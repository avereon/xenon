package com.xeomar.xenon.update;

import com.xeomar.util.YmlUtil;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MarketCardTest {

	// Test that the catalog card can be de-serialized
	@Test
	public void testReadCardFromYaml() throws Exception {
		InputStream input = getClass().getResourceAsStream( MarketCard.CARD );
		MarketCard card = YmlUtil.load( input, MarketCard.class );
		assertThat( card.getName(), is( "Xeomar" ) );
		assertThat( card.getIconUri(), is( "http://xeomar.com/download/image/xeomar.png" ) );
		assertThat( card.getCardUri(), is( "http://xeomar.com/download/{0}/catalog/card/{1}" ) );

		List<String> products = new ArrayList<>();
		products.add( "http://xeomar.com/download/{0}/product/card/{1}" );
		assertThat( card.getProducts(), contains( products.toArray() ) );
	}

}
