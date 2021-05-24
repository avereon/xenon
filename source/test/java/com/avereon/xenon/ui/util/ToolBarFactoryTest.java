package com.avereon.xenon.ui.util;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class ToolBarFactoryTest extends BaseUiFactoryTest {

	@Test
	void testCreateToolbar() {
		ToolBar toolbar = ToolBarFactory.createToolBar( getProgram(), "new open save close" );
		assertThat( toolbar.getItems().size(), is( 4 ) );
	}

	@Test
	void testCreateToolbarWithTray() {
		ToolBar toolbar = ToolBarFactory.createToolBar( getProgram(), "new open save close | edit[undo redo]" );
		assertThat( toolbar.getItems().size(), is( 6 ) );
		assertThat( toolbar.getItems().get(5), instanceOf( Button.class ));
		// TODO Test the button popup
	}

}
