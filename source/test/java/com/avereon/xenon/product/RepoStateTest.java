package com.avereon.xenon.product;

import com.avereon.util.TextUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RepoStateTest {

	// Test that the catalog card can be unmarshalled
	@Test
	public void testLoadCards() throws Exception {
		List<RepoState> cards = RepoState.forProduct( getClass() );
		assertThat( cards.size(), is( 2 ) );

		assertThat( cards.get( 0 ).getName(), is( "Avereon Official" ) );
		assertThat( cards.get( 0 ).getUrl(), is( "https://www.avereon.com/download/stable" ) );
		assertThat( cards.get( 0 ).getIcon(), is( "https://www.avereon.com/download/stable/avereon/provider/icon" ) );
		assertThat( cards.get( 0 ).isEnabled(), is( true ) );
		assertThat( cards.get( 0 ).isRemovable(), is( false ) );
		assertThat( cards.get( 0 ).getRank(), is( -2 ) );

		assertThat( cards.get( 1 ).getName(), is( "Avereon Nightly" ) );
		assertThat( cards.get( 1 ).getUrl(), is( "https://www.avereon.com/download/latest" ) );
		assertThat( cards.get( 1 ).getIcon(), is( "https://www.avereon.com/download/stable/avereon/provider/icon" ) );
		assertThat( cards.get( 1 ).isEnabled(), is( false ) );
		assertThat( cards.get( 1 ).isRemovable(), is( false ) );
		assertThat( cards.get( 1 ).getRank(), is( -1 ) );
	}

	@Test
	public void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "[{\"name\" : \"Avereon\", \"extra\" : \"unknown\"}]";
		List<RepoState> card = RepoState.loadCards( new ByteArrayInputStream( state.getBytes( TextUtil.CHARSET) ) );
		assertThat( card.get(0).getName(), CoreMatchers.is( "Avereon" ) );
	}

}
