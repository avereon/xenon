package com.avereon.xenon.workpane;

import com.avereon.xenon.BaseFxPlatformTestCase;
import com.avereon.xenon.asset.Asset;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

public class ToolTabPaneTest extends BaseFxPlatformTestCase {

	protected Asset asset = new Asset( URI.create( "" ) );

	@Test
	void testButton() {
		// given/when
		Button button = resolve( new Button() );

		// then
		assertThat( button.getSkin() ).isNotNull();
	}

	@Test
	public void testToolTabPane() {
		// given/when
		ToolTabPane toolTabPane = resolve( new ToolTabPane() );

		// then
		assertThat( toolTabPane.getSkin() ).isNotNull();
	}

	@Test
	public void testAddToolTab() {
		// given
		ToolTabPane toolTabPane = new ToolTabPane();
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );
		Tool tool3 = new MockTool( asset );
		ToolTab tab1 = new ToolTab( tool1 );
		ToolTab tab2 = new ToolTab( tool2 );
		ToolTab tab3 = new ToolTab( tool3 );
		toolTabPane.getTabs().add( tab1 );
		toolTabPane.getTabs().add( tab2 );
		toolTabPane.getTabs().add( tab3 );

		// when
		resolve( toolTabPane );

		// then
		assertThat( tool3.getParent().getParent() ).isSameAs( toolTabPane );
		assertThat( tool2.getParent().getParent() ).isSameAs( toolTabPane );
		assertThat( tool1.getParent().getParent() ).isSameAs( toolTabPane );
	}

	@Test
	public void testRemoveToolTab() {
		// given
		ToolTabPane toolTabPane = new ToolTabPane();
		Tool tool1 = new MockTool( asset );
		Tool tool2 = new MockTool( asset );
		Tool tool3 = new MockTool( asset );
		ToolTab tab1 = new ToolTab( tool1 );
		ToolTab tab2 = new ToolTab( tool2 );
		ToolTab tab3 = new ToolTab( tool3 );
		toolTabPane.getTabs().add( tab1 );
		toolTabPane.getTabs().add( tab2 );
		toolTabPane.getTabs().add( tab3 );
		resolve( toolTabPane );

		// when
		Fx.run( () -> toolTabPane.getTabs().remove( tab2 ) );
		Fx.waitFor( TIMEOUT );

		assertThat( toolTabPane.getTabs().size() ).isEqualTo( 2 );
		assertThat( toolTabPane.getTabs().get( 0 ) ).isSameAs( tab1 );
		assertThat( toolTabPane.getTabs().get( 1 ) ).isSameAs( tab3 );
	}

}
