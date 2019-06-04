package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.product.RepoCard;
import com.xeomar.xenon.ProgramTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProductManagerLogicTest extends ProgramTestCase {

	private ProductManagerLogic logic;

	@Before
	public void setup() {
		logic = new ProductManagerLogic( program );
	}

	@Test
	public void testDetermineProducts() {
		String group = "eg.provider";

		RepoCard repoA = new RepoCard();
		RepoCard repoB = new RepoCard();
		RepoCard repoC = new RepoCard();

		Set<ProductCard> repoAProducts = new HashSet<>();
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "aaaaa" ).setVersion( "0.3-SNAPSHOT" ) );
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "bbbbb" ).setVersion( "0.3-SNAPSHOT" ) );

		Set<ProductCard> repoBProducts = new HashSet<>();
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "aaaaa" ).setVersion( "0.2-SNAPSHOT" ) );
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "bbbbb" ).setVersion( "0.4" ) );

		Set<ProductCard> repoCProducts = new HashSet<>();
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "aaaaa" ).setVersion( "0.5-SNAPSHOT" ) );
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "bbbbb" ).setVersion( "0.3-SNAPSHOT" ) );

		Map<RepoCard, Set<ProductCard>> products = new HashMap<>();
		products.put( repoA, repoAProducts );
		products.put( repoB, repoBProducts );
		products.put( repoC, repoCProducts );

		Set<ProductCard> updates = logic.determineProducts( products, false );

		// Check the cards and their repos...
		updates.forEach( System.out::println );

		assertThat( updates.size(), is( 2 ) );
	}

}
