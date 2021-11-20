package com.avereon.xenon.test.ui.util;

import com.avereon.xenon.ui.util.ToolBarFactory;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolBarFactoryTest extends BaseUiFactoryTest {

	@Test
	void testCreateToolbar() {
		ToolBar toolbar = ToolBarFactory.createToolBar( getProgram(), "new open save close" );
		assertThat( toolbar.getItems().size() ).isEqualTo( 4 );
	}

	@Test
	void testCreateToolbarWithTray() {
		ToolBar toolbar = ToolBarFactory.createToolBar( getProgram(), "new open save close | edit[undo redo]" );
		assertThat( toolbar.getItems().size() ).isEqualTo( 6 );
		assertThat( toolbar.getItems().get( 5 ) ).isInstanceOf( Button.class );
		// TODO Test the button popup
	}

}
