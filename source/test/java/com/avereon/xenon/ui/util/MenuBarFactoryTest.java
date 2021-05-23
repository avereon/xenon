package com.avereon.xenon.ui.util;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MenuBarFactoryTest extends BaseUiFactoryTest {

	@Test
	void testCreateMenuBar() {
		MenuBar bar = MenuBarFactory.createMenuBar( getProgram(), "file[new,open,save,close],edit[undo,redo],help[about]" );
		assertThat( bar.getMenus().get( 0 ).getItems().size(), is( 4 ) );
		assertThat( bar.getMenus().get( 1 ).getItems().size(), is( 2 ) );
		assertThat( bar.getMenus().get( 2 ).getItems().size(), is( 1 ) );
		assertThat( bar.getMenus().size(), is( 3 ) );
	}

	@Test
	void testCreateMenu() {
		Menu menu = MenuBarFactory.createMenu( getProgram(), "file[new open save close]", false );
		assertThat( menu.getItems().size(), is( 4 ) );
	}

}
