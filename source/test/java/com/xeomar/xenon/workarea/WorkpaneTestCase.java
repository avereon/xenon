package com.xeomar.xenon.workarea;

import com.xeomar.xenon.FxPlatformTestCase;
import com.xeomar.xenon.resource.Resource;
import javafx.geometry.Side;
import org.junit.Before;

import java.net.URI;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkpaneTestCase extends FxPlatformTestCase {

	protected Workpane workpane;

	// TODO Rename to toolview when tests are complete
	protected WorkpaneView toolview;

	protected Resource resource = new Resource( URI.create( "" ) );

	@Override
	@Before
	public void setup() throws Exception {
		super.setup();

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
	}

}
