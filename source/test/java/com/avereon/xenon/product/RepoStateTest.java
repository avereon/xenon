package com.avereon.xenon.product;

import com.avereon.util.TextUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepoStateTest {

	// Test that the catalog card can be unmarshalled
	@Test
	void testLoadCards() throws Exception {
		List<RepoState> cards = RepoState.forProduct( getClass() );
		assertThat( cards.size() ).isEqualTo( 2 );

		assertThat( cards.get( 0 ).getName() ).isEqualTo( "Avereon Official" );
		assertThat( cards.get( 0 ).getUrl() ).isEqualTo( "https://www.avereon.com/download/stable" );
		//assertThat( cards.get( 0 ).getIcon()).isEqualTo( "https://www.avereon.com/download/stable/avereon/provider/icon" ) ;
		assertThat( cards.get( 0 ).getIcons().get( 0 ) ).isEqualTo( "provider" );
		assertThat( cards.get( 0 ).getIcons().get( 1 ) ).isEqualTo( "https://www.avereon.com/download/stable/avereon/provider/icon" );
		assertThat( cards.get( 0 ).getIcons().size() ).isEqualTo( 2 );
		assertThat( cards.get( 0 ).isEnabled() ).isEqualTo( true );
		assertThat( cards.get( 0 ).isRemovable() ).isEqualTo( false );
		assertThat( cards.get( 0 ).getRank() ).isEqualTo( -2 );

		assertThat( cards.get( 1 ).getName() ).isEqualTo( "Avereon Latest" );
		assertThat( cards.get( 1 ).getUrl() ).isEqualTo( "https://www.avereon.com/download/latest" );
		//assertThat( cards.get( 1 ).getIcon()).isEqualTo( "https://www.avereon.com/download/stable/avereon/provider/icon" ) ;
		assertThat( cards.get( 1 ).getIcons().get( 0 ) ).isEqualTo( "provider" );
		assertThat( cards.get( 1 ).getIcons().get( 1 ) ).isEqualTo( "https://www.avereon.com/download/stable/avereon/provider/icon" );
		assertThat( cards.get( 1 ).getIcons().size() ).isEqualTo( 2 );
		assertThat( cards.get( 1 ).isEnabled() ).isEqualTo( false );
		assertThat( cards.get( 1 ).isRemovable() ).isEqualTo( false );
		assertThat( cards.get( 1 ).getRank() ).isEqualTo( -1 );
	}

	@Test
	void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "[{\"name\" : \"Avereon\", \"extra\" : \"unknown\"}]";
		List<RepoState> card = RepoState.loadCards( new ByteArrayInputStream( state.getBytes( TextUtil.CHARSET ) ) );
		assertThat( card.get( 0 ).getName() ).isEqualTo( "Avereon" );
	}

}
