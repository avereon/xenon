package com.xeomar.xenon.update;

import com.xeomar.util.TextUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MarketCardTest {

	// Test that the catalog card can be unmarshalled
	@Test
	public void testLoadCard() throws Exception {
		InputStream input = getClass().getResourceAsStream( MarketCard.CARD );
		MarketCard card = MarketCard.loadCard( input );
		assertThat( card.getName(), is( "Xeomar" ) );
		assertThat( card.getIconUri(), is( "provider" ) );
		assertThat( card.getCardUri(), is( "https://xeomar.com/download" ) );

		Set<String> products = new HashSet<>();
//		products.add( "https://xeomar.com/download?artifact=arrow" );
		products.add( "https://xeomar.com/download?channel=latest&artifact=mouse&category=product&type=card" );
//		products.add( "https://xeomar.com/download?artifact=serra" );
//		products.add( "https://xeomar.com/download?artifact=marra" );
//		products.add( "https://xeomar.com/download?artifact=ocean" );
//		products.add( "https://xeomar.com/download?artifact=weave" );
//		products.add( "https://xeomar.com/download?artifact=arrow" );
		assertThat( card.getProducts(), containsInAnyOrder( products.toArray() ) );
	}

	@Test
	public void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Xeomar\", \"extra\" : \"unknown\"}";
		MarketCard card = MarketCard.loadCard( new ByteArrayInputStream( state.getBytes( TextUtil.CHARSET) ) );
		assertThat( card.getName(), CoreMatchers.is( "Xeomar" ) );
	}

}
