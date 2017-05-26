package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.testutil.FxTestCase;
import javafx.geometry.Side;
import org.junit.Before;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WorkpaneTestCase extends FxTestCase {

	protected Workpane workpane;

	protected ToolView view;

	@Override
	@Before
	public void setup() throws Exception {
		super.setup();

		workpane = new Workpane();
		view = workpane.getDefaultView();

		assertThat( view.getEdge( Side.TOP ).isWall(), is( true ) );
		assertThat( view.getEdge( Side.BOTTOM ).isWall(), is( true ) );
		assertThat( view.getEdge( Side.LEFT ).isWall(), is( true ) );
		assertThat( view.getEdge( Side.RIGHT ).isWall(), is( true ) );

		assertThat( view.getEdge( Side.TOP ).getPosition(), is( 0d ) );
		assertThat( view.getEdge( Side.BOTTOM ).getPosition(), is( 1d ) );
		assertThat( view.getEdge( Side.LEFT ).getPosition(), is( 0d ) );
		assertThat( view.getEdge( Side.RIGHT ).getPosition(), is( 1d ) );
	}

}
