package com.avereon.xenon.workpane;

import com.avereon.xenon.FxPlatformTestCase;
import com.avereon.xenon.asset.Asset;
import javafx.geometry.Side;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WorkpaneTestCase extends FxPlatformTestCase {

	static double WORKPANE_WIDTH = 1000000;

	static double WORKPANE_HEIGHT = 1000000;

	protected Workpane workpane;

	protected Asset asset = new Asset( URI.create( "" ) );

	// TODO Rename to toolview when tests are complete
	WorkpaneView toolview;

	@BeforeEach
	public void setup() throws Exception {
		workpane = new Workpane();
		toolview = workpane.getActiveView();

		assertThat( toolview.getEdge( Side.TOP ).isWall(), is( true ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).isWall(), is( true ) );
		assertThat( toolview.getEdge( Side.LEFT ).isWall(), is( true ) );
		assertThat( toolview.getEdge( Side.RIGHT ).isWall(), is( true ) );

		assertThat( toolview.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( toolview.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( toolview.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );

		// Workpane size must be set for move methods to work correctly.
		new Scene( workpane, WORKPANE_WIDTH, WORKPANE_HEIGHT );
		assertThat( workpane.getWidth(), is( WORKPANE_WIDTH ) );
		assertThat( workpane.getHeight(), is( WORKPANE_HEIGHT ) );

		// Layout the workpane
		workpane.layout();
	}

}
