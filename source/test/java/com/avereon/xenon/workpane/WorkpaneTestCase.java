package com.avereon.xenon.workpane;

import com.avereon.xenon.FxPlatformTestCase;
import com.avereon.xenon.asset.Asset;
import javafx.geometry.Side;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkpaneTestCase extends FxPlatformTestCase {

	static double WORKPANE_WIDTH = 1000000;

	static double WORKPANE_HEIGHT = 1000000;

	protected Workpane workpane;

	protected Asset asset = new Asset( URI.create( "" ) );

	WorkpaneView view;

	@BeforeEach
	public void setup() throws Exception {
		workpane = new Workpane();
		view = workpane.getActiveView();

		assertThat( view.getEdge( Side.TOP ).isWall() ).isEqualTo( true );
		assertThat( view.getEdge( Side.BOTTOM ).isWall() ).isEqualTo( true );
		assertThat( view.getEdge( Side.LEFT ).isWall() ).isEqualTo( true );
		assertThat( view.getEdge( Side.RIGHT ).isWall() ).isEqualTo( true );

		assertThat( view.getEdge( Side.TOP ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition() ).isEqualTo( 1d );
		assertThat( view.getEdge( Side.LEFT ).getPosition() ).isEqualTo( 0d );
		assertThat( view.getEdge( Side.RIGHT ).getPosition() ).isEqualTo( 1d );

		// Workpane size must be set for move methods to work correctly.
		new Scene( workpane, WORKPANE_WIDTH, WORKPANE_HEIGHT );
		assertThat( workpane.getWidth() ).isEqualTo( WORKPANE_WIDTH );
		assertThat( workpane.getHeight() ).isEqualTo( WORKPANE_HEIGHT );

		// Layout the workpane
		workpane.layout();
	}

}
