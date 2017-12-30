package com.xeomar.xenon.update;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MarketCardTest {

	// Test that the catalog card can be unmarshalled
	@Test
	public void testLoadCard() throws Exception {
		InputStream input = getClass().getResourceAsStream( MarketCard.CARD );
		MarketCard card = MarketCard.loadCard( input );
		assertThat( card.getName(), is( "Xeomar" ) );
		assertThat( card.getIconUri(), is( "http://xeomar.com/images/icons/xeomar.png" ) );
		assertThat( card.getCardUri(), is( "http://xeomar.com/download/xenon/catalog/card/{0}" ) );

		List<String> products = new ArrayList<>();
		products.add( "http://xeomar.com/download/xenon/product/card/{0}" );
		assertThat( card.getProducts(), contains( products.toArray() ) );
	}

	@Test
	public void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Xeomar\", \"extra\" : \"unknown\"}";
		MarketCard card = MarketCard.loadCard( new ByteArrayInputStream( state.getBytes( "UTF-8" ) ) );
		assertThat( card.getName(), CoreMatchers.is( "Xeomar" ) );
	}

}
