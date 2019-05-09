package com.xeomar.xenon.update;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class RepoCardTest {

	@Test
	public void testLoadCards() throws Exception {
		List<RepoCard> cards = RepoCard.forProduct();
		assertThat( cards.size(), is( 2 ) );

		assertThat( cards.get( 0 ).getName(), is( "Xeomar Official" ) );
		assertThat( cards.get( 0 ).getRepo(), is( "https://xeomar.com/download/stable" ) );
		assertThat( cards.get( 0 ).getIcon(), is( "provider" ) );
		assertThat( cards.get( 0 ).isEnabled(), is( true ) );
		assertThat( cards.get( 0 ).isRemovable(), is( false ) );

		assertThat( cards.get( 1 ).getName(), is( "Xeomar Nightly" ) );
		assertThat( cards.get( 1 ).getRepo(), is( "https://xeomar.com/download/latest" ) );
		assertThat( cards.get( 1 ).getIcon(), is( "provider" ) );
		assertThat( cards.get( 1 ).isEnabled(), is( false ) );
		assertThat( cards.get( 1 ).isRemovable(), is( true ) );
	}

}
