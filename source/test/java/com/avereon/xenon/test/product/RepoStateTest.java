package com.avereon.xenon.test.product;

import com.avereon.util.TextUtil;
import com.avereon.xenon.product.RepoState;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RepoStateTest {

	// Test that the catalog card can be unmarshalled
	@Test
	void testLoadCards() throws Exception {
		List<RepoState> cards = RepoState.forProduct( getClass() );
		assertThat( cards.size(), is( 2 ) );

		assertThat( cards.get( 0 ).getName(), is( "Avereon Official" ) );
		assertThat( cards.get( 0 ).getUrl(), is( "https://www.avereon.com/download/stable" ) );
		//assertThat( cards.get( 0 ).getIcon(), is( "https://www.avereon.com/download/stable/avereon/provider/icon" ) );
		assertThat( cards.get( 0 ).getIcons().get( 0 ), is( "provider" ) );
		assertThat( cards.get( 0 ).getIcons().get( 1 ), is( "https://www.avereon.com/download/stable/avereon/provider/icon" ) );
		assertThat( cards.get( 0 ).getIcons().size(), is( 2 ) );
		assertThat( cards.get( 0 ).isEnabled(), is( true ) );
		assertThat( cards.get( 0 ).isRemovable(), is( false ) );
		assertThat( cards.get( 0 ).getRank(), is( -2 ) );

		assertThat( cards.get( 1 ).getName(), is( "Avereon Nightly" ) );
		assertThat( cards.get( 1 ).getUrl(), is( "https://www.avereon.com/download/latest" ) );
		//assertThat( cards.get( 1 ).getIcon(), is( "https://www.avereon.com/download/stable/avereon/provider/icon" ) );
		assertThat( cards.get( 1 ).getIcons().get( 0 ), is( "provider" ) );
		assertThat( cards.get( 1 ).getIcons().get( 1 ), is( "https://www.avereon.com/download/stable/avereon/provider/icon" ) );
		assertThat( cards.get( 1 ).getIcons().size(), is( 2 ) );
		assertThat( cards.get( 1 ).isEnabled(), is( false ) );
		assertThat( cards.get( 1 ).isRemovable(), is( false ) );
		assertThat( cards.get( 1 ).getRank(), is( -1 ) );
	}

	@Test
	void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "[{\"name\" : \"Avereon\", \"extra\" : \"unknown\"}]";
		List<RepoState> card = RepoState.loadCards( new ByteArrayInputStream( state.getBytes( TextUtil.CHARSET ) ) );
		assertThat( card.get( 0 ).getName(), CoreMatchers.is( "Avereon" ) );
	}

}
