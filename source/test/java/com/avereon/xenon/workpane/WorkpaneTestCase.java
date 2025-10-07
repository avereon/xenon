package com.avereon.xenon.workpane;

import com.avereon.xenon.BaseFxPlatformTestCase;
import com.avereon.xenon.asset.Resource;
import javafx.geometry.Side;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class WorkpaneTestCase extends BaseFxPlatformTestCase {

	protected Workpane workpane;

	protected Resource resource = new Resource( URI.create( "" ) );

	WorkpaneView view;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();

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
		new Scene( workpane, SCENE_WIDTH, SCENE_HEIGHT );
		assertThat( workpane.getWidth() ).isEqualTo( SCENE_WIDTH );
		assertThat( workpane.getHeight() ).isEqualTo( SCENE_HEIGHT );

		// Layout the workpane
		workpane.layout();
	}

}
