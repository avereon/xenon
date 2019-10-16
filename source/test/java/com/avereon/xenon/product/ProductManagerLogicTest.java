package com.avereon.xenon.product;

import com.avereon.product.ProductCard;
import com.avereon.xenon.ProgramTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.avereon.xenon.product.ProductMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProductManagerLogicTest extends ProgramTestCase {

	private String group = "eg.provider";

	private String timestamp = "2018-06-11 06:30:07";

	private ProductManagerLogic logic;

	private Map<RepoState, Set<ProductCard>> repos;

	private RepoState stable = new RepoState().setUrl( "a" );

	private RepoState preview = new RepoState().setUrl( "b" );

	private RepoState nightly = new RepoState().setUrl( "c" );

	@BeforeEach
	public void setup() {
		logic = new ProductManagerLogic( program );
		generateRepoProductMap();

		// Start with the repos disabled
		stable.setEnabled( false );
		preview.setEnabled( false );
		nightly.setEnabled( false );
	}

	@Test
	public void testDetermineAvailableProductsWithAllDisabledRepos() {
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

		nightly.setEnabled( true );
		cards = getProductMap( logic.determineAvailableProducts( repos ) );
		assertThat( cards.get( a.getProductKey() ), matches( a.setVersion( "0.7-SNAPSHOT" ) ) );
		assertThat( cards.get( b.getProductKey() ), matches( b.setVersion( "0.4-SNAPSHOT" ) ) );
		assertThat( cards.size(), is( 2 ) );
	}

	@Test
	public void testDetermineUpdateableProductsWithLatestVersionNotInstalled() {
		// In this test product A is not installed and therefore will not have an
		// update. Because the installed version of Product B is not the latest
		// version it should be included in the resulting update collection.
		ProductCard installedB = new ProductCard()
			.setGroup( group )
			.setArtifact( "productb" )
			.setVersion( "0.2" )
			.setTimestamp( timestamp );

		stable.setEnabled( true );
		preview.setEnabled( true );
		nightly.setEnabled( true );

		Set<ProductCard> updates = logic.determineUpdateableProducts( repos, Map.of( installedB.getProductKey(), installedB ) );
		Map<String, ProductCard> cards = getProductMap( updates );

		ProductCard b = new ProductCard()
			.setGroup( group )
			.setArtifact( "productb" )
			.setVersion( "0.4-SNAPSHOT" )
			.setTimestamp( timestamp );

		assertThat( cards.get( b.getProductKey() ), matches( b ) );
		assertThat( updates.size(), is( 1 ) );
	}

	@Test
	public void testDetermineUpdateableProductsWithLatestVersionInstalled() {
		// In this test both product A and product B are installed and are the
		// the latest version. Because they are both at the latest version there
		// should not be any updates available.
		ProductCard installedA = new ProductCard()
			.setGroup( group )
			.setArtifact( "producta" )
			.setVersion( "0.5" )
			.setTimestamp( timestamp );
		ProductCard installedB = new ProductCard()
			.setGroup( group )
			.setArtifact( "productb" )
			.setVersion( "0.2" )
			.setTimestamp( timestamp );

		stable.setEnabled( true );

		Map<String, ProductCard> installedProducts = Map.of( installedA.getProductKey(), installedA, installedB.getProductKey(), installedB );
		Set<ProductCard> updates = logic.determineUpdateableProducts( repos, installedProducts );

		assertThat( updates.size(), is( 0 ) );
	}

	@Test
	public void testDetermineUpdateableProductsWithJustTwoVersions() {
		Map<RepoState, Set<ProductCard>> repos = new HashMap<>();

		Set<ProductCard> products = new HashSet<>();
		products.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "producta" )
			.setVersion( "0.6" )
			.setTimestamp( timestamp ) );
		RepoState repo = new RepoState().setUrl( "a" ).setEnabled( true );
		repos.put( repo, products );

		ProductCard installed = new ProductCard()
			.setGroup( group )
			.setArtifact( "producta" )
			.setVersion( "0.7-SNAPSHOT" )
			.setTimestamp( timestamp );

		Map<String, ProductCard> installedProducts = Map.of( installed.getProductKey(), installed );

		Set<ProductCard> updates = logic.determineUpdateableProducts( repos, installedProducts );
		assertThat( updates.size(), is( 0 ) );
	}

	private void generateRepoProductMap() {
		Set<ProductCard> stableProducts = new HashSet<>();
		stableProducts.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "producta" )
			.setVersion( "0.5" )
			.setTimestamp( timestamp ) );
		stableProducts.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "productb" )
			.setVersion( "0.2" )
			.setTimestamp( timestamp ) );

		Set<ProductCard> previewProducts = new HashSet<>();
		previewProducts.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "producta" )
			.setVersion( "0.6b7" )
			.setTimestamp( timestamp ) );
		previewProducts.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "productb" )
			.setVersion( "0.3b19" )
			.setTimestamp( timestamp ) );

		Set<ProductCard> nightlyProducts = new HashSet<>();
		nightlyProducts.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "producta" )
			.setVersion( "0.7-SNAPSHOT" )
			.setTimestamp( timestamp ) );
		nightlyProducts.add( new ProductCard()
			.setGroup( group )
			.setArtifact( "productb" )
			.setVersion( "0.4-SNAPSHOT" )
			.setTimestamp( timestamp ) );

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
