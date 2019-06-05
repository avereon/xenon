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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProductManagerLogicTest extends ProgramTestCase {

	private String group = "eg.provider";

	private String timestamp = "2018-06-11 06:30:07";

	private ProductManagerLogic logic;

	private Map<RepoCard, Set<ProductCard>> products;

	@Before
	public void setup() {
		generateRepoProductMap();
		logic = new ProductManagerLogic( program );
	}

	@Test
	public void testDetermineAvailableProducts() {
		Set<ProductCard> updates = logic.determineAvailableProducts( products );

		ProductCard a = new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.5-SNAPSHOT" ).setTimestamp( timestamp );
		ProductCard b = new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4" ).setTimestamp( timestamp );
		assertThat( updates, contains( a, b ) );
		assertThat( updates.size(), is( 2 ) );
	}

	@Test
	public void testDetermineUpdateableProducts() {
		// Since installedB should already be installed is should not be in the result
		ProductCard installedB  =new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4" ).setTimestamp( timestamp );
		Map<String, ProductCard> installedProducts = new HashMap<>();
		installedProducts.put( installedB.getProductKey(), installedB );
		Set<ProductCard> updates = logic.determineProducts( products, installedProducts );

		ProductCard a = new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.5-SNAPSHOT" ).setTimestamp( timestamp );
		//ProductCard b = new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4" ).setTimestamp( timestamp );
		assertThat( updates, contains( a ) );
		assertThat( updates.size(), is( 1 ) );
	}

	private void generateRepoProductMap() {
		RepoCard repoA = new RepoCard().setRepo( "a" );
		RepoCard repoB = new RepoCard().setRepo( "b" );
		RepoCard repoC = new RepoCard().setRepo( "c" );

		Set<ProductCard> repoAProducts = new HashSet<>();
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.3-SNAPSHOT" ).setTimestamp( timestamp ) );
		repoAProducts.add( new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.3-SNAPSHOT" ).setTimestamp( timestamp ) );

		Set<ProductCard> repoBProducts = new HashSet<>();
		repoBProducts.add( new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.2-SNAPSHOT" ).setTimestamp( timestamp ) );
		repoBProducts.add( new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4" ).setTimestamp( timestamp ) );

		Set<ProductCard> repoCProducts = new HashSet<>();
		repoCProducts.add( new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.5-SNAPSHOT" ).setTimestamp( timestamp ) );
		repoCProducts.add( new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.3-SNAPSHOT" ).setTimestamp( timestamp ) );

		this.products = new HashMap<>();
		products.put( repoA, repoAProducts );
		products.put( repoB, repoBProducts );
		products.put( repoC, repoCProducts );
	}

}
