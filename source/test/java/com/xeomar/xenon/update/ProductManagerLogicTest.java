package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.product.RepoCard;
import com.xeomar.xenon.ProgramTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.xeomar.xenon.update.ProductMatcher.matches;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProductManagerLogicTest extends ProgramTestCase {

	private String group = "eg.provider";

	private String timestamp = "2018-06-11 06:30:07";

	private ProductManagerLogic logic;

	private Map<RepoCard, Set<ProductCard>> repos;

	private RepoCard stable = new RepoCard().setRepo( "a" );

	private RepoCard preview = new RepoCard().setRepo( "b" );

	private RepoCard nightly = new RepoCard().setRepo( "c" );

	@Before
	public void setup() {
		logic = new ProductManagerLogic( program );
		generateRepoProductMap();
	}

	@Test
	public void testDetermineAvailableProductsWithAllDisabledRepos() {
		// FIXME The repos are all disabled, no products should be available
		Set<ProductCard> updates = logic.determineAvailableProducts( repos );
		assertThat( updates.size(), is( 0 ) );
	}

	@Test
	public void testDetermineAvailableProductsWithEnabledRepos() {
		ProductCard a = new ProductCard().setGroup( group ).setArtifact( "producta" ).setTimestamp( timestamp );
		ProductCard b = new ProductCard().setGroup( group ).setArtifact( "productb" ).setTimestamp( timestamp );

		stable.setEnabled( true );
		Map<String, ProductCard> cards = getProductMap( logic.determineAvailableProducts( repos ) );
		assertThat( cards.get( a.getProductKey() ), matches( a.setVersion( "0.5" ) ) );
		assertThat( cards.get( b.getProductKey() ), matches( b.setVersion( "0.2" ) ) );
		assertThat( cards.size(), is( 2 ) );

		preview.setEnabled( true );
		cards = getProductMap( logic.determineAvailableProducts( repos ) );
		assertThat( cards.get( a.getProductKey() ), matches( a.setVersion( "0.6b7" ) ) );
		assertThat( cards.get( b.getProductKey() ), matches( b.setVersion( "0.3b19" ) ) );
		assertThat( cards.size(), is( 2 ) );

		preview.setEnabled( true );
		cards = getProductMap( logic.determineAvailableProducts( repos ) );
		assertThat( cards.get( a.getProductKey() ), matches( a.setVersion( "0.7-SNAPSHOT" ) ) );
		assertThat( cards.get( b.getProductKey() ), matches( b.setVersion( "0.4-SNAPSHOT" ) ) );
		assertThat( cards.size(), is( 2 ) );
	}

	@Test
	public void testDetermineUpdateableProductsWithLatestVersionNotInstalled() {
		// Because the installed Product B is not the latest version is should
		// still be included in the resulting update collection.
		ProductCard installedB = new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.2" ).setTimestamp( timestamp );

		Set<ProductCard> updates = logic.determineProducts( repos, Map.of( installedB.getProductKey(), installedB ) );
		Map<String, ProductCard> cards = getProductMap( updates );

		ProductCard a = new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.7-SNAPSHOT" ).setTimestamp( timestamp );
		ProductCard b = new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4-SNAPSHOT" ).setTimestamp( timestamp );

		assertThat( cards.get( a.getProductKey() ), matches( a ) );
		assertThat( cards.get( b.getProductKey() ), matches( b ) );
		assertThat( updates.size(), is( 2 ) );
	}

	//	@Test
	//	public void testDetermineUpdateableProductsWithLatestVersionInstalled() {
	//		ProductCard installedB = new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4" ).setTimestamp( timestamp );
	//		Set<ProductCard> updates = logic.determineProducts( repos, Map.of( installedB.getProductKey(), installedB ) );
	//
	//		ProductCard a = new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.5-SNAPSHOT" ).setTimestamp( timestamp );
	//		//ProductCard b = new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4" ).setTimestamp( timestamp );
	//		assertThat( updates, contains( a ) );
	//		assertThat( updates.size(), is( 2 ) );
	//	}

	private void generateRepoProductMap() {
		Set<ProductCard> stableProducts = new HashSet<>();
		stableProducts.add( new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.5" ).setTimestamp( timestamp ) );
		stableProducts.add( new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.2" ).setTimestamp( timestamp ) );

		Set<ProductCard> previewProducts = new HashSet<>();
		previewProducts.add( new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.6b7" ).setTimestamp( timestamp ) );
		previewProducts.add( new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.3b19" ).setTimestamp( timestamp ) );

		Set<ProductCard> nightlyProducts = new HashSet<>();
		nightlyProducts.add( new ProductCard().setGroup( group ).setArtifact( "producta" ).setVersion( "0.7-SNAPSHOT" ).setTimestamp( timestamp ) );
		nightlyProducts.add( new ProductCard().setGroup( group ).setArtifact( "productb" ).setVersion( "0.4-SNAPSHOT" ).setTimestamp( timestamp ) );

		this.repos = new HashMap<>();
		repos.put( stable, stableProducts );
		repos.put( preview, previewProducts );
		repos.put( nightly, nightlyProducts );
	}

	private Map<String, ProductCard> getProductMap( Collection<ProductCard> cards ) {
		Map<String, ProductCard> map = new HashMap<>();
		cards.forEach( ( c ) -> map.put( c.getProductKey(), c ) );
		return map;
	}

}
